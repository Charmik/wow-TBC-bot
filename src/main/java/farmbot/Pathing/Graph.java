package farmbot.Pathing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Random;

import javafx.geometry.Point3D;
import javafx.util.Pair;
import net.sf.javaml.core.kdtree.KDTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.components.Navigation;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.Player;

public class Graph {

    private static final Logger logger = LoggerFactory.getLogger(Graph.class);
    private List<Vertex> vertices = new ArrayList<>();
    double[][] d;
    int[][] p;
    private Random random = new Random();

    public Graph() {
    }

    public boolean mobIsNearToTheGraph(CreatureObject unitObject) {
        Pair<Point3D, Double> nearestPointTo = getNearestPointTo(unitObject);
        return nearestPointTo != null && nearestPointTo.getValue() < 400.0D;
    }

    public Pair<Point3D, Double> getNearestPointTo(Player player) {
        return getNearestPointTo(new Point3D((double) player.getX(), (double) player.getY(), (double) player.getZ()));
    }

    public Pair<Point3D, Double> getNearestPointTo(CreatureObject unit) {
        return getNearestPointTo(new Point3D((double) unit.getX(), (double) unit.getY(), (double) unit.getZ()));
    }

    public Pair<Point3D, Double> getNearestPointTo(Point3D unitPoint) {
        Pair<Vertex, Double> nearestPointTo = getNearestPointTo(unitPoint, vertices);
        return new Pair<>(nearestPointTo.getKey().coordinates, nearestPointTo.getValue());
    }

    private Pair<Vertex, Double> getNearestPointTo(
        Point3D unitPoint,
        List<Vertex> vertices)
    {
        logger.debug("getNearestPointTo input=" + unitPoint);
        double min = Double.MAX_VALUE;
        Vertex returnVertex = null;
        for (Vertex v : vertices) {
            double distance = Navigation.evaluateDistanceFromTo(v.getCoordinates(), unitPoint);
            if (distance < min) {
                min = distance;
                returnVertex = v;
            }
        }
        return new Pair<>(returnVertex, min);
    }

    public void clear() {
        vertices.clear();
    }

    public void buildGraph(Path path) {
        if (path.getPoints().isEmpty()) {
            logger.info("buildGraph: dont build because point in file=" + path.getFileName() + " is empty");
            return;
        }
        int prevSize = vertices.size();
        vertices.add(new Vertex(path.getPoints().get(0), vertices.size(), path.getFileName()));
        for (int i = 1; i < path.getPoints().size(); i++) {
            Vertex prevVertex = vertices.get(vertices.size() - 1);
            Vertex currentVertex = new Vertex(path.getPoints().get(i), prevSize + i, path.getFileName());
            prevVertex.add(currentVertex);
            currentVertex.add(prevVertex);
            vertices.add(currentVertex);
        }
        for (int i = 0; i < vertices.size(); i++) {
            if (vertices.get(i).index != i) {
                throw new IllegalArgumentException(vertices.get(i).index + " " + i);
            }
        }
        if (vertices.size() > path.getPoints().size()) {
            mergeDifferentGraphs(
                vertices.get(prevSize),
                vertices.get(vertices.size() - 1),
                vertices.subList(0, prevSize));

            //mergeDifferentGraphs2(prevSize);
        }
        deleteDuplicateVertexes();
        logger.info("GRAPH SIZE=" + vertices.size());
        logger.info("graph=" + vertices);
        int vIndex = -1;
        for (Vertex x : vertices) {
            logger.debug(x.toString());
            vIndex++;
            for (Vertex y : x.neighbors) {
                if (y.index > vertices.size()) {
                    logger.debug(String.valueOf(y.index));
                    throw new IllegalArgumentException("found index from = " +
                        vIndex + " " + y.index + " vertex, but size=" + vertices.size());
                }
            }
        }
    }

    //merge not only 1st and last point to the previous graph, but all points fom new
    private void mergeDifferentGraphs2(int prevSize) {
        for (int i = prevSize; i < vertices.size(); i++) {
            for (int j = 0; j < prevSize; j++) {
                Vertex newVertex = vertices.get(i);
                Vertex oldVertex = vertices.get(j);

            }
        }
    }

    private void mergeDifferentGraphs(
        Vertex first,
        Vertex last,
        List<Vertex> vertices)
    {
        Pair<Vertex, Double> firstNearest = getNearestPointTo(first.getCoordinates(), vertices);
        addEdge(first, firstNearest);
        Pair<Vertex, Double> lastNearest = getNearestPointTo(last.getCoordinates(), vertices);
        addEdge(last, lastNearest);
    }

    private void addEdge(
        Vertex newVertex,
        Pair<Vertex, Double> nearestPointInGraph)
    {
        if (nearestPointInGraph.getValue() < 30) {
            String f = String.format("%-58s", "merge: newFile=" + newVertex.fileName);
            logger.info(f + nearestPointInGraph.getKey().fileName);
            newVertex.add(nearestPointInGraph.getKey());
            nearestPointInGraph.getKey().add(newVertex);
        }
    }

    private void deleteDuplicateVertexes() {
        KDTree tree = new KDTree(3);
        for (Vertex vertex : vertices) {
            double arr[] = new double[3];
            arr[0] = vertex.coordinates.getX();
            arr[1] = vertex.coordinates.getY();
            arr[2] = vertex.coordinates.getZ();
            tree.insert(arr, vertex);
        }
        int i = 0;
        while (i < vertices.size()) {
            Vertex vertex = vertices.get(i);
            double arr[] = new double[3];
            arr[0] = vertex.coordinates.getX();
            arr[1] = vertex.coordinates.getY();
            arr[2] = vertex.coordinates.getZ();
            //[0] is the same vertex
            Vertex nearest = (Vertex) (tree.nearest(arr, 2)[1]);
            if (vertex.coordinates.distance(nearest.coordinates) < 3.0D && !vertex.equals(nearest)) {
                int indexFirst = vertex.index;
                int indexSecond = nearest.index;

                if (indexFirst == indexSecond) {
                    throw new IllegalStateException("indexFirst=indexSecond " + indexFirst);
                }

                Vertex firstVertex = vertices.get(indexFirst);
                Vertex secondVertex = vertices.get(indexSecond);

                addNeighborsFromSecondToFirst(firstVertex, secondVertex);
                addFirstVertexInsteadSecond(firstVertex, secondVertex);
                reindexVertexes(indexSecond);

                arr[0] = secondVertex.coordinates.getX();
                arr[1] = secondVertex.coordinates.getY();
                arr[2] = secondVertex.coordinates.getZ();
                tree.delete(arr);
            } else {
                i++;
            }
        }
    }

    private void addFirstVertexInsteadSecond(
        Vertex firstVertex,
        Vertex secondVertex)
    {
        for (Vertex v : vertices) {
            boolean wasRemoved = false;
            for (Vertex neighbor : v.neighbors) {
                if (neighbor.equals(secondVertex)) {
                    v.neighbors.remove(neighbor);
                    wasRemoved = true;
                    break;
                }
            }
            if (wasRemoved && !v.equals(firstVertex) && v.index != firstVertex.index) {
                v.neighbors.add(firstVertex);
            }
        }
    }

    private void reindexVertexes(int indexSecond) {
        if (indexSecond != vertices.size() - 1) {
            for (int i = indexSecond; i < vertices.size() - 1; ++i) {
                vertices.set(i, vertices.get(i + 1));
            }
        }
        vertices.remove(vertices.size() - 1);
        for (Vertex v : vertices) {
            if (v.index > indexSecond) {
                --v.index;
            }
        }
    }

    private void addNeighborsFromSecondToFirst(
        Vertex firstVertex,
        Vertex secondVertex)
    {
        secondVertex.neighbors.remove(firstVertex);
        for (Vertex v : secondVertex.neighbors) {
            boolean has = false;
            for (Vertex u : firstVertex.neighbors) {
                if (v.index == u.index) {
                    has = true;
                }
            }
            if (!has && firstVertex.index != v.index) {
                firstVertex.neighbors.add(v);
            }
        }
    }

    void normalize() {
        Vertex v0 = vertices.get(0);
        double x0 = v0.coordinates.getX();
        double y0 = v0.coordinates.getY();
        double z0 = v0.coordinates.getZ();
        for (Vertex v : vertices) {
            v.coordinates = v.coordinates.subtract(x0, y0, z0);
        }
    }

    void dfs() {
        dfs(vertices.get(0));
    }

    private void dfs(Vertex v) {
        v.visit = true;
        for (Vertex neighbor : v.neighbors) {
            if (!neighbor.visit) {
                dfs(neighbor);
            }
        }
    }

    public void dijkstra() {
        logger.info("verticlesSize={}", vertices.size());
        initArrays();
        for (int from = 0; from < vertices.size(); from++) {
            dijkstra(from);
        }
    }

    private void dijkstra(int startVertex) {
        PriorityQueue<QItem> queue = new PriorityQueue<>(vertices.size());
        queue.add(new QItem(startVertex, 0));
        while (!queue.isEmpty()) {
            QItem q = queue.poll();
            Vertex v = vertices.get(q.vertexId);
            for (Vertex u : v.neighbors) {
                double newCost = d[startVertex][v.index] + v.coordinates.distance(u.coordinates);
                if (newCost < d[startVertex][u.index]) {
                    d[startVertex][u.index] = newCost;
                    p[startVertex][u.index] = v.index;
                    queue.add(new QItem(u.index, newCost));
                }
            }
        }
    }

    public void floyd() {
        logger.info("started init floyd");
        initArrays();
        logger.info("started initDistanceInMatrix in floyd");
        initDistanceInMatrix();
        logger.info("started floyd-processing");
        processingFloyd();
        logger.info("finished floyd");
    }

    private void initArrays() {
        int size = vertices.size();
        d = new double[size][size];
        p = new int[size][size];
        for (int k = 0; k < size; ++k) {
            Arrays.fill(d[k], Double.MAX_VALUE);
            Arrays.fill(p[k], -1);
            d[k][k] = 0.0D;
        }
    }

    private void processingFloyd() {
        for (int k = 0; k < vertices.size(); ++k) {
            for (int i = 0; i < vertices.size(); ++i) {
                for (int j = 0; j < vertices.size(); ++j) {
                    if (d[i][k] < Double.MAX_VALUE && d[k][j] < Double.MAX_VALUE && d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                        p[i][j] = p[k][j];
                    }
                }
            }
        }
    }

    private void initDistanceInMatrix() {
        for (int k = 0; k < vertices.size(); ++k) {
            Vertex v = vertices.get(k);
            Vertex u = null;
            try {
                for (int i = 0; i < v.neighbors.size(); i++) {
                    u = v.neighbors.get(i);
                    p[k][u.index] = k;
                    d[k][u.index] = v.coordinates.distance(u.coordinates);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                logger.error(e.getMessage() + " u= " + u);
                throw e;
            }
        }
    }

    public List<Vertex> getShortestPath(
        Point3D start,
        Point3D finish)
    {
        Point3D startInGraph = getNearestPointTo(start).getKey();
        Point3D finishInGraph = getNearestPointTo(finish).getKey();
        Optional<Vertex> startVertex = vertices.stream().filter((v) -> v.coordinates.equals(startInGraph)).findAny();
        Optional<Vertex> finishVertex = vertices.stream().filter((v) -> v.coordinates.equals(finishInGraph)).findAny();
        //logger.info(" start {}, finish{}", start, finish);
        if (startVertex.isPresent() && finishVertex.isPresent()) {
            return getShortestPath(startVertex.get().index, finishVertex.get().index);
        } else {
            logger.debug("graph doesn't contain start or finish points: startVertexPresent=" + startVertex.isPresent() + ", finishVertexPresent=" + finishVertex.isPresent());
            return Collections.emptyList();
        }
    }

    public List<Vertex> getShortestPathFromPlayerToPoint(
        Player player,
        Point3D finish)
    {
        //  logger.info("player.getCoordinates()=" + player.getCoordinates());
        return getShortestPath(getNearestPointTo(player).getKey(), finish);
    }

    public static class Vertex {

        public Point3D coordinates;
        public int index;
        public List<Vertex> neighbors;
        public boolean visit;
        public String fileName;

        public Vertex(
            Point3D coordinates,
            int index,
            String fileName)
        {
            this.coordinates = coordinates;
            this.index = index;
            this.visit = false;
            this.neighbors = new ArrayList<>();
            this.fileName = fileName;
        }

        public Vertex(Point3D coordinates)
        {
            this(coordinates, -1, null);
        }

        public Point3D getCoordinates() {
            return coordinates;
        }

        public void add(Vertex currentVertex) {
            neighbors.add(currentVertex);
        }

        @Override
        public String toString() {
            return "Vertex{" +
                "coordinates=" + coordinates +
                ", index=" + index +
                ", visit=" + visit +
                ", fileName=" + fileName +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Vertex vertex = (Vertex) o;

            return index == vertex.index;
        }

        @Override
        public int hashCode() {
            return index;
        }
    }

    public List<Vertex> getShortestPath(
        int i,
        int j)
    {
        return getShortestPath(vertices.get(i), vertices.get(j));
    }

    private List<Vertex> getShortestPath(
        Vertex v,
        Vertex u)
    {
        List<Vertex> list = new ArrayList<>();
        list.add(u);
        for (int prev = p[v.index][u.index]; prev != v.index && prev != -1; prev = p[v.index][prev]) {
            list.add(vertices.get(prev));
        }
        if (!v.equals(u)) {
            list.add(v);
        }
        Collections.reverse(list);
        return list;
    }

    public Point3D getRandomCoordinates() {
        int index = Math.abs(random.nextInt(vertices.size()));
        return vertices.get(index).coordinates;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    private class QItem implements Comparable<QItem> {
        int vertexId;
        double distance;

        QItem(
            int vertexId,
            double distance)
        {
            this.vertexId = vertexId;
            this.distance = distance;
        }

        @Override
        public int compareTo(QItem o) {
            return Double.compare(this.distance, distance);
        }
    }
}
