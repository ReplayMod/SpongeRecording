/*
 * This file is part of SpongeRecording, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2015 johni0702 <https://github.com/johni0702>
 * Copyright (c) contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.replaymod.sponge.recording.example;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.replaymod.sponge.recording.Connection;
import com.replaymod.sponge.recording.Recorder;
import com.replaymod.sponge.recording.ReplayMetaData;
import com.replaymod.sponge.recording.event.ConnectionClosedEvent;
import com.replaymod.sponge.recording.event.ConnectionInitializingEvent;
import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.io.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Example plugin demonstrating a very simple usage of the SpongeRecording API.
 * This plugin records every connection and saves them into the 'recording' folder.
 */
@Plugin(
        id = "SpongeRecordingExample",
        name = "SpongeRecording Example Plugin",
        version = "0.0.1",
        dependencies = "required-after:SpongeRecording"
)
public class ExampleSpongeRecordingPlugin {
    @Inject
    private Logger logger;

    /**
     * Map for storing our output stream used for each connection.
     * There may be multiple networking thread, therefore we have to use a thread-safe map.
     */
    private final Map<Connection, OutputStream> outputStreams = new ConcurrentHashMap<>();

    /**
     * This even is called whenever a new connection is established.
     * @param event The event
     */
    @Listener
    public void onConnectionInit(ConnectionInitializingEvent event) {
        Connection connection = event.getConnection();
        // If some other plugin already requested that this connection be recorded, we don't have to
        if (!connection.getRecorder().isPresent()) {
            // However if we're the first, start the recording
            connection.startRecording();
        }

        // All recordings are stored in this folder
        File folder = new File("recordings");
        if (!folder.exists()) { // Make sure the folder exists
            folder.mkdirs(); // If it doesn't, create it
        }
        // The file for storing the replay is named UserName-SOME-RANDOM-UUID-STRING.mcpr
        File file = new File(folder, String.format("%s-%s.mcpr", event.getName(), UUID.randomUUID().toString()));
        OutputStream out = null;
        try {
            // For simplicity we just create a file output stream
            // Writing is usually done from the networking thread(s), in a real plugin this should probably write
            // to an output stream that buffers its output in memory and uses another thread for writing to disk.
            out = new BufferedOutputStream(new FileOutputStream(file));
            // We then add the output stream to the recorded, by doing so in this event we assure that every packet is
            // recorded.
            connection.getRecorder().get().addOutput(out);
            // We have to remember the output stream so we can later tell the recorder which one to close
            outputStreams.put(connection, out);

            logger.info("Now recording connection {} (Name: {}).", connection, event.getName());
        } catch (IOException e) {
            logger.error("Failed to start recording: ", e);
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * This even it called whenever a connection is closed.
     * Every connection is guaranteed to fire the init even before this one is fired and this event
     * is fired exactly once for every init event.
     * @param event The event
     */
    @Listener
    public void onConnectionClosed(ConnectionClosedEvent event) {
        Connection connection = event.getConnection();
        Optional<Recorder> recorder = connection.getRecorder();
        if (recorder.isPresent()) { // This can only be absent if an exception was thrown in onConnectionInit
            OutputStream outputStream = outputStreams.remove(connection);
            if (outputStream != null) { // Same here
                // Get the current meta data for the recording; there is no need to add our own
                // as this method already adds pretty much all we need in this simple example plugin
                ReplayMetaData metaData = recorder.get().getMetaData();
                try {
                    // This call will end the recording for this output stream, write the supplied meta data and
                    // close the stream. If an exception occurs, we should consider the generated .mcpr corrupt.
                    recorder.get().endRecording(outputStream, metaData);

                    logger.info("Recording of {} finished.", connection);
                } catch (IOException e) {
                    logger.error("Failed to end recording: ", e);
                }
            }
        }
    }
}
