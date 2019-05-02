/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.wrapper.GameProfileWrapper;
import net.jitse.npclib.events.NPCDestroyEvent;
import net.jitse.npclib.events.NPCSpawnEvent;
import net.jitse.npclib.events.trigger.TriggerType;
import net.jitse.npclib.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * @author Jitse Boonstra
 */
public abstract class NPC implements PacketHandler, ActionHandler {

    protected final UUID uuid = UUID.randomUUID();
    // Below was previously = (int) Math.ceil(Math.random() * 100000) + 100000 (new is experimental).
    protected final int entityId = Integer.MAX_VALUE - NPCManager.getAllNPCs().size();

    protected double cosFOV = Math.cos(Math.toRadians(60));
    protected String name = uuid.toString().replace("-", "").substring(0, 10);

    private final Set<UUID> shown = new HashSet<>();
    private final Set<UUID> autoHidden = new HashSet<>();

    protected final double autoHideDistance;
    protected final Skin skin;
    protected final List<String> lines;

    protected JavaPlugin plugin;
    protected GameProfileWrapper gameProfile;
    protected Location location;

    public NPC(JavaPlugin plugin, Skin skin, double autoHideDistance, List<String> lines) {
        this.plugin = plugin;
        this.skin = skin;
        this.autoHideDistance = autoHideDistance;
        this.lines = lines == null ? Collections.emptyList() : lines;

        NPCManager.add(this);
    }

    protected GameProfileWrapper generateGameProfile(UUID uuid, String name) {
        GameProfileWrapper gameProfile = new GameProfileWrapper(uuid, name);

        if (skin != null) {
            gameProfile.addSkin(skin);
        }

        return gameProfile;
    }


    public void destroy() {
        destroy(true);
    }

    public void destroy(boolean scheduler) {
        NPCManager.remove(this);

        // Destroy NPC for every player that is still seeing it.
        for (UUID uuid : shown) {
            if (autoHidden.contains(uuid)) {
                continue;
            }

            hide(Bukkit.getPlayer(uuid), true, scheduler);
        }
    }

    public void disableFOV() {
        this.cosFOV = 0; // Or equals Math.cos(1/2 * Math.PI).
    }

    public void setFOV(double fov) {
        this.cosFOV = Math.cos(Math.toRadians(60));
    }

    public Set<UUID> getShown() {
        return shown;
    }

    public Set<UUID> getAutoHidden() {
        return autoHidden;
    }

    public Location getLocation() {
        return location;
    }

    public double getAutoHideDistance() {
        return autoHideDistance;
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean isActuallyShown(Player player) {
        return shown.contains(player.getUniqueId()) && !autoHidden.contains(player.getUniqueId());
    }

    public void create(Location location) {
        this.location = location;

        createPackets();
    }

    public void show(Player player) {
        show(player, false);
    }

    public void show(Player player, boolean auto) {
        NPCSpawnEvent event = new NPCSpawnEvent(this, player, auto ? TriggerType.AUTOMATIC : TriggerType.MANUAL);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (!canSeeNPC(player)) {
            if (!auto) {
                shown.add(player.getUniqueId());
            }

            autoHidden.add(player.getUniqueId());
            return;
        }

        if (auto) {
            sendShowPackets(player);
        } else {
            if (isActuallyShown(player)) {
                throw new RuntimeException("Cannot call show method twice.");
            }

            if (shown.contains(player.getUniqueId())) {
                return;
            }

            shown.add(player.getUniqueId());

            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location) <= autoHideDistance) {
                sendShowPackets(player);
            } else {
                autoHidden.add(player.getUniqueId());
            }
        }
    }

    private boolean canSeeNPC(Player player) {
        Vector dir = location.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getLocation().getDirection()) >= cosFOV;
    }

    public void hide(Player player) {
        hide(player, false, true);
    }

    public void hide(Player player, boolean auto, boolean scheduler) {
        NPCDestroyEvent event = new NPCDestroyEvent(this, player, auto ? TriggerType.AUTOMATIC : TriggerType.MANUAL);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (auto) {
            sendHidePackets(player, scheduler);
        } else {
            if (!shown.contains(player.getUniqueId())) {
                throw new RuntimeException("Cannot call hide method without calling NPC#show.");
            }

            shown.remove(player.getUniqueId());

            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location) <= autoHideDistance) {
                sendHidePackets(player, scheduler);
            } else {
                autoHidden.remove(player.getUniqueId());
            }
        }
    }
}
