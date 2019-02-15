/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_7_R4;

import net.jitse.npclib.api.NPC;
import net.jitse.npclib.nms.v1_7_R4.packets.PacketPlayOutEntityHeadRotationWrapper;
import net.jitse.npclib.nms.v1_7_R4.packets.PacketPlayOutNamedEntitySpawnWrapper;
import net.jitse.npclib.nms.v1_7_R4.packets.PacketPlayOutPlayerInfoWrapper;
import net.jitse.npclib.nms.v1_7_R4.packets.PacketPlayOutScoreboardTeamWrapper;
import net.jitse.npclib.skin.Skin;
import net.minecraft.server.v1_7_R4.*;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class NPC_v1_7_R4 extends NPC {

    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeamRegister, packetPlayOutScoreboardTeamUnregister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;

    private GameProfile legacyGameProfile;

    public NPC_v1_7_R4(JavaPlugin plugin, Skin skin, double autoHideDistance, List<String> lines) {
        super(plugin, skin, autoHideDistance, lines);
        // TODO: Add multi-line text support.
        this.name = lines.get(0);
    }

    @Override
    public void createPackets() {
        this.legacyGameProfile = generateLegacyGameProfile(uuid, name.length() < 16 ? name : name.substring(0, 15));
        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();

        // Packets for spawning the NPC:
        this.packetPlayOutScoreboardTeamRegister = new PacketPlayOutScoreboardTeamWrapper()
                .createRegisterTeam(legacyGameProfile.getId().toString().replace("-", "").substring(0, 10), name); // First packet to send.

        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(0, legacyGameProfile, name.length() < 16 ? name : name.length() < 16 ? name : name.substring(0, 15)); // Second packet to send.

        this.packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawnWrapper()
                .create(legacyGameProfile, location, entityId); // Third packet to send.

        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .create(location, entityId); // Fourth packet to send.

        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(4, legacyGameProfile, name.length() < 16 ? name : name.substring(0, 15)); // Fifth packet to send (delayed).

        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.

        // Second packet to send is "packetPlayOutPlayerInfoRemove".

        this.packetPlayOutScoreboardTeamUnregister = new PacketPlayOutScoreboardTeamWrapper()
                .createUnregisterTeam(legacyGameProfile.getId().toString().replace("-", "").substring(0, 10)); // Third packet to send.
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutScoreboardTeamRegister);
        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 50);
    }

    @Override
    public void sendHidePackets(Player player, boolean scheduler) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);

        if (scheduler) {
            // Sending this a bit later so the player doesn't see the name (for that split second).
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    playerConnection.sendPacket(packetPlayOutScoreboardTeamUnregister), 5);
        } else {
            playerConnection.sendPacket(packetPlayOutScoreboardTeamUnregister);
        }
    }

    private GameProfile generateLegacyGameProfile(UUID uuid, String name) {
        GameProfile gameProfile = new GameProfile(uuid, name);

        if (skin != null) {
            gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }

        return gameProfile;
    }
}
