/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_19_R1.packets;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutPlayerInfoWrapper {

    public PacketPlayOutPlayerInfo create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction action, GameProfile gameProfile, String name) {
        return this.createDataSerializer((data)->{
            PacketPlayOutPlayerInfo.PlayerInfoData playerInfoData = new PacketPlayOutPlayerInfo.PlayerInfoData(gameProfile, 1, EnumGamemode.b,
                    CraftChatMessage.fromString(ChatColor.DARK_GRAY + "[NPC] " + name)[0], null);
            List<PacketPlayOutPlayerInfo.PlayerInfoData> list = Collections.singletonList(playerInfoData);
            data.a(action);
            Method method = action.getDeclaringClass().getDeclaredMethod("a", PacketDataSerializer.class, PacketPlayOutPlayerInfo.PlayerInfoData.class);
            method.setAccessible(true);
            data.a(list, (PacketDataSerializer.b<PacketPlayOutPlayerInfo.PlayerInfoData>) (a, b)->this.unsafe(()->method.invoke(action, a, b)));
            return new PacketPlayOutPlayerInfo(data);
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
    private interface UnsafeRunnable {
        void run() throws Exception;
    }

    private void unsafe(UnsafeRunnable run) {
        try {
            run.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    private interface UnsafeFunction<K, T> {
        T apply(K k) throws Exception;
    }
}
