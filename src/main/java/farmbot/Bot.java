package farmbot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import farmbot.Pathing.BotPath;
import farmbot.Pathing.FarmList;
import farmbot.Pathing.Graph;
import farmbot.Pathing.Graph.Vertex;
import farmbot.Pathing.Path;
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
import wow.memory.objects.UnitObject;

class Bot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private static boolean killGrayMobs;
    private final WowInstance wowInstance = new WowInstance("World of Warcraft");
    private final CtmManager ctmManager;
    private final Player player;
    private final ObjectManager objectManager;
    private final Healer healer;
    private final Movement movement;
    private final String[] args;
    private final Random random;
    private final Graph graph;
    private final Fighter fighter;
    private final TargetManager targetManager;
    private FarmList farmList;

    Bot(String[] args) {
        this.ctmManager = wowInstance.getCtmManager();
        this.player = wowInstance.getPlayer();
        this.objectManager = wowInstance.getObjectManager();
        logger.info(Arrays.toString(args));
        this.args = args;
        this.healer = new Healer(player, wowInstance);
        this.graph = new Graph();
        this.targetManager = new TargetManager(graph, player, objectManager);
        this.fighter = new Fighter(player, objectManager, ctmManager, wowInstance, healer, graph, targetManager);
        this.movement = new Movement(player, healer, ctmManager, wowInstance, fighter, targetManager);
        Looter.configure(player, ctmManager, wowInstance);
        this.random = new Random();
        killGrayMobs = BotPath.killGrayMobs(args);
        farmList = configureFarmList();
        new Thread(new CloseHandler()).start();
    }

    public static boolean isKillGrayMobs() {
        return killGrayMobs;
    }

    public void run() throws IOException {
        long prevBuff = System.currentTimeMillis();
        // make 0 if you want go to sell items at start
        long lastTimeSell = System.currentTimeMillis();
        int countDidntFindMob = 0;
        while (true) {
            healer.heal(null);
            lastTimeSell = repairAndSellItems(lastTimeSell);
            prevBuff = makeBuffs(prevBuff);

            Point3D nearestPointToPlayer = graph.getNearestPointTo(player).getKey();
            if (!player.isDead()) {
                farmList = checkChangingFarmList(farmList);
            } else if (player.isDead() && movement.getCorpseCoordinate() != null) {
                goToTheCorpse(nearestPointToPlayer);
            }
            //even if movement.getCorpseCoordinate() != null, because bugs + smth strange always can happen, that troop not there where is coordinates
            if (player.isDead()) {
                movement.ress();
                goToRandomPoint(nearestPointToPlayer);
            }
            if (player.isInCombat()) {
                List<UnitObject> enemies = targetManager.getMobsForAttack();
                fighter.killListOfMobs(enemies);
            }
            if (!tryFindNearestMobToKill(nearestPointToPlayer)) {
                countDidntFindMob++;
            }
            if (countDidntFindMob == 500) {
                goToRandomPoint(nearestPointToPlayer);
                countDidntFindMob = 0;
            }
        }
    }

    private boolean tryFindNearestMobToKill(Point3D nearestPointToPlayer) {
        objectManager.refillUnits();
        UnitObject nearestMobForAttack = targetManager.getNearestMobForAttack();
        healer.regenMana();
        if (nearestMobForAttack != null) {
            Point3D nearestPointToMob = graph.getNearestPointTo(nearestMobForAttack).getKey();
            makeRoute(nearestMobForAttack, nearestPointToPlayer, nearestPointToMob, null);
            if (nearestMobForAttack.getHealth() == 100) {
                player.target(nearestMobForAttack);
                fighter.kill(nearestPointToMob, nearestMobForAttack);
            } else {
                logger.info("mob health is not 100%, so skip it");
            }
            return true;
        } else {
//            Utils.sleeping(player, 10000);
//            goToRandomPoint(nearestPointToPlayer);
            logger.info("didnt find any mob");
            return false;
        }
    }

    //TODO: move somewhere
    private long makeBuffs(long prevBuff) {
        long now = System.currentTimeMillis();
        if (Utils.isPassedEnoughTime(prevBuff, now, 1500000L)) {
            healer.makeBuffs();
            prevBuff = now;
        }
        return prevBuff;
    }

    //TODO: move somewhere
    private long repairAndSellItems(long lastTimeSell) throws IOException {
        Point3D nearestPointToPlayer;
        if (!player.isDead() && farmList.getRepairFileName() != null) {
            logger.info("played is not dead");
            Seller seller = new Seller(
                BotPath.getPath("repairs//" + farmList.getRepairFileName()).getPoints(),
                movement,
                objectManager,
                ctmManager,
                wowInstance,
                player,
                healer);
            if (System.currentTimeMillis() - lastTimeSell > 4800000L) {
                logger.error("going to sell items, passed enough time");
                Point3D start = graph.getNearestPointTo(player).getKey();
                nearestPointToPlayer = graph.getNearestPointTo(seller.getPath().get(0)).getKey();
                makeRoute(null, start, nearestPointToPlayer, null);
                seller.goToSellAndComeBack();
                lastTimeSell = System.currentTimeMillis();
            }
        }
        return lastTimeSell;
    }

    //TODO: move somewhere
    private FarmList configureFarmList() {
        FarmList farmList = FarmList.getFarmListByLevel(player);
        if (args.length > 0) {
            int beginIndex = args[0].indexOf("/");
            if (beginIndex != -1) {
                args[0] = args[0].substring(beginIndex + 1);
            }
            logger.info("you have farmList in commandLine=" + args[0]);
            farmList = FarmList.getFarmListByFileName(args[0]);
        }

        logger.info("farmList was choose: " + farmList);
        logger.info("fileName: " + farmList.getCurrentFileName());
        updateGraph(farmList);
        return farmList;
    }

    //TODO: move to Movement
    private void goToTheCorpse(Point3D nearestPointToPlayer) {
        logger.info("player is dead, trying to find own corpse");
        Point3D nearestPointToTroop = graph.getNearestPointTo(movement.getCorpseCoordinate()).getKey();
        makeRoute(null, nearestPointToPlayer, nearestPointToTroop, movement.getCorpseCoordinate());
        if (!player.isDead()) {
            movement.resetCorpseCoordinate();
        }
    }

    //TODO: move somewhere
    private FarmList checkChangingFarmList(FarmList farmList) {
        if (farmList != farmList.getNextFarmList()
            && farmList.getNextFileName() != null
            && farmList.getNextFarmList() != FarmList.ANY
            && farmList.getNextFarmList().getLowLevel() <= player.getLevel()) {
            String fileNameToNextFarmPoint = farmList.getCurrentFileName() + "_" + farmList.getNextFarmList().getCurrentFileName();
            logger.info("fileNameToNextFarmPoint=" + fileNameToNextFarmPoint);
            Path pathToNextFarmPoint = BotPath.getPathFromFile(fileNameToNextFarmPoint);
            makeCircle(pathToNextFarmPoint);
            farmList = farmList.getNextFarmList();
            updateGraph(farmList);
        }
        return farmList;
    }

    //TODO: move somewhere
    private void goToRandomPoint(Point3D nearestPointToPlayer) {
        logger.error("didn't find any mob near to the graph, try go to random vertex");
        Point3D finish = graph.getRandomCoordinates();
        double v = Navigation.evaluateDistanceFromTo(player.getCoordinates(), finish);
        if (v > 2600000) { //too far away to the point
            //makeGlobalGraph
        }
        makeRoute(null, nearestPointToPlayer, finish, null);
        //delete GlobalGraph
    }

    //TODO: move somewhere
    private void updateGraph(FarmList farmList) {
        graph.clear();
        graph.buildGraph(BotPath.getPathFromFile(farmList.getCurrentFileName()));
        graph.floyd();
    }

    //TODO: move to Movemenet
    private void makeRoute(
        UnitObject nearestMobForAttack,
        Point3D nearestPointToPlayer,
        Point3D pointToGo,
        Point3D finishPoint)
    {
        if ((!farmList.isRunFreely()) || nearestMobForAttack == null) {
            List<Vertex> shortestPath = graph.getShortestPath(nearestPointToPlayer, pointToGo);
            if (finishPoint != null) {
                logger.info("add finishPoint=" + finishPoint + " to our path");
                shortestPath.add(new Vertex(finishPoint, graph.getVertices().size(), null));
            }
            logger.info("shortestPath.size()=" + shortestPath.size());
            for (Vertex v : shortestPath) {
                boolean wasReleased = movement.goToNextPoint(v.getCoordinates());
                if (wasReleased) {
                    break;
                }
                if (nearestMobForAttack != null && Navigation.evaluateDistanceFromTo(player, nearestMobForAttack) < 1000.0D) {
                    if (player.getLevel() < 20) {
                        ctmManager.stop();
                    }
                    logger.info("distance too short, go to kill mob!");
                    break;
                }
                if (nearestMobForAttack != null && nearestMobForAttack.getHealth() != 100) {
                    break;
                }
            }
        }
    }

    //TODO: move to Movement
    private void makeCircle(Path path) {
        int refillCount = -1;
        for (Point3D nextPoint : path.getPoints()) {
            if (random.nextInt(8) == 0) {
                wowInstance.click(WinKey.D3);
            }
            ++refillCount;
            if (refillCount % 10 == 0) {
                objectManager.refillUnits();
                refillCount = 0;
            }
            boolean wasReleased = movement.goToNextPoint(nextPoint);
            if (wasReleased) {
                break;
            }
        }
    }

    private void sell() {
    }

    private void sendMail() {
    }
}
