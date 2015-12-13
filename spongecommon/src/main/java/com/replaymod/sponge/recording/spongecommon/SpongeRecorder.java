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

import com.replaymod.sponge.recording.AbstractRecorder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.spongepowered.api.Game;

import java.io.IOException;
import java.util.Map;

public class SpongeRecorder extends AbstractRecorder<SpongeConnection> {

    public SpongeRecorder(Game game, SpongeConnection connection) {
        super(game, connection);

        ChannelPipeline pipeline = connection.getChannel().get().pipeline();
        pipeline.addLast("recorder_compression_order", new PipelineOrderHandler());
        pipeline.addBefore("encoder", "outbound_recorder", new OutboundPacketRecorder());
        pipeline.addBefore("decoder", "inbound_recorder", new InboundPacketRecorder());
    }

    @Override
    protected synchronized void writePacket(boolean fromServer, ByteBuf data) throws IOException {
        super.writePacket(fromServer, data);
    }

    /**
     * Make sure that compression occurs after recording.
     * There isn't really any point in compressing packets, the replay as a whole is already compressed.<br><br>
     * The target is this pipeline (our handlers are marked with >):<br>
     *   timeout=io.netty.handler.timeout.ReadTimeoutHandler<br>
     *   splitter=net.minecraft.util.MessageDeserializer2<br>
     *   decompress=net.minecraft.network.NettyCompressionDecoder<br>
     * > inbound_recorder=de.johni0702.sponge.recording.spongecommon.SpongeRecorder$InboundPacketRecorder<br>
     *   decoder=net.minecraft.util.MessageDeserializer<br>
     *   prepender=net.minecraft.util.MessageSerializer2<br>
     *   compress=net.minecraft.network.NettyCompressionEncoder<br>
     * > outbound_recorder=de.johni0702.sponge.recording.spongecommon.SpongeRecorder$OutboundPacketRecorder<br>
     *   encoder=net.minecraft.util.MessageSerializer<br>
     * > connection_events=de.johni0702.sponge.recording.spongecommon.SpongeConnectionEventListener<br>
     *   fml:packet_handler=net.minecraftforge.fml.common.network.handshake.NetworkDispatcher<br>
     *   packet_handler=net.minecraft.network.NetworkManager<br>
     * > recorder_compression_order=de.johni0702.sponge.recording.spongecommon.SpongeRecorder$PipelineOrderHandler<br>
     */
    private class PipelineOrderHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ChannelPipeline pipeline = ctx.pipeline();
            // First make sure the outbound recorder is at the right place
            boolean foundRecorder = false;
            for (Map.Entry<String, ChannelHandler> e : pipeline) {
                if ("outbound_recorder".equals(e.getKey())) {
                    foundRecorder = true;
                }
                if (foundRecorder && "compress".equals(e.getKey())) {
                    // They're in the wrong order :(
                    ChannelHandler recorder = pipeline.remove("outbound_recorder");
                    pipeline.addBefore("encoder", "outbound_recorder", recorder);
                    break;
                }
            }
            // Then verify the inbound recorder
            foundRecorder = false;
            for (Map.Entry<String, ChannelHandler> e : pipeline) {
                if ("inbound_recorder".equals(e.getKey())) {
                    foundRecorder = true;
                }
                if (foundRecorder && "decompress".equals(e.getKey())) {
                    // They're in the wrong order :(
                    ChannelHandler recorder = pipeline.remove("inbound_recorder");
                    pipeline.addBefore("decoder", "inbound_recorder", recorder);
                    break;
                }
            }
            super.write(ctx, msg, promise);
        }
    }

    /**
     * Records outbound packet data and writes it to the output stream.
     */
    @ChannelHandler.Sharable
    private class OutboundPacketRecorder extends ChannelDuplexHandler {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                int index = buf.readerIndex();
                writePacket(true, buf);
                buf.readerIndex(index);
            }
            super.write(ctx, msg, promise);
        }
    }

    /**
     * Records inbound packet data and writes it to the output stream.
     */
    @ChannelHandler.Sharable
    private class InboundPacketRecorder extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                int index = buf.readerIndex();
                writePacket(false, buf);
                buf.readerIndex(index);
            }
            super.channelRead(ctx, msg);
        }
    }
}
