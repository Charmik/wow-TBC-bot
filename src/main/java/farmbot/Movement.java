package farmbot;

import java.util.List;
import java.util.Random;

import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Navigation;
import wow.components.Navigation.Coordinates3D;
import wow.memory.CtmManager;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

class Movement {

    private static final Logger logger = LoggerFactory.getLogger(Movement.class);
    private final Player player;
    private final Random random;
    private final Healer healer;
    private final CtmManager ctmManager;
    private final WowInstance wowInstance;
    private final Fighter fighter;
    private TargetManager targetManager;
    private Point3D corpseCoordinate;

    Movement(
        Player player,
        Healer healer,
        CtmManager ctmManager,
        WowInstance wowInstance,
        Fighter fighter,
        TargetManager targetManager)
    {
        this.player = player;
        this.healer = healer;
        this.ctmManager = ctmManager;
        this.wowInstance = wowInstance;
        this.fighter = fighter;
        this.targetManager = targetManager;
        this.random = new Random();
        this.corpseCoordinate = null;
    }

    boolean goToNextPoint(Point3D nextPoint) {
        int count = 0;
        while (!Navigation.isNear(new Coordinates3D(player.getX(), player.getY(), player.getZ()), nextPoint)) {
            ++count;
            if (System.currentTimeMillis() - healer.getTimeLastHeal() >= 6000L) {
                healer.regenMana();
            }
            healer.heal(null);
            if (!player.isDead()) {
                if (player.isInCombat()) {
                    List<UnitObject> enemies = targetManager.getMobsForAttack();
                    fighter.killListOfMobs(enemies);
                    break;
                }
            } else if (ress()) {
                logger.info("break from loop in movement, because we released a spirit");
                return true;
            }
            boolean success = ctmManager.goTo(nextPoint, true);
            if (!success && count % 2 == 0) {
                logger.error("can't reach next point=" + nextPoint);
                tryUnstuck();
            }
            if (count % 4 == 0) {
                return false;
            }
        }
        return false;
    }

    private void tryUnstuck() {
        logger.info("character seems to be stucked, so try to make random movements (jump forward -> go back -> left/right)");
        wowInstance.keyDown(WinKey.W);
        int iterations = random.nextInt(10);
        for (int i = 0; i < iterations; ++i) {
            wowInstance.click(WinKey.SPACEBAR);
            Utils.sleep(500L);
            if (player.isInCombat()) {
                break;
            }
        }
        wowInstance.keyUp(WinKey.W);
        long timeToRun = random.nextInt(5000);
        if (!pressButtonAndCheckCombat(WinKey.S, timeToRun)) {
            WinKey button = WinKey.Q;
            if (random.nextBoolean()) {
                button = WinKey.E;
            }
            if (!player.isInCombat()) {
                pressButtonAndCheckCombat(button, timeToRun);
                logger.info("stop try unstuck");
            }
        }
    }

    private boolean pressButtonAndCheckCombat(
        WinKey winKey,
        long time)
    {
        boolean isCombat = false;
        wowInstance.keyDown(winKey);
        for (int i = 0; (long) i < time / 1000L; ++i) {
            Utils.sleep(1000L);
            if (player.isInCombat()) {
                isCombat = true;
            }
        }
        wowInstance.keyUp(winKey);
        return isCombat;
    }

    boolean ress() {
        //logger.info("try ress()");
        if (player.isDeadLyingDown()) {
            logger.info("player isDeadLyingDown");
            if (corpseCoordinate == null) {
                corpseCoordinate = player.getCoordinates();
            }
            Utils.sleep(100L);
            wowInstance.click(WinKey.D9, 0L);
            Utils.sleep(500L);
            logger.error("corpseCoordinate=" + corpseCoordinate);
            return true;
        } else {
            if (player.isSpirit()) {
                //logger.info("player is spirit");
                wowInstance.click(WinKey.D0, 0L);
            }
            return false;
        }
    }

    public Point3D getCorpseCoordinate() {
        return corpseCoordinate;
    }

    public void resetCorpseCoordinate() {
        corpseCoordinate = null;
    }
}
