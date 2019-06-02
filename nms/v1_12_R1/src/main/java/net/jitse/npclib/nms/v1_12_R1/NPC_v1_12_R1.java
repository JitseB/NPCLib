/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_12_R1;

import net.jitse.npclib.api.NPC;
import net.jitse.npclib.nms.holograms.Hologram;
import net.jitse.npclib.nms.v1_12_R1.packets.PacketPlayOutEntityHeadRotationWrapper;
import net.jitse.npclib.nms.v1_12_R1.packets.PacketPlayOutNamedEntitySpawnWrapper;
import net.jitse.npclib.nms.v1_12_R1.packets.PacketPlayOutPlayerInfoWrapper;
import net.jitse.npclib.nms.v1_12_R1.packets.PacketPlayOutScoreboardTeamWrapper;
import net.jitse.npclib.skin.Skin;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jitse Boonstra
 */
public class NPC_v1_12_R1 extends NPC {

    private Hologram hologram;
    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeamRegister, packetPlayOutScoreboardTeamUnregister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    private List<PacketPlayOutEntityEquipment> packetPlayOutEntityEquipment = new ArrayList<>();

    public NPC_v1_12_R1(JavaPlugin plugin, Skin skin, double autoHideDistance, List<String> lines, List<ItemStack> equipment) {
        super(plugin, skin, autoHideDistance, lines, equipment, null);
    }

    @Override
    public void createPackets() {
        this.hologram = new Hologram(location.clone().add(0, 0.5, 0), lines);
        hologram.generatePackets(true, false);

        this.gameProfile = generateGameProfile(uuid, name);
        PacketPlayOutPlayerInfoWrapper packetPlayOutPlayerInfoWrapper = new PacketPlayOutPlayerInfoWrapper();

        // Packets for spawning the NPC:
        this.packetPlayOutScoreboardTeamRegister = new PacketPlayOutScoreboardTeamWrapper()
                .createRegisterTeam(name); // First packet to send.

        this.packetPlayOutPlayerInfoAdd = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, gameProfile, name); // Second packet to send.

        if(equipment != null) {
            for (int i = 0; i < equipment.size(); i++) {
                if(equipment.get(i) != null) {
                    this.packetPlayOutEntityEquipment.add(new PacketPlayOutEntityEquipment(getEntityId(), EnumItemSlot.values()[i], CraftItemStack.asNMSCopy(equipment.get(i))));
                }
            }
        }

        this.packetPlayOutNamedEntitySpawn = new PacketPlayOutNamedEntitySpawnWrapper()
                .create(uuid, location, entityId); // Third packet to send.

        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .create(location, entityId); // Fourth packet to send.

        this.packetPlayOutPlayerInfoRemove = packetPlayOutPlayerInfoWrapper
                .create(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, gameProfile, name); // Fifth packet to send (delayed).

        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.

        // Second packet to send is "packetPlayOutPlayerInfoRemove".

        this.packetPlayOutScoreboardTeamUnregister = new PacketPlayOutScoreboardTeamWrapper()
                .createUnregisterTeam(name); // Third packet to send.
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutScoreboardTeamRegister);
        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);
        for(PacketPlayOutEntityEquipment equip : packetPlayOutEntityEquipment) {
            if (equip != null) playerConnection.sendPacket(equip);
        }

        hologram.spawn(player);


        Bukkit.getScheduler().runTaskLater(plugin, () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 50);
    }

    @Override
    public void sendHidePackets(Player player, boolean scheduler) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);

        hologram.destroy(player);

        if (scheduler) {
            // Sending this a bit later so the player doesn't see the name (for that split second).
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    playerConnection.sendPacket(packetPlayOutScoreboardTeamUnregister), 5);
        } else {
            playerConnection.sendPacket(packetPlayOutScoreboardTeamUnregister);
        }
    }
}
