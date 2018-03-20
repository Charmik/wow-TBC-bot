package farmbot.Pathing;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author alexlovkov
 */
public class GraphTest {

    @Test
    public void testOneFileFloydAndDijkstra() {
        List<Path> allPaths = BotPath.getAllPaths();
        Graph graph1 = new Graph();
        Graph graph2 = new Graph();
        graph1.buildGraph(allPaths.get(0));
        graph2.buildGraph(allPaths.get(0));

        graph1.floyd();
        graph2.dijkstra();

        int size = graph1.d.length;
        for (int i = 0; i < size; i++) {
            int index = i;
            /*
            int[] p1 = graph1.p[index];
            int[] p2 = graph2.p[index];
            System.out.println(Arrays.toString(p1));
            System.out.println(Arrays.toString(p2));
            */
            Assert.assertArrayEquals("index=" + index, graph1.d[index], graph2.d[index], 0.01);
            Assert.assertArrayEquals("index=" + index, graph1.p[index], graph2.p[index]);
        }
    }

    @Test
    public void testAllFileFloydAndDijkstra() {
        GlobalGraph graphWithFloyd = new GlobalGraph();
        GlobalGraph graphWithDijkstra = new GlobalGraph();
        graphWithFloyd.buildGlobalGraph();
        graphWithFloyd.floyd();
        graphWithDijkstra.buildGlobalGraph();
        graphWithDijkstra.dijkstra();
        int size = graphWithFloyd.graph.d.length;
        for (int i = 0; i < size; i++) {
            Assert.assertArrayEquals(graphWithFloyd.graph.d[i], graphWithDijkstra.graph.d[i], 0.0001);
            Assert.assertArrayEquals(graphWithFloyd.graph.p[i], graphWithDijkstra.graph.p[i]);
        }
    }
}
