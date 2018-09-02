package auction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceLogger {

    private static final Logger logger = LoggerFactory.getLogger(PriceLogger.class);
    private final String path;

    private BufferedWriter logPricesBufferedWriter;

    public PriceLogger(String path) throws IOException {
        this.path = path;
        this.logPricesBufferedWriter = new BufferedWriter(new FileWriter(path, true));
    }

    public void logBuyingPrice(Item item, int price) {
        try {
            logPricesBufferedWriter.write(new Date() + " " + item + " " + price + "\n");
            logPricesBufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    int getLastPriceForItem(int itemId) {
        try {
            List<String> strings = Files.readAllLines(Paths.get(path));
            Collections.reverse(strings);
            for (String s : strings) {
                s = s.trim().replaceAll("\\s+", " ");
                String[] split = s.split(" ");
                if (split.length < 16) {
                    logger.error("split length < 16, something saved wrong in history");
                    continue;
                }
                int price = Integer.valueOf(split[split.length - 1]);
                int id = Integer.valueOf(split[split.length - 9]);
                if (itemId == id) {
                    return price;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.toString());
            return -1;
        }
        return -1;
    }
}
