package auction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.objects.AuctionManager;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

/**
 * @author alexlovkov
 */
public class Buyer {

    private static final Logger logger = LoggerFactory.getLogger(Buyer.class);
    private static final int MAX_PAGES = 1000;
    private static final int SLEEP1 = 1350;
    private static final int SLEEP2 = 200;

    private static final WowInstance wowInstance = WowInstance.getInstance();
    private final String faction;
    private final FilesManager filesManager;
    private final Analyzer analyzer;
    private final ObjectManager objectManager;
    private Player player;
    private CtmManager ctmManager;
    private String tmpFile;
    private BufferedWriter historyBufferedWriter;
    private BufferedWriter logPricesBufferedWriter;
    private boolean scanOnlyFirstPage;
    private Set<Integer> setBuyingItems = new HashSet<>();

    public Buyer(boolean scanOnlyFirstPage) throws IOException {
        this.player = wowInstance.getPlayer();
        if (player.getFaction().isHorde()) {
            this.faction = "horde";
        } else {
            this.faction = "alliance";
        }
        logger.info("faction: " + faction);
        String folder = "history_auction" + File.separator + faction;
        this.analyzer = new Analyzer(wowInstance, folder);
        this.ctmManager = wowInstance.getCtmManager();
        this.objectManager = wowInstance.getObjectManager();
        this.tmpFile = folder + File.separator + "tmp.txt";
        this.filesManager = new FilesManager(folder);
        this.logPricesBufferedWriter = new BufferedWriter(new FileWriter(folder + File.separator + "logPrices.txt", true));
        this.scanOnlyFirstPage = scanOnlyFirstPage;
    }

    public static void main(String[] args) throws InterruptedException, ParseException, IOException {
        boolean scanOnlyFirstPage = false;
        if (args.length > 0) {
            logger.info("going to scan only first page");
            scanOnlyFirstPage = true;
        }

        Buyer buyer = new Buyer(scanOnlyFirstPage);
        buyer.analyze();
//        buyer.printCurrentPage();
    }

    void resetAuc() throws IOException {
        if (!scanOnlyFirstPage) {
            resetTmpFile();
        }
        logger.info("reset auction");
        //close auctionHouse
        wowInstance.click(WinKey.D1);
        Utils.sleep(6000);
        objectManager.refillUnits();
        Optional<UnitObject> nearestUnitTo = objectManager.getNearestUnitTo(this.player);
        if (nearestUnitTo.isPresent()) {
            UnitObject unitObject = nearestUnitTo.get();
            this.ctmManager.interactNpc(unitObject);
            Utils.sleep(1000L);
            this.ctmManager.interactNpc(unitObject);
            Utils.sleep(100L);
            wowInstance.click(WinKey.W);
            Utils.sleep(100);
            wowInstance.click(WinKey.W);
            Utils.sleep(100);
            wowInstance.click(WinKey.S);
            Utils.sleep(100);
            wowInstance.click(WinKey.S);
            Utils.sleep(100);
            wowInstance.click(WinKey.S);
            Utils.sleep(1000L);
            wowInstance.click(WinKey.D2);
        } else {
            logger.error("auc wasn't found");
        }
        wowInstance.click(WinKey.D5);
        Utils.sleep(2000);
    }

    private void resetTmpFile() throws IOException {
        historyBufferedWriter = new BufferedWriter(new FileWriter(tmpFile));
        initWrite();
    }


    void analyze() throws InterruptedException, IOException, ParseException {
        if (scanOnlyFirstPage) {
            analazeOnlyFirstPage();
        } else {
            analyzeFullAuction();
        }
    }

    private void analazeOnlyFirstPage() throws IOException {
        logger.info("analazeOnlyFirstPage");
        resetAuc();
        AuctionManager auctionManager = wowInstance.getAuctionManager();
        int count = -1;
        for (; ; ) {
            count++;
            if (count % 50 == 0) {
                try {
                    analyzer.calculate();
                } catch (Throwable e) {
                    count = -1;
                    continue;
                }
            }
            Item[] itemsFromCurrentPage = auctionManager.getItemsFromCurrentPage();
            if (!validateItems(itemsFromCurrentPage)) {
                resetAuc();
                auctionManager = wowInstance.getAuctionManager();
                continue;
            }
            for (int i = 0; i < itemsFromCurrentPage.length; i++) {
                Item item = itemsFromCurrentPage[i];
                Analyzer.BuyType buyType = analyzer.buyItem(item, i + 1);
                if (buyType != Analyzer.BuyType.NONE) {
                    logBuyingPrice(item);
                    // TODO: delete this
                    Utils.sleep(3000);
                }
            }
            wowInstance.click(WinKey.D5);
            Utils.sleep(3000);
        }
    }

    private void analyzeFullAuction() throws IOException, ParseException, InterruptedException {
        logger.info("analyzeFullAuction");
        boolean firstIteration = true;
        for (; ; ) {
            //TODO: merge only current.txt file not all of them every time
            resetTmpFile();
            analyzer.calculate();
            resetAuc();
            AuctionManager auctionManager = wowInstance.getAuctionManager();
            int page;
            for (page = 1; page <= MAX_PAGES; page++) {
                if (page % 30 == 0 && firstIteration) {
                    System.out.println("page:" + page);
                }
                Item[] itemsFromCurrentPage = auctionManager.getItemsFromCurrentPageWithRetry();
                if (itemsFromCurrentPage == null) {
                    break;
                }
                if (!firstIteration) {
                    for (int i = 0; i < itemsFromCurrentPage.length; i++) {
                        Item item = itemsFromCurrentPage[i];
                        Analyzer.BuyType buyType = analyzer.buyItem(item, i + 1);
                        if (buyType != Analyzer.BuyType.NONE) {
                            logBuyingPrice(item);
                            Utils.sleep(1 * 60 * 1000);
                        }
                        //sell item price: max(currentAuc(min), stats.min)
                    }
                }
                writeCurrentAuc(itemsFromCurrentPage);
                Thread.sleep(SLEEP1);
                auctionManager.nextPage();
                Thread.sleep(SLEEP2);
            }
            if (page < 300) {
                logger.warn("found not enough pages, something wrong");
            }
            firstIteration = false;
            historyBufferedWriter.flush();
            historyBufferedWriter.close();
            filesManager.addToDataBase("history_auction" + File.separator + faction, tmpFile);
        }
    }

    private boolean validateItems(Item[] itemsFromCurrentPage) {
        if (itemsFromCurrentPage == null) {
            return false;
        }
        for (Item item : itemsFromCurrentPage) {
            if (item.getAuctionId() == 0) {
                return false;
            }
        }
        return true;
    }

    private void logBuyingPrice(Item item) throws IOException {
        if (!setBuyingItems.contains(item.getAuctionId())) {
            logPricesBufferedWriter.write(new Date() + " " + scanOnlyFirstPage + " " + item + "\n");
            logPricesBufferedWriter.flush();
            setBuyingItems.add(item.getAuctionId());
        }
    }

    private void initWrite() throws IOException {
        historyBufferedWriter.write(new Date() + "\n");
        historyBufferedWriter.write("current, don't have statistics\n");
    }

    private void writeCurrentAuc(Item[] items) throws IOException {
        for (Item item : items) {
            historyBufferedWriter.write(item.toString() + "\n");
        }
    }


    private void printCurrentPage() throws IOException, ParseException, InterruptedException {
        AuctionManager auctionManager = wowInstance.getAuctionManager();
        Item[] itemsFromCurrentPage = auctionManager.getItemsFromCurrentPageWithRetry();
        if (itemsFromCurrentPage == null) {
            return;
        }
        for (int i = 0; i < itemsFromCurrentPage.length; i++) {
            Item item = itemsFromCurrentPage[i];
            System.out.println("index=" + i + " " + item);
        }

    }
}
