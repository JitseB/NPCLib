/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.hologram;

import com.comphenix.tinyprotocol.Reflection;
import com.bnstra.npclib.internal.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Hologram {

    private static final double DELTA = 0.3;

    // Classes:
    private static final Class<?> CHAT_BASE_COMPONENT_CLASS = Reflection.getClass("{nms}.IChatBaseComponent", "net.minecraft.network.chat.IChatBaseComponent");
    private static final Class<?> ENTITY_ARMOR_STAND_CLASS = Reflection.getClass("{nms}.EntityArmorStand", "net.minecraft.world.entity.decoration.EntityArmorStand");
    private static final Class<?> ENTITY_LIVING_CLASS = Reflection.getClass("{nms}.EntityLiving", "net.minecraft.world.entity.EntityLiving");
    private static final Class<?> ENTITY_CLASS = Reflection.getClass("{nms}.Entity", "net.minecraft.world.entity.Entity");
    private static final Class<?> CRAFT_WORLD_CLASS = Reflection.getCraftBukkitClass("CraftWorld");
    private static final Class<?> CRAFT_PLAYER_CLASS = Reflection.getCraftBukkitClass("entity.CraftPlayer");

    private static final Class<?> BUKKIT_ENTITY_ARMOR_STAND_CLASS = Reflection.getClass("org.bukkit.entity.ArmorStand");

    private static final Class<?> PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS = Reflection.getClass(
            "{nms}.PacketPlayOutSpawnEntityLiving", "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving", "net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_DESTROY_CLASS = Reflection.getClass(
            "{nms}.PacketPlayOutEntityDestroy", "net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy");
    private static final Class<?> PACKET_PLAY_OUT_ENTITY_METADATA_CLASS = Reflection.getClass(
            "{nms}.PacketPlayOutEntityMetadata", "net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata");
    private static final Class<?> DATAWATCHER_CLASS = Reflection.getClass("{nms}.DataWatcher", "net.minecraft.network.syncher.DataWatcher");
    private static final Class<?> ENTITY_PLAYER_CLASS = Reflection.getClass("{nms}.EntityPlayer", "net.minecraft.server.level.EntityPlayer");
    private static final Class<?> PLAYER_CONNECTION_CLASS = Reflection.getClass("{nms}.PlayerConnection", "net.minecraft.server.network.PlayerConnection");
    private static final Class<?> PACKET_CLASS = Reflection.getClass("{nms}.Packet", "net.minecraft.network.protocol.Packet");

    // Constructors:
    private static final Reflection.ConstructorInvoker PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR = Reflection
            .getConstructor(PACKET_PLAY_OUT_ENTITY_DESTROY_CLASS, int[].class);

    // Fields:
    private static final Reflection.FieldAccessor<?> PLAYER_CONNECTION_FIELD = Reflection.getField(ENTITY_PLAYER_CLASS, PLAYER_CONNECTION_CLASS, 0);

    // Methods:
    private static final Reflection.MethodInvoker GET_BUKKIT_ENTITY = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
            "getBukkitEntity");

    private static final Reflection.MethodInvoker PLAYER_GET_HANDLE_METHOD = Reflection.getMethod(CRAFT_PLAYER_CLASS,
            "getHandle");
//    private static final Reflection.MethodInvoker GET_DATAWATCHER_METHOD = Reflection.getMethod(ENTITY_CLASS,
//            "getDataWatcher");
    private static final Reflection.MethodInvoker GET_DATAWATCHER_METHOD = Reflection.getTypedMethod(ENTITY_CLASS,
            null, DATAWATCHER_CLASS);

    private final List<Object> armorStands = new ArrayList<>();
    private final List<Object> showPackets = new ArrayList<>();
    private final List<Object> hidePackets = new ArrayList<>();
    private final List<Object> metaPackets = new ArrayList<>();

    // Additional constructors and methods that require version checking
    private final Reflection.ConstructorInvoker packetPlayOutSpawnEntityLivingConstructor;
    private final Reflection.ConstructorInvoker packetPlayOutEntityMetadataConstructor;

    private final Reflection.MethodInvoker setSmallMethod;
    private final Reflection.MethodInvoker setLocationMethod;
    private final Reflection.MethodInvoker setInvisibleMethod;
    private final Reflection.MethodInvoker setBasePlateMethod;
    private final Reflection.MethodInvoker setArmsMethod;
    private final Reflection.MethodInvoker sendPacketMethod;
    private final Reflection.MethodInvoker getIdMethod;
    private final Reflection.MethodInvoker setMarkerMethod;

    private Reflection.MethodInvoker getNonDefaultValuesMethod, getUUIDMethod;
    private Object emptyVec3d, armorStandEntityType;

    // Stupid ChatComponent methods changed at 1.19 R1, cannot set until checked
    // For now setting equal to null, *should* not result in errors being thrown
    private Reflection.ConstructorInvoker chatComponentTextConstructor = null;
    private Reflection.MethodInvoker chatComponentFromString = null;

    private final MinecraftVersion version;
    private final Location start;
    private final Object worldServer;

    private List<String> text;

    public Hologram(MinecraftVersion version, Location location, List<String> text) {
        this.version = version;
        this.start = location;
        this.text = text;

        this.worldServer = Reflection.getMethod(CRAFT_WORLD_CLASS, "getHandle").invoke(location.getWorld());

        if (version.isAboveOrEqual(MinecraftVersion.V1_21_R4)) {
            setMarkerMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "u", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1)) {
            setMarkerMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                            "v", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
            setMarkerMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "t", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_8_R3)) {
            setMarkerMethod = Reflection.getMethod(BUKKIT_ENTITY_ARMOR_STAND_CLASS,
                            "setMarker", boolean.class);
        } else {
            throw new IllegalStateException("Could not find setMarker method for EntityArmorStand on version "
                    + version);
        }

        if (version.isAboveOrEqual(MinecraftVersion.V1_21_R4)) {
            Class<?> vec3dClass = Reflection.getClass("net.minecraft.world.phys.Vec3D");
            Reflection.ConstructorInvoker vec3dConstructor = Reflection.getConstructor(vec3dClass,
                    double.class, double.class, double.class);
            this.emptyVec3d = vec3dConstructor.invoke(0,0,0);
            Class<?> entityTypesClass = Reflection.getClass("net.minecraft.world.entity.EntityTypes");
            try {
                this.armorStandEntityType = entityTypesClass.getField("g").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Bukkit.broadcastMessage("Could not find ArmorStand entity type for V1_21_R4, " +
                        "expected to be net.minecraft.world.entity.EntityTypes$g");
                throw new RuntimeException(e);
            }
            this.getUUIDMethod = Reflection.getTypedMethod(ENTITY_CLASS, "cG", UUID.class);

            packetPlayOutSpawnEntityLivingConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, int.class,
                            UUID.class, double.class, double.class, double.class, float.class,
                            float.class, entityTypesClass, int.class, vec3dClass, double.class);
            packetPlayOutEntityMetadataConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, List.class);
            // The metadata packet constructor now seeks a list of datawatcher objects
            // these can be retrieved from the datawatcher object using the following method
            getNonDefaultValuesMethod = Reflection.getMethod(DATAWATCHER_CLASS, "c");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R3)) {
            Class<?> vec3dClass = Reflection.getClass("net.minecraft.world.phys.Vec3D");
            Reflection.ConstructorInvoker vec3dConstructor = Reflection.getConstructor(vec3dClass,
                    double.class, double.class, double.class);
            this.emptyVec3d = vec3dConstructor.invoke(0,0,0);
            Class<?> entityTypesClass = Reflection.getClass("net.minecraft.world.entity.EntityTypes");
            try {
                this.armorStandEntityType = entityTypesClass.getField("f").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Bukkit.broadcastMessage("Could not find ArmorStand entity type for V1_21_R3, " +
                        "expected to be net.minecraft.world.entity.EntityTypes$g");
                throw new RuntimeException(e);
            }
            this.getUUIDMethod = Reflection.getTypedMethod(ENTITY_CLASS, "cG", UUID.class);

            packetPlayOutSpawnEntityLivingConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, int.class,
                            UUID.class, double.class, double.class, double.class, float.class,
                            float.class, entityTypesClass, int.class, vec3dClass, double.class);
            packetPlayOutEntityMetadataConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, List.class);
            // The metadata packet constructor now seeks a list of datawatcher objects
            // these can be retrieved from the datawatcher object using the following method
            getNonDefaultValuesMethod = Reflection.getMethod(DATAWATCHER_CLASS, "c");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R2)) {
            Class<?> vec3dClass = Reflection.getClass("net.minecraft.world.phys.Vec3D");
            Reflection.ConstructorInvoker vec3dConstructor = Reflection.getConstructor(vec3dClass,
                    double.class, double.class, double.class);
            this.emptyVec3d = vec3dConstructor.invoke(0,0,0);
            Class<?> entityTypesClass = Reflection.getClass("net.minecraft.world.entity.EntityTypes");
            try {
                this.armorStandEntityType = entityTypesClass.getField("f").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Bukkit.broadcastMessage("Could not find ArmorStand entity type for V1_21_R2, " +
                        "expected to be net.minecraft.world.entity.EntityTypes$f");
                throw new RuntimeException(e);
            }
            this.getUUIDMethod = Reflection.getTypedMethod(ENTITY_CLASS, "cG", UUID.class);

            packetPlayOutSpawnEntityLivingConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, int.class,
                            UUID.class, double.class, double.class, double.class, float.class,
                            float.class, entityTypesClass, int.class, vec3dClass, double.class);
            packetPlayOutEntityMetadataConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, List.class);
            // The metadata packet constructor now seeks a list of datawatcher objects
            // these can be retrieved from the datawatcher object using the following method
            getNonDefaultValuesMethod = Reflection.getMethod(DATAWATCHER_CLASS, "c");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1)) {
            Class<?> vec3dClass = Reflection.getClass("net.minecraft.world.phys.Vec3D");
            Reflection.ConstructorInvoker vec3dConstructor = Reflection.getConstructor(vec3dClass,
                    double.class, double.class, double.class);
            this.emptyVec3d = vec3dConstructor.invoke(0,0,0);
            Class<?> entityTypesClass = Reflection.getClass("net.minecraft.world.entity.EntityTypes");
            try {
                this.armorStandEntityType = entityTypesClass.getField("d").get(null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Bukkit.broadcastMessage("Could not find ArmorStand entity type for V1_21_R1, " +
                        "expected to be net.minecraft.world.entity.EntityTypes$c");
                throw new RuntimeException(e);
            }
            this.getUUIDMethod = Reflection.getTypedMethod(ENTITY_CLASS, "cz", UUID.class);

            packetPlayOutSpawnEntityLivingConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, int.class,
                            UUID.class, double.class, double.class, double.class, float.class,
                            float.class, entityTypesClass, int.class, vec3dClass, double.class);
            packetPlayOutEntityMetadataConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, List.class);
            // The metadata packet constructor now seeks a list of datawatcher objects
            // these can be retrieved from the datawatcher object using the following method
            getNonDefaultValuesMethod = Reflection.getMethod(DATAWATCHER_CLASS, "c");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_19_R2)) {
            packetPlayOutSpawnEntityLivingConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, ENTITY_CLASS);
            packetPlayOutEntityMetadataConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, List.class);
            // The metadata packet constructor now seeks a list of datawatcher objects
            // these can be retrieved from the datawatcher object using the following method
            getNonDefaultValuesMethod = Reflection.getMethod(DATAWATCHER_CLASS, "c");
        } else {
            packetPlayOutSpawnEntityLivingConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_SPAWN_ENTITY_LIVING_CLASS, ENTITY_LIVING_CLASS);
            packetPlayOutEntityMetadataConstructor = Reflection
                    .getConstructor(PACKET_PLAY_OUT_ENTITY_METADATA_CLASS, int.class, DATAWATCHER_CLASS, boolean.class);
        }

        if (version.isAboveOrEqual(MinecraftVersion.V1_19_R1)) {
            chatComponentFromString = Reflection.getMethod(
                    Reflection.getClass("{obc}.util.CraftChatMessage"), "fromString", String.class);
        } else {
            chatComponentTextConstructor = Reflection
                    .getConstructor(Reflection.getClass("{nms}.ChatComponentText",
                            "net.minecraft.network.chat.ChatComponentText"), String.class);
        }

        // TODO: Should probably clean up this extensive if statement
        if (version.isAboveOrEqual(MinecraftVersion.V1_21_R4)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "a_", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "t", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "k", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "b", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "b", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "ao");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R2)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "a_", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "u", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "k", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "b", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "b", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "ar");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "a_", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "u", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "k", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "b", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "b", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "an");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "a_", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "t", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "k", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "b", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "b", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "al");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_20_R3)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "a_", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "t", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "j", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "s", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            // Since 1.20 R2, a packet listener can be passed
            // We set it to null since it is not useful for us here
            Class<?> clazz = Reflection.getClass("net.minecraft.network.PacketSendListener");
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "a", PACKET_CLASS, clazz);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "aj");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_20_R2)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "e", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "t", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "j", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "s", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            // Since 1.20 R2, a packet listener can be passed
            // We set it to null since it is not useful for us here
            Class<?> clazz = Reflection.getClass("net.minecraft.network.PacketSendListener");
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "a", PACKET_CLASS, clazz);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "ah");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_19_R3)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "e", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "t", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "j", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "s", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "a", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "af");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_19_R2)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "f", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "j", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "s", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "r", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "a", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_CLASS,
                    "ah");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "e", double.class, double.class, double.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "a", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                            "j", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "e", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "r", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "a", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "ae");
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_16_R3)) { // TODO: Might need to be lower
            this.setLocationMethod = Reflection.getMethod(ENTITY_CLASS,
                    "setPosition", double.class, double.class, double.class);

            // These mappings are not correct? I am confused
            // From: https://mappings.dev/1.17.1/net/minecraft/world/entity/Entity.html
//            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
//                    "a", boolean.class);
//            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
//                    "j", boolean.class);
//            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
//                    "s", boolean.class);
//            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
//                    "r", boolean.class);
//            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
//                    "a", PACKET_CLASS);
//            this.getIdMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
//                    "Z");

            // These work
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setSmall", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setInvisible", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setBasePlate", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setArms", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "sendPacket", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "getId");
        } else {
            this.setLocationMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setLocation", double.class, double.class, double.class, float.class, float.class);
            this.setSmallMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setSmall", boolean.class);
            this.setInvisibleMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setInvisible", boolean.class);
            this.setBasePlateMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setBasePlate", boolean.class);
            this.setArmsMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "setArms", boolean.class);
            this.sendPacketMethod = Reflection.getMethod(PLAYER_CONNECTION_CLASS,
                    "sendPacket", PACKET_CLASS);
            this.getIdMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS,
                    "getId");
        }

        createPackets();
    }

    private void createPackets() {
        Reflection.MethodInvoker gravityMethod;
        if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4)) {
            gravityMethod = Reflection.getMethod(ENTITY_CLASS, "f", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
            gravityMethod = Reflection.getMethod(ENTITY_CLASS, "e", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_10_R1)) {
            gravityMethod = Reflection.getMethod(ENTITY_CLASS, "setNoGravity", boolean.class);
        } else {
            gravityMethod = Reflection.getMethod(ENTITY_ARMOR_STAND_CLASS, "setGravity", boolean.class);
        }

        Reflection.MethodInvoker customNameMethod;
        if (version.isAboveOrEqual(MinecraftVersion.V1_19_R1)) {
            customNameMethod = Reflection.getMethod(ENTITY_CLASS, "b", CHAT_BASE_COMPONENT_CLASS);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
            customNameMethod = Reflection.getMethod(ENTITY_CLASS, "a", CHAT_BASE_COMPONENT_CLASS);
        } else {
            customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                    version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);
        }

        Reflection.MethodInvoker customNameVisibilityMethod;
        if (version.isAboveOrEqual(MinecraftVersion.V1_21_R4)) {
            customNameVisibilityMethod = Reflection.getMethod(ENTITY_CLASS, "o", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1)) {
            customNameVisibilityMethod = Reflection.getMethod(ENTITY_CLASS, "p", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4)) {
            customNameVisibilityMethod = Reflection.getMethod(ENTITY_CLASS, "o", boolean.class);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
            customNameVisibilityMethod = Reflection.getMethod(ENTITY_CLASS, "n", boolean.class);
        } else {
            customNameVisibilityMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomNameVisible", boolean.class);
        }

        Location location = start.clone().add(0, (DELTA * text.size()) + (setMarkerMethod != null ? 1f : 0f), 0); // markers drop the armor stand's nametag by around 1 block
        Class<?> worldClass = worldServer.getClass().getSuperclass();

        if (start.getWorld().getEnvironment() != World.Environment.NORMAL) {
            worldClass = worldClass.getSuperclass();
        }

//        Reflection.ConstructorInvoker entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
//                Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class, double.class) :
//                Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        // Replacement for issue #59
        Reflection.ConstructorInvoker entityArmorStandConstructor;
        try {
            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class, double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        } catch (IllegalStateException exception) {
            worldClass = worldClass.getSuperclass();

            entityArmorStandConstructor = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass, double.class, double.class, double.class) :
                    Reflection.getConstructor(ENTITY_ARMOR_STAND_CLASS, worldClass));
        }
        // end #59

        for (String line : text) {
            Object entityArmorStand = (version.isAboveOrEqual(MinecraftVersion.V1_14_R1) ?
                    entityArmorStandConstructor.invoke(worldServer, location.getX(), location.getY(), location.getZ()) :
                    entityArmorStandConstructor.invoke(worldServer));

            if (!version.isAboveOrEqual(MinecraftVersion.V1_14_R1)) {
                setLocationMethod.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ(), 0, 0);
            }
            if (!version.isAboveOrEqual(MinecraftVersion.V1_17_R1)) {
                setLocationMethod.invoke(entityArmorStand, location.getX(), location.getY(), location.getZ());
            }

            if (version.isAboveOrEqual(MinecraftVersion.V1_19_R1)) {
                customNameMethod.invoke(entityArmorStand, ((Object[]) chatComponentFromString.invoke(null, line))[0]);
            } else {
                customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                        chatComponentTextConstructor.invoke(line) : line);
            }

            customNameVisibilityMethod.invoke(entityArmorStand, true);
            gravityMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_9_R2));
            setSmallMethod.invoke(entityArmorStand, true);
            setInvisibleMethod.invoke(entityArmorStand, true);
            if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
                // Method is actually set NO baseplate, so argument should be true
                setBasePlateMethod.invoke(entityArmorStand, true);
            } else {
                setBasePlateMethod.invoke(entityArmorStand, false);
            }
            setArmsMethod.invoke(entityArmorStand, false);

            if (setMarkerMethod != null) { // setMarker isn't a method in 1.8_R2, so still check if it exists in the first place.
                if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1)) {
                    setMarkerMethod.invoke(entityArmorStand, true);
                } else {
                    Object bukkitEntity = GET_BUKKIT_ENTITY.invoke(entityArmorStand); // if it does, grab the Bukkit Entity
                    ArmorStand as = (ArmorStand) bukkitEntity; // reflection wasn't working here for some reason- just using a regular ArmorStand object since it's not version-dependent.
                    as.setMarker(true); // set the marker state
                }
            }

            armorStands.add(entityArmorStand);

            // Create and add the associated show and hide packets.
            if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1))
                showPackets.add(packetPlayOutSpawnEntityLivingConstructor.invoke(
                        (int) getIdMethod.invoke(entityArmorStand), (UUID) getUUIDMethod.invoke(entityArmorStand),
                        location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw(),
                        armorStandEntityType, 0, emptyVec3d, 0));
            else showPackets.add(packetPlayOutSpawnEntityLivingConstructor.invoke(entityArmorStand));

            hidePackets.add(PACKET_PLAY_OUT_ENTITY_DESTROY_CONSTRUCTOR
                    .invoke(new int[]{(int) getIdMethod.invoke(entityArmorStand)}));

            if (version.isAboveOrEqual(MinecraftVersion.V1_19_R2)) {
                Object dataWatcher = GET_DATAWATCHER_METHOD.invoke(entityArmorStand);
                metaPackets.add(packetPlayOutEntityMetadataConstructor.invoke(
                        getIdMethod.invoke(entityArmorStand),
                        getNonDefaultValuesMethod.invoke(dataWatcher)));
            } else {
                // For 1.15 R1 and up.
                metaPackets.add(packetPlayOutEntityMetadataConstructor.invoke(
                        getIdMethod.invoke(entityArmorStand),
                        GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                        true));
            }

            location.subtract(0, DELTA, 0);
        }
    }

    public List<Object> getUpdatePackets(List<String> text) {
        List<Object> updatePackets = new ArrayList<>();

        if (this.text.size() != text.size()) {
            throw new IllegalArgumentException("When updating the text, the old and new text should have the same amount of lines");
        }

        Reflection.MethodInvoker customNameMethod;
        if (version.isAboveOrEqual(MinecraftVersion.V1_19_R1)) {
            customNameMethod = Reflection.getMethod(ENTITY_CLASS, "b", CHAT_BASE_COMPONENT_CLASS);
        } else if (version.isAboveOrEqual(MinecraftVersion.V1_18_R1)) {
            customNameMethod = Reflection.getMethod(ENTITY_CLASS, "a", CHAT_BASE_COMPONENT_CLASS);
        } else {
            customNameMethod = Reflection.getMethod(ENTITY_CLASS, "setCustomName",
                    version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ? CHAT_BASE_COMPONENT_CLASS : String.class);
        }

        Location location = start.clone().add(0, (DELTA * text.size()) + (setMarkerMethod != null ? 1f : 0f), 0); // markers drop the armor stand's nametag by around 1 block
        for (int i = 0; i < text.size(); i++) {
            Object entityArmorStand = armorStands.get(i);
            String oldLine = this.text.get(i);
            String newLine = text.get(i);


            if (version.isAboveOrEqual(MinecraftVersion.V1_19_R1)) {
                customNameMethod.invoke(entityArmorStand, ((Object[]) chatComponentFromString.invoke(null, newLine))[0]);
            } else {
                customNameMethod.invoke(entityArmorStand, version.isAboveOrEqual(MinecraftVersion.V1_13_R1) ?
                        chatComponentTextConstructor.invoke(newLine) : newLine);
            }

            if (version.isAboveOrEqual(MinecraftVersion.V1_21_R1))
                showPackets.set(i, packetPlayOutSpawnEntityLivingConstructor.invoke(
                        (int) getIdMethod.invoke(entityArmorStand), (UUID) getUUIDMethod.invoke(entityArmorStand),
                        location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw(),
                        armorStandEntityType, 0, emptyVec3d, 0));
            else showPackets.set(i, packetPlayOutSpawnEntityLivingConstructor.invoke(entityArmorStand));

            if (newLine.isEmpty() && !oldLine.isEmpty()) {
                updatePackets.add(hidePackets.get(i));
            } else if (!newLine.isEmpty() && oldLine.isEmpty()) {
                updatePackets.add(showPackets.get(i));
            } else if (!oldLine.equals(newLine)) {
                // Update the line for all players using a Metadata packet.
                if (version.isAboveOrEqual(MinecraftVersion.V1_19_R2)) {
                    Object dataWatcher = GET_DATAWATCHER_METHOD.invoke(entityArmorStand);
                    updatePackets.add(packetPlayOutEntityMetadataConstructor.invoke(
                            getIdMethod.invoke(entityArmorStand),
                            getNonDefaultValuesMethod.invoke(dataWatcher)));
                } else {
                    updatePackets.add(packetPlayOutEntityMetadataConstructor.invoke(
                            getIdMethod.invoke(entityArmorStand),
                            GET_DATAWATCHER_METHOD.invoke(entityArmorStand),
                            true));
                }
            }
            location.subtract(0, DELTA, 0);
        }

        this.text = text;

        return updatePackets;
    }

    public void update(Player player, List<Object> updatePackets) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));

        for (Object packet : updatePackets) {
            if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4))
                sendPacketMethod.invoke(playerConnection, packet);
            else if( version.isAboveOrEqual(MinecraftVersion.V1_20_R2))
                sendPacketMethod.invoke(playerConnection, packet, null);
            else sendPacketMethod.invoke(playerConnection, packet);
        }
    }

    public void show(Player player) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));

        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to spawn the line.
            if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4))
                sendPacketMethod.invoke(playerConnection, showPackets.get(i));
            else if( version.isAboveOrEqual(MinecraftVersion.V1_20_R2))
                sendPacketMethod.invoke(playerConnection,  showPackets.get(i), null);
            else sendPacketMethod.invoke(playerConnection,  showPackets.get(i));

            if (version.isAboveOrEqual(MinecraftVersion.V1_15_R1)) {
                if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4))
                    sendPacketMethod.invoke(playerConnection, metaPackets.get(i));
                else if( version.isAboveOrEqual(MinecraftVersion.V1_20_R2))
                    sendPacketMethod.invoke(playerConnection,  metaPackets.get(i), null);
                else sendPacketMethod.invoke(playerConnection,  metaPackets.get(i));
            }
        }
    }

    public void hide(Player player) {
        Object playerConnection = PLAYER_CONNECTION_FIELD.get(PLAYER_GET_HANDLE_METHOD.invoke(player));

        for (int i = 0; i < text.size(); i++) {
            if (text.get(i).isEmpty()) continue; // No need to hide the line (as it was never spawned).
            if (version.isAboveOrEqual(MinecraftVersion.V1_20_R4))
                sendPacketMethod.invoke(playerConnection, hidePackets.get(i));
            else if( version.isAboveOrEqual(MinecraftVersion.V1_20_R2))
                sendPacketMethod.invoke(playerConnection,  hidePackets.get(i), null);
            else sendPacketMethod.invoke(playerConnection,  hidePackets.get(i));
        }
    }
}
