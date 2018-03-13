package farmbot.Pathing;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.WowInstance;

public class RecordPath {
    private static final Logger log = LoggerFactory.getLogger(RecordPath.class);

    public RecordPath() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Started " + RecordPath.class);
        WowInstance wowInstance = new WowInstance("World of Warcraft");
        String fileName = "output.txt";
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.close();
        Thread.sleep(1000L);
        float prevx = 0.0F;
        float prevy = 0.0F;
        float prevz = 0.0F;

        while (true) {
            float x = wowInstance.getPlayer().getX();
            float y = wowInstance.getPlayer().getY();
            float z = wowInstance.getPlayer().getZ();
            if (x == prevx && y == prevy && z == prevz) {
                Thread.sleep(500L);
            } else {
                log.info(x + " " + y + " " + z);
                Files.write(Paths.get(fileName), ("" + x + " " + y + " " + z + "\n").getBytes(), StandardOpenOption.APPEND);
                Thread.sleep(500L);
                prevx = x;
                prevy = y;
                prevz = z;
            }
        }
    }
}
