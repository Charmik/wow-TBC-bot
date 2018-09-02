package auction;

import java.util.List;

import bgbot.Movement;
import farmbot.Pathing.GlobalGraph;
import farmbot.Pathing.Graph;
import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.WowInstance;
import wow.memory.objects.Player;

public class AuctionMovement {

    private static final Logger logger = LoggerFactory.getLogger(AuctionMovement.class);

    private static final Point3D STORMWIND_MAIL_BOX = new Point3D(-8876.547, 651.18896, 96.040054);
    private static final Point3D STORMWIND_AUCTION = new Point3D(-8814.421, 661.2754, 95.42299);
    // TODO: real coordinates
    private static final Point3D THUNDER_BLUFF_MAILBOX = new Point3D(-1263.1705, 45.47196, 127.60867);
    private static final Point3D THUNDER_BLUFF_AUCTION = new Point3D(-1210.8663, 95.01946, 134.33122);

    private final Movement movement;
    private final Player player;
    private final GlobalGraph globalGraph = new GlobalGraph("routesAuc");
    private final Point3D mailboxCoordinates;
    private final Point3D auctionCoordinates;

    public AuctionMovement(WowInstance wowInstance) {
        this.movement = new Movement(wowInstance.getPlayer(), wowInstance.getCtmManager(), wowInstance, wowInstance.getObjectManager());
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
    }
}
