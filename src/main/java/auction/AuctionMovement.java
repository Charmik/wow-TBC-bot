package auction;

import java.util.List;

import bgbot.Movement;
import farmbot.Pathing.GlobalGraph;
import farmbot.Pathing.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import wow.WowInstance;
import wow.components.Coordinates;

public class AuctionMovement {

    private static final Logger logger = LoggerFactory.getLogger(AuctionMovement.class);

    private static final Coordinates STORMWIND_MAIL_BOX = new Coordinates(-8876.547f, 651.18896f, 96.040054f);
    private static final Coordinates STORMWIND_AUCTION = new Coordinates(-8814.421f, 661.2754f, 95.42299f);

    private static final Coordinates IRON_MAIL_BOX = new Coordinates(-4911.0693f, -975.3947f, 501.4486f);
    private static final Coordinates IRON_AUCTION = new Coordinates(-4962.4834f, -905.2312f, 503.8378f);

    private static final Coordinates THUNDER_BLUFF_MAILBOX = new Coordinates(-1263.1705f, 45.47196f, 127.60867f);
    private static final Coordinates THUNDER_BLUFF_AUCTION = new Coordinates(-1210.8663f, 95.01946f, 134.33122f);

    private final Movement movement;
    private final WowInstance wowInstance;
    private final GlobalGraph globalGraph = new GlobalGraph("routesAuc");
    private final Coordinates mailboxCoordinates;
    private final Coordinates auctionCoordinates;

    AuctionMovement(WowInstance wowInstance) {
        this.movement = new Movement(wowInstance.getPlayer(), wowInstance.getCtmManager(), wowInstance, wowInstance.getObjectManager());
        this.wowInstance = wowInstance;
        this.globalGraph.buildGlobalGraph();
        if (wowInstance.getPlayer().getFaction().isAlliance()) {
            this.mailboxCoordinates = IRON_MAIL_BOX;
            this.auctionCoordinates = IRON_AUCTION;
        } else {
            this.mailboxCoordinates = THUNDER_BLUFF_MAILBOX;
            this.auctionCoordinates = THUNDER_BLUFF_AUCTION;
        }
    }

    public void goToMail() {
        logger.info("goToMail");
        List<Graph.Vertex> path = globalGraph.getShortestPathFromPlayerToPoint(
            wowInstance.getPlayer(), mailboxCoordinates);
        goTo(path);
    }

    void goToAuction() {
        logger.info("goToAuction");
        List<Graph.Vertex> path = globalGraph.getShortestPathFromPlayerToPoint(
            wowInstance.getPlayer(), auctionCoordinates);
        goTo(path);
    }

    private void goTo(List<Graph.Vertex> path) {
        for (Graph.Vertex vertex : path) {
            if (!movement.goToNextPoint(vertex.coordinates)) {
                logger.info("couldn't go to the point:{}", vertex.coordinates);
                return;
            }
        }
        Utils.sleep(2000);
        Coordinates lastPoint = path.get(path.size() - 1).getCoordinates();
        wowInstance.getCtmManager().moveTo(lastPoint);
        Utils.sleep(3000);
        wowInstance.getCtmManager().stop();
        Utils.sleep(1000);
    }

    boolean farAwayFromAuction() {
        double distance = auctionCoordinates.distance(wowInstance.getPlayer().getCoordinates());
        boolean tooFarAway = distance > 5;
        if (tooFarAway) {
            logger.info("player is too far away from auction, distance: {} coordinates:{}",
                distance, wowInstance.getPlayer().getCoordinates());
        }
        return tooFarAway;
    }

    public Coordinates getMailboxCoordinates() {
        return mailboxCoordinates;
    }

    public Coordinates getAuctionCoordinates() {
        return auctionCoordinates;
    }
}
