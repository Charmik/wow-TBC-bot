package farmbot.Pathing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotPath {

    private final static Logger logger = LoggerFactory.getLogger(BotPath.class);

    public BotPath() {
    }

    public static Path getPath(String fileName) {
        if (!fileName.endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        try {
            List<Point3D> points = Files.readAllLines(Paths.get(fileName)).stream().map((e) -> {
                String[] split = e.split(" ");
                return new Point3D((double) Float.valueOf(split[0]), (double) Float.valueOf(split[1]), (double) Float.valueOf(split[2]));
            }).collect(Collectors.toList());
            return new Path(points, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("can't read fileName=" + fileName);
        }
    }

    public static Path getPathFromFile(String folder, String fileName) {
        fileName = folder + File.separator + fileName;
        Path path = getPath(fileName);
        logger.info("getPathFromFile path.size()=" + path.getPoints().size());
        return path;
    }

    public static List<Path> getAllPaths(String foldName) {
        File folder = new File(foldName);
        File[] files = folder.listFiles();
        List<Path> paths = new ArrayList<>();
        for (File file : files) {
            logger.info(file.getName());
            Path path = getPath(file.getPath());
            paths.add(path);
        }
        logger.info(Arrays.toString(files));
        paths.sort(Comparator.comparing(Path::getFileName));
        return paths;
    }

    // just for testing
    public static void main(String[] args) {
        GlobalGraph globalGraph = new GlobalGraph("routes");
        globalGraph.buildGlobalGraph();

        /*
        for (; ; ) {
            long start = System.currentTimeMillis();
            globalGraph = new GlobalGraph();
            globalGraph.buildGlobalGraph(); //bug jvm? what?
            System.out.println("time:" + (System.currentTimeMillis() - start));
        }
        */

        globalGraph.dijkstra();
        //add some stupid test that we have paths.size > 0

        Path pathFromFile = getPathFromFile("routes","30-32_needles.txt");
        Point3D point3D = pathFromFile.getPoints().get(5);
        for (int i = 0; i < 5; i++) {
            System.out.println();
        }

        //spirit: 3019.7263 3593.3203 145.75897
        //troop: 2751.772460 3577.03540039 139.6567077

        //List<Graph.Vertex> vertices = globalGraph.getShortestPath(new Point3D(-5538.576, -3498.06, -51.020306), point3D);
        List<Graph.Vertex> vertices = globalGraph.getShortestPath(new Point3D(3019.7263, 3593.3203, 145.75897),
            new Point3D(2751.772460, 3577.03540039, 139.6567077));
        System.out.println("size=" + vertices.size());
        vertices.forEach(System.out::println);


    }

    public static boolean killGrayMobs(String[] args) {
        for (String s : args) {
            if (s.equals("killGrayMobs")) {
                logger.info("killGrayMobs");
                return true;
            }
        }
        return false;
    }
}
