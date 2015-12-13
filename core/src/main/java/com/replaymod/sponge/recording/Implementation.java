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

import org.spongepowered.api.Game;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Represents a platform for the current sponge implementation.
 */
public interface Implementation {

    /**
     * Whether this platform is functional on the current sponge implementation.
     * @param game The game
     * @return {@code true} if it is functional, {@code false} otherwise
     */
    boolean isFunctional(Game game);

    /**
     * Initializes this platform.
     * @param plugin The plugin container
     * @param game The game
     */
    void initialize(PluginContainer plugin, Game game);

    /**
     * TODO: Temporary. Figure out if/how to use data api.
     */
    Connection getConnection(Player player);
}
