package wow.memory;

/**
 * @author alexlovkov
 */
public enum CtmAction {

    FACE_TARGET(0x1),
    STOP(0x3),
    WALK(0x4),
    MOVE_AND_INTERACT_NPC(0x5),
    LOOT(0x6),
    MOVE_AND_INTERACT_OBJECT(0x7),
    FACE_OTHER(0x8), // ?
    SKIN(0x9),
    ATTACK(0xA),
    NOTHING(0x13);

    private int value;

    CtmAction(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
