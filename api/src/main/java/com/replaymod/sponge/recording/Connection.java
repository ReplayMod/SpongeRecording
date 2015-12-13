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

import com.google.common.base.Optional;
import com.replaymod.sponge.recording.event.ConnectionInitializingEvent;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.entity.living.player.Player;

/**
 * Represents a unique connection to a client.
 */
public interface Connection {

    /**
     * Whether this connection is alive.
     * @return {@code true} if this connection is alive, {@code false} otherwise
     */
    boolean isAlive();

    /**
     * Return the player for this connection if the connection is alive and the player has authenticated.
     * @return The player
     */
    Optional<Player> getPlayer();

    /**
     * Return the minecraft version of this connection.
     * @return Minecraft version of this connection
     */
    MinecraftVersion getMcVersion();

    /**
     * Return the recorder which is recording this connection.
     * @return The recorder
     */
    Optional<Recorder> getRecorder();

    /**
     * Start the recording of this connection. The replay is only guaranteed to contain all packets when called during
     * the {@link ConnectionInitializingEvent}.
     * @return The recorder
     * @throws IllegalStateException if this connection is already being recorded
     */
    Recorder startRecording() throws IllegalStateException;

}
