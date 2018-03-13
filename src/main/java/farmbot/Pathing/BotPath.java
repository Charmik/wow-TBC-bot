//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package farmbot.Pathing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
        fileName = "routes\\" + fileName;
        Path path = getPath(fileName);
        logger.info("path.size()=" + path.getPoints().size());
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
        return paths;
    }

    public static void main(String[] args) {
        GlobalGraph globalGraph = new GlobalGraph();
        System.out.println();
        System.out.println();
        System.out.println();
        globalGraph.buildGlobalGraph();
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
