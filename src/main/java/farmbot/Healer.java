package farmbot;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

public class Healer {

    private static final Logger logger = LoggerFactory.getLogger(Healer.class);
    private final Player player;
    private final WowInstance wowInstance;
    private long timeLastHeal = 0L;

    Healer(
        Player player,
        WowInstance wowInstance)
    {
        this.player = player;
        this.wowInstance = wowInstance;
    }

    public void heal(UnitObject target) {
        boolean isDead = player.isDead();
        if (!isDead) {
            if (System.currentTimeMillis() - timeLastHeal >= 12000L) {
                int healthPercent = player.getHealthPercent();
                int manaPercent = player.getManaPercent();
                boolean inCombat = player.isInCombat();
                if ((inCombat && healthPercent <= 51 || !inCombat && healthPercent < 65) && manaPercent > 35 && healthPercent > 5) {
                    List<UnitObject> mobsTargetingMe = wowInstance.getObjectManager().getMobsTargetingMe(true);
                    logger.info(mobsTargetingMe.size() + " targeting me");
                    int i;
                    if (mobsTargetingMe.size() == 1) {
                        i = (mobsTargetingMe.get(0)).getHealth();
                        logger.info("this mob have " + i + "% health, and mine is " + healthPercent);
                        if (i < healthPercent) {
                            logger.info("don't heal because we have only 1 enemy and his health is less then mine");
                            return;
                        }
                    }

                    if (target != null && target.getComboPoints() == 5) {
                        //in player-farm you have 100 energy
                        if (player.getEnergy() != 100) {
                            wowInstance.click(WinKey.D2);
                            logger.info("casted 5 combo before heal");
                            Utils.sleep(500L);
                        }
                    }

                    if (target != null && target.isDead()) {
                        return;
                    }

                    logger.info("healing at " + healthPercent + " % health");
                    //when you got stun and your hp didn't change - try again
                    castRegrowth();
                    Utils.sleep(50);
                    if (player.isInCombat() && player.getHealthPercent() < healthPercent) {
                        logger.info("##################### INSIDE COMBAT + player.getHealthPercent() < healthPercent");
                        Utils.sleep(4000);
                        if (player.getHealthPercent() < healthPercent) {
                            castRegrowth();
                        }
                    }

                    logger.info("casted D4");
                    if (inCombat && manaPercent > 40) {
                        logger.info("in combat and mana > 40, cast another heal D2");
                        for (i = 0; i < 5; ++i) {
                            Utils.sleep(100L);
                            wowInstance.click(WinKey.D2);
                        }
                    }

                    timeLastHeal = System.currentTimeMillis();
                    if (manaPercent < 80) {
                        logger.info("try cast innervate, may be on cd");

                        for (i = 0; i < 10; ++i) {
                            Utils.sleep(100L);
                            wowInstance.click(WinKey.D5);
                            wowInstance.click(WinKey.D3);
                        }
                    }

                    for (i = 0; i < 20; ++i) {
                        Utils.sleep(100L);
                        wowInstance.click(WinKey.D3);
                    }
                }
            }
        }
    }

    private void castRegrowth() {
        for (int j = 0; j < 10; j++) {
            Utils.sleep(100L);
            wowInstance.click(WinKey.D4);
            Utils.sleep(50);
        }
        while (player.isCasting()) {
            Utils.sleep(100L);
        }
    }

    public void makeBuffs() {
        logger.info("makeBuffs");
        wowInstance.click(WinKey.MINUS);
        Utils.sleep(2500L);
        wowInstance.click(WinKey.PLUS);
        Utils.sleep(2500L);
        wowInstance.click(WinKey.D3);
        Utils.sleep(2500L);
    }

    public void regenMana() {
        int mana = player.getManaPercent();
        int needMana = 60;
        if (player.getLevel() < 20) {
            needMana = 90;
        }
        while (mana < needMana) {
            if (player.isInCombat() || player.isDead()) {
                break;
            }
            logger.info("starting regen mana");
            logger.info("hp=" + player.getHealth() + " mana=" + mana + "%  wait until " + needMana + "%");
            for (int i = 0; i < 15; ++i) {
                Utils.sleep(1000L);
                mana = player.getManaPercent();
                boolean inCombat = player.isInCombat();
                boolean isDead = player.isDead();
                if (inCombat || isDead || mana > needMana) {
                    break;
                }
            }
            wowInstance.click(WinKey.D3);
        }
    }

    public long getTimeLastHeal() {
        return timeLastHeal;
    }
}
