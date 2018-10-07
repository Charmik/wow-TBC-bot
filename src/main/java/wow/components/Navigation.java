package wow.components;

import java.awt.*;

import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;
import wow.memory.objects.WowObject;
import wow.navigation.Zones;

/**
 * @author Cargeh
 */
public class Navigation {

    private static float NEAR_COORDS_DIFFERENCE = 2.0f;
    private static float NEAR_COORDS_DIFFERENCE_MOB = 8.0f;
    private static float NEAR_COORDS_DIFFERENCE_MOB_WITF_FF = 16.0f;
    private static float NEAR_COORDS_DIFFERENCE_AS_CASTER = 25 * 25;
    private static float IS_MOB_NEAR = 40.0f;
    private static float IS_NEAR_TO_POINT = 2f;

    /**
     * Converts WoW General Map Memory coordinates to local zone coordinates
     * that you can see in addons such as Cartographer.
     * <p>
     * X = ABS((CharacterX-X1) / (X2-X1) * 100)
     * Y = ABS((CharacterY-Y1) / (Y2-Y1) * 100)
     * <p>
     * I think because of the fucked up trigonometric circle in wow, where north is 0,
     * coordinates also seem to go that way. So X is really Y while Y is really X.
     * However, addons such as cartographer do show the correct X and Y (standart, that is),
     * so I have to resort to returning y, x. Also, if you fuck with it - make sure you check
     * before commiting changes. It seems to break whenever you change shit around.
     */
    public static Point.Double convertCoordinates(
            int zoneId,
            double wowXCoord,
            double wowYCoord) {
        Zones.Zone zone = Zones.getZone(zoneId);
        double x = Math.abs(((wowXCoord - zone.getX()) / zone.getXDifference()) * 100);
        double y = Math.abs(((wowYCoord - zone.getY()) / zone.getYDifference()) * 100);
        return new Point.Double(y, x);
    }

    public static double evaluateDistanceFromTo(
            Player player,
            WowObject object) {
        return evaluateDistanceFromTo(get3DCoordsFor(player), get3DCoordsFor(object));
    }

    public static double evaluateDistanceFromTo(
        Coordinates unitCoords,
            WowObject object) {
        return evaluateDistanceFromTo(unitCoords, get3DCoordsFor(object));
    }

    public static double evaluateDistanceFromTo(
            Coordinates firstUnit,
            Coordinates secondUnit) {
        float xCoord = firstUnit.getX() - secondUnit.getX();
        float yCoord = firstUnit.getY() - secondUnit.getY();
        float zCoord = firstUnit.getZ() - secondUnit.getZ();
        return Math.sqrt(Math.pow(xCoord, 2.0) + Math.pow(yCoord, 2.0) + Math.pow(zCoord, 2.0));
    }

    public static boolean areNear(
            Player player,
            WowObject object) {
        Coordinates playerCoords = get3DCoordsFor(player);
        Coordinates objectCoords = get3DCoordsFor(object);
        return getCoordsXDifference(playerCoords, objectCoords) <= IS_MOB_NEAR
                && getCoordsYDifference(playerCoords, objectCoords) <= IS_MOB_NEAR;
    }

    public static boolean areNear(
        Coordinates playerCoords,
            WowObject object,
            int level,
            boolean goToAsMelee) {
        Coordinates objectCoords = get3DCoordsFor(object);
        float f = NEAR_COORDS_DIFFERENCE_MOB;
        if (level >= 30 && !goToAsMelee) {
            f = NEAR_COORDS_DIFFERENCE_MOB_WITF_FF;
        }
        return getCoordsXDifference(playerCoords, objectCoords) <= f
                && getCoordsYDifference(playerCoords, objectCoords) <= f;
    }

    public static boolean areNear(
        Coordinates playerCoords,
            WowObject object) {
        return areNear(playerCoords, object, -1, false);
    }

    public static boolean areNearAsCaster(
        Coordinates playerCoords,
            WowObject object) {
        Coordinates objectCoords = get3DCoordsFor(object);
        float coordsXDifference = getCoordsXDifference(playerCoords, objectCoords);
        float coordsYDifference = getCoordsYDifference(playerCoords, objectCoords);
        float diff = coordsXDifference * coordsXDifference + coordsYDifference * coordsYDifference;
        return diff <= NEAR_COORDS_DIFFERENCE_AS_CASTER;
    }

    public static boolean areNear(
        Coordinates firstUnit,
        Coordinates secondUnit)
    {
        return getCoordsXDifference(firstUnit, secondUnit) <= NEAR_COORDS_DIFFERENCE
                && getCoordsYDifference(firstUnit, secondUnit) <= NEAR_COORDS_DIFFERENCE;
    }

    public static boolean isNear(
            Coordinates player,
            Coordinates point) {
        double x = Math.abs(player.getX() - point.getX());
        double y = Math.abs(player.getY() - point.getY());
        double z = Math.abs(player.getZ() - point.getZ());
        return x <= IS_NEAR_TO_POINT
                && y <= IS_NEAR_TO_POINT
                && z <= IS_NEAR_TO_POINT;
    }

    public static float getCoordsXDifference(
        Coordinates firstUnit,
        Coordinates secondUnit)
    {
        return Math.abs(firstUnit.getX() - secondUnit.getX());
    }

    public static float getCoordsYDifference(
        Coordinates firstUnit,
        Coordinates secondUnit)
    {
        return Math.abs(firstUnit.getY() - secondUnit.getY());
    }

    public static Coordinates get3DCoordsFor(Player player) {
        return new Coordinates(player.getX(), player.getY(), player.getZ());
    }

    public static Coordinates get3DCoordsFor(WowObject object) {
        return new Coordinates(object.getX(), object.getY(), object.getZ());
    }

    public static boolean isUnitTooFarFromPoint(
            Coordinates nextPoint,
            Coordinates point,
            boolean inCombat) {
        Coordinates nextPointCoordinates = new Coordinates((float) nextPoint.getX(), (float) nextPoint.getY(), (float) nextPoint.getZ());
        Coordinates unitCoordinats = new Coordinates((float) point.getX(), (float) point.getY(), (float) point.getZ());
        double distanceBetweenUnitAndPoint = evaluateDistanceFromTo(nextPointCoordinates, unitCoordinats);
        return distanceBetweenUnitAndPoint > 300.0D && !inCombat;
    }

    public static boolean isUnitTooFarFromPoint(
            Coordinates nextPoint,
            UnitObject unit,
            boolean inCombat) {
        Coordinates nextPointCoordinates = new Coordinates((float) nextPoint.getX(), (float) nextPoint.getY(), (float) nextPoint.getZ());
        Coordinates unitCoordinats = get3DCoordsFor((WowObject) unit);
        double distanceBetweenUnitAndPoint = evaluateDistanceFromTo(nextPointCoordinates, unitCoordinats);
        return distanceBetweenUnitAndPoint > 300.0D && !inCombat;
    }
}
