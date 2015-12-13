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

import com.google.common.base.Optional;
import com.replaymod.sponge.recording.Recorder;
import com.replaymod.sponge.recording.Reflection;
import com.replaymod.sponge.recording.spongecommon.event.SpongeConnectionClosedEvent;
import com.replaymod.sponge.recording.spongecommon.event.SpongeConnectionInitializingEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.UUID;

/**
* Channel adapter for firing connection events.
*/
@ChannelHandler.Sharable
public class SpongeConnectionEventListener extends ChannelDuplexHandler {

    private final SpongeConnection spongeConnection;
    private boolean firedInitializing;
    private boolean firedClosed;

    public SpongeConnectionEventListener(SpongeConnection spongeConnection) {
        this.spongeConnection = spongeConnection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove(this).addBefore("packet_handler", "connection_events", this);
        super.channelActive(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!firedInitializing && "net.minecraft.network.login.server.S02PacketLoginSuccess".equals(msg.getClass().getName())) {
            Object profile = Reflection.getFieldValueByType(msg.getClass(), "com.mojang.authlib.GameProfile", msg);
            String name = (String) Reflection.getFieldValueByType(profile.getClass(), "java.lang.String", profile);
            UUID uuid = (UUID) Reflection.getFieldValueByType(profile.getClass(), "java.util.UUID", profile);
            spongeConnection.getGame().getEventManager().post(new SpongeConnectionInitializingEvent(spongeConnection, name, uuid));
            firedInitializing = true;
            Optional<Recorder> recorder = spongeConnection.getRecorder();
            if (recorder.isPresent()) {
                SpongeRecorder spongeRecorder = (SpongeRecorder) recorder.get();
                ByteBuf buf = ctx.alloc().buffer(16);
                buf.writeLong(uuid.getMostSignificantBits());
                buf.writeLong(uuid.getLeastSignificantBits());
                spongeRecorder.writePacket(false, buf);
                buf.release();
            }
        }
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!firedClosed && firedInitializing) {
            spongeConnection.getGame().getEventManager().post(new SpongeConnectionClosedEvent(spongeConnection));
            firedClosed = true;
        }
        super.channelInactive(ctx);
    }

    public SpongeConnection getConnection() {
        return spongeConnection;
    }
}
