package com.bnstra.npclib.nms.v1_20_R2.packets;

import io.netty.buffer.Unpooled;
import com.bnstra.npclib.api.state.NPCAnimation;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;

public class PacketPlayOutAnimationWrapper {

    public PacketPlayOutAnimation create(NPCAnimation npcAnimation, int entityId)  {
        return this.createDataSerializer((data)->{
            data.c(entityId);
            data.k(npcAnimation.getId());
            return new PacketPlayOutAnimation(data);
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
