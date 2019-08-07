/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.internal;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCHideEvent;
import net.jitse.npclib.api.events.NPCShowEvent;
import net.jitse.npclib.api.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class SimpleNPC implements NPC, PacketHandler {

    protected final UUID uuid = UUID.randomUUID();
    protected final int entityId = Integer.MAX_VALUE - NPCManager.getAllNPCs().size();
    protected final String name = uuid.toString().replace("-", "").substring(0, 10);
    protected final GameProfile gameProfile = new GameProfile(uuid, name);

    protected final List<String> lines;

    private final Set<UUID> shown = new HashSet<>();
    private final Set<UUID> autoHidden = new HashSet<>();

    protected double cosFOV = Math.cos(Math.toRadians(60));

    protected NPCLib instance;
    protected Location location;
    protected Skin skin;

    public SimpleNPC(NPCLib instance, List<String> lines) {
        this.instance = instance;
        this.lines = lines == null ? Collections.emptyList() : lines;

        NPCManager.add(this);
    }

    public NPCLib getInstance() {
        return instance;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public NPC setSkin(Skin skin) {
        this.skin = skin;

        gameProfile.getProperties().get("textures").clear();

        if (skin != null) {
            gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        }

        return this;
    }

    @Override
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
        this.cosFOV = Math.cos(Math.toRadians(fov));
    }

    public Set<UUID> getShown() {
        return shown;
    }

    public Set<UUID> getAutoHidden() {
        return autoHidden;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean isShown(Player player) {
        return shown.contains(player.getUniqueId()) && !autoHidden.contains(player.getUniqueId());
    }

    @Override
    public NPC setLocation(Location location) {
        this.location = location;
        return this;
    }

    @Override
    public NPC create() {
        createPackets();
        return this;
    }

    @Override
    public void show(Player player) {
        show(player, false);
    }

    public void show(Player player, boolean auto) {
        NPCShowEvent event = new NPCShowEvent(this, player, auto);
        Bukkit.getServer().getPluginManager().callEvent(event);
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
            if (isShown(player)) {
                throw new RuntimeException("Cannot call show method twice.");
            }

            if (shown.contains(player.getUniqueId())) {
                return;
            }

            shown.add(player.getUniqueId());

            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location)
                    <= instance.getAutoHideDistance()) {
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

    @Override
    public void hide(Player player) {
        hide(player, false, true);
    }

    public void hide(Player player, boolean auto, boolean scheduler) {
        NPCHideEvent event = new NPCHideEvent(this, player, auto);
        Bukkit.getServer().getPluginManager().callEvent(event);
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

            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location)
                    <= instance.getAutoHideDistance()) {
                sendHidePackets(player, scheduler);
            } else {
                autoHidden.remove(player.getUniqueId());
            }
        }
    }
}
