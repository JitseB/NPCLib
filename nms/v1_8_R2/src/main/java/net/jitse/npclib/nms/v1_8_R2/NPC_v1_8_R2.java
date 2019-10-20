/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.v1_8_R2;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.hologram.Hologram;
import net.jitse.npclib.internal.MinecraftVersion;
import net.jitse.npclib.internal.SimpleNPC;
import net.jitse.npclib.nms.v1_8_R2.packets.*;
import net.minecraft.server.v1_8_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class NPC_v1_8_R2 extends SimpleNPC {

    private Hologram hologram;
    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeamRegister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    private Set<UUID> hasTeamRegistered = new HashSet<>();

    public NPC_v1_8_R2(NPCLib instance, List<String> lines) {
        super(instance, lines);
    }

    @Override
    public void createPackets() {
        this.hologram = new Hologram(location.clone().add(0, 0.5, 0), lines);
        hologram.generatePackets(MinecraftVersion.V1_8_R2);

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
    public void onLogout(Player player) {
        super.onLogout(player);
        hasTeamRegistered.remove(player.getUniqueId());
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        if (hasTeamRegistered.add(player.getUniqueId()))
            playerConnection.sendPacket(packetPlayOutScoreboardTeamRegister);
        playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
        playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        playerConnection.sendPacket(packetPlayOutEntityHeadRotation);

        hologram.spawn(player);

        Bukkit.getScheduler().runTaskLater(instance.getPlugin(), () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 50);
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);
        hologram.destroy(player);
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

        ItemStack item;
        switch (slot) {
            case HELMET:
                item = helmet;
                break;
            case CHESTPLATE:
                item = chestplate;
                break;
            case LEGGINGS:
                item = leggings;
                break;
            case BOOTS:
                item = boots;
                break;
            case MAINHAND:
                item = inHand;
                break;
            default:
                if (!auto) {
                    throw new IllegalArgumentException(slot.toString() + " is not a supported slot for the version of your server");
                }
                return;
        }

        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, slot.getSlot(), CraftItemStack.asNMSCopy(item));
        playerConnection.sendPacket(packet);
    }
}
