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

import java.lang.reflect.Field;

public class Reflection {

    public static Object getField(Class<?> cls, String name, Object instance) throws IllegalAccessException, NoSuchFieldException {
        try {
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        } catch (NoSuchFieldException e) {
            for (Field field : cls.getDeclaredFields()) {
                System.out.println(field);
            }
            throw e;
        }
    }

    public static Object getFieldValueByType(Class<?> cls, String type, Object instance) throws NoSuchFieldException, IllegalAccessException {
        for (Field field : cls.getDeclaredFields()) {
            if (type.equals(field.getType().getName())) {
                field.setAccessible(true);
                return field.get(instance);
            }
        }
        throw new NoSuchFieldException("By type " + type + " in " + cls);
    }

    public static Field getFieldByType(Class<?> cls, Class<?> type) throws NoSuchFieldException {
        for (Field field : cls.getDeclaredFields()) {
            if (type.equals(field.getType())) {
                return field;
            }
        }
        throw new NoSuchFieldException("By type " + type + " in " + cls);
    }

}
