package wow.bot;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;
import wow.components.Coordinates;
import wow.memory.objects.CreatureObject;

import static wow.components.Navigation.areNear;
import static wow.components.Navigation.areNearAsCaster;
import static wow.components.Navigation.evaluateDistanceFromTo;
import static wow.components.Navigation.get3DCoordsFor;

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
        Coordinates initial = get3DCoordsFor(player);
        Utils.sleep(500);
        Coordinates current = get3DCoordsFor(player);
        double v = evaluateDistanceFromTo(initial, current);
        System.out.println(v);
        ctmManager.stop();
    }

    @Ignore
    @Test
    public void testDistanceCast() {
        Optional<CreatureObject> target = player.getTarget();
        for (; ; ) {
            Coordinates initial = get3DCoordsFor(player);
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
        Coordinates p1 = new Coordinates(2523.686f, 1596.5973f, 1269.3445f);
        Coordinates p2 = new Coordinates(2445.8625f, 1596.8517f, 1199.2115f);
        System.out.println(p1.distance(p2));
        for (; ; ) {
            ctmManager.moveTo(new Coordinates(2046.6353f, 1647.0458f, 1170.6542f));
        }
    }
}
