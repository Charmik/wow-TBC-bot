package wow.components;

import org.junit.Ignore;
import org.junit.Test;
import util.Utils;
import wow.BaseTest;

/**
 * @author Cargeh
 */
public class CharacterMovementStateTest extends BaseTest {
    @Ignore
    @Test
    public void getState() {
        CharacterMovementState prevState = CharacterMovementState.UNKNOWN;
        while (true) {
            CharacterMovementState currentState = player.getMovementState();
            if (currentState != prevState) {
                prevState = currentState;
                System.out.println(currentState);
            }
            Utils.sleep(1000);
        }
    }
}
