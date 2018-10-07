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
import wow.memory.objects.Player;

public class AuctionMovement {

    private static final Logger logger = LoggerFactory.getLogger(AuctionMovement.class);

    private static final Coordinates STORMWIND_MAIL_BOX = new Coordinates(-8876.547f, 651.18896f, 96.040054f);
    private static final Coordinates STORMWIND_AUCTION = new Coordinates(-8814.421f, 661.2754f, 95.42299f);
    // TODO: real coordinates
    private static final Coordinates THUNDER_BLUFF_MAILBOX = new Coordinates(-1263.1705f, 45.47196f, 127.60867f);
    private static final Coordinates THUNDER_BLUFF_AUCTION = new Coordinates(-1210.8663f, 95.01946f, 134.33122f);

    private final Movement movement;
    private final WowInstance wowInstance;
    private final Player player;
    private final GlobalGraph globalGraph = new GlobalGraph("routesAuc");
    private final Coordinates mailboxCoordinates;
    private final Coordinates auctionCoordinates;

    public AuctionMovement(WowInstance wowInstance) {
        this.movement = new Movement(wowInstance.getPlayer(), wowInstance.getCtmManager(), wowInstance, wowInstance.getObjectManager());
        this.wowInstance = wowInstance;
        globalGraph.buildGlobalGraph();
        player = wowInstance.getPlayer();
        if (player.getFaction().isAlliance()) {
            mailboxCoordinates = STORMWIND_MAIL_BOX;
            auctionCoordinates = STORMWIND_AUCTION;
        } else {
            mailboxCoordinates = THUNDER_BLUFF_MAILBOX;
            auctionCoordinates = THUNDER_BLUFF_AUCTION;
        }
    }

    public void goToMail() {
        logger.info("goToMail");
        List<Graph.Vertex> path = globalGraph.getShortestPathFromPlayerToPoint(player, mailboxCoordinates);
        goTo(path);
    }

    public void goToAuction() {
        logger.info("goToAuction");
        List<Graph.Vertex> path = globalGraph.getShortestPathFromPlayerToPoint(player, auctionCoordinates);
        goTo(path);
    }

    public void goTo(List<Graph.Vertex> path) {
        for (Graph.Vertex vertex : path) {
            movement.goToNextPoint(vertex.coordinates);
        }
        Utils.sleep(2000);
        Coordinates lastPoint = path.get(path.size() - 1).getCoordinates();
        wowInstance.getCtmManager().moveTo(lastPoint);
        Utils.sleep(3000);
        wowInstance.getCtmManager().stop();
        Utils.sleep(1000);
    }

    public boolean farAwayFromAuction() {
        double distance = auctionCoordinates.distance(player.getCoordinates());
        return distance > 5;
    }
}
