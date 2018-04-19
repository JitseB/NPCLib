/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.version;

import net.jitse.npclib.api.NPC;
import net.jitse.npclib.nms.v1_10_r1.NPC_V1_10_R1;
import net.jitse.npclib.nms.v1_11_r1.NPC_V1_11_R1;
import net.jitse.npclib.nms.v1_12_r1.NPC_V1_12_R1;
import net.jitse.npclib.nms.v1_8_r1.NPC_V1_8_R1;
import net.jitse.npclib.nms.v1_8_r2.NPC_V1_8_R2;
import net.jitse.npclib.nms.v1_8_r3.NPC_V1_8_R3;
import net.jitse.npclib.nms.v1_9_r1.NPC_V1_9_R1;
import net.jitse.npclib.nms.v1_9_r2.NPC_V1_9_R2;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author Jitse Boonstra
 */
public enum Version {

    V1_8_R1("v1_8_R1", NPC_V1_8_R1.class),
    V1_8_R2("v1_8_R2", NPC_V1_8_R2.class),
    V1_8_R3("v1_8_R3", NPC_V1_8_R3.class),
    V1_9_R1("v1_9_R1", NPC_V1_9_R1.class),
    V1_9_R2("v1_9_R2", NPC_V1_9_R2.class),
    V1_10_R1("v1_10_R1", NPC_V1_10_R1.class),
    V1_11_R1("v1_11_R1", NPC_V1_11_R1.class),
    V1_12_R1("v1_12_R1", NPC_V1_12_R1.class);

    private String version;
    private Class<?> clazz;

    Version(String version, Class<?> clazz) {
        this.version = version;
        this.clazz = clazz;
    }

    public NPC createNPC(Object... params) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return (NPC) clazz.getConstructors()[0].newInstance(params);
    }

    public static Optional<Version> getByName(String version) {
        return Arrays.stream(values()).filter(value -> value.version.equals(version)).findFirst();
    }
}
