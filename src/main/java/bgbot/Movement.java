package bgbot;

import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Navigation;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.objects.Player;
import wow.navigation.Zones;

public class Movement {

    private static Logger log = LoggerFactory.getLogger(Movement.class);
    private Player player;
    private CtmManager ctmManager;
    private WowInstance wowInstance;
    private ObjectManager objectManager;

    public Movement(Player player, CtmManager ctmManager, WowInstance wowInstance, ObjectManager objectManager) {
        this.player = player;
        this.ctmManager = ctmManager;
        this.wowInstance = wowInstance;
        this.objectManager = objectManager;
    }

    public boolean goToNextPoint(Point3D nextPoint) {
        int count = 0;
        Zones.Zone zone = player.getZone();
        while (!Navigation.isNear(new Navigation.Coordinates3D(player.getX(), player.getY(), player.getZ()), nextPoint)) {
            // teleported
            if (!player.getZone().equals(zone)) {
                log.error("prev zone:{} != current:{}", zone, player.getZone());
                return false;
            }
            if (player.getZone().isShatrhCity()) {
                log.error("zone is shatr:{}", player.getZone());
                return false;
            }
            if (player.isInCombat() && !mobTargetingMe()) {
                // doesn't work for AV
                //return false;
            }
            if (player.isDead()) {
                return true;
            }
            double distance = player.getCoordinates().distance(nextPoint);

            boolean checkDistance = true;
            // for jump from bg
            if (player.getZone().isEye() || player.getZone().isWarsong()) {
                // TODO: merge logic of 2 bgs
                if (player.getZone().isEye()) {
                    if (player.getCoordinates().getZ() - nextPoint.getZ() > 30 && distance < 150) {
                        checkDistance = false;
                    }
                }
            }
            if (checkDistance && distance > 300) {
                log.info("the next point is too far away, break distance:" + distance);
                return false;
            }
            //travel form
            wowInstance.click(WinKey.D7);
            ++count;
            //log.info("count=" + count);
            if (count % 3 == 0) {
                System.out.println("can't go to the point, stop and sleep");
                System.out.println("player: " + player.getCoordinates());
                System.out.println("nextPoint: " + nextPoint);
                System.out.println("distance: " + distance);
                ctmManager.stop();
                castMount();
                return false;
            }
            if (player.isDead()) {
                break;
            }
            boolean success = ctmManager.goTo(nextPoint, false);
            if (success) {
                return true;
            }
        }
        ctmManager.stop();
        if (count > 0) {
            log.error("exit because count:{}", count);
            return false;
        }
        return true;
    }

    private void castMount() {
        log.info("try to cast mount");
        ctmManager.stop();
        Utils.sleep(1000L);
        wowInstance.click(WinKey.D0);
        Utils.sleep(3500L);
        log.info("mount caster, go to the point");
    }

    public void ress() {
        log.info("try ress character");
        Utils.sleep(3000);
    }

    public boolean mobTargetingMe() {
        /*
        objectManager.refillUnits();
        Map<Long, UnitObject> units = objectManager.getUnits();
        for(Map.Entry<Long,UnitObject> entry : units.entrySet()) {
            if (entry.getValue().isTargetingMe()) {
                log.info("found mob attacking me");
                return true;
            }
        }
        */
        return false;
    }
}
