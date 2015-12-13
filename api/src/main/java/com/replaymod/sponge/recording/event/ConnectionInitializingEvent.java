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

import java.util.UUID;

/**
 * Called when a connection to a player is initialized.<br>
 * <br>
 * Recording can be activated at a later point in time, however the recording may not have all packets
 * unless started during this event.<br>
 * <br>
 * Users have already been authenticated with Mojang services, however no black-/whitelist checks have been performed
 * not has any plugin had the chance to decide whether the player is allowed to join.<br>
 * <br>
 * Do <b>never</b> perform any blocking or expensive operations during this event. If you're not yet sure whether to
 * record a connection, it's best to do so and discard it later if it turns out you don't need it.
 */
public interface ConnectionInitializingEvent extends ConnectionEvent {

    /**
     * Return the nick name of the client.<br>
     * <br>
     * The returned name might not be unique. If you need a unique way to identify this connection, use
     * {@link #getConnection()}.
     * @return Name of the player
     */
    String getName();

    /**
     * Return the UUID of the client.<br>
     * <br>
     * The returned UUID might not be unique. If you need a unique way to identify this connection, use
     * {@link #getConnection()}.
     * @return UUID of the player
     */
    UUID getUUID();

}
