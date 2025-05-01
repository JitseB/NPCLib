/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.nms.v1_13_R1;

import com.bnstra.npclib.nms.v1_13_R1.packets.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.bnstra.npclib.NPCLib;
import com.bnstra.npclib.api.skin.Skin;
import com.bnstra.npclib.api.state.NPCAnimation;
import com.bnstra.npclib.api.state.NPCSlot;
import com.bnstra.npclib.hologram.Hologram;
import com.bnstra.npclib.internal.MinecraftVersion;
import com.bnstra.npclib.internal.NPCBase;
import net.minecraft.server.v1_13_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class NPC_v1_13_R1 extends NPCBase {

    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeamRegister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;

    private final HashMap<UUID, Integer> playerInfoRemoveTimers = new HashMap<>();

    public NPC_v1_13_R1(NPCLib instance, List<String> lines) {
        super(instance, lines, MinecraftVersion.V1_13_R1);
    }

    @Override
    public Hologram getHologram(Player player) {
        Hologram holo = super.getHologram(player);
        if (holo == null) {
            holo = new Hologram(super.version, location.clone().add(0, 0.5, 0), getText(player));
        }
        super.playerHologram.put(player.getUniqueId(), holo);
        return holo;
    }


    @Override
    public void createPackets() {
        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();

        // Packets for spawning the NPC:
        this.packetPlayOutScoreboardTeamRegister = new PacketPlayOutScoreboardTeamWrapper()
                .createRegisterTeam(name); // First packet to send.

        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameProfile, name); // Second packet to send.

        this.packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawnWrapper()
                .create(uuid, location, entityId); // Third packet to send.

        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .create(location, entityId); // Fourth packet to send.

        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, gameProfile, name); // Fifth packet to send (delayed).

        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        if (hasTeamRegistered.add(player.getUniqueId()))
            playerConnection.sendPacket(packetPlayOutScoreboardTeamRegister);
        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);

        getHologram(player).show(player);

        // If there is already a timer, remove the old one (it is redundant)
        if (playerInfoRemoveTimers.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(playerInfoRemoveTimers.get(player.getUniqueId()));

        // Removing the player info after 10 seconds.
        playerInfoRemoveTimers.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(instance.getPlugin(), () -> {
            if (isShown(player)) {
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);
            }
            playerInfoRemoveTimers.remove(player.getUniqueId());
        }, 200).getTaskId());
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);

        getHologram(player).hide(player);
    }

    @Override
    public void sendMetadataPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadataWrapper().create(activeStates, entityId);

        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendEquipmentPacket(Player player, NPCSlot slot, boolean auto) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        EnumItemSlot nmsSlot = slot.getNmsEnum(EnumItemSlot.class);
        ItemStack item = getItem(slot);

        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, nmsSlot, CraftItemStack.asNMSCopy(item));
        playerConnection.sendPacket(packet);
    }

    @Override
    public void sendAnimationPacket(Player player, NPCAnimation animation) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutAnimation packet = new PacketPlayOutAnimationWrapper().create(animation, entityId);
        playerConnection.sendPacket(packet);
    }

    @Override
    public void updateSkin(Skin skin) {
        GameProfile newProfile = new GameProfile(uuid, name);
        newProfile.getProperties().get("textures").clear();
        newProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        this.packetPlayOutPlayerInfoAdd = new PacketPlayOutPlayerInfoWrapper().create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, newProfile, name);

        for (UUID shownUuid : super.getShown()) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                sendHidePackets(player);
                sendShowPackets(player);
                sendMetadataPacket(player);
                sendEquipmentPackets(player);
            }
        }
    }

    @Override
    public void sendHeadRotationPackets(Location location) {
        for (UUID shownUuid : super.getShown()) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

                Location npcLocation = getLocation();
                Vector dirBetweenLocations = location.toVector().subtract(npcLocation.toVector());

                npcLocation.setDirection(dirBetweenLocations);

                float yaw = npcLocation.getYaw();
                float pitch = npcLocation.getPitch();

                connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(getEntityId(), (byte) ((yaw % 360.) * 256 / 360), (byte) ((pitch % 360.) * 256 / 360), false));
                connection.sendPacket(new PacketPlayOutEntityHeadRotationWrapper().create(npcLocation, entityId));
            }
        }
    }
}
