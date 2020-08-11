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
    protected final Set<UUID> hasTeamRegistered = new HashSet<>();
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
    // Per-player holograms
    protected final Map<UUID, List<String>> uniqueText = new HashMap<>();
    protected final Map<UUID, Hologram> textDisplayHolograms = new HashMap<>();

    public NPCBase(NPCLib instance, List<String> text) {
        this.instance = instance;
        this.text = text == null ? Collections.emptyList() : text;
    }

    public NPCLib getInstance() {
        return instance;
    }

    @Override
    public Hologram getPlayerHologram(Player player) {
        Validate.notNull(player, "Player cannot be null");
        return textDisplayHolograms.getOrDefault(player.getUniqueId(), null);
    }


    @Override
    public NPC removePlayerLines(Player targetPlayer) {
        Validate.notNull(targetPlayer, "Player cannot be null");
        setPlayerLines(null, targetPlayer);
        return this;
    }

    @Override
    public NPC removePlayerLines(Player targetPlayer, boolean update) {
        Validate.notNull(targetPlayer, "Player cannot be null");
        setPlayerLines(null, targetPlayer, update);
        return this;
    }

    @Override
    public NPC setPlayerLines(List<String> uniqueLines, Player targetPlayer) {
        Validate.notNull(targetPlayer, "Player cannot be null");
        if (uniqueLines == null) uniqueText.remove(targetPlayer.getUniqueId());
        else uniqueText.put(targetPlayer.getUniqueId(), uniqueLines);
        return this;
    }

    @Override
    public NPC setPlayerLines(List<String> uniqueLines, Player targetPlayer, boolean update) {
        Validate.notNull(targetPlayer, "Player cannot be null");

        List<String> originalLines = getPlayerLines(targetPlayer);
        setPlayerLines(uniqueLines, targetPlayer);
        if (update) {

            uniqueLines = getPlayerLines(targetPlayer); // retrieve the player lines from this function, incase it's been removed.

            if (originalLines.size() != uniqueLines.size()) { // recreate the entire hologram
                Hologram originalhologram = getPlayerHologram(targetPlayer);
                originalhologram.hide(targetPlayer); // essentially destroy the hologram
                textDisplayHolograms.remove(targetPlayer.getUniqueId()); // remove the old obj
            }

            if (isShown(targetPlayer)) { //only show hologram if the player is in range
                Hologram hologram = getPlayerHologram(targetPlayer);
                List<Object> updatePackets = hologram.getUpdatePackets(getPlayerLines(targetPlayer));
                hologram.update(targetPlayer, updatePackets);
            }
        }
        return this;
    }

    @Override
    public List<String> getPlayerLines(Player targetPlayer) {
        Validate.notNull(targetPlayer, "Player cannot be null");
        return uniqueText.getOrDefault(targetPlayer.getUniqueId(), text);
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
    public NPC setLocation(Location location) {
        this.location = location;
        return this;
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
        show(player, false);
    }

    public void show(Player player, boolean auto) {
        Validate.notNull(player, "Player cannot be null");

        NPCShowEvent event = new NPCShowEvent(this, player);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        if (isShown(player)) throw new IllegalArgumentException("NPC is already shown to player");
        if (!player.isOnline()) throw new IllegalArgumentException("Player is not online");

        shown.add(player.getUniqueId());
        sendShowPackets(player);
        sendMetadataPacket(player);
        sendEquipmentPackets(player);
    }

    @Override
    public void hide(Player player) {
        Validate.notNull(player, "Player cannot be null");

        // TODO: Handle holograms

        NPCHideEvent event = new NPCHideEvent(this, player);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        if (!isShown(player))
            throw new IllegalArgumentException("NPC was never shown to the player");

        shown.remove(player.getUniqueId());
        if (player.isOnline()) sendHidePackets(player);
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
        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player))
                sendMetadataPacket(player);
        }
        return this;
    }

    @Override
    public void playAnimation(NPCAnimation animation) {
        Validate.notNull(animation, "Animation cannot be null");

        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player))
                sendAnimationPacket(player, animation);
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

        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player))
                sendEquipmentPacket(player, slot, false);
        }
        return this;
    }

    @Override
    public NPC setText(List<String> text) {
        uniqueText.clear();

        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                Hologram originalHologram = getPlayerHologram(player);
                originalHologram.hide(player); // essentially destroy the hologram
                textDisplayHolograms.remove(player.getUniqueId()); // remove the old obj
                Hologram hologram = getPlayerHologram(player); // let it regenerate
                List<Object> updatePackets = hologram.getUpdatePackets(getPlayerLines(player));
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

    @Override
    public boolean isShown(Player player) {
        Validate.notNull(player, "Player cannot be null");
        return shown.contains(player.getUniqueId());
    }
}
