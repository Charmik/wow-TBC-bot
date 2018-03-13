package wow.memory.objects;

import wow.memory.Address;
import wow.memory.ObjectManager;
import wow.memory.WowMemory;


/**
 * Is either an NPC or a mob. Offsets are different for players.
 *
 * @author Cargeh
 */
public final class UnitObject extends CreatureObject {
    private static final Address HEALTH_DESCRIPTOR = Address.DESCRIPTOR.OBJ_UNIT_HP;
    private static final Address COMBO_POINTS = Address.STATIC.TARGET_COMBO_POINTS;

    public UnitObject(
        long baseAddress,
        long guid,
        ObjectManager.ObjectType type,
        WowMemory wowMemory)
    {
        super(baseAddress, guid, type, wowMemory);
    }

    public int getHealth() {
        return readIntDescriptor(HEALTH_DESCRIPTOR);
    }

    public boolean isDead() {
        int health = getHealth();
        return health == 0 || health == 1;
    }

    public int getComboPoints() {
        return readByte(COMBO_POINTS);
    }

    @Override
    public String toString() {
        return "UnitObject{" +
            "guid=" + guid +
            " baseAddress=" + baseAddress +
            '}';
    }
}
