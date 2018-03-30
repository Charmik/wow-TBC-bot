package auction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

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

    private static final WowInstance wowInstance = new WowInstance("World of Warcraft");
    final String faction;
    private final FilesManager filesManager;
    private final Analyzer analyzer;
    private final ObjectManager objectManager;
    private Player player;
    private CtmManager ctmManager;
    private AuctionManager auctionManager;
    private String tmpFile;
    private BufferedWriter bw;

    public Buyer() {
        this.player = wowInstance.getPlayer();
        if (player.getFaction().isHorde()) {
            faction = "horde";
        } else {
            faction = "alliance";
        }
        logger.info("faction: " + faction);
        this.analyzer = new Analyzer("history_auction" + File.separator + faction);
        this.ctmManager = wowInstance.getCtmManager();
        this.objectManager = wowInstance.getObjectManager();
        tmpFile = "history_auction" + File.separator + faction + File.separator + "tmp.txt";
        filesManager = new FilesManager("history_auction" + File.separator + faction);
    }

    public static void main(String[] args) throws InterruptedException, ParseException, IOException {
        Buyer buyer = new Buyer();
        buyer.analyze();
    }

    void resetAuc() {
        logger.info("resetAuc");
        //close auctionHouse
        wowInstance.click(WinKey.D1);
        Utils.sleep(3000);
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
        Utils.sleep(3000);
    }


    void analyze() throws InterruptedException, IOException, ParseException {
        boolean firstIteration = true;
        for (; ; ) {
            //TODO: merge only current.txt file not all of them every time
            analyzer.calculate();
            resetAuc();
            bw = new BufferedWriter(new FileWriter(tmpFile));
            initWrite();
            this.auctionManager = wowInstance.getAuctionManager();

            Item[] itemsFromCurrentPage = null;
            boolean foundLastPage = false;
            for (int page = 1; page <= MAX_PAGES; page++) {
                if (page % 30 == 0) {
                    //System.out.println("page:" + page);
                }
                int lastExpiredTime = -1;
                if (itemsFromCurrentPage != null) {
                    lastExpiredTime = itemsFromCurrentPage[0].getExpireTime();
                }
                int countGetPreviousPage = 1;
                do {
                    itemsFromCurrentPage = auctionManager.getItemsFromCurrentPage();
                    countGetPreviousPage++;
                    //try to force nextPage again, we will miss this page, doesn't matter, bug wow.
                    if (countGetPreviousPage % 10000 == 0) {
                        clickNextPage();
                    }
                    //found lastPage
                    if (countGetPreviousPage == 30000) {
                        foundLastPage = true;
                        break;
                    }
                } while (itemsFromCurrentPage[0].getExpireTime() == lastExpiredTime);
                if (foundLastPage) {
                    break;
                }
                //if (!firstIteration) {
                for (Item item : itemsFromCurrentPage) {
                    analyzer.buyItem(item);
                }
                //}
                writeCurrentAuc(itemsFromCurrentPage);
                Thread.sleep(SLEEP1);
                clickNextPage();
                Thread.sleep(SLEEP2);
            }
            firstIteration = false;
            bw.flush();
            bw.close();
            filesManager.addToDataBase("history_auction" + File.separator + faction, tmpFile);
        }
    }

    private void initWrite() throws IOException {
        bw.write(new Date() + "\n");
        bw.write("current, don't have statistics\n");
    }

    private void writeCurrentAuc(Item[] items) throws IOException {
        for (Item item : items) {
            bw.write(item.toString() + "\n");
        }
        bw.flush();
    }

    private void clickNextPage() {
        wowInstance.click(WinKey.D4);
    }

}
