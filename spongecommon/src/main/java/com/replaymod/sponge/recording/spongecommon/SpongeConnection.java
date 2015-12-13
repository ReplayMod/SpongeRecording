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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.replaymod.sponge.recording.Connection;
import com.replaymod.sponge.recording.Recorder;
import com.replaymod.sponge.recording.spongecommon.accessor.SpongeConnectionFromPlayerConnectionAccessor;
import io.netty.channel.Channel;
import org.spongepowered.api.Game;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.PlayerConnection;

import java.lang.ref.WeakReference;

/**
 * A connection from a client to a sponge server.
 */
public class SpongeConnection implements Connection {

    /**
     * The game instance.
     */
    private final Game game;

    /**
     * Weak reference to the netty channel of this connection.
     */
    private final WeakReference<Channel> channel;

    /**
     * Weak reference to the player of this connection.
     * When null, the player doesn't exit yet or hasn't been queried yet.
     */
    private WeakReference<Player> player;

    /**
     * The recorder recording this connection.
     * This is {@code null} until {@link #startRecording()} is called.
     */
    private Recorder recorder;

    /**
     * Create a new connection handle for the specified netty channel.
     *
     * @param game    The game instance
     * @param channel The netty channel
     */
    public SpongeConnection(Game game, Channel channel) {
        this.game = game;
        this.channel = new WeakReference<>(channel);
    }

    /**
     * Returns the netty channel for this connection.
     *
     * @return Optional netty channel
     */
    public Optional<Channel> getChannel() {
        return Optional.fromNullable(channel.get());
    }

    /**
     * Returns the game.
     *
     * @return The game instance
     */
    public Game getGame() {
        return game;
    }

    @Override
    public boolean isAlive() {
        Optional<Channel> channel = getChannel();
        return channel.isPresent() && channel.get().isActive();
    }

    @Override
    public Optional<Player> getPlayer() {
        return player == null ? Optional.<Player>absent() : Optional.fromNullable(player.get());
    }

    @Override
    public MinecraftVersion getMcVersion() {
        return game.getPlatform().getMinecraftVersion();
    }

    @Override
    public Optional<Recorder> getRecorder() {
        return Optional.fromNullable(recorder);
    }

    @Override
    public synchronized Recorder startRecording() throws IllegalStateException {
        Preconditions.checkState(recorder == null, "Already recording.");
        return recorder = new SpongeRecorder(game, this);
    }

    public void setPlayer(Player player) {
        Preconditions.checkState(this.player == null, "Player already set.");
        this.player = new WeakReference<>(player);
    }

    private static final Function<PlayerConnection, SpongeConnection> spongeConnectionAccessor =
            new SpongeConnectionFromPlayerConnectionAccessor();

    public static SpongeConnection get(PlayerConnection playerConnection) {
        return spongeConnectionAccessor.apply(playerConnection);
    }
}
