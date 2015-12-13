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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a recorder recording a connection.
 */
public interface Recorder {

    /**
     * Return the connection which is being recorded.
     * @return The connection
     */
    Connection getConnection();

    /**
     * Return the duration of the current recording.
     * @return Duration in milliseconds.
     */
    long getDuration();

    /**
     * Get a snapshot of the current meta data. The returned meta data contains things like
     * the UUIDs of all players in the replay, duration, time of start, etc.
     * @return The meta data object
     */
    ReplayMetaData getMetaData();

    /**
     * Adds a new output stream to which the replay is saved.
     * The specified output stream will be wrapped in a zip output stream to create the .mcpr file.
     * @param out The output stream
     * @throws IOException if an I/O error occurred
     */
    void addOutput(OutputStream out) throws IOException;

    /**
     * Adds a new output stream to which the replay is saved.
     * Raw packet data will be written to the specified output stream. No compression will occur.
     * Recording to the output stream can be stopped by closing the output stream.
     * @param out The output stream
     * @throws IOException if an I/O error occurred
     */
    void addRawOutput(OutputStream out) throws IOException;

    /**
     * Ends the recording for the specified output stream and writes meta data. The output stream will then be closed.
     * @param out The output stream
     * @param metaData Meta data for the replay or {@code null} if the output stream contains raw data
     * @throws IllegalStateException if the specified output stream is unknown (has not been added) to the recorder
     * @throws IOException if an I/O error occurred
     */
    void endRecording(OutputStream out, ReplayMetaData metaData) throws IllegalStateException, IOException;

}
