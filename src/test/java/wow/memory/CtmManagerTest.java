package wow.memory;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.UnitObject;

/**
 * @author Cargeh
 */
public class CtmManagerTest extends BaseTest {

    @Ignore
    @Test
    public void testMoveTo() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(ctmManager::face);
        System.out.println(player.getEnergy());
    }

    @Ignore
    @Test
    public void goToNearestUnit() {
        int visitedUnits = 0;
        while (true) {
            Optional<UnitObject> nearestUnit = objectManager.getAndExcludeNearestUnitTo(player);
            if (nearestUnit.isPresent()) {
                ctmManager.goTo(nearestUnit.get());
                if (visitedUnits++ > 15) {
                    objectManager.scanForNewUnits();
                    visitedUnits = 0;
                }
            }
        }
    }

    @Ignore
    @Test
    public void attackTarget() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(ctmManager::attack);
    }

    @Ignore
    @Test
    public void gotoAndStop() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(object -> {
            ctmManager.moveTo(object);
            Utils.sleep(500);
            System.out.println("stop");
            ctmManager.stop();
        });
    }

    // openes the window, but doesnt pick shit up
    @Ignore
    @Test
    public void lootTarget() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(ctmManager::loot);
    }

    @Ignore
    @Test
    public void faceTarget() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(ctmManager::face);
    }

    @Ignore
    @Test
    public void interactNpc() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(ctmManager::interactNpc);
    }

    @Ignore
    @Test
    public void getGUID() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(System.out::print);
    }
}
