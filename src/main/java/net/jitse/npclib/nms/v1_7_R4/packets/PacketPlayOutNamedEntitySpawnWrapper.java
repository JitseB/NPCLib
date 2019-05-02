/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_7_R4.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_7_R4.DataWatcher;
import net.minecraft.server.v1_7_R4.PacketPlayOutNamedEntitySpawn;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Location;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutNamedEntitySpawnWrapper {

    public PacketPlayOutNamedEntitySpawn create(GameProfile gameProfile, Location location, int entityId) {
        PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawn();

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "a", int.class)
                .set(packetPlayOutNamedEntitySpawn, entityId);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "b", GameProfile.class)
                .set(packetPlayOutNamedEntitySpawn, gameProfile);
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "c", int.class)
                .set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getX() * 32.0D));
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "d", int.class)
                .set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getY() * 32.0D));
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "e", int.class)
                .set(packetPlayOutNamedEntitySpawn, (int) Math.floor(location.getZ() * 32.0D));
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "f", byte.class)
                .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "g", byte.class)
                .set(packetPlayOutNamedEntitySpawn, (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));

        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(10, (byte) 127);

        Reflection.getField(packetPlayOutNamedEntitySpawn.getClass(), "i", DataWatcher.class)
                .set(packetPlayOutNamedEntitySpawn, dataWatcher);

        return packetPlayOutNamedEntitySpawn;
    }
}
