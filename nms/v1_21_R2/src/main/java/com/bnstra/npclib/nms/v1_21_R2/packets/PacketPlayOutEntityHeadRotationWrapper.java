/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_21_R2.packets;

import com.comphenix.tinyprotocol.Reflection;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import org.bukkit.Location;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutEntityHeadRotationWrapper {

    private static final Reflection.ConstructorInvoker PACKET_CONSTRUCTOR = Reflection
            .getConstructor(PacketPlayOutEntityHeadRotation.class, PacketDataSerializer.class);

    public PacketPlayOutEntityHeadRotation create(Location location, int entityId) {
        return (PacketPlayOutEntityHeadRotation) this.createDataSerializer(data->{
            data.c(entityId);
            data.l((byte)((int)(location.getYaw() * 256.0F / 360.0F)));
            return PACKET_CONSTRUCTOR.invoke(data);
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
