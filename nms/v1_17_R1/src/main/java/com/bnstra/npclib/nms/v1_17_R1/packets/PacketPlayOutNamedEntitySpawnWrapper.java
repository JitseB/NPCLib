/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_17_R1.packets;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import org.bukkit.Location;

import com.comphenix.tinyprotocol.Reflection;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(UUID uuid, Location location, int entityId) {
        return this.createDataSerializer((data)->{
            data.d(entityId);
            data.a(uuid);
            data.writeDouble(location.getX());
            data.writeDouble(location.getY());
            data.writeDouble(location.getZ());
            data.writeByte((byte)((int)(location.getYaw() * 256.0F / 360.0F)));
            data.writeByte((byte)((int)(location.getPitch() * 256.0F / 360.0F)));
            return new PacketPlayOutNamedEntitySpawn(data);
        });
    }

    private <T> T createDataSerializer(UnsafeFunction<PacketDataSerializer, T> callback) {
        PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
        T result = null;
        try {
            result = callback.apply(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            data.release();
        }
        return result;
    }

    @FunctionalInterface
    private interface UnsafeFunction<K, T> {
        T apply(K k) throws Exception;
    }
}
