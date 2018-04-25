/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_12_R1.packets;

import com.comphenix.tinyprotocol.Reflection;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutEntityTeleportWrapper {

    public PacketPlayOutEntityTeleport create(int entityId, Location location) {
        PacketPlayOutEntityTeleport packetPlayOutEntityTeleport = new PacketPlayOutEntityTeleport();

        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "a", int.class)
                .set(packetPlayOutEntityTeleport, entityId);
        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "b", double.class)
                .set(packetPlayOutEntityTeleport, location.getX());
        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "c", double.class)
                .set(packetPlayOutEntityTeleport, location.getY());
        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "d", double.class)
                .set(packetPlayOutEntityTeleport, location.getZ());
        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "e", byte.class)
                .set(packetPlayOutEntityTeleport, (byte) ((int) (location.getYaw() * 256.0F / 360.0F)));
        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "f", byte.class)
                .set(packetPlayOutEntityTeleport, (byte) ((int) (location.getPitch() * 256.0F / 360.0F)));
        Reflection.getField(packetPlayOutEntityTeleport.getClass(), "g", boolean.class)
                .set(packetPlayOutEntityTeleport, location.getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR);

        return packetPlayOutEntityTeleport;
    }
}
