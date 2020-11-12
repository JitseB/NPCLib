package net.jitse.npclib.nms.v1_16_R2;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;

import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.hologram.Hologram;
import net.jitse.npclib.internal.MinecraftVersion;
import net.jitse.npclib.internal.NPCBase;
import net.jitse.npclib.nms.v1_16_R2.packets.PacketPlayOutAnimationWrapper;
import net.jitse.npclib.nms.v1_16_R2.packets.PacketPlayOutEntityHeadRotationWrapper;
import net.jitse.npclib.nms.v1_16_R2.packets.PacketPlayOutEntityMetadataWrapper;
import net.jitse.npclib.nms.v1_16_R2.packets.PacketPlayOutNamedEntitySpawnWrapper;
import net.jitse.npclib.nms.v1_16_R2.packets.PacketPlayOutPlayerInfoWrapper;
import net.jitse.npclib.nms.v1_16_R2.packets.PacketPlayOutScoreboardTeamWrapper;
import net.minecraft.server.v1_16_R2.EnumItemSlot;
import net.minecraft.server.v1_16_R2.PacketPlayOutAnimation;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntity;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_16_R2.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_16_R2.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R2.PlayerConnection;

/**
 * @author Jitse Boonstra
 */
public class NPC_v1_16_R2 extends NPCBase {

    private PacketPlayOutNamedEntitySpawn packetPlayOutNamedEntitySpawn;
    private PacketPlayOutScoreboardTeam packetPlayOutScoreboardTeamRegister;
    private PacketPlayOutPlayerInfo packetPlayOutPlayerInfoAdd, packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    private PacketPlayOutAnimation packetPlayOutAnimation;

    public NPC_v1_16_R2(NPCLib instance, List<String> lines) {
        super(instance, lines);
    }

    @Override
    public Hologram getPlayerHologram(Player player) {
        Hologram holo = super.getPlayerHologram(player);
        if (holo == null) {
            holo = new Hologram(MinecraftVersion.V1_16_R1, location.clone().add(0, 0.5, 0), getPlayerLines(player));
        }
        super.textDisplayHolograms.put(player.getUniqueId(), holo);
        return holo;
    }

    @Override
    public void createPackets() {
        Bukkit.getOnlinePlayers().forEach(this::createPackets);
    }

    @Override
    public void createPackets(Player player) {

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
        sendMetadataPacket(player);

        getPlayerHologram(player).show(player);

        // Removing the player info after 10 seconds.
        Bukkit.getScheduler().runTaskLater(instance.getPlugin(), () ->
                playerConnection.sendPacket(packetPlayOutPlayerInfoRemove), 200);
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        playerConnection.sendPacket(packetPlayOutEntityDestroy);
        playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);

        getPlayerHologram(player).hide(player);
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

        Pair<EnumItemSlot, net.minecraft.server.v1_16_R2.ItemStack> pair = new Pair<>(nmsSlot, CraftItemStack.asNMSCopy(item));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, Collections.singletonList(pair));
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;
            playerConnection.sendPacket(packetPlayOutPlayerInfoRemove);
            playerConnection.sendPacket(packetPlayOutEntityDestroy);
            playerConnection.sendPacket(packetPlayOutPlayerInfoAdd);
            playerConnection.sendPacket(packetPlayOutNamedEntitySpawn);
        }
    }
    
    @Override
    public void sendHeadRotationPackets(Location location) {
    	for (Player player : Bukkit.getOnlinePlayers()) {    		
    		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
    		
    		Location npcLocation = getLocation();
    		Vector dirBetweenLocations = location.toVector().subtract(npcLocation.toVector());
    		
            npcLocation.setDirection(dirBetweenLocations);
            
            float yaw = npcLocation.getYaw();
            float pitch = npcLocation.getPitch();
            
            connection.sendPacket(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId, (byte) ((yaw % 360.) * 256 / 360), (byte) ((pitch % 360.) * 256 / 360), false));
            connection.sendPacket(new PacketPlayOutEntityHeadRotationWrapper().create(npcLocation, entityId));
    	}
    }
}
