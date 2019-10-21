package net.jitse.npclib.nms.v1_8_R1.hologram;

import net.jitse.npclib.hologram.HologramBase;
import org.bukkit.Location;

import java.util.List;

public class Hologram extends HologramBase {

    // Perhaps all logic methods should be placed in the base class instead then
    // use a similar approach to the NPCBase class, with a HologramPacketHandler class...
    // That way I can't mess up logic on different version whilst implementing new features.
    // *cough cough* hologram text updates. I can already see this go wrong.
    public Hologram(Location start, List<String> text) {
        super(start, text);
    }

//    @Override
//    public void show(Player player) {
//        UUID uuid = player.getUniqueId();
//        if (shown.contains(uuid))
//            throw new IllegalArgumentException("Hologram is already shown to player");
//
//        // TODO: Send packets
//
//        this.shown.add(uuid);
//    }
//
//    @Override
//    public void hide(Player player) {
//        UUID uuid = player.getUniqueId();
//        if (!shown.contains(uuid))
//            throw new IllegalArgumentException("Hologram is not shown to player");
//
//        // TODO: Send packets
//
//        this.shown.remove(uuid);
//    }
//
//    @Override
//    public void silentHide(UUID uuid) {
//        if (!shown.contains(uuid))
//            throw new IllegalArgumentException("Hologram is not shown to player");
//        this.shown.remove(uuid);
//    }
//
//    @Override
//    public void updateText(List<String> text) {
//
//    }


    @Override
    public void sendTextUpdatePackets(int index, String newLine) {
        if (newLine.isEmpty()) {
            // Check if line was empty before, if not, remove the hologram line.
        } else {
            // Check if line was empty before, if it was, create the hologram line.
            // If the line was not empty before and it isn't now, update its text.
        }

        // Send the packets to all players that can see the hologram (i.e. shown set).
    }
}
