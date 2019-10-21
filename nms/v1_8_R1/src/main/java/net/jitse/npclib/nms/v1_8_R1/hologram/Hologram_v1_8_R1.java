package net.jitse.npclib.nms.v1_8_R1.hologram;

import net.jitse.npclib.hologram.HologramBase;
import net.minecraft.server.v1_8_R1.EntityArmorStand;
import net.minecraft.server.v1_8_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.List;

public class Hologram_v1_8_R1 extends HologramBase {

    // Perhaps all logic methods should be placed in the base class instead then
    // use a similar approach to the NPCBase class, with a HologramPacketHandler class...
    // That way I can't mess up logic on different version whilst implementing new features.
    // *cough cough* hologram text updates. I can already see this go wrong.
    public Hologram_v1_8_R1(Location start, List<String> text) {
        super(start, text);
    }

    @Override
    public void createPackets() {
        Location top = start.clone().add(0, DELTA * text.size(), 0);

        for (String line : text) {
            if (line.isEmpty()) {
                top.subtract(0, DELTA, 0);
                continue;
            }

            EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) top.getWorld()).getHandle());
            armorStand.setLocation(top.getX(), top.getY(), top.getZ(), 0, 0);
            armorStand.setCustomName(line); // Does this method update? Or should teams be used?
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setSmall(true);
            armorStand.setInvisible(true);
            armorStand.setBasePlate(false);
            armorStand.setArms(false);

            PacketPlayOutSpawnEntityLiving lineShowPacket = new PacketPlayOutSpawnEntityLiving(armorStand);
            PacketPlayOutEntityDestroy lineHidePacket = new PacketPlayOutEntityDestroy(armorStand.getId());
            // TODO: Add packets and ArmorStand object to some sort of manager.

            top.subtract(0, DELTA, 0);
        }
    }

    @Override
    public void sendShowPackets(Player player) {

    }

    @Override
    public void sendHidePackets(Player player) {

    }

    @Override
    public void createTextUpdatePacket(String oldLine, String newLine) {

    }

    @Override
    public void sendTextUpdatePackets(Player player) {

    }

    @Override
    public void sendTextUpdatePackets(Player player, int index, String newLine) {
        if (newLine.isEmpty()) {
            // Check if line was empty before, if not, remove the hologram line.
        } else {
            // Check if line was empty before, if it was, create the hologram line.
            // If the line was not empty before and it isn't now, update its text.
        }

        // Send the packets to all players that can see the hologram (i.e. shown set).
    }
}
