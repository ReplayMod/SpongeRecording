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
package com.replaymod.sponge.recording.spongecommon;

import com.replaymod.sponge.recording.Implementation;
import com.replaymod.sponge.recording.Reflection;
import io.netty.channel.ChannelFuture;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Platform for servers based on SpongeCommon.
 */
public class SpongeImplementation implements Implementation {
    @Override
    public boolean isFunctional(Game game) {
        try {
            return Class.forName("org.spongepowered.common.SpongeGame").isInstance(game);
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    @Override
    public void initialize(PluginContainer plugin, Game game) {
        game.getEventManager().registerListeners(plugin, this);
    }

    @Listener
    public void hookChannels(GameInitializationEvent event) {
        Game game = event.getGame();
        Server server = game.getServer();
        Method getNetworkSystem = null;
        for (Method m : server.getClass().getMethods()) {
            if ("net.minecraft.network.NetworkSystem".equals(m.getReturnType().getName())) {
                getNetworkSystem = m;
            }
        }
        if (getNetworkSystem == null) {
            throw new RuntimeException("Could not find getNetworkSystem in " + server);
        }
        try {
            Object networkSystem = getNetworkSystem.invoke(server);
            SpongeChannelInitializer channelInitializer = new SpongeChannelInitializer(game);
            @SuppressWarnings("unchecked")
            List<ChannelFuture> endpoints = (List) Reflection.getField(networkSystem.getClass(), "field_151274_e", networkSystem);
            for (ChannelFuture endpoint : endpoints) {
                endpoint.channel().pipeline().addFirst(channelInitializer);
            }
        } catch (IllegalAccessException | NoSuchFieldException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Link the connection and the player objects as soon as possible.
     * @param event The event
     */
    @Listener(order = Order.PRE)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();
        SpongeConnection connection = SpongeConnection.get(player.getConnection());
        connection.setPlayer(player);
    }

    @Override
    public SpongeConnection getConnection(Player player) {
        return SpongeConnection.get(player.getConnection());
    }
}
