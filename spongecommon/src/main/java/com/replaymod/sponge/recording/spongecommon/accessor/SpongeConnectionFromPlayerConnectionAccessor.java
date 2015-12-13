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
package com.replaymod.sponge.recording.spongecommon.accessor;

import com.google.common.base.Function;
import com.replaymod.sponge.recording.Reflection;
import com.replaymod.sponge.recording.spongecommon.SpongeConnection;
import com.replaymod.sponge.recording.spongecommon.SpongeConnectionEventListener;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.spongepowered.api.network.PlayerConnection;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * Provides access to the {@link SpongeConnection} of a {@link PlayerConnection}.
 */
public class SpongeConnectionFromPlayerConnectionAccessor implements Function<PlayerConnection, SpongeConnection> {
    private final Field netManager;
    private final Field channel;

    public SpongeConnectionFromPlayerConnectionAccessor() {
        try {
            Class<?> NetHandlerPlayServer = Class.forName("net.minecraft.network.NetHandlerPlayServer");
            Class<?> NetworkManager = Class.forName("net.minecraft.network.NetworkManager");

            netManager = Reflection.getFieldByType(NetHandlerPlayServer, NetworkManager);
            channel = Reflection.getFieldByType(NetworkManager, Channel.class);

            netManager.setAccessible(true);
            channel.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }

    @Nullable
    @Override
    public SpongeConnection apply(PlayerConnection input) {
        try {
            ChannelPipeline pipeline = ((Channel) channel.get(netManager.get(input))).pipeline();
            SpongeConnectionEventListener handler = pipeline.get(SpongeConnectionEventListener.class);
            return handler == null ? null : handler.getConnection();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
