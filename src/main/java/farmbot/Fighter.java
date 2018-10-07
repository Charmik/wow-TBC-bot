package farmbot;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import farmbot.Pathing.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Coordinates;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

public class Fighter {
    private static final Logger logger = LoggerFactory.getLogger(Fighter.class);
    private final Player player;
    private final CtmManager ctmManager;
    private final WowInstance wowInstance;
    private final Healer healer;
    private Graph graph;
    private TargetManager targetManager;
    private ObjectManager objectManager;

    Fighter(
        Player player,
        CtmManager ctmManager,
        WowInstance wowInstance,
        Healer healer,
        Graph graph,
        TargetManager targetManager,
        ObjectManager objectManager)
    {
        this.player = player;
        this.ctmManager = ctmManager;
        this.wowInstance = wowInstance;
        this.healer = healer;
        this.graph = graph;
        this.targetManager = targetManager;
        this.objectManager = objectManager;
    }

    void kill(
        Coordinates nextPoint,
        UnitObject target)
    {
        logger.info("findNearestMobAndAttack started");
        Fighter.State state = new Fighter.State(player, target);
        kill(nextPoint, state);
    }

    private void kill(
        Coordinates nextPoint,
        Fighter.State state)
    {
        healer.heal(state.getTarget());
        wowInstance.click(WinKey.D3);
        int initUnitMana = state.getTarget().getMana();
        boolean wentAsMeele = false;
        //if (state.updateTargetHealth() != 0) {
            int cnt = -1;
            Set<UnitObject> unitsForLoot = new HashSet<>();
            int prevHealth = -1;
            int countNotSuccesGoToMob = 0;
            state.updateInCombat();
            state.updatePlayerTarget();
            state.updateTargetHealth();
            for (; state.getTargetHealth() > 90 || state.isInCombat() || state.getTargetHealth() > 0; Utils.sleep(10L)) {
                ++cnt;
                state.updateInCombat();
                if (state.isInCombat()
                    && !state.target.isTargetingMe()) {
//                    logger.info("exit from kill, because another mob attacked me");
//                    ctmManager.stop();
//                    logger.info("break1");
//                    break;
                }
                //it means mob casting something, maybe he is away from us
                //if (state.getTarget().getMana() < initUnitMana && !wentAsMeele) {
                if (initUnitMana > 0) {
                    logger.info("mana of unit changed, go to attack it as melee");
                    Looter.goTo(state.getTarget(), nextPoint, true);
                    wentAsMeele = true;
                }
                if (cnt % 100 == 0) {
                    state.updateComboPoints();
                    state.updateEnergy();
                }
                if (cnt % 500 == 0 && state.updateIsDead()) {
                    logger.info("died, targetHealth=" + state.getTargetHealth());
                    break;
                }
                if (cnt % 5 == 0) {
                    healer.heal(state.getTarget());
                    //because now we have player as a target
                    findNextMobInFight(state);
                }
                if (cnt % 20 == 0) {
                    state.updatePlayerTarget();
                }
                if (cnt % 50 == 0) {
                    logger.info("face to target health:" + state.getTarget().getHealth() + " level:" + state.getTarget().getLevel());
                    ctmManager.face(state.getTarget());
                    Utils.sleep(100);
                }
                if (cnt < 100 && cnt % 25 == 0 || cnt % 100 == 0) {
//                    boolean success = Looter.goTo(state.getTarget(), nextPoint, state.getPlayer().getHealthPercent() < 100);
                    boolean success = Looter.goTo(state.getTarget(), nextPoint, true);
                    if (player.getLevel() < 20 && cnt % 200 == 0) {
                        ctmManager.face(state.getTarget());
                    }
                    if (!success) {
                        logger.info("not success go to mob");
                        ++countNotSuccesGoToMob;
                        logger.info("countNotSuccessGoToMob=" + countNotSuccesGoToMob);
                    }
                }
                if (countNotSuccesGoToMob > 3) {
                    logger.info("quit from findNearestMobAndAttack because countNotSuccesGoToMob=" + countNotSuccesGoToMob);
                    break;
                }
                if (cnt % 2000 == 0) {
                    if (state.getTargetHealth() == prevHealth) {
                        logger.info("quit from findNearestMobAndAttack because target's hp doesn't change");
                        wowInstance.click(WinKey.S, 10000L);
                        break;
                    }
                    prevHealth = state.getTargetHealth();
                }
                if (cnt % 20 == 0) {
                    state.updateInCombat();
                }
                if (cnt % 300 == 0) {
                    objectManager.refillUnits();
                    findNextMobInFight(state);
                    state.updatePlayerTarget();
                }
                unitsForLoot.add(state.getTarget());
                if (cnt % 25 == 0) {
                    state.updateTargetHealth();
                    castSpell(state.getTargetHealth(), state.getComboPoints(), state.getEnergy(), cnt % 300 == 0);
                }
            }
            if (!player.isDead()) {
                Looter.getLoot(unitsForLoot);
            }
        //}
    }

    private void findNextMobInFight(Fighter.State state) {
        if (state.isInCombat()) {
            logger.info("choose target who attacks me");
            List<UnitObject> mobsForAttack = targetManager.getMobsForAttack();
            if (mobsForAttack != null && mobsForAttack.size() > 0) {
                logger.info("found mob in findNearestMobAndAttack");
                Optional<UnitObject> minHealthUnit = mobsForAttack.stream().filter(CreatureObject::isTargetingMe).min(Comparator.comparingInt(UnitObject::getHealth));
                minHealthUnit.ifPresent(state::setTarget);
            }
        }
    }

    public void killListOfMobs(List<UnitObject> enemies) {
        Coordinates nearestPointToPlayer = graph.getNearestPointTo(player).getKey();
        int healthPercent = player.getHealthPercent();
        int manaPercent = player.getManaPercent();
        logger.info("killListOfMobs, my Health is:" + healthPercent + " mana:" + manaPercent);
        //we respawned and mobs attacked us!
        if (healthPercent <= 50 && healthPercent >= 47 && manaPercent >= 50 && manaPercent <= 55 && enemies.size() > 0) {
            logger.info("player.getHealthPercent() <= 50, so let's heal before fight, enemies.size()={}", enemies.size());
            healer.forceHeal(healthPercent, manaPercent, true);
        }
        for (UnitObject unit : enemies) {
            logger.info("found nearest unit=" + unit + " for attack, going to kill!");
            kill(nearestPointToPlayer, unit);
        }
    }

    private void castSpell(
        int targetHealth,
        int comboPoints,
        int energy,
        boolean castFaerieFire)
    {
        if (energy >= 35) {
            if (comboPoints == 5) {
                if (targetHealth > 30 && energy < 80) {
                    return;
                }
                wowInstance.click(WinKey.D2);
                wowInstance.click(WinKey.D3);
            } else {
                wowInstance.click(WinKey.D3);
            }
        }

        if (castFaerieFire) {
            logger.info("cast D1 - FF");
            wowInstance.click(WinKey.D1);
        }

    }

    public static class State {
        private final Player player;
        private UnitObject target;
        private boolean inCombat;
        private int energy;
        private boolean isDead;
        private int comboPoints;
        private int targetHealth;

        State(
            Player player,
            UnitObject target)
        {
            this.player = player;
            this.target = target;
        }

        public Player getPlayer() {
            return player;
        }

        public UnitObject getTarget() {
            return target;
        }

        public void setTarget(UnitObject target) {
            this.target = target;
        }

        boolean isInCombat() {
            return inCombat;
        }

        int getEnergy() {
            return energy;
        }

        public boolean isDead() {
            return isDead;
        }

        int getComboPoints() {
            return comboPoints;
        }

        int getTargetHealth() {
            return targetHealth;
        }

        void updatePlayerTarget() {
            player.target(target);
        }

        boolean updateInCombat() {
            inCombat = player.isInCombat();
            return inCombat;
        }

        int updateEnergy() {
            energy = player.getEnergy();
            return energy;
        }

        boolean updateIsDead() {
            isDead = player.isDead();
            return isDead;
        }

        int updateComboPoints() {
            comboPoints = target.getComboPoints();
            return comboPoints;
        }

        int updateTargetHealth() {
            targetHealth = target.getHealth();
            return targetHealth;
        }
    }
}
