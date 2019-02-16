/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.nms.holograms;

import com.comphenix.tinyprotocol.Reflection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jitse Boonstra
 */
public class Hologram {

    private final double delta = 0.3;

    private List<Object> armorStands = new ArrayList<>();
    private Set<Object> spawnPackets = new HashSet<>();
    private Set<Object> destroyPackets = new HashSet<>();

    // Classes:
    private static final Class<?> CHAT_COMPONENT_TEXT_CLAZZ = Reflection.getMinecraftClass("ChatComponentText");
    private static final Class<?> CHAT_BASE_COMPONENT_CLAZZ = Reflection.getMinecraftClass("IChatBaseComponent");
    private static final Class<?> ENTITY_ARMOR_STAND_CLAZZ = Reflection.getMinecraftClass("EntityArmorStand");
    private static final Class<?> ENTITY_LIVING_CLAZZ = Reflection.getMinecraftClass("EntityLiving");
    private static final Class<?> ENTITY_CLAZZ = Reflection.getMinecraftClass("Entity");
    private static final Class<?> CRAFT_BUKKIT_CLASS = Reflection.getCraftBukkitClass("CraftWorld");
    private static final Class<?> CRAFT_PLAYER_CLAZZ = Reflection.getCraftBukkitClass("entity.CraftPlayer");
    private static final Class<?> PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLAZZ = Reflection.getMinecraftClass(
            "PacketPlayOutSpawnEntityLiving");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_DESTROY_CLAZZ = Reflection.getMinecraftClass(
            "PacketPlayOutEntityDestroy");
    private static final Class<?> ENTITY_PLAYER_CLAZZ = Reflection.getMinecraftClass("EntityPlayer");
    private static final Class<?> PLAYER_CONNECTION_CLAZZ = Reflection.getMinecraftClass("PlayerConnection");
    private static final Class<?> PACKET_CLAZZ = Reflection.getMinecraftClass("Packet");

    // Constructors:
    private static final Reflection.ConstructorInvoker CHAT_COMPONENT_TEXT_CONSTRUCTOR = Reflection
            .getConstructor(CHAT_COMPONENT_TEXT_CLAZZ, String.class);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLAZZ, ENTITY_LIVING_CLAZZ);
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_ENTITY_DESTROY_CLAZZ, int[].class);

    // Fields:
    private static final Reflection.FieldAccessor playerConnectionField = Reflection.getField(ENTITY_PLAYER_CLAZZ,
            "playerConnection", PLAYER_CONNECTION_CLAZZ);

    // Methods:
    private static final Reflection.MethodInvoker SET_LOCATION_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
            "setLocation", double.class, double.class, double.class, float.class, float.class);
    private static final Reflection.MethodInvoker SET_SMALL_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
            "setSmall", boolean.class);
    private static final Reflection.MethodInvoker SET_INVISIBLE_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
            "setInvisible", boolean.class);
    private static final Reflection.MethodInvoker SET_BASE_PLATE_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
            "setBasePlate", boolean.class);
    private static final Reflection.MethodInvoker SET_ARMS_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
            "setArms", boolean.class);
    private static final Reflection.MethodInvoker PLAYER_GET_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLAZZ,
            "getHandle");
    private static final Reflection.MethodInvoker SEND_PACKET_METHOD = Reflection.getMethod(PLAYER_CONNECTION_CLAZZ,
            "sendPacket", PACKET_CLAZZ);
    private static final Reflection.MethodInvoker GET_ID_METHOD = Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
            "getId");

    private final Location start;
    private final List<String> lines;
    private final Object worldServer;

    public Hologram(Location location, List<String> lines) {
        this.start = location;
        this.lines = lines;

        this.worldServer = Reflection.getMethod(CRAFT_BUKKIT_CLASS, "getHandle")
                .invoke(CRAFT_BUKKIT_CLASS.cast(location.getWorld()));

    }

    public void generatePackets(boolean above1_9_r2, boolean above_1_12_r1) {
        Reflection.MethodInvoker gravityMethod = (above1_9_r2 ? Reflection.getMethod(ENTITY_CLAZZ,
                "setNoGravity", boolean.class) : Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
                "setGravity", boolean.class));

        Reflection.MethodInvoker customNameMethod = (above_1_12_r1 ? Reflection.getMethod(ENTITY_CLAZZ,
                "setCustomName", CHAT_BASE_COMPONENT_CLAZZ) : Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
                "setCustomName", String.class));

        Reflection.MethodInvoker customNameVisibilityMethod = (above_1_12_r1 ? Reflection.getMethod(ENTITY_CLAZZ,
                "setCustomNameVisible", boolean.class) : Reflection.getMethod(ENTITY_ARMOR_STAND_CLAZZ,
                "setCustomNameVisible", boolean.class));

        Location location = start.clone().add(0, delta * lines.size(), 0);
        Class<?> worldClass = worldServer.getClass().getSuperclass();

        if (start.getWorld().getEnvironment() != World.Environment.NORMAL) {
            worldClass = worldClass.getSuperclass();
        }

        Reflection.ConstructorInvoker entityArmorStandConstructor = Reflection
                .getConstructor(ENTITY_ARMOR_STAND_CLAZZ, worldClass);

        for (String line : lines) {
            Object entityArmorStand = entityArmorStandConstructor.invoke(worldServer);

            SET_LOCATION_METHOD.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ(), 0, 0);
            customNameMethod.invoke(entityArmorStand, above_1_12_r1 ? CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(line) : line);
            customNameVisibilityMethod.invoke(entityArmorStand, true);
            gravityMethod.invoke(entityArmorStand, above1_9_r2);
            SET_SMALL_METHOD.invoke(entityArmorStand, true);
            SET_INVISIBLE_METHOD.invoke(entityArmorStand, true);
            SET_BASE_PLATE_METHOD.invoke(entityArmorStand, false);
            SET_ARMS_METHOD.invoke(entityArmorStand, false);

            location.subtract(0, delta, 0);

            if (line.isEmpty()) {
                continue;
            }

            armorStands.add(entityArmorStand);

            Object spawnPacket = PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CONSTRUCTOR.invoke(entityArmorStand);
            spawnPackets.add(spawnPacket);

            Object destroyPacket = PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR
                    .invoke(new int[]{(int) GET_ID_METHOD.invoke(entityArmorStand)});
            destroyPackets.add(destroyPacket);
        }
    }

//    public void updateText(List<String> newLines) {
//        if (lines.size() != newLines.size()) {
//            throw new IllegalArgumentException("New NPC text cannot differ in size from old text.");
//            return;
//        }
//
//        int i = 0;
//        for (String oldLine : lines) {
//            if (oldLine.isEmpty() && !newLines.get(i).isEmpty()) {
//                // Need to spawn
//            }
//            i++;
//        }
//        customNameMethod.invoke(entityArmorStand, above_1_12_r1 ? CHAT_COMPONENT_TEXT_CONSTRUCTOR.invoke(line) : line);
//    }

    public void spawn(Player player) {
        Object playerConnection = playerConnectionField.get(PLAYER_GET_HANDLE_METHOD
                .invoke(CRAFT_PLAYER_CLAZZ.cast(player)));

        for (Object packet : spawnPackets) {
            SEND_PACKET_METHOD.invoke(playerConnection, packet);
        }
    }

    public void destroy(Player player) {
        Object playerConnection = playerConnectionField.get(PLAYER_GET_HANDLE_METHOD
                .invoke(CRAFT_PLAYER_CLAZZ.cast(player)));

        for (Object packet : destroyPackets) {
            SEND_PACKET_METHOD.invoke(playerConnection, packet);
        }
    }
}
