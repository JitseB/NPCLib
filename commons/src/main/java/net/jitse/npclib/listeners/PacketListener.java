/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.listeners;

import net.jitse.npclib.NPCManager;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.events.NPCInteractEvent;
import net.jitse.npclib.events.click.ClickType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tinyprotocol.LegacyTinyProtocol;
import tinyprotocol.Reflection;
import tinyprotocol.TinyProtocol;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Jitse Boonstra
 */
public class PacketListener {

    // Classes:
    private final Class<?> packetPlayInUseEntityClazz = Reflection.getMinecraftClass("PacketPlayInUseEntity");

    // Fields:
    private final Reflection.FieldAccessor entityIdField = Reflection.getField(packetPlayInUseEntityClazz, "a", int.class);
    private final Reflection.FieldAccessor actionField = Reflection.getField(packetPlayInUseEntityClazz, "action", Object.class);

    // Prevent players from clicking at very high speeds.
    private final Set<UUID> delay = new HashSet<>();

    public void start(JavaPlugin plugin) {
        String versionName = plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
        if (versionName.equals("v1_7_R4")) {
            new LegacyTinyProtocol(plugin) {

                @Override
                public Object onPacketInAsync(Player player, net.minecraft.util.io.netty.channel.Channel channel, Object packet) {

                    if (packetPlayInUseEntityClazz.isInstance(packet)) {
                        NPC npc = NPCManager.getAllNPCs().stream().filter(
                                check -> check.isActuallyShown(player) && check.getEntityId() == (int) entityIdField.get(packet))
                                .findFirst().orElse(null);

                        if (npc == null) {
                            // Default player, not doing magic with the packet.
                            return super.onPacketInAsync(player, channel, packet);
                        }

                        if (delay.contains(player.getUniqueId())) {
                            return null;
                        }

                        ClickType clickType = actionField.get(packet).toString()
                                .equals("ATTACK") ? ClickType.LEFT_CLICK : ClickType.RIGHT_CLICK;

                        Bukkit.getPluginManager().callEvent(new NPCInteractEvent(player, clickType, npc));

                        UUID uuid = player.getUniqueId();
                        delay.add(uuid);
                        Bukkit.getScheduler().runTask(plugin, () -> delay.remove(uuid));
                        return null;
                    }

                    return super.onPacketInAsync(player, channel, packet);
                }
            };
        } else {
            new TinyProtocol(plugin) {

                @Override
                public Object onPacketInAsync(Player player, io.netty.channel.Channel channel, Object packet) {

                    if (packetPlayInUseEntityClazz.isInstance(packet)) {
                        NPC npc = NPCManager.getAllNPCs().stream().filter(
                                check -> check.isActuallyShown(player) && check.getEntityId() == (int) entityIdField.get(packet))
                                .findFirst().orElse(null);

                        if (npc == null) {
                            // Default player, not doing magic with the packet.
                            return super.onPacketInAsync(player, channel, packet);
                        }

                        if (delay.contains(player.getUniqueId())) {
                            return null;
                        }

                        ClickType clickType = actionField.get(packet).toString()
                                .equals("ATTACK") ? ClickType.LEFT_CLICK : ClickType.RIGHT_CLICK;

                        Bukkit.getPluginManager().callEvent(new NPCInteractEvent(player, clickType, npc));

                        UUID uuid = player.getUniqueId();
                        delay.add(uuid);
                        Bukkit.getScheduler().runTask(plugin, () -> delay.remove(uuid));
                        return null;
                    }

                    return super.onPacketInAsync(player, channel, packet);
                }
            };
        }
    }
}
