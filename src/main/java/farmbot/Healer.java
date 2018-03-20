package farmbot;

import java.util.List;
import java.util.Optional;

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

    public Healer(
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
                List<UnitObject> mobsTargetingMe = wowInstance.getObjectManager().getMobsTargetingMe(true);
                int WHEN_HEAL_IN_FIGHT = 50;
                if (player.getLevel() < 65 && mobsTargetingMe.size() == 1) {
                    WHEN_HEAL_IN_FIGHT = 40;
                }
                if ((inCombat && healthPercent <= WHEN_HEAL_IN_FIGHT || !inCombat && healthPercent < 65) && manaPercent > 35 && healthPercent > 5) {
                    logger.info(mobsTargetingMe.size() + " targeting me");
                    int targetHealth;
                    if (mobsTargetingMe.size() == 1) {
                        targetHealth = (mobsTargetingMe.get(0)).getHealth();
                        logger.info("this mob have " + targetHealth + "% health, and mine is " + healthPercent);
                        if (targetHealth < healthPercent) {
                            logger.info("don't heal because we have only 1 enemy and his health is less then mine");
                            return;
                        }
                    } else {
                        Optional<Integer> min = mobsTargetingMe.stream().map(UnitObject::getHealth).min(Integer::compare);
                        if (min.isPresent() && min.get() < 5 && healthPercent > 25) {
                            logger.info("exit from heal, because 1 mob has less than 5% hp, kill it and then heal");
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
                    Utils.sleep(2000);
                    int EPSILON_HEALTH = 50;
                    logger.info("player.getHealthPercent()={} , healthPercent={}", player.getHealthPercent(), healthPercent);
                    if (player.isInCombat() && player.getHealthPercent() + EPSILON_HEALTH < healthPercent) {
                        logger.info("##################### INSIDE COMBAT + player.getHealthPercent() < healthPercent");
                        Utils.sleep(4000);
                        if (player.getHealthPercent() + EPSILON_HEALTH < healthPercent) {
                            castRegrowth();
                        }
                    }

                    logger.info("casted D4");
                    int MANA_WHEN_CAST_REJUVENATION = 40;
                    if (player.getLevel() > 50) {
                        MANA_WHEN_CAST_REJUVENATION = 30;
                    }
                    if (inCombat && manaPercent > MANA_WHEN_CAST_REJUVENATION) {
                        logger.info("in combat and mana > 40, cast another heal D2");
                        for (targetHealth = 0; targetHealth < 5; ++targetHealth) {
                            Utils.sleep(100L);
                            wowInstance.click(WinKey.D2);
                        }
                    }

                    timeLastHeal = System.currentTimeMillis();
                    if (manaPercent < 80) {
                        logger.info("try cast innervate, may be on cd");

                        for (targetHealth = 0; targetHealth < 10; ++targetHealth) {
                            Utils.sleep(100L);
                            wowInstance.click(WinKey.D5);
                            wowInstance.click(WinKey.D3);
                        }
                    }

                    for (targetHealth = 0; targetHealth < 20; ++targetHealth) {
                        Utils.sleep(100L);
                        wowInstance.click(WinKey.D3);
                    }
                }
            }
        }
    }

    private void castRegrowth() {
        long startCast = System.currentTimeMillis();
        for (int j = 0; j < 10; j++) {
            Utils.sleep(30L);
            wowInstance.click(WinKey.D4);
            Utils.sleep(30);
        }
        while (player.isCasting()) {
            Utils.sleep(100L);
        }
        logger.info("castRegrowth, time passed:" + (System.currentTimeMillis() - startCast));
    }

    public void makeBuffs() {
        logger.info("makeBuffs");
        //mark
        wowInstance.click(WinKey.MINUS);
        Utils.sleep(2500L);
        //omen
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
                wowInstance.click(WinKey.D5);
            }
            heal(null);
            wowInstance.click(WinKey.D3);
        }
    }

    public long getTimeLastHeal() {
        return timeLastHeal;
    }
}
