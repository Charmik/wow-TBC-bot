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
    private static final Address CHAR_MAXIMUM_HEALT = Address.OFFSET.CHAR_MAXIMUM_HEALTH;

    private final int arr[] = new int[100];

    public PlayerObject(
        long baseAddress,
        long guid,
        ObjectType type,
        WowMemory wowMemory)
    {
        super(baseAddress, guid, type, wowMemory);
    }

    public boolean isDead() {
        return getHealth() == 0 || getHealth() == 1;
    }

    @Override
    public int getHealth() {
        return readIntOffset(CHAR_HEALTH);
    }

    @Override
    public int getMaximumHealth() {
        return readIntOffset(CHAR_MAXIMUM_HEALT);
    }

    @Override
    public int needHealthForFull() {
        return getMaximumHealth() - getHealth();
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
