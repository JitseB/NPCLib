/*
 * Copyright (c) 2018 Jitse Boonstra
 */

package net.jitse.npclib.api.skin;

public class Skin {

    private final String value, signature;

    public Skin(String value, String signature) {
        this.value = value;
        this.signature = signature;
    }

    public String getValue() {
        return this.value;
    }

    public String getSignature() {
        return this.signature;
    }
}
