/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_18_R2.packets;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import org.bukkit.Location;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutEntityHeadRotationWrapper {

    public PacketPlayOutEntityHeadRotation create(Location location, int entityId) {
        return this.createDataSerializer(data->{
            data.d(entityId);
            data.writeByte((byte)((int)(location.getYaw() * 256.0F / 360.0F)));
            return new PacketPlayOutEntityHeadRotation(data);
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
