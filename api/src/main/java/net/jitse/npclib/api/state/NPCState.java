package net.jitse.npclib.api.state;

public enum NPCState {

    ON_FIRE((byte) 1),
    CROUCHED((byte) 2),
    INVISIBLE((byte) 32);

    private byte b;

    NPCState(byte b) {
        this.b = b;
    }

    public byte getByte() {
        return b;
    }

    public static byte getMasked(final NPCState... status) {
        byte b = 0;
        for (NPCState s : status) b |= s.getByte();
        return b;
    }
}

