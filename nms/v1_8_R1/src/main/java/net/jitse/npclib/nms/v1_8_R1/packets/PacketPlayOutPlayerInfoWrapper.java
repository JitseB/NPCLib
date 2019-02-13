/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_8_R1.packets;

import com.comphenix.tinyprotocol.Reflection;
import com.mojang.authlib.GameProfile;
import net.jitse.npclib.api.wrapper.GameProfileWrapper;
import net.minecraft.server.v1_8_R1.*;

import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutPlayerInfoWrapper {

    public PacketPlayOutPlayerInfo create(EnumPlayerInfoAction action, GameProfileWrapper gameProfileWrapper, String name) {
        GameProfile gameProfile = (GameProfile) gameProfileWrapper.getGameProfile();

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo();
        Reflection.getField(packetPlayOutPlayerInfo.getClass(), "a", EnumPlayerInfoAction.class)
                .set(packetPlayOutPlayerInfo, action);

        PlayerInfoData playerInfoData = new PlayerInfoData(packetPlayOutPlayerInfo, gameProfile,
                1, EnumGamemode.NOT_SET, ChatSerializer.a(name));

        Reflection.FieldAccessor<List> fieldAccessor = Reflection.getField(packetPlayOutPlayerInfo.getClass(),
                "b", List.class);

        List<PlayerInfoData> list = fieldAccessor.get(packetPlayOutPlayerInfo);
        list.add(playerInfoData);
        fieldAccessor.set(packetPlayOutPlayerInfo, list);

        return packetPlayOutPlayerInfo;
    }
}
