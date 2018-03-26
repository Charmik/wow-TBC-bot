package auction;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import farmbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.objects.AuctionManager;

public class AuctionBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private static final int MAX_PAGES = 600;

    private static final WowInstance wowInstance = new WowInstance("World of Warcraft");
    private final AuctionManager auctionManager;

    public AuctionBot() {
        this.auctionManager = wowInstance.getAuctionManager();
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException {
        AuctionBot auctionBot = new AuctionBot();
        auctionBot.saveDataToFile();
    }

    public void saveDataToFile() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
        List<Item> items = getAllItemsFromAuc();
        PrintWriter writer = new PrintWriter("auc0.txt", "UTF-8");
        for (Item item : items) {
            //logger.info(item.toString());
            writer.println(item.getAuctionId() + " " + item.getItemId() + " " + item.getCount() + " " + item.getUsableItem() + " " + item.getCurrentBid() + " " + item.getBuyOut() + " "
                + item.getExpireTime());
        }
        logger.info("closing file");
        writer.close();
    }

    public List<Item> getAllItemsFromAuc() throws InterruptedException {
        List<Item> items = new ArrayList<>();
        Item[] itemsFromCurrentPage = null;
        List<Integer> readFromMemoryCounts = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        long longestScanning = -1;
        int countForceScanning = 0;
        boolean foundLastPage = false;
        for (int page = 1; page <= MAX_PAGES; page++) {
            logger.info("page=" + page);
            int lastExpiredTime = -1;
            if (itemsFromCurrentPage != null) {
                lastExpiredTime = itemsFromCurrentPage[0].getExpireTime();
            }
            int countGetPreviousPage = -1;
            long startPageScanning = System.currentTimeMillis();
            boolean flagForceButtonNextPage = false;
            do {
                itemsFromCurrentPage = auctionManager.getItemsFromCurrentPage();
                countGetPreviousPage++;
                //try to force nextPage again, we will miss this page, doesn't matter, bug wow.
                if (countGetPreviousPage == 5000) {
                    clickNextPage();
                    flagForceButtonNextPage = true;
                    countForceScanning++;
                }
                //found lastPage
                if (countGetPreviousPage == 10000) {
                    foundLastPage = true;
                    break;
                }
            } while (itemsFromCurrentPage[0].getExpireTime() == lastExpiredTime);
            if (foundLastPage) {
                break;
            }
            Collections.addAll(items, itemsFromCurrentPage);
            long timePageScannig = System.currentTimeMillis() - startPageScanning;
            if (!flagForceButtonNextPage && timePageScannig > longestScanning) {
                longestScanning = timePageScannig;
            }
            //logger.info("countGetPreviousPage=" + countGetPreviousPage);
            readFromMemoryCounts.add(countGetPreviousPage);
            Thread.sleep(400);
            clickNextPage();
            Thread.sleep(100);
        }
        logger.info("longestScanning=" + longestScanning);
        logger.info("countForceScanning=" + countForceScanning);
        logger.info(readFromMemoryCounts.toString());
        logger.info("scanningTime:", (System.currentTimeMillis() - startTime) / 1000);
        return items;

    }

    private void clickNextPage() {
        wowInstance.click(WinKey.D4);
    }
}
