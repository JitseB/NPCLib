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
import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.api.state.NPCState;
import net.jitse.npclib.hologram.Hologram;
import net.jitse.npclib.utilities.MathUtil;
import org.apache.commons.lang.Validate;
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
    protected boolean created = false;

    protected NPCLib instance;
    protected List<String> text;
    protected Location location;
    protected Skin skin;

    //protected Hologram hologram;

    protected final Map<NPCSlot, ItemStack> items = new EnumMap<>(NPCSlot.class);

    // Storage for per-player text;
    protected final Map<UUID, List<String>> uniqueText = new HashMap<>();
    protected final Map<UUID, Hologram> textDisplayHolograms = new HashMap<>();

    public NPCBase(NPCLib instance, List<String> text) {
        this.instance = instance;
        this.text = text == null ? Collections.emptyList() : text;

        NPCManager.add(this);
    }

    public NPCLib getInstance() {
        return instance;
    }

    @Override
    public Hologram getPlayerHologram(Player player) {
        Validate.notNull(player, "Player cannot be null.");
        Hologram playerHologram = textDisplayHolograms.getOrDefault(player.getUniqueId(), null);
        return playerHologram;
    }


    @Override
    public NPC removePlayerLines(Player targetPlayer) {
        Validate.notNull(targetPlayer, "Player cannot be null.");
        setPlayerLines(null, targetPlayer);
        return this;
    }

    @Override
    public NPC removePlayerLines(Player targetPlayer, boolean update) {
        Validate.notNull(targetPlayer, "Player cannot be null.");
        setPlayerLines(null, targetPlayer, update);
        return this;
    }

    @Override
    public NPC setPlayerLines(List<String> uniqueLines, Player targetPlayer) {
        Validate.notNull(targetPlayer, "Player cannot be null.");
        if (uniqueLines == null) uniqueText.remove(targetPlayer.getUniqueId());
        else uniqueText.put(targetPlayer.getUniqueId(), uniqueLines);
        return this;
    }

    @Override
    public NPC setPlayerLines(List<String> uniqueLines, Player targetPlayer, boolean update) {
        Validate.notNull(targetPlayer, "Player cannot be null.");
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
        Validate.notNull(targetPlayer, "Player cannot be null.");
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
    public void destroy() {
        NPCManager.remove(this);

        // Destroy NPC for every player that is still seeing it.
        for (UUID uuid : shown) {
            if (autoHidden.contains(uuid)) {
                continue;
            }
            Player plyr = Bukkit.getPlayer(uuid); // destroy the per player holograms
            if (plyr != null) {
                getPlayerHologram(plyr).hide(plyr);
                hide(plyr, true);
            }
        }
    }

    public void disableFOV() {
        this.cosFOV = 0;
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
        Objects.requireNonNull(player, "Player object cannot be null");
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
        this.created = true;
        return this;
    }

    @Override
    public boolean isCreated() {
        return created;
    }

    public void onLogout(Player player) {
        getAutoHidden().remove(player.getUniqueId());
        getShown().remove(player.getUniqueId()); // Don't need to use NPC#hide since the entity is not registered in the NMS server.
        hasTeamRegistered.remove(player.getUniqueId());
    }

    public boolean inRangeOf(Player player) {
        if (player == null) return false;
        if (!player.getWorld().equals(location.getWorld())) {
            // No need to continue our checks, they are in different worlds.
            return false;
        }

        // If Bukkit doesn't track the NPC entity anymore, bypass the hiding distance variable.
        // This will cause issues otherwise (e.g. custom skin disappearing).
        double hideDistance = instance.getAutoHideDistance();
        double distanceSquared = player.getLocation().distanceSquared(location);
        double bukkitRange = Bukkit.getViewDistance() << 4;

        return distanceSquared <= MathUtil.square(hideDistance) && distanceSquared <= MathUtil.square(bukkitRange);
    }

    public boolean inViewOf(Player player) {
        Vector dir = location.toVector().subtract(player.getEyeLocation().toVector()).normalize();
        return dir.dot(player.getEyeLocation().getDirection()) >= cosFOV;
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

        if (isShown(player)) {
            throw new IllegalArgumentException("NPC is already shown to player");
        }

        if (auto) {
            sendShowPackets(player);
            sendMetadataPacket(player);
            sendEquipmentPackets(player);

            // NPC is auto-shown now, we can remove the UUID from the set.
            autoHidden.remove(player.getUniqueId());
        } else {
            // Adding the UUID to the set.
            shown.add(player.getUniqueId());

            if (inRangeOf(player) && inViewOf(player)) {
                // The player can see the NPC and is in range, send the packets.
                sendShowPackets(player);
                sendMetadataPacket(player);
                sendEquipmentPackets(player);
            } else {
                // We'll wait until we can show the NPC to the player via auto-show.
                autoHidden.add(player.getUniqueId());
            }
        }
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

        if (!shown.contains(player.getUniqueId())) {
            throw new IllegalArgumentException("NPC cannot be hidden from player before calling NPC#show first");
        }

        if (auto) {
            if (autoHidden.contains(player.getUniqueId())) {
                throw new IllegalStateException("NPC cannot be auto-hidden twice");
            }

            sendHidePackets(player);

            // NPC is auto-hidden now, we will add the UUID to the set.
            autoHidden.add(player.getUniqueId());
        } else {
            // Removing the UUID from the set.
            shown.remove(player.getUniqueId());

            if (inRangeOf(player)) {
                // The player is in range of the NPC, send the packets.
                sendHidePackets(player);
            } else {
                // We don't have to send any packets, just don't let it auto-show again by removing the UUID from the set.
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
    public void playAnimation(NPCAnimation animation) {
        for (UUID shownUuid : shown) {
            Player player = Bukkit.getPlayer(shownUuid);
            if (player != null && isShown(player)) {
                sendAnimationPacket(player, animation);
            }
        }
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
    public void lookAt(Location location) {
    	sendHeadRotationPackets(location);
    }
}
