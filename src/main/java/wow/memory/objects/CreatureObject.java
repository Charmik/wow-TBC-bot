package wow.memory.objects;

import wow.components.Faction;
import wow.components.UnitReaction;
import wow.memory.Address;
import wow.memory.ObjectManager;
import wow.memory.WowMemory;

/**
 * @author Cargeh
 */
public abstract class CreatureObject extends WowObject {

    private static final Address DESCRIPTORS_OFFSET = Address.DESCRIPTOR.DESCRIPTOR_OFFSET;
    private static final Address FACTION_DESCRIPTOR = Address.DESCRIPTOR.OBJ_UNIT_FACTION;
    private static final Address TARGET_DESCRIPTOR = Address.DESCRIPTOR.OBJ_TARGET;
    private static final Address LEVEL_DESCRIPTOR = Address.DESCRIPTOR.OBJ_UNIT_LEVEL;

    private final int descriptorsAddress;
    private UnitReaction unitReaction;

    CreatureObject(
        long baseAddress,
        long guid,
        ObjectManager.ObjectType type,
        WowMemory wowMemory)
    {
        super(baseAddress, guid, type, wowMemory);

        this.descriptorsAddress = initializeDescriptorAddress();
        super.setDescriptorAddress(descriptorsAddress);
    }

    public abstract int getHealth();

    public abstract int getMaximumHealth();

    public abstract int needHealthForFull();

    private int initializeDescriptorAddress() {
        return readIntOffset(DESCRIPTORS_OFFSET);
    }

    public Faction getFaction() {
        return Faction.getFaction(getFactionId());
    }

    private int getFactionId() {
        return readIntDescriptor(FACTION_DESCRIPTOR);
    }

    public boolean isTargetingMe() {
        return getTargetGuid() == memoryManager.getPlayer().getGuid();
    }

    public long getTargetGuid() {
        return Integer.toUnsignedLong(readIntDescriptor(TARGET_DESCRIPTOR));
    }

    public UnitReaction getUnitReaction() {
        return unitReaction;
    }

    public void setUnitReaction(UnitReaction unitReaction) {
        this.unitReaction = unitReaction;
    }

    public int getLevel() {
        return readIntDescriptor(LEVEL_DESCRIPTOR);
    }

    public long getDescriptorsAddress() {
        return descriptorsAddress;
    }
}
