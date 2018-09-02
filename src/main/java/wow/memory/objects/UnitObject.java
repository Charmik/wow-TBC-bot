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
    private static final Address MAX_HEALTH_DESCRIPTOR = Address.DESCRIPTOR.OBJ_MAX_UNIT_HP;
    private static final Address MANA_DESCRIPTOR = Address.DESCRIPTOR.OBJ_UNIT_MANA;
    private static final Address UNIT_BLOCK_DESCRIPTOR = Address.DESCRIPTOR.OBJ_BLOCK;
    private static final Address COMBO_POINTS = Address.STATIC.TARGET_COMBO_POINTS;

    public UnitObject(
        long baseAddress,
        long guid,
        ObjectManager.ObjectType type,
        WowMemory wowMemory)
    {
        super(baseAddress, guid, type, wowMemory);
    }

    @Override
    public int getHealth() {
        return readIntDescriptor(HEALTH_DESCRIPTOR);
    }

    @Override
    public int getMaximumHealth() {
        return readIntDescriptor(MAX_HEALTH_DESCRIPTOR);
    }

    @Override
    public int needHealthForFull() {
        return getMaximumHealth() - getHealth();
    }

    public int getMana() {
        return readIntDescriptor(MANA_DESCRIPTOR);
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

    //TODO: delete
    public int[] readBlock() {
        return readBlock(UNIT_BLOCK_DESCRIPTOR, 0);
        /*
        Memory memory = readMemory(baseAddress, Address.DESCRIPTOR.OBJ);
        int arr[] = new int[100];
        memory.read(0, arr, 0, 7);
        memory.clear();
        return arr;
        */
    }
}
