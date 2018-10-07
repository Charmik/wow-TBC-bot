package farmbot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Coordinates;
import wow.memory.CtmManager;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

/**
 * @author alexlovkov
 */
public class Looter {

    private static final Logger logger = LoggerFactory.getLogger(Looter.class);
    public static Player player;
    public static CtmManager ctmManager;
    public static WowInstance wowInstance;

    public static void configure(
        Player player,
        CtmManager ctmManager,
        WowInstance wowInstance)
    {

        Looter.player = player;
        Looter.ctmManager = ctmManager;
        Looter.wowInstance = wowInstance;
    }

    public static void getLoot(Set<UnitObject> unitsForLoot)
    {
        logger.info("units for loot:  " + unitsForLoot);
        ArrayList<UnitObject> list = new ArrayList<>(unitsForLoot);
        Collections.reverse(list);
        logger.info("looting mobs, count=" + list.size());
        for (UnitObject unit : list) {
            getLoot(unit);
        }
    }

    private static void getLoot(UnitObject unit) {
        logger.info("getLoot");
        goTo(unit, null, false);
        ctmManager.loot(unit);
        Utils.sleep(500L);
        wowInstance.click(WinKey.D6);
        Utils.sleep(500L);
        wowInstance.click(WinKey.W);
        wowInstance.click(WinKey.D6);
        Utils.sleep(500L);
        wowInstance.click(WinKey.D6);
        Utils.sleep(500L);
        ctmManager.skinning(unit);
        Utils.sleep(500L);
        wowInstance.click(WinKey.W);
        Utils.sleep(1700L);
        wowInstance.click(WinKey.D6);
        wowInstance.click(WinKey.D7);
        //wowInstance.click(WinKey.W);
    }

    //TODO: move to Movement
    public static boolean goTo(
        UnitObject unitObject,
        Coordinates nextPoint,
        boolean goToAsMelee)
    {
        if (player.getLevel() < 20) {
            return ctmManager.goToAsCaster(unitObject);
        } else {
            return ctmManager.goTo(unitObject, wowInstance, nextPoint, player.getLevel(), goToAsMelee);
        }
    }
}
