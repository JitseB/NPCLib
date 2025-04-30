/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_19_R3.packets;

import com.comphenix.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_19_R3.util.CraftChatMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Jitse Boonstra
 */
public class ClientboundPlayerInfoWrapper {

    public ClientboundPlayerInfoUpdatePacket createAddPlayerPacket(GameProfile gameProfile, String name) {
        return this.createDataSerializer((data)->{
            // Set the type to ADD_PLAYERz
            data.a(EnumSet.of(ClientboundPlayerInfoUpdatePacket.a.a), ClientboundPlayerInfoUpdatePacket.a.class);

            // Build the player info record (and turn it into a list)
            ClientboundPlayerInfoUpdatePacket.b playerInfoData = new ClientboundPlayerInfoUpdatePacket.b(
                    gameProfile.getId(), gameProfile, false, 1, EnumGamemode.b,
                    CraftChatMessage.fromString(ChatColor.DARK_GRAY + "[NPC] " + name)[0],
                    new RemoteChatSession.a(gameProfile.getId(), null));
            Collection<ClientboundPlayerInfoUpdatePacket.b> list = Collections.singletonList(playerInfoData);

            // Note to self: this monster is semi-copied from the ClientboundPlayerInfoUpdatePacket class
            // There is a method which takes a PacketDataSerializer in which the packet's data is moved to
            data.a(list, (PacketDataSerializer.b<ClientboundPlayerInfoUpdatePacket.b>) (var0x, var1) -> {
                var0x.a(var1.a());
                // Write the player info data
                Reflection.FieldAccessor<ClientboundPlayerInfoUpdatePacket.a.b> accessor = Reflection.getField(
                        ClientboundPlayerInfoUpdatePacket.a.class,"h", ClientboundPlayerInfoUpdatePacket.a.b.class);
                ClientboundPlayerInfoUpdatePacket.a.b hFieldObject = accessor.get(ClientboundPlayerInfoUpdatePacket.a.a);
                hFieldObject.write(var0x, var1);
            });
            return new ClientboundPlayerInfoUpdatePacket(data);
        });
    }

    public ClientboundPlayerInfoRemovePacket createRemovePlayerPacket(UUID uuid) {
        return new ClientboundPlayerInfoRemovePacket(Collections.singletonList(uuid));
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
