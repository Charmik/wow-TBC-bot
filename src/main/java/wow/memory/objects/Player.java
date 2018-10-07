package wow.memory.objects;

import java.awt.geom.Point2D.Double;
import java.util.Optional;

import wow.components.CharacterMovementState;
import wow.components.CharacterState;
import wow.components.Coordinates;
import wow.components.Faction;
import wow.components.Navigation;
import wow.memory.Address;
import wow.memory.Address.DESCRIPTOR;
import wow.memory.Address.OFFSET;
import wow.memory.Address.STATIC;
import wow.memory.MemoryAware;
import wow.memory.WowMemory;
import wow.navigation.Zones;
import wow.navigation.Zones.Zone;

public final class Player extends MemoryAware {
    private static final Address CHAR_BASE_POINTER;
    private static final Address CHAR_ACCNAME;
    private static final Address CHAR_ISINGAME;
    private static final Address CHAR_NAME;
    private static final Address CHAR_ZONEID;
    private static final Address CHAR_LVL;
    private static final Address CHAR_ANGLE;
    private static final Address TARGET_GUID;
    private static final Address TARGET_VENDOR_WINDOW_ID;
    private static final Address CURSOR_STATE;
    private static final Address CHAR_HEALTH_OFFSET;
    private static final Address CHAR_MANA_OFFSET;
    private static final Address CHAR_MAXIMUM_HEALTH_OFFSET;
    private static final Address CHAR_MAXIMUM_MANA_OFFSET;
    private static final Address CHAR_ENERGY_OFFSET;
    private static final Address CHAR_RAGE_OFFSET;
    private static final Address CHAR_X_OFFSET;
    private static final Address CHAR_Y_OFFSET;
    private static final Address CHAR_Z_OFFSET;
    private static final Address CHAR_CASTING_SPELL_ID_OFFSET;
    private static final Address CHAR_MOVEMENT_STATE_OFFSET;
    private static final Address CHAR_STATE_OFFSET;
    private static final Address GUID_OFFSET;
    private static final Address DESCRIPTORS_OFFSET;
    private static final Address CHAR_COMBAT_STATE_DESCRIPTOR;
    private static final Address FACTION_DESCRIPTOR;

    static {
        CHAR_BASE_POINTER = STATIC.CHAR_BASE_POINTER;
        CHAR_ACCNAME = STATIC.CHAR_ACCNAME;
        CHAR_ISINGAME = STATIC.CHAR_ISINGAME;
        CHAR_NAME = STATIC.CHAR_NAME;
        CHAR_ZONEID = STATIC.CHAR_ZONEID;
        CHAR_LVL = STATIC.CHAR_LVL;
        CHAR_ANGLE = STATIC.CHAR_ANGLE;
        TARGET_GUID = STATIC.TARGET_GUID;
        TARGET_VENDOR_WINDOW_ID = STATIC.TARGET_VENDOR_WINDOW_ID;
        CURSOR_STATE = STATIC.CURSOR_STATE;
        CHAR_HEALTH_OFFSET = OFFSET.CHAR_HEALTH;
        CHAR_MANA_OFFSET = OFFSET.CHAR_MANA;
        CHAR_MAXIMUM_HEALTH_OFFSET = OFFSET.CHAR_MAXIMUM_HEALTH;
        CHAR_MAXIMUM_MANA_OFFSET = OFFSET.CHAR_MAXIMUM_MANA;
        CHAR_ENERGY_OFFSET = OFFSET.CHAR_ENERGY;
        CHAR_RAGE_OFFSET = OFFSET.CHAR_RAGE;
        CHAR_X_OFFSET = OFFSET.CHAR_X;
        CHAR_Y_OFFSET = OFFSET.CHAR_Y;
        CHAR_Z_OFFSET = OFFSET.CHAR_Z;
        CHAR_CASTING_SPELL_ID_OFFSET = OFFSET.CHAR_CASTING_SPELL_ID;
        CHAR_MOVEMENT_STATE_OFFSET = OFFSET.CHAR_MOVEMENT_STATE;
        CHAR_STATE_OFFSET = OFFSET.CHAR_STATE;
        GUID_OFFSET = OFFSET.OBJ_BASE_GUID;
        DESCRIPTORS_OFFSET = DESCRIPTOR.DESCRIPTOR_OFFSET;
        CHAR_COMBAT_STATE_DESCRIPTOR = DESCRIPTOR.CHAR_COMBAT_STATE;
        FACTION_DESCRIPTOR = DESCRIPTOR.OBJ_UNIT_FACTION;
    }

    private final WowMemory wowMemory;
    private long baseAddress;
    private int descriptorAddress;
    private long guid;

    public Player(WowMemory wowMemory) {
        super(wowMemory);
        this.wowMemory = wowMemory;
        this.baseAddress = getPlayerBaseAddress();
        super.setBaseAddress(baseAddress);
        this.descriptorAddress = initializeDescriptorAddress();
        super.setDescriptorAddress((long) descriptorAddress);
        this.guid = initializeGuid();
        //log.info("player baseAddress=" + baseAddress + " descriptorAddress=" + descriptorAddress);
    }

    public void updatePlayer() {
        baseAddress = getPlayerBaseAddress();
        super.setBaseAddress(baseAddress);
        descriptorAddress = initializeDescriptorAddress();
        super.setDescriptorAddress((long) descriptorAddress);
        guid = initializeGuid();
    }

    private long getPlayerBaseAddress() {
        int unsignedValue = readInt(CHAR_BASE_POINTER);
        return Integer.toUnsignedLong(unsignedValue);
    }

    private int initializeDescriptorAddress() {
        return readIntOffset(DESCRIPTORS_OFFSET);
    }

    private long initializeGuid() {
        return Integer.toUnsignedLong(readIntOffset(GUID_OFFSET));
    }

    public String getAccountName() {
        return readString(CHAR_ACCNAME);
    }

    public boolean isInGame() {
        return readByte(CHAR_ISINGAME) == 1;
    }

    public String getName() {
        return readString(CHAR_NAME);
    }

    public int getZoneId() {
        return readInt(CHAR_ZONEID);
    }

    public Zone getZone() {
        return Zones.getZone(getZoneId());
    }

    // TODO: add wsg
    public boolean onBg() {
        Zone zone = Zones.getZone(getZoneId());
        return (zone.isEye() || zone.isArathiBasin() || zone.isAlterac() || zone.isWarsong());
    }

    public int getLevel() {
        return readInt(CHAR_LVL);
    }

    public float getAngle() {
        return readFloat(CHAR_ANGLE);
    }

    public Optional<CreatureObject> getTarget() {
        return wowMemory.getObjectManager().getCreature(getTargetGuid());
    }

    private long getTargetGuid() {
        return readLong(TARGET_GUID);
    }

    public void target(WowObject object) {
        if (object != null) {
            target(object.getGuid());
        }
    }

    protected void target(long guid) {
        writeLong(TARGET_GUID, guid);
    }

    public void target(Player player) {
        target(getGuid());
    }

    public int getMouseCursorState() {
        return readByte(CURSOR_STATE);
    }

    public boolean isVendorWindowOpen() {
        return getVendorWindowId() != 0L;
    }

    public long getVendorWindowId() {
        int signedValue = readInt(TARGET_VENDOR_WINDOW_ID);
        return Integer.toUnsignedLong(signedValue);
    }

    public int getHealth() {
        return readIntOffset(CHAR_HEALTH_OFFSET);
    }

    public int getMana() {
        return readIntOffset(CHAR_MANA_OFFSET);
    }

    public int getHealthPercent() {
        int current = readIntOffset(CHAR_HEALTH_OFFSET);
        int max = readIntOffset(CHAR_MAXIMUM_HEALTH_OFFSET);
        return (int) ((double) current / (double) max * 100.0D);
    }

    public int getManaPercent() {
        int current = readIntOffset(CHAR_MANA_OFFSET);
        int max = readIntOffset(CHAR_MAXIMUM_MANA_OFFSET);
        return (int) ((double) current / (double) max * 100.0D);
    }

    public boolean isDead() {
        int health = getHealth();
        return health == 1 || health == 0;
    }

    public boolean isSpirit() {
        int health = getHealth();
        return health == 1;
    }

    public boolean isDeadLyingDown() {
        int health = getHealth();
        return health == 0;
    }

    /*
    public int[] readBlock() {
        return readIntPlayerBlock(OFFSET.CHAR_BLOCK);
    }
    */

    public int getEnergy() {
        return readIntOffset(CHAR_ENERGY_OFFSET);
    }

    public int getRage() {
        return readIntOffset(CHAR_RAGE_OFFSET) / 10;
    }

    public Double getMapCoordinates() {
        int zoneId = getZoneId();
        double xWowCoord = (double) getX();
        double yWowCoord = (double) getY();
        return Navigation.convertCoordinates(zoneId, xWowCoord, yWowCoord);
    }

    public Coordinates getCoordinates() {
        return new Coordinates(getX(), getY(), getZ());
    }

    public float getX() {
        return readFloatOffset(CHAR_X_OFFSET);
    }

    public float getY() {
        return readFloatOffset(CHAR_Y_OFFSET);
    }

    public float getZ() {
        return readFloatOffset(CHAR_Z_OFFSET);
    }

    public boolean isCasting() {
        return getCastingSpellId() != 0;
    }

    public int getCastingSpellId() {
        return readIntOffset(CHAR_CASTING_SPELL_ID_OFFSET);
    }

    public CharacterMovementState getMovementState() {
        return CharacterMovementState.getState(readShortOffset(CHAR_MOVEMENT_STATE_OFFSET));
    }

    public CharacterState getState() {
        return CharacterState.getState(readShortOffset(CHAR_STATE_OFFSET));
    }

    public Faction getFaction() {
        return Faction.getFaction(getFactionId());
    }

    private int getFactionId() {
        return readIntDescriptor(FACTION_DESCRIPTOR);
    }

    public boolean isInCombat() {
        byte value = readByteDescriptor(CHAR_COMBAT_STATE_DESCRIPTOR);
        switch (value) {
            case 0:
                return false;
            case 4:
                return false;
            case 8:
                return true;
            default:
                return false;
        }
    }

    public long getBaseAddress() {
        return baseAddress;
    }

    public int getDescriptorAddress() {
        return descriptorAddress;
    }

    public long getGuid() {
        return guid;
    }

    public String toString() {
        return "Player{name=" + getName() + "accName=" + getAccountName() + "zone=" + getZoneId() + "level=" + getLevel() + "mapCoords=" + getMapCoordinates() + ", baseAddress=" + baseAddress + ", descriptorAddress=" + descriptorAddress + '}';
    }
}
