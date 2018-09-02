package farmbot.Pathing;

import java.util.List;
import java.util.Random;

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

    public GlobalGraph(String foldName) {
        this.graph = new Graph();
        this.paths = BotPath.getAllPaths(foldName);
    }

    public void buildGlobalGraph() {
        logger.info("start build globalGraph");
        for (Path path : paths) {
            graph.buildGraph(path);
        }
        graph.dijkstra();
        logger.info("globalGraph size=" + graph.getVertices().size());
    }

    private void substractPoint(Path path) {
        for (int i = 0; i < path.getPoints().size(); i++) {
            Point3D point3D = path.getPoints().get(i);
            Point3D subtract = point3D.subtract(-5538.576, -3498.06, -51.020306);
            path.getPoints().set(i, subtract);
        }
    }

    //! CALL ONLY FROM TESTS
    public void floyd() {
        graph.floyd();
    }

    public void dijkstra() {
        graph.dijkstra();
    }

    public Pair<Point3D, Double> getNearestPointTo(Point3D point) {
        return graph.getNearestPointTo(point);
    }

    public List<Graph.Vertex> getShortestPath(
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

    public Point3D getRandomPointFromGraph() {
        Random random = new Random();
        return graph.getVertices().get(random.nextInt(graph.getVertices().size())).coordinates;
    }

    public void reset() {
        graph.clear();
    }

    public void normalize() {
        graph.normalize();
    }

    public List<Graph.Vertex> getVertices() {
        return graph.getVertices();
    }

}
