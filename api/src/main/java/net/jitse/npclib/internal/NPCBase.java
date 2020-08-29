/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.internal;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.NPCLibManager;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.events.NPCHideEvent;
import net.jitse.npclib.api.events.NPCShowEvent;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.api.state.NPCState;
import net.jitse.npclib.hologram.Hologram;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class NPCBase implements NPC, NPCPacketHandler {

    protected final int entityId = Integer.MAX_VALUE - NPCLibManager.getLibrary().getNPCs().size();
    protected final Set<UUID> team = new HashSet<>();
    protected final Set<NPCState> activeStates = EnumSet.noneOf(NPCState.class);

    private final Set<UUID> shown = new HashSet<>();

    // 12/4/20, JMB: Changed the UUID in order to enable LabyMod Emotes:
    // This gives a format similar to: 528086a2-4f5f-2ec2-0000-000000000000
    protected UUID uuid = new UUID(new Random().nextLong(), 0);
    protected String name = uuid.toString().replace("-", "").substring(0, 10);
    protected GameProfile gameProfile = new GameProfile(uuid, name);
    protected boolean created = false;

    protected NPCLib instance;
    protected List<String> text;
    protected Location location;
    protected Skin skin;

    protected final Map<NPCSlot, ItemStack> items = new EnumMap<>(NPCSlot.class);
    protected final Map<UUID, Hologram> holograms = new HashMap<>(); // Per player holograms

    public NPCBase(NPCLib instance, Location location, List<String> text) {
        this.instance = instance;
        this.text = text == null ? Collections.emptyList() : text;
    }

    @Override
    public List<String> getText(Player player) {
        return holograms.get(player.getUniqueId()).getText();
    }

    @Override
    public List<String> getText() {
        // TODO: Throw error if per-player holograms are used
        Hologram hologram = holograms.values().stream().findFirst().orElse(null);
        return hologram != null ? hologram.getText() : null;
    }

    @Override
    public void updateText(Player player, List<String> text) {
        holograms.get(player.getUniqueId()).update(player, text == null ? Collections.emptyList() : text);
    }

    @Override
    public void teleport(Location location) {
        this.location = location;
        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) sendTeleportationPacket(player);
        }
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
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
    public Location getLocation() {
        return location;
    }

    @Override
    public World getWorld() {
        return location != null ? location.getWorld() : null;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public NPC create() {
        createPackets();
        this.created = true;
        return this;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    @Override
    public void show(Player player) {
        Validate.notNull(player, "Player cannot be null");
        if (!created) throw new IllegalStateException("NPC packets have not been generated yet. Use NPC#create");

        NPCShowEvent event = new NPCShowEvent(this, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        if (isShown(player)) throw new IllegalArgumentException("NPC is already shown to player");
        if (!player.isOnline()) throw new IllegalArgumentException("Player is not online");

        shown.add(player.getUniqueId());
        holograms.put(player.getUniqueId(), createHologram(player));

        sendShowPackets(player);
        sendMetadataPacket(player);
        sendEquipmentPackets(player);
    }

    @Override
    public void hide(Player player) {
        Validate.notNull(player, "Player cannot be null");
        if (!created) throw new IllegalStateException("NPC packets have not been generated yet. Use NPC#create");

        NPCHideEvent event = new NPCHideEvent(this, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        if (!isShown(player)) throw new IllegalArgumentException("NPC was never shown to the player");

        shown.remove(player.getUniqueId());
        holograms.remove(player.getUniqueId());

        if (player.isOnline()) {
            sendHidePackets(player);
            // TODO: Remove team
        }
    }

    @Override
    public boolean getState(NPCState state) {
        Validate.notNull(state, "State cannot be null");
        return activeStates.contains(state);
    }

    @Override
    public NPC toggleState(NPCState state) {
        Validate.notNull(state, "State cannot be null");

        if (activeStates.contains(state)) activeStates.remove(state);
        else activeStates.add(state);

        // Send a new metadata packet to all players that can see the NPC.
        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) sendMetadataPacket(player);
        }
        return this;
    }

    @Override
    public void playAnimation(NPCAnimation animation) {
        Validate.notNull(animation, "Animation cannot be null");

        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) sendAnimationPacket(player, animation);
        }
    }

    @Override
    public ItemStack getItem(NPCSlot slot) {
        Validate.notNull(slot, "Slot cannot be null");
        return items.get(slot);
    }

    @Override
    public NPC setItem(NPCSlot slot, ItemStack item) {
        Validate.notNull(slot, "Slot cannot be null");
        items.put(slot, item);

        for (UUID uuid : shown) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) sendEquipmentPacket(player, slot, false);
        }
        return this;
    }

    @Override
    public boolean isShown(Player player) {
        Validate.notNull(player, "Player cannot be null");
        return shown.contains(player.getUniqueId());
    }
}
