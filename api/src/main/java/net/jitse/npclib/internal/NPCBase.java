/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCHideEvent;
import net.jitse.npclib.api.events.NPCShowEvent;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.api.state.NPCState;
import net.jitse.npclib.hologram.Hologram;
import net.labymod.utilities.LMCUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class NPCBase implements NPC, NPCPacketHandler {

    protected final int entityId = Integer.MAX_VALUE - NPCManager.getAllNPCs().size();
    protected final Set<UUID> hasTeamRegistered = new HashSet<>();
    protected final Set<NPCState> activeStates = EnumSet.noneOf(NPCState.class);

    private final Set<UUID> shown = new HashSet<>();
    private final Set<UUID> autoHidden = new HashSet<>();

    protected double cosFOV = Math.cos(Math.toRadians(60));
    // 12/4/20, JMB: Changed the UUID in order to enable LabyMod Emotes:
    // This gives a format similar to: 528086a2-4f5f-2ec2-0000-000000000000
    protected UUID uuid = new UUID(new Random().nextLong(), 0);
    protected String name = uuid.toString().replace("-", "").substring(0, 10);
    protected GameProfile gameProfile = new GameProfile(uuid, name);

    protected NPCLib instance;
    protected List<String> text;
    protected Location location;
    protected Skin skin;
    protected Hologram hologram;

    protected final Map<NPCSlot, ItemStack> items = new EnumMap<>(NPCSlot.class);

    public NPCBase(NPCLib instance, List<String> text) {
        this.instance = instance;
        this.text = text == null ? Collections.emptyList() : text;

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
        if (skin != null)
            gameProfile.getProperties().put("textures", new Property("textures", skin.getValue(), skin.getSignature()));

        return this;
    }

    @Override
    public void destroy() {
        NPCManager.remove(this);

        // Destroy NPC for every player that is still seeing it.
        for (UUID uuid : shown) {
            if (autoHidden.contains(uuid)) {
                continue;
            }

            hide(Bukkit.getPlayer(uuid), true);
        }
    }

    @Override
    public void forceLabyModEmote(Player receiver, int emoteId) {
        JsonArray array = new JsonArray();
        JsonObject forcedEmote = new JsonObject();
        forcedEmote.addProperty("uuid", uuid.toString());
        forcedEmote.addProperty("emote_id", emoteId);
        array.add(forcedEmote);
        LMCUtils.sendLMCMessage(receiver, "emote_api", array.getAsJsonObject());
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

    @Override
    public World getWorld() {
        return location != null ? location.getWorld() : null;
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

    public void onLogout(Player player) {
        getAutoHidden().remove(player.getUniqueId());
        getShown().remove(player.getUniqueId()); // Don't need to use NPC#hide since the entity is not registered in the NMS server.
        hasTeamRegistered.remove(player.getUniqueId());
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
            sendMetadataPacket(player);
            sendEquipmentPackets(player);
        } else {
            if (isShown(player)) {
                throw new IllegalStateException("Cannot call show method twice.");
            }

            if (shown.contains(player.getUniqueId())) {
                return;
            }

            shown.add(player.getUniqueId());

            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location)
                    <= instance.getAutoHideDistance()) {
                sendShowPackets(player);
                sendMetadataPacket(player);
                sendEquipmentPackets(player);
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
        hide(player, false);
    }

    public void hide(Player player, boolean auto) {
        NPCHideEvent event = new NPCHideEvent(this, player, auto);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        if (auto) {
            sendHidePackets(player);
        } else {
            if (!shown.contains(player.getUniqueId())) {
                throw new IllegalStateException("Cannot call hide method without calling NPC#show.");
            }

            shown.remove(player.getUniqueId());

            if (player.getWorld().equals(location.getWorld()) && player.getLocation().distance(location)
                    <= instance.getAutoHideDistance()) {
                sendHidePackets(player);
            } else {
                autoHidden.remove(player.getUniqueId());
            }
        }
    }

    @Override
    public boolean getState(NPCState state) {
        return activeStates.contains(state);
    }

    @Override
    public NPC toggleState(NPCState state) {
        if (activeStates.contains(state)) {
            activeStates.remove(state);
        } else {
            activeStates.add(state);
        }

        // Send a new metadata packet to all players that can see the NPC.
        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                sendMetadataPacket(player);
            }
        }
        return this;
    }

    @Override
    public ItemStack getItem(NPCSlot slot) {
        Objects.requireNonNull(slot, "Slot cannot be null");

        return items.get(slot);
    }

    @Override
    public NPC setItem(NPCSlot slot, ItemStack item) {
        Objects.requireNonNull(slot, "Slot cannot be null");

        items.put(slot, item);

        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                sendEquipmentPacket(player, slot, false);
            }
        }
        return this;
    }

    @Override
    public NPC setText(List<String> text) {
        List<Object> updatePackets = hologram.getUpdatePackets(text);

        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                hologram.update(player, updatePackets);
            }
        }

        this.text = text;
        return this;
    }

    @Override
    public List<String> getText() {
        return text;
    }
}
