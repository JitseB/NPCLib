package net.jitse.npclib.hologram;

import org.bukkit.entity.Player;

public interface HologramPacketHandler {

    void createPackets();

    void sendShowPackets(Player player);

    void sendHidePackets(Player player);

    void createTextUpdatePacket(String oldLine, String newLine);

    void sendTextUpdatePackets(Player player);
}
