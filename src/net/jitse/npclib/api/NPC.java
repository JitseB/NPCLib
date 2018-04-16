/*
 * Copyright (c) Jitse Boonstra 2018 All rights reserved.
 */

package net.jitse.npclib.api;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.jitse.npclib.NPCManager;
import net.jitse.npclib.events.NPCDestroyEvent;
import net.jitse.npclib.events.NPCSpawnEvent;
import net.jitse.npclib.events.trigger.TriggerType;
import net.jitse.npclib.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * @author Jitse Boonstra
 */
public abstract class NPC {

    protected final UUID uuid = UUID.randomUUID();
    protected final String name = uuid.toString().replace("-", "").substring(0, 10);
    protected final int entityId = (int) Math.ceil(Math.random() * 100000) + 100000;

    private final Set<UUID> shown = new HashSet<>();
    private final Set<UUID> autoHidden = new HashSet<>();

    protected final double autoHideDistance;
    protected final Skin skin;
    protected final List<String> lines;

    protected JavaPlugin plugin;
    protected GameProfile gameProfile;
    protected Location location;

    public NPC(JavaPlugin plugin, Skin skin, double autoHideDistance, List<String> lines) {
        this.plugin = plugin;
        this.skin = skin;
        this.autoHideDistance = autoHideDistance;
        this.lines = (lines == null ? Collections.emptyList() : lines);

        NPCManager.add(this);
    }

    protected GameProfile generateGameProfile(UUID uuid, String name) {
        GameProfile gameProfile = new GameProfile(uuid, name);

        if (skin != null) {
            gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }

        return gameProfile;
    }

    public void destroy() {
        NPCManager.remove(this);
        shown.stream().filter(u -> !autoHidden.contains(u)).forEach(u -> hide(Bukkit.getPlayer(u), true));
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

    // Generate packets.
    public abstract void create(Location location);

    public void show(Player player) {
        show(player, false);
    }

    public void show(Player player, boolean auto) {
        NPCSpawnEvent event = new NPCSpawnEvent(this, player, auto ? TriggerType.AUTOMATIC : TriggerType.MANUAL);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (auto) {
            sendShowPackets(player);
        } else {
            if (shown.contains(player.getUniqueId())) {
                throw new RuntimeException("Cannot call show method twice.");
            }

            shown.add(player.getUniqueId());

            if (player.getLocation().distance(location) <= autoHideDistance) {
                sendShowPackets(player);
            } else {
                if (!autoHidden.contains(player.getUniqueId())) {
                    autoHidden.add(player.getUniqueId());
                }
            }
        }
    }

    // Internal method.
    protected abstract void sendShowPackets(Player player);

    public void hide(Player player) {
        hide(player, false);
    }

    public void hide(Player player, boolean auto) {
        NPCDestroyEvent event = new NPCDestroyEvent(this, player, auto ? TriggerType.AUTOMATIC : TriggerType.MANUAL);
        plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (auto) {
            sendHidePackets(player);
        } else {
            if (!shown.contains(player.getUniqueId())) {
                throw new RuntimeException("Cannot call hide method without calling NPC#show.");
            }

            shown.remove(player.getUniqueId());

            if (player.getLocation().distance(location) <= autoHideDistance) {
                sendHidePackets(player);
            } else {
                if (autoHidden.contains(player.getUniqueId())) {
                    autoHidden.remove(player.getUniqueId());
                }
            }
        }
    }

    // Internal method.
    protected abstract void sendHidePackets(Player player);
}
