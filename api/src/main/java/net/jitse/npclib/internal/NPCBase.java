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
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.api.state.NPCState;
import net.jitse.npclib.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public abstract class NPCBase implements NPC, NPCPacketHandler {

    protected final UUID uuid = UUID.randomUUID();
    protected final int entityId = Integer.MAX_VALUE - NPCManager.getAllNPCs().size();
    protected final String name = uuid.toString().replace("-", "").substring(0, 10);
    protected final GameProfile gameProfile = new GameProfile(uuid, name);

    private final Set<UUID> shown = new HashSet<>();
    private final Set<UUID> autoHidden = new HashSet<>();

    protected double cosFOV = Math.cos(Math.toRadians(60));
    protected NPCState[] activeStates = new NPCState[]{};

    protected NPCLib instance;
    protected List<String> text;
    protected Location location;
    protected Skin skin;
    protected Hologram hologram;

    // offHand support in 1.9 R1 and later.
    protected ItemStack helmet, chestplate, leggings, boots, inHand, offHand;

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
                throw new RuntimeException("Cannot call show method twice.");
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
                throw new RuntimeException("Cannot call hide method without calling NPC#show.");
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
    public NPC toggleState(NPCState state) {
        int inActiveStatesIndex = -1;
        if (activeStates.length == 0) { // If there're no active states, this is the first to be toggled (on).
            activeStates = new NPCState[]{state};
        } else { // Otherwise, there have been states that were toggled, check if we need to toggle something off.
            for (int i = 0; i < activeStates.length; i++) {
                if (activeStates[i] == state) { // If the state is to be toggled off, save the index so we can remove it.
                    inActiveStatesIndex = i;
                    break;
                }
            }

            if (inActiveStatesIndex > -1) { // If there's a state to be toggled of, create a new array with all items but the one to be toggled off.
                NPCState[] newArr = new NPCState[activeStates.length - 1];
                for (int i = 0; i < newArr.length; i++) {
                    if (inActiveStatesIndex == i) {
                        continue;
                    } else if (i < inActiveStatesIndex) {
                        newArr[i] = activeStates[i];
                    } else {
                        newArr[i] = activeStates[i + 1];
                    }
                }
                activeStates = newArr;
            } else { // Else, we need to add a state by appending our state to the array.
                NPCState[] newArr = new NPCState[activeStates.length + 1];
                System.arraycopy(activeStates, 0, newArr, 0, activeStates.length);
                newArr[activeStates.length] = state;
                activeStates = newArr;
            }
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
        if (slot == null) {
            throw new NullPointerException("Slot cannot be null");
        }
        switch (slot) {
            case HELMET:
                return this.helmet;
            case CHESTPLATE:
                return this.chestplate;
            case LEGGINGS:
                return this.leggings;
            case BOOTS:
                return this.boots;
            case MAINHAND:
                return this.inHand;
            case OFFHAND:
                return this.offHand;
            default:
                throw new IllegalArgumentException("Entered an invalid inventory slot");
        }
    }
    
    @Override
    public NPC setItem(NPCSlot slot, ItemStack item) {
        if (slot == null) {
            throw new NullPointerException("Slot cannot be null");
        }

        switch (slot) {
            case HELMET:
                this.helmet = item;
                break;
            case CHESTPLATE:
                this.chestplate = item;
                break;
            case LEGGINGS:
                this.leggings = item;
                break;
            case BOOTS:
                this.boots = item;
                break;
            case MAINHAND:
                this.inHand = item;
                break;
            case OFFHAND:
                this.offHand = item;
                break;
            default:
                throw new IllegalArgumentException("Entered an invalid inventory slot");
        }

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
