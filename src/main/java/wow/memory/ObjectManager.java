package wow.memory;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.components.Coordinates;
import wow.components.Faction;
import wow.components.UnitReaction;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.Player;
import wow.memory.objects.PlayerObject;
import wow.memory.objects.UnitObject;

import static wow.components.Navigation.evaluateDistanceFromTo;
import static wow.components.Navigation.get3DCoordsFor;
import static wow.memory.ObjectManager.ObjectType.CONTAINER;
import static wow.memory.ObjectManager.ObjectType.PLAYER;
import static wow.memory.ObjectManager.ObjectType.UNIT;

/**
 * @author Cargeh
 */
public final class ObjectManager extends MemoryAware {

    private static Logger log = LoggerFactory.getLogger(ObjectManager.class);

    private static final Address FIRST_OBJECT_OFFSET = Address.OFFSET.OBJ_FIRST;
    private static final Address TYPE_OFFSET = Address.OFFSET.OBJ_BASE_TYPE;
    private static final Address GUID_OFFSET = Address.OFFSET.OBJ_BASE_GUID;
    private static final Address NEXT_OBJECT_OFFSET = Address.OFFSET.OBJ_NEXT;

    private final WowMemory wowMemory;
    private final Map<Long, PlayerObject> players;
    private final Map<Long, UnitObject> units;

    ObjectManager(WowMemory wowMemory) {
        super(wowMemory);
        super.setBaseAddress(getObjectManagerAddress());

        this.wowMemory = wowMemory;
        this.players = new HashMap<>();
        this.units = new HashMap<>();
    }

    private long getObjectManagerAddress() {
        Address objectManagerOffset = Address.OFFSET.OBJ_MANAGER;
        long clientConnection = getClientConnectionAddress();
        int signedValue = readIntOffset(clientConnection, objectManagerOffset);
        return Integer.toUnsignedLong(signedValue);
    }

    private long getClientConnectionAddress() {
        Address clientConnection = Address.STATIC.CLIENT_CONNECTION;
        int signedAddress = readInt(clientConnection);
        return Integer.toUnsignedLong(signedAddress);
    }

    public void refillPlayers() {
        //log.info("refillPlayers");
        if (!players.isEmpty()) {
            players.clear();
        }
        scanForNewPlayers();
    }

    public void refillUnits() {
        //log.info("refillUnits in the memory, EXPENSIVE OPERATION!!!!");
        if (!units.isEmpty()) {
            units.clear();
        }
        scanForNewUnits();
    }

    public void refillObjects() {
        if (!players.isEmpty()) {
            players.clear();
        }
        if (!units.isEmpty()) {
            units.clear();
        }
        scanForNewObjects();
    }

    public final void scanForNewObjects() {
        scanForNewPlayers();
        scanForNewUnits();
    }

    public final void scanForNewPlayers() {
        scanForNewObjectsWithType(PLAYER, players);
    }

    public final void scanForNewUnits() {
        scanForNewObjectsWithType(UNIT, units);
    }

    public final void scanForNew() {
        scanForNewObjectsWithType(CONTAINER, units);

    }

    private <T> void scanForNewObjectsWithType(
        ObjectType requiredType,
        Map<Long, T> map)
    {
        Faction playerFaction = wowMemory.getPlayer().getFaction();

        long currentObjAddress = getFirstObjectAddress();
        while (isObject(currentObjAddress)) {
            ObjectType objType = getObjectType(currentObjAddress);
            if (objType == requiredType) {
                long guid = getObjectGuid(currentObjAddress);
                if (isNew(guid, requiredType)) {
                    Optional<T> wowObject = initializeTypedObject(currentObjAddress, guid, requiredType);
                    wowObject.ifPresent(object -> {
                            map.put(guid, object);
                            if (object instanceof CreatureObject)
                                initializeUnitReaction(playerFaction, (CreatureObject) object);
                        }
                    );
                }
            }
            currentObjAddress = getNextObjectAddress(currentObjAddress);
        }
    }

    private long getFirstObjectAddress() {
        int signedAddress = readIntOffset(FIRST_OBJECT_OFFSET);
        return Integer.toUnsignedLong(signedAddress);
    }

    /**
     * Checks whether the given address is the object manager object address.
     * Returns false when the object manager contains no more objects.
     */
    private boolean isObject(long currentObjAddress) {
        return (currentObjAddress != 0) && ((currentObjAddress & 1) == 0);
    }

    private ObjectType getObjectType(long objectBaseAddress) {
        int typeIdentifier = readIntOffset(objectBaseAddress, TYPE_OFFSET);
        return ObjectType.getType(typeIdentifier);
    }

    private long getObjectGuid(long objectBaseAddress) {
        return readLongOffset(objectBaseAddress, GUID_OFFSET);
    }

    private boolean isNew(
        long guid,
        ObjectType type)
    {
        switch (type) {
            case PLAYER:
                return !players.containsKey(guid);
            case UNIT:
                return !units.containsKey(guid);
            default:
                return false;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> initializeTypedObject(
        long baseAddress,
        long guid,
        ObjectType type)
    {
        switch (type) {
            case PLAYER:
                return Optional.of((T) new PlayerObject(baseAddress, guid, type, wowMemory));
            case UNIT:
                return Optional.of((T) new UnitObject(baseAddress, guid, type, wowMemory));
            default:
                return Optional.empty();
        }
    }

    private void initializeUnitReaction(
        Faction playerFaction,
        CreatureObject object)
    {
        UnitReaction unitReaction = UnitReaction.getReaction(playerFaction, object.getFaction());
        object.setUnitReaction(unitReaction);
    }

    private long getNextObjectAddress(long currentObjectAddress) {
        int signedValue = readIntOffset(currentObjectAddress, NEXT_OBJECT_OFFSET);
        return Integer.toUnsignedLong(signedValue);
    }

    /**
     * Returns and deletes the nearest object. Useful in cases
     * when you're grinding. Don't want a dead mob in the objects map.
     */
    public Optional<UnitObject> getAndExcludeNearestUnitTo(Player player) {
        Optional<UnitObject> nearestPlayer = getNearestUnitTo(player);
        nearestPlayer.ifPresent(wowObject -> units.remove(wowObject.getGuid()));
        return nearestPlayer;
    }

    public Optional<UnitObject> getNearestUnitTo(Player player) {
        Coordinates playerCoordinates = get3DCoordsFor(player);
        return units.values().stream()
            .min(Comparator.comparingDouble(unit -> evaluateDistanceFromTo(playerCoordinates, unit)));
    }

    public Optional<UnitObject> getNearestAuctioneer(Player player) {
        Coordinates playerCoordinates = get3DCoordsFor(player);
        return units.values().stream()
            .filter(e -> e.getLevel() == 50 && e.getMana() == 0 && e.getTargetGuid() == 0 && e.getHealth() == 100)
            .min(Comparator.comparingDouble(unit -> evaluateDistanceFromTo(playerCoordinates, unit)));
    }

    public Optional<UnitObject> getNearestEnemyMob(
        Player player,
        Collection<UnitObject> mobs)
    {
        return getNearestEnemyMob(player, mobs.stream());
    }

    public Optional<UnitObject> getNearestEnemyMob(
        Player player,
        Stream<UnitObject> mobs)
    {
        Coordinates playerCoordinates = get3DCoordsFor(player);
        return mobs
            .filter(e -> UnitReaction.getReaction(player.getFaction(), e.getFaction()).canAttack())
            .min(Comparator.comparingDouble(unit -> evaluateDistanceFromTo(playerCoordinates, unit)));
    }

    public Optional<PlayerObject> getAndExcludeNearestPlayerTo(Player player) {
        Optional<PlayerObject> nearestPlayer = getNearestPlayerTo(player);
        nearestPlayer.ifPresent(wowObject -> players.remove(wowObject.getGuid()));
        return nearestPlayer;
    }

    public Optional<PlayerObject> getNearestPlayerTo(Player player) {
        Coordinates playerCoordinates = get3DCoordsFor(player);
        return Optional.ofNullable(players.values().stream()
            .min(Comparator.comparingDouble(unit -> evaluateDistanceFromTo(playerCoordinates, unit)))
            .orElse(null));
    }

    public Optional<PlayerObject> getPlayer(long guid) {
        return Optional.ofNullable(players.get(guid));
    }

    public Optional<UnitObject> getUnit(long guid) {
        UnitObject value = units.get(guid);
        return Optional.ofNullable(value);
    }

    public Optional<CreatureObject> getCreature(long guid) {
        UnitObject unitObject = units.get(guid);
        if (unitObject != null)
            return Optional.of(unitObject);

        PlayerObject playerObject = players.get(guid);
        if (playerObject != null)
            return Optional.of(playerObject);

        return Optional.empty();
    }

    public List<UnitObject> getMobsTargetingMe(boolean makeRefill) {
        if (makeRefill) {
            refillUnits();
        }
        return units
            .values()
            .stream()
            .filter(CreatureObject::isTargetingMe)
            .collect(Collectors.toList());
    }

    public Map<Long, PlayerObject> getPlayers() {
        return players;
    }

    public Map<Long, UnitObject> getUnits() {
        return units;
    }

    public void removeUnit(long guid) {
        units.remove(guid);
    }

    public enum ObjectType {
        OBJECT,
        ITEM,
        CONTAINER,
        UNIT,
        PLAYER,
        GAME_OBJECT,
        DYNAMIC_OBJECT,
        CORPSE,
        AREA_TRIGGER,
        SCENE_OBJECT;

        private static ObjectType[] types = ObjectType.values();

        // can be incorrect i
        private static ObjectType getType(int i) {
            return types[i];
        }
    }
}
