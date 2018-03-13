package wow.memory.objects;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;

/**
 * @author Cargeh
 */
public class UnitObjectTest extends BaseTest {

    @Ignore
    @Test
    public void testUnitReaction() {
        int counter = 0;
        WowObject lastObject = null;
        while (true) {
            Optional<CreatureObject> target = player.getTarget();
            if (target.isPresent() && !target.get().equals(lastObject)) {
                WowObject object = target.get();
                if (object instanceof CreatureObject)
                    System.out.println("Reaction: " + ((CreatureObject) object).getUnitReaction());
                else
                    System.out.println("fuck off");
                lastObject = object;
            }
            Utils.sleep(500);

            if (counter++ > 10) {
                objectManager.scanForNewObjects();
            }
        }
    }
}
