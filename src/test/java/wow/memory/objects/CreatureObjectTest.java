package wow.memory.objects;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;

import static wow.memory.Address.STATIC.TEST;

/**
 * @author Cargeh
 */
public class CreatureObjectTest extends BaseTest {
    @Ignore
    @Test
    public void getTargetAddresses() {
        Optional<CreatureObject> target = player.getTarget();

        if (target.isPresent()) {
            WowObject object = target.get();
//            final PlayerObject PO = (PlayerObject) object;
//            final int[] memory = PO.readBlockMemory(Address.OFFSET.CHAR_BLOCK);
//            System.out.println(Arrays.toString(memory));
            System.out.println("Base address: " + Long.toHexString(object.getBaseAddress()));

            final UnitObject t = (UnitObject) object;
            final int[] ints = t.readBlockMemory(TEST);
            System.out.printf(Arrays.toString(ints));
            System.out.println("Descriptors address: " + Long.toHexString((t).getDescriptorsAddress()));
        }
    }

    @Ignore
    @Test
    public void testLevel() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(creatureObject -> System.out.println(creatureObject.getLevel()));
    }

    @Ignore
    @Test
    public void testUnitReaction() {
        Optional<CreatureObject> target = player.getTarget();
        target.ifPresent(creatureObject -> System.out.println(creatureObject.getUnitReaction()));
    }

    @Ignore
    @Test
    public void pingTargetHealth() {
        int lastHp = Integer.MAX_VALUE;
        while (true) {
            Optional<CreatureObject> target = player.getTarget();
            if (target.isPresent()) {
                int hp = getHp(target.get());
                if (hp != lastHp) {
                    lastHp = hp;
                    System.out.println("HP: " + hp);
                    if (hp == 0)
                        System.out.println("-------------");
                }
            }
            Utils.sleep(500);
        }
    }

    private int getHp(WowObject object) {
        if (object instanceof UnitObject) {
            return ((UnitObject) object).getHealth();
        } else if (object instanceof PlayerObject) {
            return ((PlayerObject) object).getHealth();
        }
        return Integer.MIN_VALUE;
    }

    @Ignore
    @Test
    public void testGetCombo() {
        Optional<CreatureObject> object = player.getTarget();
        if (object.isPresent()) {
            if (object.get() instanceof UnitObject) {
                System.out.println((((UnitObject) object.get()).getComboPoints()));
            }
        }

    }
}
