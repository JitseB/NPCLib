package net.jitse.npclib.api.state;

public enum NPCSlot {

    HELMET(4),
    CHESTPLATE(3),
    LEGGINGS(2),
    BOOTS(1),
    MAINHAND(0),
    OFFHAND(5);

    int slot;

    NPCSlot(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }
}
