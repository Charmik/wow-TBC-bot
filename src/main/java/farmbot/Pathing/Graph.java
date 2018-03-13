package farmbot.Pathing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javafx.geometry.Point3D;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.components.Navigation;
import wow.memory.objects.CreatureObject;
import wow.memory.objects.Player;

public class Graph {
    private static final Logger logger = LoggerFactory.getLogger(Graph.class);
    private List<Vertex> vertices = new ArrayList<>();
    private double[][] d;
    private int[][] p;
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
        //logger.info("getNearestPointTo input=" + unitPoint);
        double min = 1.7976931348623157E308D;
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
            //logger.info("buildGraph: dont build because point in file=" + path.getFileName() + " is empty");
            return;
        }
        int prevSize = vertices.size();
        vertices.add(new Vertex(path.getPoints().get(0), vertices.size(), path.getFileName()));
        for (int i = 1; i < path.getPoints().size(); ++i) {
            Vertex prevVertex = vertices.get(i - 1);
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
        }
        deleteDuplicateVertexes();
        //logger.info("GRAPH SIZE=" + vertices.size());
        //logger.info("graph=" + vertices);
        for (Vertex x : vertices) {
            //logger.info(x.toString());
            for (Vertex y : x.neighbors) {
                if (y.index > vertices.size()) {
                    //logger.info(String.valueOf(y.index));
                    throw new IllegalArgumentException(y.index + " size=" + vertices.size());
                }
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
        if (nearestPointInGraph.getValue() < 10000) {
            System.out.println("merge files: newFile=" + newVertex.fileName + " oldFile=" + nearestPointInGraph.getKey().fileName);
            newVertex.add(nearestPointInGraph.getKey());
            nearestPointInGraph.getKey().add(newVertex);
        }
    }

    private void deleteDuplicateVertexes() {
        boolean foundDuplicate = true;
        while (true) {
            int indexFirst;
            int indexSecond;
            do {
                if (!foundDuplicate) {
                    for (indexFirst = 1; indexFirst < vertices.size(); ++indexFirst) {
                        if (indexFirst - vertices.get(indexFirst - 1).index != 1) {
                            throw new RuntimeException("indexes are not correct, maybe bug " + indexFirst);
                        }
                    }
                    return;
                }
                foundDuplicate = false;
                indexFirst = -1;
                indexSecond = -1;
                for (int i = 0; i < vertices.size(); ++i) {
                    for (int j = i + 1; j < vertices.size(); ++j) {
                        if (vertices.get(i).coordinates.distance(vertices.get(j).coordinates) < 10.0D) {
                            foundDuplicate = true;
                            indexFirst = i;
                            indexSecond = j;
                            break;
                        }
                    }
                    if (foundDuplicate) {
                        break;
                    }
                }
            } while (!foundDuplicate);
            Vertex firstVertex = vertices.get(indexFirst);
            Vertex secondVertex = vertices.get(indexSecond);
            secondVertex.neighbors.remove(firstVertex);
            for (Vertex v : secondVertex.neighbors) {
                boolean has = false;
                for (Vertex u : firstVertex.neighbors) {
                    if (v.index == u.index) {
                        has = true;
                    }
                }
                if (!has) {
                    firstVertex.neighbors.add(v);
                }
            }
            for (Vertex v : vertices) {
                boolean wasRemoved = false;
                for (Vertex neighbor : v.neighbors) {
                    if (neighbor.equals(secondVertex)) {
                        v.neighbors.remove(neighbor);
                        wasRemoved = true;
                        break;
                    }
                }
                if (wasRemoved && !v.equals(firstVertex)) {
                    v.neighbors.add(firstVertex);
                }
            }
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

    public void floyd() {
        int size = vertices.size();
        d = new double[size][size];
        p = new int[size][size];

        int k;
        for (k = 0; k < size; ++k) {
            Arrays.fill(d[k], 1.7976931348623157E308D);
            Arrays.fill(p[k], -1);
            d[k][k] = 0.0D;
        }
        for (k = 0; k < vertices.size(); ++k) {
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
        for (k = 0; k < size; ++k) {
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    if (d[i][k] < 1.7976931348623157E308D && d[k][j] < 1.7976931348623157E308D && d[i][k] + d[k][j] < d[i][j]) {
                        d[i][j] = d[i][k] + d[k][j];
                        p[i][j] = p[k][j];
                    }
                }
            }
        }
    }

    public List<Vertex> getShortestPath(
        Point3D start,
        Point3D finish)
    {
        Optional<Vertex> startVertex = vertices.stream().filter((v) -> v.coordinates.equals(start)).findAny();
        Optional<Vertex> finishVertex = vertices.stream().filter((v) -> v.coordinates.equals(finish)).findAny();
        if (startVertex.isPresent() && finishVertex.isPresent()) {
            return getShortestPath(startVertex.get().index, finishVertex.get().index);
        } else {
            //logger.info("graph doesn't contain start or finish points: startVertexPresent=" + startVertex.isPresent() + ", finishVertexPresent=" + finishVertex.isPresent());
            return Collections.emptyList();
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
                '}';
        }
    }
}
