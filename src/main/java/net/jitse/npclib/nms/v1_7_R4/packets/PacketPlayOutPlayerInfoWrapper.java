/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_7_R4.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo;
import net.minecraft.util.com.mojang.authlib.GameProfile;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutPlayerInfoWrapper {

    public PacketPlayOutPlayerInfo create(int action, GameProfile gameProfile, String name) {
        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();

        // Action values:
        // 0 = Add player
        // 1 = Update gamemode
        // 2 = Update latency
        // 3 = Update display name
        // 4 = Remove player
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "action", int.class)
                .set(packetPlayOutPlayerInfo, action);
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "player", GameProfile.class)
                .set(packetPlayOutPlayerInfo, gameProfile);
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "username", String.class)
                .set(packetPlayOutPlayerInfo, name);
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "ping", int.class)
                .set(packetPlayOutPlayerInfo, 0);
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "gamemode", int.class)
                .set(packetPlayOutPlayerInfo, 1);

        return packetPlayOutPlayerInfo;
    }
}
