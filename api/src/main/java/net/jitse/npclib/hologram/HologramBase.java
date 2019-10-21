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
    }

    abstract public void show(Player player);

    abstract public void hide(Player player);

    abstract public void silentHide(UUID uuid);

    abstract public void updateText(List<String> text);
}
