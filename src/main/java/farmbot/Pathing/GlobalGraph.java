package farmbot.Pathing;

import java.util.List;

import javafx.geometry.Point3D;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.Player;

public class GlobalGraph {

    private static final Logger logger = LoggerFactory.getLogger(GlobalGraph.class);

    private Graph graph;
    private List<Path> paths;

    public GlobalGraph() {
        this.graph = new Graph();
        this.paths = BotPath.getAllPaths();
    }

    public void buildGlobalGraph() {
        for (Path path : paths) {
            graph.buildGraph(path);
        }
        graph.floyd();
        logger.info("globalGraph size=" + graph.getVertices().size());
    }

    public void floyd() {
        graph.floyd();
    }

    public Pair<Point3D, Double> getNearestPointTo(Point3D point) {
        return graph.getNearestPointTo(point);
    }

    public List<Graph.Vertex> XXXTest(
        Point3D start,
        Point3D finish)
    {
        return graph.getShortestPath(start, finish);
    }

    public List<Graph.Vertex> getShortestPathFromPlayerToPoint(
        Player player,
        Point3D finish)
    {
        return graph.getShortestPathFromPlayerToPoint(player, finish);
    }

    public Pair<Point3D, Double> getNearestPointTo(CreatureObject unit) {
        return graph.getNearestPointTo(unit);
    }

    public void reset() {
        graph.clear();
    }
}
