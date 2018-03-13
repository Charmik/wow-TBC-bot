package wow.memory.objects;

import com.sun.jna.Memory;
import wow.memory.Address;
import wow.memory.ObjectManager.ObjectType;
import wow.memory.WowMemory;

/**
 * @author Cargeh
 */
public final class PlayerObject extends CreatureObject {
    private static final Address OBJ_PLAYER_CASTING = Address.OFFSET.OBJ_PLAYER_ISCASTING;
    private static final Address CHAR_HEALTH = Address.OFFSET.CHAR_HEALTH;

    public PlayerObject(
        long baseAddress,
        long guid,
        ObjectType type,
        WowMemory wowMemory)
    {
        super(baseAddress, guid, type, wowMemory);
    }

    public int getHealth() {
        return readIntOffset(CHAR_HEALTH);
    }


    public int needHealthForFull() {
        Memory memory = readMemory(baseAddress, Address.OFFSET.CHAR_BLOCK);
        int arr[] = new int[100];
        memory.read(0, arr, 0, 7);
        memory.clear();
        int currentHealth = arr[0];
        int maxHealth = arr[6];
        if (currentHealth < 2) {
            return -1;
        }
        return maxHealth - currentHealth;
    }

    /**
     * @return (int) id of spell being cast
     */
    public int getCastingSpellId() {
        return readIntOffset(OBJ_PLAYER_CASTING);
    }

    public boolean isCasting() {
        return getCastingSpellId() != 0;
    }

    @Override
    public String toString() {
        return "PlayerObject{" +
            "guid=" + guid +
            "baseAddress=" + baseAddress +
            '}';
    }
}
