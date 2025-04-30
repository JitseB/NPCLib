/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package com.bnstra.npclib.internal;

public enum MinecraftVersion {

    V1_8_R3,
    V1_9_R1,
    V1_9_R2,
    V1_10_R1,
    V1_11_R1,
    V1_12_R1,
    V1_13_R1,
    V1_13_R2,
    V1_14_R1,
    V1_15_R1,
    V1_16_R1,
    V1_16_R2,
    V1_16_R3,
    V1_17_R1,
    V1_18_R1,
    V1_18_R2,
    V1_19_R1,
    V1_19_R2,
    V1_19_R3,
    V1_20_R1,
    V1_20_R2,
    V1_20_R3,
    V1_20_R4,
    V1_21_R1,
    V1_21_R2,
    V1_21_R3,
    V1_21_R4;

    public boolean isAboveOrEqual(MinecraftVersion compare) {
        return ordinal() >= compare.ordinal();
    }
}
