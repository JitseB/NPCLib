/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_7_R4.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_7_R4.PacketPlayOutScoreboardTeam;

import java.util.Collection;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutScoreboardTeamWrapper {

    public PacketPlayOutScoreboardTeam createRegisterTeam(String uuidName, String name) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "f", int.class)
                .set(packetPlayOutScoreboardTeam, 0);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "b", String.class)
                .set(packetPlayOutScoreboardTeam, name.length() < 16 ? name : name.substring(0, 15));
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "a", String.class)
                .set(packetPlayOutScoreboardTeam, uuidName);
        if (name.length() > 16) {
            Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "d", String.class)
                    .set(packetPlayOutScoreboardTeam, name.substring(15));
        }
        if (name.length() > 32) {
            Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "c", String.class)
                    .set(packetPlayOutScoreboardTeam, name.substring(31));
        }
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "g", int.class)
                .set(packetPlayOutScoreboardTeam, 1);
        Reflection.FieldAccessor<Collection> collectionFieldAccessor = Reflection.getField(
                packetPlayOutScoreboardTeam.getClass(), "e", Collection.class);
        Collection collection = collectionFieldAccessor.get(packetPlayOutScoreboardTeam);
        collection.add(name.length() < 16 ? name : name.substring(0, 15));
        collectionFieldAccessor.set(packetPlayOutScoreboardTeam, collection);

        return packetPlayOutScoreboardTeam;
    }

    public PacketPlayOutScoreboardTeam createUnregisterTeam(String uuidName) {
        PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeam();

        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "f", int.class)
                .set(packetPlayOutScoreboardTeam, 1);
        Reflection.getField(packetPlayOutScoreboardTeam.getClass(), "a", String.class)
                .set(packetPlayOutScoreboardTeam, uuidName);

        return packetPlayOutScoreboardTeam;
    }
}
