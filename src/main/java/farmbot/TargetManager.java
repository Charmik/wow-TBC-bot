package farmbot;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import farmbot.Pathing.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.components.Navigation;
import wow.components.UnitReaction;
import wow.memory.ObjectManager;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

/**
 * @author alexlovkov
 */
public class TargetManager {

    private final static Logger logger = LoggerFactory.getLogger(TargetManager.class);

    private final Graph graph;
    private final Player player;
    private final ObjectManager objectManager;

    public TargetManager(
        Graph graph,
        Player player,
        ObjectManager objectManager)
    {
        this.graph = graph;
        this.player = player;
        this.objectManager = objectManager;
    }

    UnitObject getNearestMobForAttack() {
        List<UnitObject> mobs = objectManager.getUnits().values().stream().filter((unit) -> {
            if (Bot.isKillGrayMobs()) {
                return true;
            } else {
                int unitLevel = unit.getLevel();
                int playerLevel = player.getLevel();
                return unit.getHealth() == 100 && unitLevel <= playerLevel + 2 && !isGray(playerLevel, unitLevel);
            }
        })
            .filter((unit) -> !unit.getFaction().isAlliance() && !unit.getFaction().isHorde())
            .filter((unit) -> unit.getUnitReaction() != UnitReaction.FRIENDLY)
            .filter(graph::mobIsNearToTheGraph)
            .sorted(Comparator.comparingDouble((unit) -> Navigation.evaluateDistanceFromTo(player, unit)))
            .collect(Collectors.toList());

        if (player.getLevel() < 40) {
            List<UnitObject> hostileMobs = mobs.stream()
                .filter((e) -> e.getUnitReaction() == UnitReaction.HOSTILE)
                .collect(Collectors.toList());

            if (hostileMobs.size() > 0) {
                final double distanceToYellowMob = Navigation.evaluateDistanceFromTo(player, mobs.get(0));
                final double distanceToHostileMob = Navigation.evaluateDistanceFromTo(player, hostileMobs.get(0));
                if (distanceToHostileMob < distanceToYellowMob * 2) {
                    mobs = hostileMobs;
                }
            }
        }
        //TODO: return some mob for attacking
        for (UnitObject unit : mobs) {
            return unit;
        }
        return null;
    }

    public List<UnitObject> getMobsForAttack()
    {
        logger.info("getMobsForAttack somebody attacks us!");
        return objectManager.getMobsTargetingMe(true);
    }

    private boolean isGray(
        int playerLevel,
        int mobLevel)
    {
        int grayLevel;
        if (playerLevel >= 1 && playerLevel <= 5) {
            grayLevel = 0;
        } else if (playerLevel >= 6 && playerLevel <= 49) {
            grayLevel = playerLevel - playerLevel / 10 - 5;
        } else if (playerLevel == 50) {
            grayLevel = 40;
        } else if (playerLevel >= 51 && playerLevel <= 59) {
            grayLevel = playerLevel - playerLevel / 5 - 1;
        } else {
            if (playerLevel < 60 || playerLevel > 70) {
                throw new RuntimeException("playerLevel is not correct");
            }

            grayLevel = playerLevel - 9;
        }

        return mobLevel <= grayLevel;
    }
}
