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

    public static Path getPathFromFile(String fileName) {
        fileName = "routes" + File.separator + fileName;
        Path path = getPath(fileName);
        logger.info("getPathFromFile path.size()=" + path.getPoints().size());
        return path;
    }

    public static List<Path> getAllPaths() {
        File folder = new File("routes");
        File[] files = folder.listFiles();
        List<Path> paths = new ArrayList<>();
        for (File file : files) {
            System.out.println(file.getName());
            System.out.println(file.getAbsolutePath());
            Path path = getPath(file.getAbsolutePath());
            paths.add(path);
        }
        System.out.println(Arrays.toString(files));
        paths.sort(Comparator.comparing(Path::getFileName));
        return paths;
    }

    public static void main(String[] args) {
        GlobalGraph globalGraph = new GlobalGraph();
        System.out.println();
        System.out.println();
        System.out.println();
        globalGraph.buildGlobalGraph();
        globalGraph.floyd();
/*
        for (; ; ) {
            // globalGraph.buildGlobalGraph(); зависание jvm? what?
            long start = System.currentTimeMillis();
            globalGraph.floyd();
            System.out.println("time:" + (System.currentTimeMillis() - start));
        }
        */

        //add some stupid test that we have paths.size > 0
        Path pathFromFile = getPathFromFile("48-50_ungoro");
        Point3D point3D = pathFromFile.getPoints().get(5);

//        List<Graph.Vertex> vertices = globalGraph.XXXTest(new Point3D(-6735.23046875, -2138.382080078125, -270.9570007324219), point3D);
        /*
         */
        List<Graph.Vertex> vertices = globalGraph.XXXTest(new Point3D(-7203.90185546875, -2436.10302734375, -218.1342010498047), point3D); //на дорожке где-то (from spirit)
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
