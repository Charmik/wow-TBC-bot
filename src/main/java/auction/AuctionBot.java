package auction;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import farmbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.objects.AuctionManager;
import wow.memory.objects.Player;

public class AuctionBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private static final int MAX_PAGES = 1000;
    private static final int SLEEP1 = 1350;
    private static final int SLEEP2 = 200;
    private static final Random random = new Random();
    private static final String FILE_NAME = "auc_" + random.nextInt(99) + ".txt";

    private static final WowInstance wowInstance = new WowInstance("World of Warcraft");
    public final AuctionManager auctionManager;
    private final Player player;

    private AuctionBot() {
        this.auctionManager = wowInstance.getAuctionManager();
        player = wowInstance.getPlayer();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        AuctionBot auctionBot = new AuctionBot();
        auctionBot.saveDataToFile();
//        printCurrentPage(auctionBot);
//        sortAuctionId(auctionBot, "auc_3000_3000_12.txt");
    }

    private static void sortAuctionId(AuctionBot auctionBot, String fileName) throws IOException {
        List<Item> items = auctionBot.readFromFile(fileName);
        HashSet<Item> set = new HashSet<>(items);
        System.out.println(items.size() + " " + set.size());
        Collections.sort(items);

        List<Integer> ints = items.stream().map(Item::getAuctionId).sorted().collect(Collectors.toList());
        for (Integer x : ints) {
            System.out.println(x);
        }
    }

    private static void printCurrentPage(AuctionBot auctionBot) {
        Item[] itemsFromCurrentPage = auctionBot.auctionManager.getItemsFromCurrentPage();
        for (Item item : itemsFromCurrentPage) {
            System.out.println(item);
        }
    }

    private List<Item> readFromFile(String file) throws IOException {
        List<Item> list = new ArrayList<>();
        List<String> strings = Files.readAllLines(Paths.get(file));
        for (String s : strings) {
            Object[] arr = Arrays.stream(s.split(" ")).map(Integer::valueOf).toArray();
            list.add(new Item((Integer) arr[0], (Integer) arr[1], (Integer) arr[2], (Integer) arr[3], (Integer) arr[4], (Integer) arr[5], (Integer) arr[6], (Integer) arr[7]));
        }
        return list;
    }

    private void saveDataToFile() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {
        logger.info("FILE_NAME=" + FILE_NAME + " isAlliance? - " + player.getFaction().isAlliance());
        List<Item> items = getAllItemsFromAuc();
        int oldSize = items.size();
        //but of the wow, sometimes you see the same items by scanning, so delete the equals by auctionId
        if (items.size() < 10000) {
            logger.info("items size only: " + items.size() + " dont save it");
            return;
        }
        items = new ArrayList<>(new HashSet<>(items));
        String msg = "oldSize:" + oldSize + " setSize:" + items.size() + "  " + ((double) items.size() / (double) oldSize);
        logger.info(msg);

        PrintWriter writer = new PrintWriter(FILE_NAME, "UTF-8");
        writer.println(new Date());
        writer.println(msg);
        for (Item item : items) {
            //logger.info(item.toString());
            writer.println(item);
        }
        logger.info("closing file:" + FILE_NAME);
        writer.close();
    }

    private List<Item> getAllItemsFromAuc() throws InterruptedException {
        List<Item> items = new ArrayList<>();
        Item[] itemsFromCurrentPage = null;
        List<Integer> readFromMemoryCounts = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        int countForceScanning = 0;
        boolean foundLastPage = false;
        for (int page = 1; page <= MAX_PAGES; page++) {
            //logger.info("page=" + page);
            int lastExpiredTime = -1;
            if (itemsFromCurrentPage != null) {
                lastExpiredTime = itemsFromCurrentPage[0].getExpireTime();
            }
            int countGetPreviousPage = 1;
            do {
                itemsFromCurrentPage = auctionManager.getItemsFromCurrentPage();
                countGetPreviousPage++;
                //try to force nextPage again, we will miss this page, doesn't matter, bug wow.
                if (countGetPreviousPage % 3000 == 0) {
                    logger.info("can't read memory too long, try force next page");
                    clickNextPage();
                    countForceScanning++;
                }
                //found lastPage
                if (countGetPreviousPage == 9000) {
                    logger.info("can't read memory too long, it should be last page, exit");
                    foundLastPage = true;
                    break;
                }
            } while (itemsFromCurrentPage[0].getExpireTime() == lastExpiredTime);
            if (foundLastPage) {
                break;
            }
            Collections.addAll(items, itemsFromCurrentPage);
            readFromMemoryCounts.add(countGetPreviousPage);
            Thread.sleep(SLEEP1);
            clickNextPage();
            Thread.sleep(SLEEP2);
        }
        logger.info("countForceScanning=" + countForceScanning);
        logger.info(readFromMemoryCounts.toString());
        logger.info("scanningTime:" + (System.currentTimeMillis() - startTime) / 1000);
        return items;
    }

    private void clickPrevPage() {
        wowInstance.click(WinKey.D3);
    }

    private void clickNextPage() {
        wowInstance.click(WinKey.D4);
    }
}
