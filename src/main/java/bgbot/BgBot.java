package bgbot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import farmbot.Pathing.GlobalGraph;
import farmbot.Pathing.Graph;
import healbot.HealBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Coordinates;
import wow.components.Navigation;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.objects.Player;
import wow.memory.objects.PlayerObject;
import wow.memory.objects.UnitObject;

public class BgBot {

    private static Logger log = LoggerFactory.getLogger(BgBot.class);
    private static final int SLEEP_BETWEEN_BG_MINUTES = 3;
    private static final int MAX_ATTEMPTS_FOR_REG_BG = 30;
    private static final int BG_COUNT = 50;
    private static final String ROUTES_BG_FOLDER = "routesBG";
    private static final String[] PATHS = {
            ROUTES_BG_FOLDER + File.separator + "WSG",
            ROUTES_BG_FOLDER + File.separator + "AV",
            ROUTES_BG_FOLDER + File.separator + "AB",
            ROUTES_BG_FOLDER + File.separator + "EYE"};

    private WowInstance wowInstance = new WowInstance("World of Warcraft");
    private CtmManager ctmManager;
    private Player player;
    private ObjectManager objectManager;
    private HealBot healBot;
    private bgbot.Movement movement;
    private GlobalGraph globalGraph = new GlobalGraph("routesBG" + File.separator + "EYE");
    //private GlobalGraph globalGraph = new GlobalGraph(PATHS);
    private Coordinates lastPoint;
    private long lastTimestamp;
    private int globalCount = -1;
    private final int HORDE_AV_BOSS = -1320;
    private final Random random = new Random();
    private long startBgTimestamp;

    public BgBot() {
        reset();
    }

    private void reset() {
        this.ctmManager = wowInstance.getCtmManager();
        this.player = wowInstance.getPlayer();
        this.objectManager = wowInstance.getObjectManager();
        this.healBot = new HealBot();
        this.movement = new Movement(player, ctmManager, wowInstance, objectManager);

        this.lastPoint = new Coordinates(0, 0, 0);
        this.lastTimestamp = 0;
        this.startBgTimestamp = 0;
    }

    public static void main(String[] args) {
        BgBot bgBot = new BgBot();
        bgBot.run();
    }

    private void run() {
        getPaths();

        for (int i = 0; i < BG_COUNT; ++i) {
            try {
                player.updatePlayer();
                log.info("step={} from:{}", i, BG_COUNT);
                regBg();
                player.updatePlayer();

                this.startBgTimestamp = System.currentTimeMillis();
                while (player.onBg()) {
                    goRandomPointOnBg();
                }
                log.info("exit from bg");
                ctmManager.stop();

                player.updatePlayer();
                int sleepTime = 60 * 1000;
                for (int j = 0; j < SLEEP_BETWEEN_BG_MINUTES; j++) {
                    log.info("index={} from {}, sleep between bgs:{} ms", j, SLEEP_BETWEEN_BG_MINUTES, sleepTime);
                    Utils.sleep(sleepTime);
                }
                player.updatePlayer();
            } catch (Throwable e) {
                int sleepingTime = 10000;
                log.info("got exception sleeping for:{} ms", sleepingTime, e);
                Utils.sleep(sleepingTime);
                reset();
            }
        }
    }

    private void goRandomPointOnBg() {
        Coordinates randomPointFromGraph = globalGraph.getRandomPointFromGraph();
        // random always goes to horde base
        if (player.getZone().isAlterac()) {
            while (randomPointFromGraph.getX() > player.getX()) {
                randomPointFromGraph = globalGraph.getRandomPointFromGraph();
            }
            log.info("got random point:{} player coordinastes:{}", randomPointFromGraph, player.getCoordinates());
        }
        Coordinates destination = getMiddlePlayersPoint(randomPointFromGraph);

        boolean isRandomPoint = false;
        if (destination.equals(randomPointFromGraph)) {
            isRandomPoint = true;
        }
        // base of alterac, at start of bg we need just go to south
        if (destination.getX() > 700 && player.getZone().isAlterac()) {
            destination = randomPointFromGraph;
        }
        log.info("destination:" + destination + " player:" + player.getCoordinates());
        List<Graph.Vertex> path = globalGraph.getShortestPathFromPlayerToPoint(player, destination);
        //playing trying to go north

        log.info("found path:" + path.size());
        int countForMoonfire = 0;
        int last = 100;
        if (!player.getZone().isAlterac()) {
            last = 30;
        }

        if (path.size() < last) {
            last = path.size();
        }
        if (isRandomPoint) {
            last = 25;
        }
        path = path.subList(0, last);
        /*
        if (path.size() < 5) {
            log.info("path is not long enough, we are near to alliance, stay for a while");
            castMount();
            for (int i = 0; i < 3; i++) {
                if (healBot.makeOnePlayerHeal()) {
                    break;
                }
                Utils.sleep(1000);
            }
            castMount();
            return;
        }
        */

        for (Graph.Vertex vertex : path) {
            // check that we stay in one place, so bg is finished
            // understanding that's bg is over and we stay afk in one place
            // TODO: need more conditions
            if (System.currentTimeMillis() - lastTimestamp > 30 * 1000) {
                objectManager.refillPlayers();
                int size = objectManager.getPlayers().size();
                log.info("checking end of the bg current:" + player.getCoordinates() +
                        " prev:" + lastPoint + " playerSize:" + size);
                lastTimestamp = System.currentTimeMillis();
                if (player.getCoordinates().equals(lastPoint) && size <= 3) {
                    Coordinates coordinates = player.getCoordinates().add(5, 5, 5);
                    //boolean success = movement.goToNextPoint(coordinates);
                    //if (!success) {
                    log.info("we were stuck in one place more than 1 minute, sleeping...");
                    ctmManager.stop();
                    Utils.sleep(2 * 60 * 1000);
                    break;
//                    } else {
//                        log.info("we thought that it's eng of the bg, but we could go to the point:{} player's coordinates:{}",
//                                coordinates, player.getCoordinates());
//                    }
                }
            }
            lastPoint = player.getCoordinates();
            globalCount++;
            if (globalCount % 100 == 0) {
                castMount();
            }
            countForMoonfire++;
            if (countForMoonfire % 5 == 0) {
                castRandomSpellToNearestEnemy();
            }
            boolean wasDead = false;
            while (player.isDead()) {
                player.updatePlayer();
                log.info("playerIsDead");
                Utils.sleep(3000L);
                movement.ress();
                ctmManager.stop();
                wasDead = true;
                countForMoonfire = -1;
            }
            if (wasDead) {
                log.info("player was dead, so break from cycle");
                break;
            }
            // TODO: check target is not line of sight
            boolean healed = healBot.makeOnePlayerHeal();
            if (healed) {
                ctmManager.stop();
                for (int i = 0; i < 5; i++) {
                    if (player.getHealthPercent() < 75 && player.isInCombat()) {
                        // barkskin
                        castInstantSkill(WinKey.X);
                    }
                    if (!healBot.makeOnePlayerHeal()) {
                        // grasp
                        castInstantSkill(WinKey.H);
                        break;
                    }
                    if (player.getHealthPercent() < 15 && player.isInCombat()) {
                        // instant heal yourself + battlemaster
                        castInstantSkill(WinKey.D6);
                    }
                    Utils.sleep(1500);
                }
                Utils.sleep(50);
                log.info("cast moonfire because we are in combat");
                castRandomSpellToNearestEnemy();
            }

            player.updatePlayer();
            if (!player.onBg()) {
                log.info("player not on bg");
                ctmManager.stop();
                break;
            }
            if (player.getManaPercent() < 10 && !player.isDead()) {
                castInnervate();
            }
//            if (destination.getX() < player.getCoordinates().getX() || player.getCoordinates().getX() < HORDE_AV_BOSS) {
            if (!healBot.makeOnePlayerHeal()) {
                if (!movement.goToNextPoint(vertex.coordinates)) {
                    // start bg
                    if (System.currentTimeMillis() - startBgTimestamp < 180 * 1000) {
                        Utils.sleep(10 * 1000);
                    } else {
                        // TODO: try unstuck 1 time maybe?
                        log.info("bot couldn't go to the point:{} from:{}", vertex.coordinates, player.getCoordinates());
                        log.info("sleeping, because it's more safer for now");
                        // you will be kicked from BG for afk 5 min -> queue next.
                        for (int i = 0; i < 5 * 60 + 10; i++) {
                            Utils.sleep(1000);
                        /*
                        if (player.isDead()) {
                            log.info("break from sleeping because someone killed us");
                            break;
                        }
                        */
                        }
                        break;
                    }
                }
            }
        }
        if (path.size() < 10) {
            log.info("we went too small, we are around allies, sleep");
            castMount();
            // TODO: check that if we need go more than X range - break
            for (int i = 0; i < 5; i++) {
                if (healBot.makeOnePlayerHeal()) {
                    break;
                }
                Utils.sleep(1000);
            }
            castMount();
        }
    }

    private void castInstantSkill(WinKey key) {
        for (int j = 0; j < 5; j++) {
            wowInstance.click(key);
        }
    }

    // TODO: take first group who is near to player with players > 3
    private Coordinates getMiddlePlayersPoint(Coordinates randomPoint) {
        objectManager.refillPlayers();
        List<PlayerObject> ally = objectManager.getPlayers().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getFaction().isAlliance())
                .filter(e -> !e.isDead())
                .collect(Collectors.toList());
        Coordinates coordinates = new Coordinates(0, 0, 0);
        int size = ally.size();
        boolean[][] near = new boolean[size][size];
        for (int i = 0; i < ally.size(); i++) {
            for (int j = 0; j < ally.size(); j++) {
                if (ally.get(i).getCoordinates().distance(ally.get(j).getCoordinates()) < 100 &&
                        !ally.get(i).isDead() && !ally.get(j).isDead()) {
                    near[i][j] = true;
                    near[j][i] = true;
                }
            }
        }
        int max = -1;
        int index = -1;
        for (int i = 0; i < ally.size(); i++) {
            int s = 0;
            for (int j = 0; j < ally.size(); j++) {
                if (near[i][j]) {
                    s++;
                }
            }
            if (s > max) {
                max = s;
                index = i;
            }
        }
        List<PlayerObject> biggestCluster = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (near[index][i]) {
                biggestCluster.add(ally.get(i));
            }
        }
        ally = biggestCluster;
        size = ally.size();
        for (PlayerObject player : ally) {
            coordinates = new Coordinates(coordinates.getX() + player.getX(), coordinates.getY() + player.getY(), coordinates.getZ() + player.getZ());
        }
        log.info("we found {} ally, their coordinates:\n{}", ally.size(), ally.stream().map(e -> e.getCoordinates() + "\n").toArray());
        log.info("sizePlayer=" + size);
        coordinates = new Coordinates(coordinates.getX() / size, coordinates.getY() / size, coordinates.getZ() / size);

        if (player.getZone().isAlterac() && size < 4) {
            log.info("didn't find group or players, so go to random point:{}", randomPoint);
            return randomPoint;
        }
        return coordinates;
    }

    private void castInnervate() {
        Utils.sleep(500L);
        for (int i = 0; i < 5; ++i) {
            wowInstance.click(WinKey.F3);
            Utils.sleep(100);
        }
    }

    private void castRandomSpellToNearestEnemy() {
        objectManager.refillPlayers();
        objectManager.getPlayers().remove(player.getGuid());
        Optional<PlayerObject> nearestPlayerTo = objectManager.getNearestPlayerTo(player);
        if (nearestPlayerTo.isPresent()) {
            PlayerObject unitObject = nearestPlayerTo.get();
            double distance = Navigation.evaluateDistanceFromTo(player, unitObject);
            boolean isHorde = unitObject.getFaction().isHorde();
            if (isHorde && distance < 1300.0D) {
//                log.info("face to unit and cast moonfire");
                player.target(unitObject);
                // it's too obvious, that it's bot, maybe try to turn on not so hard somehow
                //ctmManager.face(unitObject);
                if (random.nextBoolean()) {
                    wowInstance.click(WinKey.PLUS);
                } else {
                    ctmManager.stop();
                    castInstantSkill(WinKey.F1);
                }
                Utils.sleep(1500);
            }
        }

    }

    private void castMount() {
        //sleep 1000, D0, isCasting -> sleep(3500)
//        log.info("try to cast mount");
        if ("toppriest".equals(player.getAccountName().toLowerCase())) {
            return;
        }
        if (healBot.makeOnePlayerHeal()) {
            return;
        }
        ctmManager.stop();
        if (healBot.makeOnePlayerHeal()) {
            return;
        }
        Utils.sleep(1000L);
        wowInstance.click(WinKey.D0);
        Utils.sleep(3600L);
//        log.info("mount casted, go to the point");
    }

    private void regBg() {
        int failed = 0;
        while (!player.onBg()) {
            player.updatePlayer();
            for (int i = 0; i < 2 && (!player.onBg()); ++i) {
                objectManager.refillUnits();
                Optional<UnitObject> unit = objectManager.getNearestUnitTo(player);
                // TODO: validate level of bg unit 61/71
                // and maybe use GUID, if it saves after restart? not sure
                wowInstance.click(WinKey.ESC);
                if (unit.isPresent()) {
                    log.info("found bg register");
                    UnitObject unitObject = unit.get();
                    ctmManager.interactNpc(unitObject);
                } else {
                    log.info("didnt find a register master");
                }
                Utils.sleep(100L);
                wowInstance.click(WinKey.W);
                wowInstance.click(WinKey.W);
                wowInstance.click(WinKey.S);
                wowInstance.click(WinKey.S);
                wowInstance.click(WinKey.S);
                Utils.sleep(1000L);
            }
            log.info("waiting in city");
            for (int i = 0; i < 360; ++i) {
                if (!player.onBg()) {
                    Utils.sleep(1000L);
                }
            }
            failed++;
            if (failed == MAX_ATTEMPTS_FOR_REG_BG) {
                log.error("couldn't get to BG for {} attempts, stop the bot", MAX_ATTEMPTS_FOR_REG_BG);
                System.exit(1);
            }
        }

    }

    private void getPaths() {
        globalGraph.buildGlobalGraph();
    }

    /*
    public void setNearestPath(
        Player player,
        List<Navigation.Coordinates> points)
    {
        Navigation.Coordinates playerCoordinates = Navigation.get3DCoordsFor(player);
        double min = Navigation.evaluateDistanceFromTo(points.get(0), playerCoordinates);
        int index = 0;

        for(int i = 1; i < points.size(); ++i) {
            double distance = Navigation.evaluateDistanceFromTo(points.get(i), playerCoordinates);
            log.info(points.get(i).toString());
            log.info("i=" + i + " distance from point to player=" + distance);
            if (distance < min) {
                min = distance;
                index = i;
            }
        }

        log.info("indexNearest=" + index);
        path.setNearestPoint((List) path.getPoints().get(index));
        path.setFromBase(index == 0);
    }
    */
}
