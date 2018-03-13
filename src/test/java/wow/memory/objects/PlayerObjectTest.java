package wow.memory.objects;

import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;

/**
 * @author Cargeh
 */
public class PlayerObjectTest extends BaseTest {
    @Ignore
    @Test
    public void testIfPlayerIsCasting() {
        while (true) {
            Optional<CreatureObject> target = player.getTarget();
            if (target.isPresent() && target.get() instanceof PlayerObject) {
                boolean isCasting = ((PlayerObject) target.get()).isCasting();
                if (isCasting)
                    System.out.println("Target player is casting");
            }
            Utils.sleep(500);
        }
    }

    @Ignore
    @Test
    public void getPlayerDebugInfo() {
        Optional<PlayerObject> nearestPlayer = objectManager.getAndExcludeNearestPlayerTo(player);
        if (nearestPlayer.isPresent()) {
            PlayerObject neareset = nearestPlayer.get();
            System.out.println(neareset.getGuid());
            System.out.println(Long.toHexString(neareset.getBaseAddress()));
            System.out.println(Long.toHexString(neareset.getDescriptorsAddress()));
            Utils.sleep(500);
        }
    }
}
