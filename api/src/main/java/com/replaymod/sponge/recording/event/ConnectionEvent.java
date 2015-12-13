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
package com.replaymod.sponge.recording.event;

import com.replaymod.sponge.recording.Connection;
import org.spongepowered.api.event.Event;

/**
 * Event related to a connection.
 */
public interface ConnectionEvent extends Event {

    /**
     * Returns a connection handle for this connection. The returned connection handle uniquely identifies this
     * connection and can be used to retrieve the associated player (if any).
     * @return Unique connection handle
     */
    Connection getConnection();
}
