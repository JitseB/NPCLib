/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_21_R4.packets;

import com.comphenix.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftChatMessage;

import java.util.*;

/**
 * @author Jitse Boonstra
 */
public class ClientboundPlayerInfoWrapper {

    private static final ClientboundPlayerInfoUpdatePacket.a ADD_PLAYER = (ClientboundPlayerInfoUpdatePacket.a)
            Reflection.getClass("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$a").getEnumConstants()[0];

    private static final Reflection.ConstructorInvoker PACKET_CONSTRUCTOR = Reflection.getConstructor(
            ClientboundPlayerInfoUpdatePacket.class, RegistryFriendlyByteBuf.class);

    public ClientboundPlayerInfoUpdatePacket createAddPlayerPacket(GameProfile gameProfile, String name) {
        return (ClientboundPlayerInfoUpdatePacket) this.createDataSerializer((data)->{
            // Set the type to ADD_PLAYER
            data.a(EnumSet.of(ADD_PLAYER), ClientboundPlayerInfoUpdatePacket.a.class);

            // Build the player info record (and turn it into a list)
            ClientboundPlayerInfoUpdatePacket.b playerInfoData = new ClientboundPlayerInfoUpdatePacket.b(
                    gameProfile.getId(), gameProfile, false, 1, EnumGamemode.b,
                    CraftChatMessage.fromString(ChatColor.DARK_GRAY + "[NPC] " + name)[0], false, 0,
                    new RemoteChatSession.a(gameProfile.getId(), null));
            Collection<ClientboundPlayerInfoUpdatePacket.b> list = Collections.singletonList(playerInfoData);

            // Note to self: this monster is semi-copied from the ClientboundPlayerInfoUpdatePacket class
            // There is a method which takes a PacketDataSerializer in which the packet's data is moved to
            data.a(list, (StreamEncoder<? super PacketDataSerializer, ClientboundPlayerInfoUpdatePacket.b>) (var0x, var1) -> {
                var0x.a(var1.a());
                // Write the player info data
                Reflection.FieldAccessor<ClientboundPlayerInfoUpdatePacket.a.b> accessor = Reflection.getField(
                        ClientboundPlayerInfoUpdatePacket.a.class,"j", ClientboundPlayerInfoUpdatePacket.a.b.class);
                ClientboundPlayerInfoUpdatePacket.a.b hFieldObject = accessor.get(ADD_PLAYER);
                hFieldObject.write(new RegistryFriendlyByteBuf(var0x.asByteBuf(), null), var1);
            });
            return PACKET_CONSTRUCTOR.invoke(new RegistryFriendlyByteBuf(data.asByteBuf(), null));
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
