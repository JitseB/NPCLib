package com.bnstra.npclib.nms.v1_21_R1;

import com.bnstra.npclib.nms.v1_21_R1.packets.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.bnstra.npclib.NPCLib;
import com.bnstra.npclib.api.skin.Skin;
import com.bnstra.npclib.api.state.NPCAnimation;
import com.bnstra.npclib.api.state.NPCSlot;
import com.bnstra.npclib.api.state.NPCState;
import com.bnstra.npclib.hologram.Hologram;
import com.bnstra.npclib.internal.MinecraftVersion;
import com.bnstra.npclib.internal.NPCBase;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EnumItemSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * @author Jitse Boonstra
 */
public class NPC_v1_21_R1 extends NPCBase {

    private PacketPlayOutSpawnEntity packetPlayOutSpawnEntity;
    private PacketPlayOutScoreboardTeam[] packetPlayOutScoreboardTeam;
    private ClientboundPlayerInfoUpdatePacket packetPlayOutPlayerInfoAdd;
    private ClientboundPlayerInfoRemovePacket packetPlayOutPlayerInfoRemove;
    private PacketPlayOutEntityHeadRotation packetPlayOutEntityHeadRotation;
    private PacketPlayOutEntityDestroy packetPlayOutEntityDestroy;
    private PacketPlayOutEntityMetadata packetPlayOutEntityMetadata;

    private final HashMap<UUID, Integer> playerInfoRemoveTimers = new HashMap<>();

    public NPC_v1_21_R1(NPCLib instance, List<String> lines) {
        super(instance, lines, MinecraftVersion.V1_21_R1);
    }

    @Override
    public Hologram getHologram(Player player) {
        Hologram hologram = super.getHologram(player);
        if (hologram == null)
            hologram = new Hologram(super.version, location.clone().add(0, 0.5, 0), getText(player));
        playerHologram.put(player.getUniqueId(), hologram);
        return hologram;
    }

    @Override
    public void createPackets() {
        ClientboundPlayerInfoWrapper clientboundPlayerInfoWrapper = new ClientboundPlayerInfoWrapper();

        // Packets for spawning the NPC:
        this.packetPlayOutScoreboardTeam = new PacketPlayOutScoreboardTeamWrapper()
                .createRegisterTeam(name, gameProfile); // First packet to send.

        this.packetPlayOutPlayerInfoAdd = clientboundPlayerInfoWrapper
                .createAddPlayerPacket(gameProfile, name); // Second packet to send.

        this.packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntityWrapper()
                .create(uuid, location, entityId); // Third packet to send.

        this.packetPlayOutEntityHeadRotation = new PacketPlayOutEntityHeadRotationWrapper()
                .create(location, entityId); // Fourth packet to send.

        this.packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadataWrapper()
                .create(Collections.singleton(NPCState.STANDING), entityId);

        this.packetPlayOutPlayerInfoRemove = clientboundPlayerInfoWrapper
                .createRemovePlayerPacket(uuid); // Fifth packet to send (delayed).

        // Packet for destroying the NPC:
        this.packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityId); // First packet to send.
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().c;

        if (hasTeamRegistered.add(player.getUniqueId())) {
            for (PacketPlayOutScoreboardTeam scoreboardPacket : packetPlayOutScoreboardTeam)
                playerConnection.b(scoreboardPacket);
        }
        playerConnection.b(packetPlayOutPlayerInfoAdd);
        playerConnection.b(packetPlayOutSpawnEntity);
        playerConnection.b(packetPlayOutEntityHeadRotation);
        playerConnection.b(packetPlayOutEntityMetadata);
        sendMetadataPacket(player);

        getHologram(player).show(player);

        // If there is already a timer, remove the old one (it is redundant)
        if (playerInfoRemoveTimers.containsKey(player.getUniqueId()))
            Bukkit.getScheduler().cancelTask(playerInfoRemoveTimers.get(player.getUniqueId()));

        // Removing the player info after 10 seconds.
        playerInfoRemoveTimers.put(player.getUniqueId(), Bukkit.getScheduler().runTaskLater(instance.getPlugin(), () -> {
            if (isShown(player)) {
                playerConnection.b(packetPlayOutPlayerInfoRemove);
            }
            playerInfoRemoveTimers.remove(player.getUniqueId());
        }, 200).getTaskId());
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().c;

        playerConnection.b(packetPlayOutEntityDestroy);
        playerConnection.b(packetPlayOutPlayerInfoRemove);

        getHologram(player).hide(player);
    }

    @Override
    public void sendMetadataPacket(Player player) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().c;
        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadataWrapper().create(activeStates, entityId);

        playerConnection.b(packet);
    }

    @Override
    public void sendEquipmentPacket(Player player, NPCSlot slot, boolean auto) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().c;

        EnumItemSlot nmsSlot = slot.getNmsEnum(EnumItemSlot.class);
        ItemStack item = getItem(slot);

        Pair<EnumItemSlot, net.minecraft.world.item.ItemStack> pair = new Pair<>(nmsSlot, CraftItemStack.asNMSCopy(item));
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(entityId, Collections.singletonList(pair));
        playerConnection.b(packet);
    }

    @Override
    public void sendAnimationPacket(Player player, NPCAnimation animation) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().c;

        PacketPlayOutAnimation packet = new PacketPlayOutAnimationWrapper().create(animation, entityId);
        playerConnection.b(packet);
    }

    @Override
    public void updateSkin(Skin skin) {
        GameProfile newProfile = new GameProfile(uuid, name);
        newProfile.getProperties().get("textures").clear();
        newProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        this.packetPlayOutPlayerInfoAdd = new ClientboundPlayerInfoWrapper().createAddPlayerPacket(newProfile, name);

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
                PlayerConnection connection = ((CraftPlayer) player).getHandle().c;

                Location npcLocation = getLocation();
                Vector dirBetweenLocations = location.toVector().subtract(npcLocation.toVector());

                npcLocation.setDirection(dirBetweenLocations);

                float yaw = npcLocation.getYaw();
                float pitch = npcLocation.getPitch();

                connection.b(new PacketPlayOutEntity.PacketPlayOutEntityLook(entityId,
                        (byte) ((yaw % 360.) * 256 / 360),
                        (byte) ((pitch % 360.) * 256 / 360), false));
                connection.b(new PacketPlayOutEntityHeadRotationWrapper()
                        .create(npcLocation, entityId));
            }
        }
    }
}
