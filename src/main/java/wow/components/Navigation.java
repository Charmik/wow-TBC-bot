package wow.components;

import java.awt.*;

import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;
import wow.memory.objects.WowObject;
import wow.navigation.Zones;

/**
 * @author Cargeh
 */
public class Navigation {

    private static Logger log = LoggerFactory.getLogger(Navigation.class);

    private static float NEAR_COORDS_DIFFERENCE = 2.0f;
    private static float NEAR_COORDS_DIFFERENCE_MOB = 4.0f;
    private static float NEAR_COORDS_DIFFERENCE_AS_CASTER = 900;
    private static float IS_MOB_NEAR = 40.0f;
    private static float IS_NEAR_TO_POINT = 2.0f;

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
        double wowYCoord)
    {
        Zones.Zone zone = Zones.getZone(zoneId);
        double x = Math.abs(((wowXCoord - zone.getX()) / zone.getXDifference()) * 100);
        double y = Math.abs(((wowYCoord - zone.getY()) / zone.getYDifference()) * 100);
        return new Point.Double(y, x);
    }

    public static double evaluateDistanceFromTo(
        Player player,
        WowObject object)
    {
        return evaluateDistanceFromTo(get2DCoordsFor(player), get2DCoordsFor(object));
    }

    public static double evaluateDistanceFromTo(
        Point3D firstUnit,
        Point3D secondUnit)
    {
        return evaluateDistanceFromTo(new Coordinates3D((float) firstUnit.getX(), (float) firstUnit.getY(), (float) firstUnit.getZ()), new Coordinates3D((float) secondUnit.getX(), (float) secondUnit.getY(), (float) secondUnit.getZ()));
    }

    public static double evaluateDistanceFromTo(
        Coordinates2D unitCoords,
        WowObject object)
    {
        return evaluateDistanceFromTo(unitCoords, get2DCoordsFor(object));
    }

    public static double evaluateDistanceFromTo(
        Coordinates2D firstUnit,
        Coordinates2D secondUnit)
    {
        float xCoord = firstUnit.x - secondUnit.x;
        float yCoord = firstUnit.y - secondUnit.y;
        return Math.pow(xCoord, 2.0) + Math.pow(yCoord, 2.0);
    }

    public static double evaluateDistanceFromTo(
        Coordinates3D firstUnit,
        Coordinates3D secondUnit)
    {
        float xCoord = firstUnit.x - secondUnit.x;
        float yCoord = firstUnit.y - secondUnit.y;
        float zCoord = firstUnit.z - secondUnit.z;
        return Math.pow(xCoord, 2.0) + Math.pow(yCoord, 2.0) + Math.pow(zCoord, 2.0);
    }

    public static boolean areNear(
        Player player,
        WowObject object)
    {
        Coordinates2D playerCoords = get2DCoordsFor(player);
        Coordinates2D objectCoords = get2DCoordsFor(object);
        return getCoordsXDifference(playerCoords, objectCoords) <= IS_MOB_NEAR
            && getCoordsYDifference(playerCoords, objectCoords) <= IS_MOB_NEAR;
    }

    public static boolean areNear(
        Coordinates2D playerCoords,
        WowObject object)
    {
        Coordinates2D objectCoords = get2DCoordsFor(object);
        return getCoordsXDifference(playerCoords, objectCoords) <= NEAR_COORDS_DIFFERENCE_MOB
            && getCoordsYDifference(playerCoords, objectCoords) <= NEAR_COORDS_DIFFERENCE_MOB;
    }

    public static boolean areNearAsCaster(
        Coordinates2D playerCoords,
        WowObject object)
    {
        Coordinates2D objectCoords = get2DCoordsFor(object);
        float coordsXDifference = getCoordsXDifference(playerCoords, objectCoords);
        float coordsYDifference = getCoordsYDifference(playerCoords, objectCoords);
        float diff = coordsXDifference * coordsXDifference + coordsYDifference * coordsYDifference;
        return diff <= NEAR_COORDS_DIFFERENCE_AS_CASTER;
    }

    public static boolean areNear(
        Coordinates2D firstUnit,
        Coordinates2D secondUnit)
    {
        return getCoordsXDifference(firstUnit, secondUnit) <= NEAR_COORDS_DIFFERENCE
            && getCoordsYDifference(firstUnit, secondUnit) <= NEAR_COORDS_DIFFERENCE;
    }

    public static boolean isNear(
        Coordinates3D player,
        Point3D point)
    {
        return Math.abs(player.x - point.getX()) <= IS_NEAR_TO_POINT
            && Math.abs(player.y - point.getY()) <= IS_NEAR_TO_POINT
            && Math.abs(player.z - point.getZ()) <= IS_NEAR_TO_POINT;
    }

    public static float getCoordsXDifference(
        Coordinates2D firstUnit,
        Coordinates2D secondUnit)
    {
        return Math.abs(firstUnit.x - secondUnit.x);
    }

    public static float getCoordsYDifference(
        Coordinates2D firstUnit,
        Coordinates2D secondUnit)
    {
        return Math.abs(firstUnit.y - secondUnit.y);
    }

    public static Coordinates2D get2DCoordsFor(Player player) {
        return new Coordinates2D(player.getX(), player.getY());
    }

    public static Coordinates2D get2DCoordsFor(WowObject object) {
        return new Coordinates2D(object.getX(), object.getY());
    }

    public static Coordinates3D get3DCoordsFor(Player player) {
        return new Coordinates3D(player.getX(), player.getY(), player.getZ());
    }

    public static Coordinates3D get3DCoordsFor(WowObject object) {
        return new Coordinates3D(object.getX(), object.getY(), object.getZ());
    }

    public static boolean isUnitTooFarFromPoint(
        Point3D nextPoint,
        Point3D point,
        boolean inCombat)
    {
        Coordinates3D nextPointCoordinates = new Coordinates3D((float) nextPoint.getX(), (float) nextPoint.getY(), (float) nextPoint.getZ());
        Coordinates3D unitCoordinats = new Coordinates3D((float) point.getX(), (float) point.getY(), (float) point.getZ());
        double distanceBetweenUnitAndPoint = evaluateDistanceFromTo(nextPointCoordinates, unitCoordinats);
        return distanceBetweenUnitAndPoint > 300.0D && !inCombat;
    }

    public static boolean isUnitTooFarFromPoint(
        Point3D nextPoint,
        UnitObject unit,
        boolean inCombat)
    {
        Coordinates3D nextPointCoordinates = new Coordinates3D((float) nextPoint.getX(), (float) nextPoint.getY(), (float) nextPoint.getZ());
        Coordinates3D unitCoordinats = get3DCoordsFor((WowObject) unit);
        double distanceBetweenUnitAndPoint = evaluateDistanceFromTo(nextPointCoordinates, unitCoordinats);
        return distanceBetweenUnitAndPoint > 300.0D && !inCombat;
    }

    public static class Coordinates2D {
        public float x;
        public float y;

        public Coordinates2D(
            float x,
            float y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "Coordinates2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
        }
    }

    public static class Coordinates3D {
        public float x;
        public float y;
        public float z;

        public Coordinates3D(
            float x,
            float y,
            float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public String toString() {
            return "Coordinates3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
        }
    }
}
