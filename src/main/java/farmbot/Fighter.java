//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package farmbot;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import farmbot.Pathing.Graph;
import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

public class Fighter {
    private static final Logger logger = LoggerFactory.getLogger(Fighter.class);
    private final Player player;
    private final ObjectManager objectManager;
    private final CtmManager ctmManager;
    private final WowInstance wowInstance;
    private final Healer healer;
    private Graph graph;
    private TargetManager targetManager;

    Fighter(
        Player player,
        ObjectManager objectManager,
        CtmManager ctmManager,
        WowInstance wowInstance,
        Healer healer,
        Graph graph,
        TargetManager targetManager)
    {
        this.player = player;
        this.objectManager = objectManager;
        this.ctmManager = ctmManager;
        this.wowInstance = wowInstance;
        this.healer = healer;
        this.graph = graph;
        this.targetManager = targetManager;
    }

    void kill(
        Point3D nextPoint,
        UnitObject target)
    {
        logger.info("findNearestMobAndAttack started");
        Fighter.State state = new Fighter.State(player, target);
        kill(nextPoint, state);
    }

    private void kill(
        Point3D nextPoint,
        Fighter.State state)
    {
        healer.heal(state.getTarget());
        wowInstance.click(WinKey.D3);
        if (state.updateTargetHealth() != 0) {
            int cnt = -1;
            Set<UnitObject> unitsForLoot = new HashSet<>();
            int prevHealth = -1;
            int countNotSuccesGoToMob = 0;
            state.updateInCombat();

            for (; state.getTargetHealth() > 90 || state.isInCombat() || state.getTargetHealth() > 0; Utils.sleep(10L)) {
                state.updateInCombat();
                if (state.isInCombat()
                    && !state.target.isTargetingMe()) {
                    logger.info("exit from kill, because another mob attacked me");
                    ctmManager.stop();
                    break;
                }

                ++cnt;
                if (cnt % 100 == 0) {
                    state.updateComboPoints();
                    state.updateEnergy();
                }

                if (cnt % 500 == 0 && state.updateIsDead()) {
                    logger.info("died, targetHealth=" + state.getTargetHealth());
                    break;
                }

                if (cnt % 15 == 0) {
                    healer.heal(state.getTarget());
                }

                if (cnt % 20 == 0) {
                    state.updatePlayerTarget();
                }

                if (cnt % 300 == 0) {
                    System.out.println("face to target");
                    ctmManager.face(state.getTarget());
                    Utils.sleep(100);
                }

                if (cnt < 100 && cnt % 25 == 0 || cnt % 100 == 0) {
                    boolean success = Looter.goTo(state.getTarget(), nextPoint);
                    if (player.getLevel() < 20 && cnt % 200 == 0) {
                        ctmManager.face(state.getTarget());
                    }

                    if (!success) {
                        logger.info("not success go to mob");
                        ++countNotSuccesGoToMob;
                        logger.info("countNotSuccessGoToMob=" + countNotSuccesGoToMob);
                    }
                }

                if (countNotSuccesGoToMob > 7) {
                    logger.info("quit from findNearestMobAndAttack because countNotSuccesGoToMob=" + countNotSuccesGoToMob);

                    break;
                }

                if (cnt % 1000 == 0) {
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
                    findNextMobInFight(state);
                }

                unitsForLoot.add(state.getTarget());
                if (cnt % 50 == 0) {
                    state.updateTargetHealth();
                    castSpell(state.getTargetHealth(), state.getComboPoints(), state.getEnergy(), cnt % 300 == 0);
                }
            }

            if (!player.isDead()) {
                Looter.getLoot(unitsForLoot);
            }

        }
    }

    private void findNextMobInFight(Fighter.State state) {
        logger.info("choose target who attacks me");
        if (state.isInCombat()) {
            List<UnitObject> mobsForAttack = targetManager.getMobsForAttack();
            if (mobsForAttack != null && mobsForAttack.size() > 0) {
                logger.info("found mob in findNearestMobAndAttack");
                Optional<UnitObject> minHealthUnit = mobsForAttack.stream().filter(CreatureObject::isTargetingMe).min(Comparator.comparingInt(UnitObject::getHealth));
                minHealthUnit.ifPresent(state::setTarget);
            }
        }
    }

    public void killListOfMobs(List<UnitObject> enemies) {
        Point3D nearestPointToPlayer = graph.getNearestPointTo(player).getKey();
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
