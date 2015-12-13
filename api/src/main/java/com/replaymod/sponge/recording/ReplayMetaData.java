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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Meta data for replay files.
 */
public final class ReplayMetaData {

    private Map<String, Object> metaData = new HashMap<String, Object>();

    public ReplayMetaData() {
        metaData.put("serverName", "Unknown");
    }

    public ReplayMetaData(ReplayMetaData from) {
        this.metaData = new HashMap<String, Object>(from.metaData);
    }

    public final <T> void set(String key, T value) {
        metaData.put(checkNotNull(key), checkNotNull(value));
    }

    @SuppressWarnings("unchecked")
    public final <T> Optional<T> get(String key) {
        return Optional.fromNullable((T) metaData.get(key));
    }

    public void setServerName(String name) {
        set("serverName", name);
    }

    public String getServerName() {
        return this.<String>get("serverName").get();
    }

    public Set<String> keys() {
        return new HashSet<String>(metaData.keySet());
    }

}
