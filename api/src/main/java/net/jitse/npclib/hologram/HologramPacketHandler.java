package net.jitse.npclib.hologram;

public interface HologramPacketHandler {

    void sendTextUpdatePackets(int index, String newLine);
}
