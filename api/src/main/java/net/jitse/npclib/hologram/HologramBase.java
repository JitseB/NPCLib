package net.jitse.npclib.hologram;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class HologramBase implements Hologram, HologramPacketHandler {

    protected final static double DELTA = 0.3;

    protected final Set<UUID> shown = new HashSet<>();

    protected final Location start;

    protected List<String> text;

    public HologramBase(Location start, List<String> text) {
        this.start = start;
        this.text = text;

        // Generate the necessary show and hide packets.
        createPackets();
    }

    @Override
    public void show(Player player) {
        UUID uuid = player.getUniqueId();
        if (shown.contains(uuid))
            throw new IllegalArgumentException("Hologram is already shown to player");

        sendShowPackets(player);

        this.shown.add(uuid);
    }

    @Override
    public void hide(Player player) {
        UUID uuid = player.getUniqueId();
        if (!shown.contains(uuid))
            throw new IllegalArgumentException("Hologram is not shown to player");

        sendHidePackets(player);

        this.shown.remove(uuid);
    }

    @Override
    public void silentHide(UUID uuid) {
        if (!shown.contains(uuid))
            throw new IllegalArgumentException("Hologram is not shown to player");

        this.shown.remove(uuid);
    }

    @Override
    public void updateText(List<String> text) {
        if (this.text.size() != text.size())
            throw new IllegalArgumentException("When updating the text, the old and new text should have the same amount of lines");

        for (int i = 0; i < text.size(); i++) {
            String oldLine = this.text.get(i);
            String newLine = text.get(i);

            if (oldLine.equals(newLine))
                continue; // No need to update.

            // Perhaps this should return an object so we can send all immediately to the shown players.
            createTextUpdatePacket(oldLine, newLine);
        }

        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                throw new IllegalStateException("Tried to update hologram for offline player");
            }

//            sendTextUpdatePackets(player, );
        }
        this.text = text;
    }
}
