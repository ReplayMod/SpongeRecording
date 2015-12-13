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

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.ServiceLoader;

@Plugin(
        id = "SpongeRecording",
        name = "SpongeRecording",
        version= "0.0.1"
)
public class RecordingPlugin {

    private final Implementation implementation;
    private final PluginContainer container;

    @Inject
    public RecordingPlugin(Game game, PluginContainer container, Logger logger) {
        this.container = container;

        // Load all implementations using the java service provider api
        // We can't use the sponge services as the implementations aren't ever registered
        Implementation implementation = null;
        for (Implementation p : ServiceLoader.load(Implementation.class)) {
            if (p.isFunctional(game)) {
                implementation = p;
                break;
            }
        }
        if (implementation == null) {
            throw new UnsupportedOperationException("The current platform is not supported by SpongeRecording.");
        } else {
            logger.info("SpongeRecording loaded. Found active platform: " + implementation);
            this.implementation = implementation;
        }
    }

    @Listener
    public void onInit(GamePreInitializationEvent event) {
        implementation.initialize(container, event.getGame());
    }

}
