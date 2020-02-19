package net.jitse.npclib.api.state;

import java.util.Collection;

public enum NPCState {

    ON_FIRE((byte) 1),
    CROUCHED((byte) 2),
    INVISIBLE((byte) 32);

    private final byte b;

    NPCState(byte b) {
        this.b = b;
    }

    public byte getByte() {
        return b;
    }

    public static byte getMasked(NPCState... states) {
        byte mask = 0;
        for (NPCState state : states) mask |= state.getByte();
        return mask;
    }

    public static byte getMasked(Collection<NPCState> states) {
        byte mask = 0;
        for (NPCState state : states) mask |= state.getByte();
        return mask;
    }
}
