package net.jitse.npclib.api.state;

public enum NPCAnimation {

    SWING_MAINHAND(0),
    TAKE_DAMAGE(1),
    SWING_OFFHAND(3),
    CRITICAL_DAMAGE(4),
    MAGICAL_DAMAGE(5);

    private int id;

    NPCAnimation(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
