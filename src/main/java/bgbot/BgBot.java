package bgbot;

import farmbot.Healer;
import farmbot.Pathing.GlobalGraph;
import farmbot.Pathing.Graph;
import healbot.HealBot;
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
import wow.memory.objects.PlayerObject;
import wow.memory.objects.UnitObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BgBot {

    private static Logger log = LoggerFactory.getLogger(BgBot.class);
    private WowInstance wowInstance = new WowInstance("World of Warcraft");
    private CtmManager ctmManager;
    private Player player;
    private ObjectManager objectManager;
    private HealBot healBot;
    private Healer healer;
    private Movement movement;
    private GlobalGraph globalGraph = new GlobalGraph("routesBG" + File.separator + "WSG");
    private Point3D lastPoint;
    private long lastTimestamp;
    private long lastPlayerHealth;
    private int globalCount = -1;
    private final int HORDE_AV_BOSS = -1320;
    private final Random random = new Random();

    public BgBot() {
        reset();
    }

    private void reset() {
        this.ctmManager = wowInstance.getCtmManager();
        this.player = wowInstance.getPlayer();
        this.objectManager = wowInstance.getObjectManager();
        this.healBot = new HealBot();
        this.healer = new Healer(player, wowInstance, ctmManager);
        this.movement = new Movement(player, ctmManager, wowInstance, objectManager);

        this.lastPoint = new Point3D(0, 0, 0);
        this.lastTimestamp = 0;
        this.lastPlayerHealth = 0;
    }

    public static void main(String[] args) {
        BgBot bgBot = new BgBot();
        bgBot.run();
    }

    private void run() {
        getPaths();
        for (int i = 0; i < 10; ++i) {
            try {
                player.updatePlayer();
                log.info("step=" + i);
                regBg();
                player.updatePlayer();

                while (player.onBg()) {
                    goRandomPointOnBg();
                }

                log.info("exit from bg");
                ctmManager.stop();

                player.updatePlayer();
                int sleepTime = 60 * 1000;
                for (int j = 0; j < 3; j++) {
                    System.out.println("index=" + j + " sleep between bgs:" + sleepTime + " ");
                    Utils.sleep(sleepTime);
                }
                player.updatePlayer();
            } catch (Throwable e) {
                reset();
            }
        }
    }

    private void goRandomPointOnBg() {
        Point3D randomPointFromGraph = globalGraph.getRandomPointFromGraph();
        // random always goes to horde base
        if (player.getZone().isAlterac()) {
            while (randomPointFromGraph.getX() > player.getX()) {
                randomPointFromGraph = globalGraph.getRandomPointFromGraph();
            }
        }
        Point3D destination = getMiddlePlayersPoint(randomPointFromGraph);

        boolean isRandomPoint = false;
        if (destination.equals(randomPointFromGraph)) {
            System.out.println("we have random point");
            isRandomPoint = true;
        }
        // base of alterac
        if (destination.getX() > 700 && player.getZone().isAlterac()) {
            destination = randomPointFromGraph;
        }
        System.out.println("destination:" + destination + " player:" + player.getCoordinates());
        List<Graph.Vertex> path = globalGraph.getShortestPathFromPlayerToPoint(player, destination);
        //playing trying to go north

        System.out.println("found path:" + path.size());
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

        for (Graph.Vertex vertex : path) {
            // check every minute that we stay in one place, so bg is finished

            // understanding that's bg is over and we stay afk in one place
            if (System.currentTimeMillis() - lastTimestamp > 20 * 1000) {
                objectManager.refillPlayers();
                int size = objectManager.getPlayers().size();
                log.info("checking end of the bg current:" + player.getCoordinates() + " prev:" + lastPoint + " playerSize:" + size
                        + "player.getHealth():" + player.getHealth() + " lastPlayerHealth:" + lastPlayerHealth);
                lastTimestamp = System.currentTimeMillis();
                if (player.getCoordinates().equals(lastPoint) && size <= 3) {
                    if (player.getHealth() == lastPlayerHealth) {
                        log.info("we were stuck in one place more than 1 minute, sleep and break from cycle");
                        ctmManager.stop();
                        Utils.sleep(2 * 60 * 1000);
                        break;
                    }
                    lastPlayerHealth = player.getHealth();
                }
                lastPoint = player.getCoordinates();
            }
            globalCount++;
            if (globalCount % 100 == 0) {
                castMount();
            }
            countForMoonfire++;
            if (countForMoonfire % 5 == 0) {
//                System.out.println("cast moonfire because countForMoonfire:" + countForMoonfire);
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
                System.out.println("player was dead, so break from cycle");
                break;
            }
            // TODO: check target is not line of sight
            boolean healed = healBot.makeOnePlayerHeal();
            if (healed) {
                ctmManager.stop();
                for (int i = 0; i < 5; i++) {
                    if (player.getHealthPercent() < 75) {
                        // barkskin
                        castInstantSkill(WinKey.X);
                    }
                    if (!healBot.makeOnePlayerHeal()) {
                        break;
                    }
                    if (player.getHealthPercent() < 15) {
                        // instant heal yourself + battlemaster
                        castInstantSkill(WinKey.D6);
                    }
                    Utils.sleep(1500);
                }
                Utils.sleep(50);
                System.out.println("cast moonfire because we are in combat");
                castRandomSpellToNearestEnemy();
            }

            player.updatePlayer();
            if (!player.onBg()) {
                System.out.println("player not on bg");
                ctmManager.stop();
                break;
            }
            if (player.getManaPercent() < 10) {
                castInnervate();
            }
//            if (destination.getX() < player.getCoordinates().getX() || player.getCoordinates().getX() < HORDE_AV_BOSS) {
            if (!healBot.makeOnePlayerHeal()) {
                if (!movement.goToNextPoint(vertex.coordinates)) {
                    // TODO: try unstuck 1 time maybe?
                    log.info("bot couldn't go to the point:{} from:{}", vertex.coordinates, player.getCoordinates());
                    log.info("sleeping, because it's more saver for now");
                    for (int i = 0; i < 310; i++) {
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
        if (path.size() < 10) {
            log.info("we went too small, we are around allies, sleep");
            castMount();
            for (int i = 0; i < 3; i++) {
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
    private Point3D getMiddlePlayersPoint(Point3D randomPoint) {
        objectManager.refillPlayers();
        List<PlayerObject> ally = objectManager.getPlayers().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getFaction().isAlliance())
                .filter(e -> !e.isDead())
                .collect(Collectors.toList());
        Point3D coordinates = new Point3D(0, 0, 0);
        int size = ally.size();
        //System.out.println("allySize:" + size);
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
            coordinates = new Point3D(coordinates.getX() + player.getX(), coordinates.getY() + player.getY(), coordinates.getZ() + player.getZ());
        }
        log.info("we found {} ally, their coordinates:\n{}", ally.size(), ally.stream().map(e -> e.getCoordinates() + "\n").toArray());
        System.out.println("sizePlayer=" + size);
        coordinates = new Point3D(coordinates.getX() / size, coordinates.getY() / size, coordinates.getZ() / size);

        if (player.getZone().isAlterac() && size < 4) {
            return randomPoint;
        }
        /*
        if (player.getZone().isAlterac()) {
            if (size < 4 || coordinates.getX() == 0 || coordinates.getX() > player.getX()) {
                //horde base
                if (player.getX() < HORDE_AV_BOSS) {
                    log.info("sleeping , because we are at horde base");
                    //Utils.sleep(30 * 1000);
                }
                return randomPoint;
            }
        }
        */
        //System.out.println("coordinates in getMiddlePlayersPoint:" + coordinates);
        return coordinates;
    }


    private void castInnervate() {
        Utils.sleep(500L);
        for (int i = 0; i < 5; ++i) {
            wowInstance.click(WinKey.F3);
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
        while (!player.onBg()) {
            player.updatePlayer();
            for (int i = 0; i < 2 && (!player.onBg()); ++i) {
                objectManager.refillUnits();
                Optional<UnitObject> unit = objectManager.getNearestUnitTo(player);
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
                wowInstance.click(WinKey.D2);
            }
            log.info("waiting in city");
            for (int i = 0; i < 360; ++i) {
                if (!player.onBg()) {
                    Utils.sleep(1000L);
                }
            }
        }

    }

    private void getPaths() {
        globalGraph.buildGlobalGraph();
    }

    /*
    public void setNearestPath(
        Player player,
        List<Navigation.Coordinates3D> points)
    {
        Navigation.Coordinates3D playerCoordinates = Navigation.get3DCoordsFor(player);
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
