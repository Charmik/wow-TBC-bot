package wow.bot;

import java.util.Optional;

import javafx.geometry.Point3D;
import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;
import wow.components.Navigation;
import wow.memory.objects.CreatureObject;

import static wow.components.Navigation.areNear;
import static wow.components.Navigation.areNearAsCaster;
import static wow.components.Navigation.evaluateDistanceFromTo;
import static wow.components.Navigation.get2DCoordsFor;

/**
 * @author Cargeh
 */
public class NavigationTest extends BaseTest {
    @Ignore
    @Test
    public void testPlayerNearTarget() {
        while (true) {
            Optional<CreatureObject> target = player.getTarget();
            if (target.isPresent() && areNear(player, target.get())) {
                System.out.println("NEAR");
            }
            Utils.sleep(300);
        }
    }

    @Ignore
    @Test
    public void testDistance() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(ctmManager::moveTo);
        Navigation.Coordinates2D initial = get2DCoordsFor(player);
        Utils.sleep(500);
        Navigation.Coordinates2D current = get2DCoordsFor(player);
        double v = evaluateDistanceFromTo(initial, current);
        System.out.println(v);
        ctmManager.stop();
    }

    @Ignore
    @Test
    public void testDistanceCast() {
        Optional<CreatureObject> target = player.getTarget();
        for (; ; ) {
            Navigation.Coordinates2D initial = get2DCoordsFor(player);
            target.ifPresent(creatureObject -> areNearAsCaster(initial, creatureObject));
            Utils.sleep(100);
        }
    }

    @Ignore
    @Test
    public void test() {
        while (true) {
            System.out.println(player.getCoordinates());
            Utils.sleep(1000);
        }
    }

    @Ignore
    @Test
    public void goToPoint() {
        Point3D p1 = new Point3D(2523.686, 1596.5973, 1269.3445);
        Point3D p2 = new Point3D(2445.8625, 1596.8517, 1199.2115);
        System.out.println(p1.distance(p2));
        for (; ; ) {
            ctmManager.moveTo(new Point3D(2046.6353, 1647.0458, 1170.6542));
        }
    }
}
