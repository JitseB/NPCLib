package net.jitse.npclib.hologram;

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
}
