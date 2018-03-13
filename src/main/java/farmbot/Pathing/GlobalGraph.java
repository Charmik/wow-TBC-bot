package farmbot.Pathing;

import java.util.List;

public class GlobalGraph {

    private Graph graph;
    private List<Path> paths;


    public GlobalGraph() {
        this.graph = new Graph();
        this.paths = BotPath.getAllPaths();
    }

    void buildGlobalGraph() {
        for (Path path : paths) {
            graph.buildGraph(path);
        }
        System.out.println(graph.getVertices().size());
    }

    public void reset() {
        graph.clear();
    }
}
