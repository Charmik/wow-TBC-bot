package farmbot.Pathing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.components.Coordinates;
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

    public GlobalGraph(String[] foldNames) {
        this.graph = new Graph();
        this.paths = new ArrayList<>();
        for (String foldName : foldNames) {
            this.paths.addAll(BotPath.getAllPaths(foldName));
        }
    }

    public void buildGlobalGraph() {
        logger.info("start build globalGraph");
        for (Path path : paths) {
            graph.buildGraph(path);
        }
        logger.info("start dijkstra");
        graph.dijkstra();
        logger.info("globalGraph size=" + graph.getVertices().size());
    }

    private void substractPoint(Path path) {
        for (int i = 0; i < path.getPoints().size(); i++) {
            Coordinates Coordinates = path.getPoints().get(i);
            Coordinates subtract = Coordinates.subtract(-5538.576f, -3498.06f, -51.020306f);
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

    public Pair<Coordinates, Double> getNearestPointTo(Coordinates point) {
        return graph.getNearestPointTo(point);
    }

    public List<Graph.Vertex> getShortestPath(
        Coordinates start,
        Coordinates finish)
    {
        return graph.getShortestPath(start, finish);
    }

    public List<Graph.Vertex> getShortestPathFromPlayerToPoint(
        Player player,
        Coordinates finish)
    {
        return graph.getShortestPathFromPlayerToPoint(player, finish);
    }

    public Pair<Coordinates, Double> getNearestPointTo(CreatureObject unit) {
        return graph.getNearestPointTo(unit);
    }

    public Coordinates getRandomPointFromGraph() {
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
