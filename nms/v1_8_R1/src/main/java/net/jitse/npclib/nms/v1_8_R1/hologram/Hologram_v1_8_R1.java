package net.jitse.npclib.nms.v1_8_R1.hologram;

import net.jitse.npclib.hologram.HologramBase;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hologram_v1_8_R1 extends HologramBase {

    private List<EntityArmorStand> nmsArmorStands;
    private List<PacketPlayOutSpawnEntityLiving> showPackets;
    private List<PacketPlayOutEntityDestroy> hidePackets;

    public Hologram_v1_8_R1(Location start, List<String> text) {
        super(start, text);
    }

    @Override
    public void createPackets() {
        Location top = start.clone().add(0, DELTA * text.size(), 0);

        List<EntityArmorStand> nmsArmorStands = new ArrayList<>();
        List<PacketPlayOutSpawnEntityLiving> showPackets = new ArrayList<>();
        List<PacketPlayOutEntityDestroy> hidePackets = new ArrayList<>();

        for (String line : text) {
            EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) top.getWorld()).getHandle());
            armorStand.setLocation(top.getX(), top.getY(), top.getZ(), 0, 0);
            armorStand.setCustomName(line);
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setSmall(true);
            armorStand.setInvisible(true);
            armorStand.setBasePlate(false);
            armorStand.setArms(false);

            nmsArmorStands.add(armorStand);

            PacketPlayOutSpawnEntityLiving lineShowPacket = new PacketPlayOutSpawnEntityLiving(armorStand);
            PacketPlayOutEntityDestroy lineHidePacket = new PacketPlayOutEntityDestroy(armorStand.getId());

            showPackets.add(lineShowPacket);
            hidePackets.add(lineHidePacket);

            top.subtract(0, DELTA, 0);
        }

        this.nmsArmorStands = nmsArmorStands;
        this.showPackets = showPackets;
        this.hidePackets = hidePackets;
    }

    @Override
    public void sendShowPackets(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to spawn the line.
            connection.sendPacket(showPackets.get(i));
        }
    }

    @Override
    public void sendHidePackets(Player player) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to hide the line (as it was never spawned).
            connection.sendPacket(hidePackets.get(i));
        }
    }

    @Override
    public void updateText(List<String> text) {
        List<Packet> toSend = new ArrayList<>();

        if (this.text.size() != text.size())
            throw new IllegalArgumentException("When updating the text, the old and new text should have the same amount of lines");

        for (int i = 0; i < text.size(); i++) {
            EntityArmorStand armorStand = nmsArmorStands.get(i);
            String oldLine = this.text.get(i);
            String newLine = text.get(i);

            armorStand.getDataWatcher().watch(2, newLine); // Update the DataWatcher object.

            if (oldLine.equals(newLine)) {
                continue; // No need to update.
            } else if (newLine.isEmpty() && !oldLine.isEmpty()) {
                // Check if line was empty before, if not, remove the hologram line.
                toSend.add(hidePackets.get(i));
            } else if (!newLine.isEmpty() && oldLine.isEmpty()) {
                // Check if line was empty before, if it was, create the hologram line.
                PacketPlayOutSpawnEntityLiving lineShowPacket = new PacketPlayOutSpawnEntityLiving(armorStand);
                toSend.add(lineShowPacket);
            } else {
                // If the line was not empty before and it isn't now, update its text.
                PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(armorStand.getId(), armorStand.getDataWatcher(), true);
                toSend.add(metadataPacket);
            }
        }

        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                throw new IllegalStateException("Tried to update hologram for offline player");
            }

            PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
            for (Packet packet : toSend) {
                connection.sendPacket(packet);
            }
        }

        this.text = text; // At last, update the text in this hologram object.
    }
}
