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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.components.Coordinates;

public class BotPath {

    private final static Logger logger = LoggerFactory.getLogger(BotPath.class);

    public BotPath() {
    }

    public static Path getPath(String fileName) {
        if (!fileName.endsWith(".txt")) {
            fileName = fileName + ".txt";
        }
        try {
            List<Coordinates> points = Files.readAllLines(Paths.get(fileName)).stream().map((e) -> {
                String[] split = e.split(" ");
                return new Coordinates( Float.valueOf(split[0]),  Float.valueOf(split[1]),  Float.valueOf(split[2]));
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
            if (file.isDirectory()) {
                continue;
            }
            logger.info(file.getName());
            Path path = getPath(file.getPath());
            paths.add(path);
        }
        logger.info(Arrays.toString(files));
        paths.sort(Comparator.comparing(Path::getFileName));
        return paths;
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
