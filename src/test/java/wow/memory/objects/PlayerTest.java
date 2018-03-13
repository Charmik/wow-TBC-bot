package wow.memory.objects;

import java.util.Collection;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;

/**
 * @author Cargeh
 */
public class PlayerTest extends BaseTest {
    @Ignore
    @Test
    public void printDebugInfo() {
        long baseAddress = player.getBaseAddress();
        long descriptorAddress = player.getDescriptorAddress();
        System.out.println(Long.toHexString(baseAddress));
        System.out.println(Long.toHexString(descriptorAddress));
        System.out.println("Guid: " + player.getGuid());
    }

    @Ignore
    @Test
    public void testInCombat() {
        boolean prevState = false;
        while (true) {
            boolean isInCombat = player.isInCombat();
            if (prevState != isInCombat) {
                prevState = isInCombat;
                if (isInCombat) {
                    System.out.println(new Date() + "; Enter combat: " + isInCombat);
                } else {
                    System.out.println(new Date() + "; Leave combat: " + isInCombat);
                }
            }
            Utils.sleep(200);
        }
    }

    @Ignore
    @Test
    public void testTargetedByAndCombatState() {
        long playerGuid = player.getGuid();
        Collection<UnitObject> units = objectManager.getUnits().values();
        int lastCounter = -1;
        boolean lastCombatValue = false;
        while (true) {
            int counter = 0;
            for (UnitObject object : units) {
                if (object.isTargetingMe()) {
                    counter++;
                }
            }
            boolean combat = player.isInCombat();
            if (combat != lastCombatValue) {
                if (combat) {
                    System.out.println("Entered combat");
                } else {
                    System.out.println("Left combat");
                }
                lastCombatValue = combat;
            }
            if (lastCounter != counter) {
                lastCounter = counter;
                System.out.println("Targeted by: " + counter);
            }
            Utils.sleep(300);
        }
    }
}
