/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_21_R4.packets;

import java.util.UUID;

import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;

/**
 * @author Jitse Boonstra
 */
public class PacketPlayOutSpawnEntityWrapper {

    public PacketPlayOutSpawnEntity create(UUID uuid, Location location, int entityId) {
        return new PacketPlayOutSpawnEntity(
                entityId,
                uuid,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getPitch(),
                location.getYaw(),
                EntityTypes.bT,
                0, // Entity data?
                new Vec3D(0, 0, 0),
                0 // Head yaw?
        );
    }
}
