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
package com.replaymod.sponge.recording;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.spongepowered.api.Game;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An abstract recorder implementing common methods.
 */
public abstract class AbstractRecorder<T extends Connection> implements Recorder {

    public static final String FILE_FORMAT = "BIMCPR";
    public static final int FILE_FORMAT_VERSION = 1;

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * The connection which is being recorded.
     */
    private final T connection;

    /**
     * Time in milliseconds at which this recorder started.
     */
    private final long startTime = System.currentTimeMillis();

    /**
     * Set of UUIDs of all players visible in the recording.
     * Subclasses should add all players as they appear.
     */
    protected final Set<UUID> playersInReplay = Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>());

    /**
     * Map every output stream it its zip output stream so we can later add more entries when closing before it.
     */
    private final Map<OutputStream, ZipOutputStream> outputs = new HashMap<OutputStream, ZipOutputStream>();

    /**
     * List of all raw output streams. Those are the output stream which receive the packet data.
     */
    private final List<OutputStream> rawOutputs = new ArrayList<OutputStream>();

    /**
     * Combines all of the {@link #rawOutputs raw} and {@link #outputs zipped} outputs for convenient writing.
     */
    private final DataOutputStream combinedOutput = new DataOutputStream(
            new MultiOutputStream(Iterables.concat(outputs.values(), rawOutputs)));

    /**
     * Creates a new abstract recorder.
     * When creating a new recorder its start time is set.
     * @param game The game instance
     * @param connection The connection being recorded
     */
    public AbstractRecorder(Game game, T connection) {
        this.game = game;
        this.connection = connection;
    }

    @Override
    public T getConnection() {
        return connection;
    }

    @Override
    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public ReplayMetaData getMetaData() {
        ReplayMetaData metaData = new ReplayMetaData();

        // Constants
        metaData.set("singleplayer", false);
        metaData.set("fileFormat", FILE_FORMAT);
        metaData.set("fileFormatVersion", FILE_FORMAT_VERSION);

        // Current replay
        metaData.set("date", startTime);
        metaData.set("duration", getDuration());
        metaData.set("players", playersInReplay);
        metaData.set("mcversion", connection.getMcVersion().getName());

        // Generator
        String recordingName = RecordingPlugin.class.getPackage().getImplementationTitle();
        String recordingVersion = RecordingPlugin.class.getPackage().getImplementationVersion();
        String spongeName = game.getPlatform().getImplementation().getName();
        String spongeVersion = game.getPlatform().getImplementation().getVersion();
        String generator = String.format("%s %s on %s (%s)", recordingName, recordingVersion, spongeName, spongeVersion);
        metaData.set("generator", generator);

        return metaData;
    }

    @Override
    public void addOutput(OutputStream out) throws IOException {
        ZipOutputStream zipOut = new ZipOutputStream(out);
        zipOut.putNextEntry(new ZipEntry("recording.tmcpr"));
        outputs.put(out, zipOut);
    }

    @Override
    public void addRawOutput(OutputStream out) {
        rawOutputs.add(out);
    }

    @Override
    public void endRecording(OutputStream out, ReplayMetaData metaData) throws IllegalStateException, IOException {
        if (metaData == null) {
            Preconditions.checkState(rawOutputs.remove(out), "Specified output is unknown or meta data is missing.");
            out.flush();
            out.close();
        } else {
            ZipOutputStream zipOut = outputs.remove(out);
            if (zipOut == null) {
                throw new IllegalStateException("Specified output is unknown or contains raw data.");
            }

            zipOut.closeEntry();

            zipOut.putNextEntry(new ZipEntry("metaData.json"));
            zipOut.write(toJson(metaData).getBytes());
            zipOut.closeEntry();

            zipOut.flush();
            zipOut.close();
        }
    }

    /**
     * Converts the meta data supplied to a JSON string.
     * @param metaData The meta data
     * @return The JSON string
     */
    @SuppressWarnings("unchecked")
    private String toJson(ReplayMetaData metaData) {
        JSONObject data = new JSONObject();
        for (String key : metaData.keys()) {
            Object value = metaData.get(key).get();
            if (value instanceof Collection) {
                JSONArray array = new JSONArray();
                for (Object element : (Collection) value) {
                    array.add(element == null ? null : element);
                }
                value = array;
            }
            data.put(key, value);
        }
        return data.toString();
    }

    /**
     * Return a data output which writes to all underlying streams.
     * @return The combined data output
     */
    protected DataOutputStream getCombinedOutput() {
        return combinedOutput;
    }

    /**
     * Write the specified packet data to the output streams.
     * @param fromServer Whether the packet is client or server bound
     * @param data The packet data (packet id and payload)
     */
    protected synchronized void writePacket(boolean fromServer, ByteBuf data) throws IOException {
        DataOutputStream out = getCombinedOutput();
        long timeAndDirection = getDuration() << 1 | (fromServer ? 0 : 1);
        writeVar(out, timeAndDirection);
        int length = data.readableBytes();
        writeVar(out, length);
        data.readBytes(out, length);
    }

    private void writeVar(OutputStream out, long var) throws IOException {
        do {
            int b = (int) (var & 0x7F);
            var >>>= 7;
            if (var > 0) {
                b |= 0x80;
            }
            out.write(b);
        } while (var > 0);
    }

    /**
     * Convenient output stream for writing to multiple streams.
     */
    private static class MultiOutputStream extends OutputStream {

        private final Iterable<OutputStream> outputs;

        public MultiOutputStream(Iterable<OutputStream> outputs) {
            this.outputs = outputs;
        }

        @Override
        public void write(int b) throws IOException {
            for (OutputStream out : outputs) {
                out.write(b);
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            for (OutputStream out : outputs) {
                out.write(b, off, len);
            }
        }

        @Override
        public void flush() throws IOException {
            for (OutputStream out : outputs) {
                out.flush();
            }
        }

        @Override
        public void close() throws IOException {
            for (OutputStream out : outputs) {
                out.close();
            }
        }
    }
}
