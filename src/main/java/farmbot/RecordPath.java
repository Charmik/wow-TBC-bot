package farmbot;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.WowInstance;

/**
 * Created by Charm on 26/07/2017.
 */
public class RecordPath {

    private static Logger log = LoggerFactory.getLogger(RecordPath.class);

    // AV x > 1413 stop and next point > our now, (going spirit)
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("Started " + RecordPath.class);

        WowInstance wowInstance = new WowInstance("World of Warcraft");

        PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
        writer.close();

        Thread.sleep(1000);
        float prevx = 0, prevy = 0, prevz = 0;
        for (; ; ) {
            float x = wowInstance.getPlayer().getX();
            float y = wowInstance.getPlayer().getY();
            float z = wowInstance.getPlayer().getZ();
            if (x == prevx && y == prevy && z == prevz) {
                Thread.sleep(500);
                continue;
            }
            log.info(x + " " + y + " " + z);
            Files.write(Paths.get("output.txt"), ("" + x + " " + y + " " + z + "\n").getBytes(), StandardOpenOption.APPEND);
            Thread.sleep(500);
            prevx = x;
            prevy = y;
            prevz = z;
        }
    }

}
