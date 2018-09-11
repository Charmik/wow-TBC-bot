package wow.memory;

import java.util.Objects;

import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Navigation;
import wow.memory.objects.Player;
import wow.memory.objects.WowObject;

import static wow.components.Navigation.Coordinates2D;
import static wow.components.Navigation.Coordinates3D;
import static wow.components.Navigation.areNear;
import static wow.components.Navigation.areNearAsCaster;
import static wow.components.Navigation.evaluateDistanceFromTo;
import static wow.components.Navigation.get2DCoordsFor;
import static wow.components.Navigation.get3DCoordsFor;
import static wow.components.Navigation.isNear;
import static wow.memory.CtmManager.CtmAction.ATTACK;
import static wow.memory.CtmManager.CtmAction.FACE_TARGET;
import static wow.memory.CtmManager.CtmAction.LOOT;
import static wow.memory.CtmManager.CtmAction.MOVE_AND_INTERACT_NPC;
import static wow.memory.CtmManager.CtmAction.NOTHING;
import static wow.memory.CtmManager.CtmAction.SKIN;
import static wow.memory.CtmManager.CtmAction.WALK;


/**
 * @author Cargeh
 */
public final class CtmManager extends MemoryAware {

    private static final Logger logger = LoggerFactory.getLogger(CtmManager.class);

    private static final Address ctmX = Address.STATIC.CTM_X;
    private static final Address ctmY = Address.STATIC.CTM_Y;
    private static final Address ctmZ = Address.STATIC.CTM_Z;
    private static final Address ctmGuid = Address.STATIC.CTM_GUID;
    private static final Address ctmMystery = Address.STATIC.CTM_MYSTERY;
    private static final Address ctmPush = Address.STATIC.CTM_PUSH;

    private static final Address ctmMysteryNothing = Address.STATIC.CTM_MYSTERY_NOTHING;
    private static final Address ctmMysteryMoving = Address.STATIC.CTM_MYSTERY_MOVING;
    private static final Address ctmMysteryAttacking = Address.STATIC.CTM_MYSTERY_ATTACKING;
    private static final Address ctmMysteryInteractUnit = Address.STATIC.CTM_MYSTERY_INTERACT_UNIT;

    private static final int goToIterationCheck = 1; // ms
    private static final double allowedDistancePerGotoIteration = 0.05;

    private static final int maxStuckWaitCounter = 5000;
    private static final int maxStuckInRowBeforeExit = 500;
    private static Logger log = LoggerFactory.getLogger(CtmManager.class);
    private final ThreadLocal<Integer> stuckInRowCounter = new ThreadLocal<>();
    private final Player player;

    public CtmManager(WowMemory wowMemory) {
        super(wowMemory);
        this.player = wowMemory.getPlayer();
    }

    /**
     * Keeps going to the object until it's near it. Checks if the player is stuck every 1 sec,
     * and if he is, it increments the stuck counter. If stuckCounter > N, we exit with fatal error.
     * We also give our character N chances to get "unstuck", have a look at maxStuckWaitCounter.
     *
     * @return true if the player is near the object, false if the player got stuck in the process
     */
    public boolean goTo(WowObject object) { // TODO [beresnev]: Use char movement state to see if he's running
        return goTo(object, null);
    }

    public boolean goTo(
            WowObject object,
            WowInstance wowInstance,
            Point3D nextPoint,
            int level,
            boolean goToAsMelee) {
        int stuckCounter = 0;
        Coordinates2D initialPosition = Navigation.get2DCoordsFor(this.player);
        while (!Navigation.areNear(initialPosition, object, level, goToAsMelee)) {
            moveTo(object);
            if (nextPoint != null) {
                double distanceToObject = Navigation.evaluateDistanceFromTo(this.player.getCoordinates(), nextPoint);
                if (distanceToObject > 10000.0D) {
                    log.error("player was too far away from point! " + nextPoint + " " + object);
                    break;
                }
            }

            if (this.player.isDead()) {
                return false;
            }
            Utils.sleep(100L);
            if (this.isStuck(initialPosition, Navigation.get2DCoordsFor(this.player)) && stuckCounter++ >= 25) {
                this.incrementAndCheckStuckCounter();
                this.stop();
                return false;
            }

            initialPosition = Navigation.get2DCoordsFor(this.player);
        }
        stop();
        return true;
    }

    public boolean goTo(
            WowObject object,
            WowInstance wowInstance) {
        int stuckCounter = 0;
        Coordinates2D initialPosition = get2DCoordsFor(player);
        moveTo(object);
        while (!areNear(initialPosition, object)) { // check every second
            // TODO: fix, broke not bg movement (farmBot)
            if (!player.onBg()) {
                return false;
            }
            if (player.isDead()) {
                return false;
            }
            moveTo(object);
            Utils.sleep(goToIterationCheck);
            //log.info("goto(object) stuckCounter=" + stuckCounter + " maxStuckWaitCounter=" + maxStuckWaitCounter);
            if (isStuck(initialPosition, get2DCoordsFor(player))) {
                if (stuckCounter++ >= maxStuckWaitCounter) {
                    incrementAndCheckStuckCounter();
                    stop();
                    return false;
                }
            }
            initialPosition = get2DCoordsFor(player);
        }
        return true;
    }

    public boolean goTo(Point3D point, boolean stopIfCombat) { // TODO [beresnev]: Use char movement state to see if he's running
        int stuckCounter = 0;
        Coordinates2D initialPosition = get2DCoordsFor(player);
        Coordinates3D initialPosition3D = get3DCoordsFor(player);
        while (!isNear(initialPosition3D, point)) { // check every second
            if (stopIfCombat && player.isInCombat()) {
                // true, because we dont need do unstuck if we met red-mob
                logger.info("exit from goTo because player.isInCombat()");
                //stop();
                return true;
            }
            if (player.isDeadLyingDown()) {
                logger.info("exit from goTo because player.isDeadLyingDown()");
                return false;
            }
            moveTo(point);
            Utils.sleep(goToIterationCheck);
            //log.info("goto(Point) stuckCounter=" + stuckCounter + " maxStuckWaitCounter=" + maxStuckWaitCounter);
            if (isStuck(initialPosition, get2DCoordsFor(player))) {
                if (stuckCounter++ >= maxStuckWaitCounter) {
                    incrementAndCheckStuckCounter();
                    stop();
                    return false;
                }
            }
            initialPosition = get2DCoordsFor(player);
            initialPosition3D = get3DCoordsFor(player);
        }
        return true;
    }

    //TOOD: copy-paste from goTo, make 1 method with general areNearMethod and distance
    public boolean goToAsCaster(WowObject object) { // TODO [beresnev]: Use char movement state to see if he's running
        int stuckCounter = 0;
        Coordinates2D initialPosition = get2DCoordsFor(player);
        while (!areNearAsCaster(initialPosition, object)) {

            if (player.isDead()) {
                return false;
            }
            moveTo(object);
            Utils.sleep(goToIterationCheck);
            if (isStuck(initialPosition, get2DCoordsFor(player))) {
                if (stuckCounter++ >= maxStuckWaitCounter) {
                    incrementAndCheckStuckCounter();
                    stop();
                    return false;
                }
            }
            initialPosition = get2DCoordsFor(player);
            if (player.isInCombat()) {
                return false;
            }
        }
        stop();
        return true;
    }

    /**
     * The character actually moves ~47 "tiles" (lets call it that) per second, but we're looking
     * at the the value below that to compensate for small obstacles that you can get through
     *
     * @see Navigation#evaluateDistanceFromTo(Coordinates2D, Coordinates2D)
     */
    private boolean isStuck(
            Coordinates2D initialPosition,
            Coordinates2D currentPosition) {
        double distanceRun = evaluateDistanceFromTo(initialPosition, currentPosition);
        return distanceRun < allowedDistancePerGotoIteration;
    }

    private void incrementAndCheckStuckCounter() {
        Integer currentStuckCounter = stuckInRowCounter.get();
        if (currentStuckCounter == null) {
            currentStuckCounter = 0;
            stuckInRowCounter.set(0);
        }
        if (currentStuckCounter == maxStuckInRowBeforeExit) {
            //throw new RuntimeException("Character seems to be stuck.");
        } else {
            stuckInRowCounter.set(++currentStuckCounter);
        }
    }

    public void moveTo(WowObject object) {
        moveTo(object.getX(), object.getY(), object.getZ(), object.getGuid());
    }

    public void moveTo(Point3D point) {
        moveTo(point.getX(), point.getY(), point.getZ());
    }

    public void moveTo(
            double x,
            double y,
            double z) {
        moveTo((float) x, (float) y, (float) z, 0);
    }

    public void moveTo(
            float x,
            float y,
            float z) {
        moveTo(x, y, z, 0);
    }

    /**
     * Sends CTM to [WALK] to the coordinates.
     */
    private void moveTo(
            float x,
            float y,
            float z,
            long guid) {
        doAction(x, y, z, guid, ctmMysteryMoving, WALK);
    }

    public void attack(WowObject object) {
        doAction(object.getX(), object.getY(), object.getZ(), object.getGuid(), ctmMysteryAttacking, ATTACK);
    }

    /**
     * This just openes the loot window, but it doesn't actually pick up the loot.
     * For some reason it requires the ctmMysteryAttacking instead if interacting
     * <p>
     * Use the following script in game to actually take the loot once the window is open.
     * /script LootSlot(N)
     */
    public void loot(WowObject object) {
        doAction(object.getX(), object.getY(), object.getZ(), object.getGuid(), ctmMysteryAttacking, LOOT);
    }

    public void skinning(WowObject object) {
        doAction(object.getX(), object.getY(), object.getZ(), object.getGuid(), ctmMysteryAttacking, SKIN);
    }

    public void interactNpc(WowObject object) {
        doAction(object.getX(), object.getY(), object.getZ(), object.getGuid(), ctmMysteryInteractUnit, MOVE_AND_INTERACT_NPC);
    }

    public void face(WowObject object) {
        doAction(object.getGuid(), ctmMysteryAttacking, FACE_TARGET);
    }

    public void stop() {
        nothing();
    }

    public void nothing() {
        doAction(0, 0, 0, 0, ctmMysteryNothing, NOTHING);
    }

    public void doAction(CtmAction ctmAction) {
        Objects.requireNonNull(ctmAction);

        writeInt(ctmPush, ctmAction.value);
    }

    public void doAction(
            long guid,
            Address mysteryAction,
            CtmAction ctmAction) {
        Objects.requireNonNull(mysteryAction);
        Objects.requireNonNull(ctmAction);
        doAction(guid, (int) mysteryAction.getValue(), ctmAction.value);
    }

    public void doAction(
            long guid,
            int mystery,
            int action) {

        writeLong(ctmGuid, guid);
        writeInt(ctmMystery, mystery);
        writeInt(ctmPush, action);
    }

    public void doAction(
            float x,
            float y,
            float z,
            long guid,
            Address mysteryAction,
            CtmAction ctmAction) {
        Objects.requireNonNull(mysteryAction);
        Objects.requireNonNull(ctmAction);

        writeFloat(ctmX, x);
        writeFloat(ctmY, y);
        writeFloat(ctmZ, z);
        writeLong(ctmGuid, guid);
        writeInt(ctmMystery, (int) mysteryAction.getValue());
        writeInt(ctmPush, ctmAction.value);
    }

    public enum CtmAction {
        FACE_TARGET(0x1),
        STOP(0x3),
        WALK(0x4),
        MOVE_AND_INTERACT_NPC(0x5),
        LOOT(0x6),
        MOVE_AND_INTERACT_OBJECT(0x7),
        FACE_OTHER(0x8), // ?
        SKIN(0x9),
        ATTACK(0xA),
        NOTHING(0x13);

        private int value;

        CtmAction(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
