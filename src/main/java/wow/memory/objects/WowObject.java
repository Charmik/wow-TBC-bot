package wow.memory.objects;

import com.sun.jna.Memory;
import wow.memory.Address;
import wow.memory.MemoryAware;
import wow.memory.ObjectManager.ObjectType;
import wow.memory.WowMemory;

/**
 * @author Cargeh
 */
public abstract class WowObject extends MemoryAware {

    private static final Address xOffset = Address.OFFSET.OBJ_BASE_X;
    private static final Address yOffset = Address.OFFSET.OBJ_BASE_Y;
    private static final Address zOffset = Address.OFFSET.OBJ_BASE_Z;
    protected final long baseAddress;
    protected final long guid;
    private final ObjectType type;
    //block memory
    private int arr[] = new int[100];

    WowObject(
        long baseAddress,
        long guid,
        ObjectType type,
        WowMemory wowMemory)
    {
        super(wowMemory);

        this.baseAddress = baseAddress;
        super.setBaseAddress(baseAddress);

        this.guid = guid;
        this.type = type;
    }

    /**
     * @return map-wide x position (not to confuse with local zone coordinates)
     */
    public float getX() {
        return readFloatOffset(xOffset);
    }

    /**
     * @return map-wide y position (not to confuse with local zone coordinates)
     */
    public float getY() {
        return readFloatOffset(yOffset);
    }

    public float getZ() {
        return readFloatOffset(zOffset);
    }

    public ObjectType getType() {
        return type;
    }

    public long getBaseAddress() {
        return baseAddress;
    }

    public long getGuid() {
        return guid;
    }

    @Override
    public abstract String toString();

    public int[] readBlockMemory(Address address) {
        Memory memory = readMemory(baseAddress, address);
        memory.read(0, arr, 0, 50);
        memory.clear();
        return arr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WowObject wowObject = (WowObject) o;
        return guid == wowObject.guid;
    }

    @Override
    public int hashCode() {
        return (int) (guid ^ (guid >>> 32));
    }
}
