/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api;

import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCAnimation;
import net.jitse.npclib.api.state.NPCSlot;
import net.jitse.npclib.api.state.NPCState;
import net.jitse.npclib.hologram.Hologram;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface NPC {

    /**
     * @param player
     * @return unique hologram for that user
     */
    Hologram getPlayerHologram(Player player);

    /**
     *
     * @param targetPlayer The target player
     * @return object instance
     * @author Gatt
     */
    NPC removePlayerLines(Player targetPlayer);
    /**
     *
     * @param targetPlayer The target player
     * @param update whether or not to update the hologram
     * @return object instance
     * @author Gatt
     */
    NPC removePlayerLines(Player targetPlayer, boolean update);

    /**
     *
     * @param uniqueLines The text that the targetPlayer will see. Null to remove
     * @param targetPlayer The target player
     * @return object instance
     * @author Gatt
     */
    NPC setPlayerLines(List<String> uniqueLines, Player targetPlayer);

    /**
     * @param uniqueLines  The text that the targetPlayer will see
     * @param targetPlayer The target player
     * @param update       whether or not to send the update packets
     * @return object instance
     * @author Gatt
     */
    NPC setPlayerLines(List<String> uniqueLines, Player targetPlayer, boolean update);

    /**
     * @param targetPlayer The target player
     * @return the lines that the targetPlayer will see, if null; default lines.
     * @author Gatt
     */
    List<String> getPlayerLines(Player targetPlayer);

    /**
     * Set the NPC's location.
     * Use this method before using {@link NPC#create}.
     *
     * @param location The spawn location for the NPC.
     * @return object instance.
     */
    NPC setLocation(Location location);

    /**
     * Set the NPC's skin.
     * Use this method before using {@link NPC#create}.
     *
     * @param skin The skin(data) you'd like to apply.
     * @return object instance.
     */
    NPC setSkin(Skin skin);

    /**
     * Get the location of the NPC.
     *
     * @return The location of the NPC.
     */
    Location getLocation();

    /**
     * Get the world the NPC is located in.
     *
     * @return The world the NPC is located in.
     */
    World getWorld();

    /**
     * Create all necessary packets for the NPC so it can be shown to players.
     *
     * @return object instance.
     */
    NPC create();

    /**
     * Check whether the NPCs packets have already been generated.
     *
     * @return Whether NPC#create has been called yet.
     */
    boolean isCreated();

    /**
     * Get the ID of the NPC.
     *
     * @return the ID of the NPC.
     */
    String getId();

    /**
     * Test if a player can see the NPC.
     * E.g. is the player is out of range, this method will return false as the NPC is automatically hidden by the library.
     *
     * @param player The player you'd like to check.
     * @return Value on whether the player can see the NPC.
     */
    boolean isShown(Player player);

    /**
     * Show the NPC to a player.
     * Requires {@link NPC#create} to be used first.
     *
     * @param player the player to show the NPC to.
     */
    void show(Player player);

    /**
     * Hide the NPC from a player.
     * Will not do anything if NPC isn't shown to the player.
     * Requires {@link NPC#create} to be used first.
     *
     * @param player The player to hide the NPC from.
     */
    void hide(Player player);

    /**
     * Destroy the NPC, i.e. remove it from the registry.
     * Requires {@link NPC#create} to be used first.
     */
    void destroy();

    /**
     * Toggle a state of the NPC.
     *
     * @param state The state to be toggled.
     * @return Object instance.
     */
    NPC toggleState(NPCState state);

    /**
     * Plays an animation as the the NPC.
     *
     * @param animation The animation to play.
     */
    void playAnimation(NPCAnimation animation);

    /**
     * Get state of NPC.
     *
     * @param state The state requested.
     * @return boolean on/off status.
     */
    boolean getState(NPCState state);

    /**
     * Change the item in the inventory of the NPC.
     *
     * @param slot The slot to set the item of.
     * @param item The item to set.
     * @return Object instance.
     */
    NPC setItem(NPCSlot slot, ItemStack item);

    NPC setText(List<String> text);

    /**
     * Get the text of an NPC
     *
     * @return List<String> text
     */
    List<String> getText();

    /**
     * Get a NPC's item.
     *
     * @param slot The slot the item is in.
     * @return ItemStack item.
     */
    ItemStack getItem(NPCSlot slot);

    /**
     * Update the skin for every play that can see the NPC.
     *
     * @param skin The new skin for the NPC.
     */
    void updateSkin(Skin skin);

    /**
     * Get the UUID of the NPC.
     *
     * @return The UUID of the NPC.
     */
    UUID getUniqueId();

	void lookAt(Location location);
}
