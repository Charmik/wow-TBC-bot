package auction;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import auction.analyzer.Analyzer;
import auction.dao.FilesManager;
import farmbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.TelegramBot;
import util.LoggerConfiguration;
import util.Utils;
import wow.WowInstance;
import wow.memory.objects.AuctionManager;
import wow.memory.objects.Player;

public class AuctionBot {

    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private static final int FREQUENCY_FOR_SELLING = 50;

    private static final WowInstance wowInstance = new WowInstance("World of Warcraft");
    private final Player player;
    private final Buyer buyer;
    private final Seller seller;
    private final Analyzer analyzer;
    private final TelegramBot telegramBot;
    private final AuctionMovement auctionMovement;
    private final Mailbox mailbox;

    private long lastTelegramMessage = 0;

    private AuctionBot(boolean scanOnlyFirstPage) throws IOException {
        player = wowInstance.getPlayer();
        String faction;
        if (player.getFaction().isHorde()) {
            faction = "horde";
        } else {
            faction = "alliance";
        }
        logger.info("faction: " + faction);
        new LoggerConfiguration().configure(faction);
        String folder = "history_auction" + File.separator + faction;
        PriceLogger priceLogger = new PriceLogger(folder + File.separator + "logPrices.txt");

        FilesManager filesManager = new FilesManager(folder);
        analyzer = new Analyzer(wowInstance, folder, priceLogger, filesManager, scanOnlyFirstPage);
        analyzer.calculate();
        buyer = new Buyer(scanOnlyFirstPage, folder, analyzer, filesManager);
        AuctionManager auctionManager = wowInstance.getAuctionManager();
        seller = new Seller(auctionManager, wowInstance, priceLogger, analyzer);
        if (player.getFaction().isHorde()) {
//            telegramBot = new TelegramBot();
            telegramBot = null;
        } else {
            telegramBot = null;
        }
        auctionMovement = new AuctionMovement(wowInstance);
        mailbox = new Mailbox(wowInstance);
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        boolean scanOnlyFirstPage = false;
        if (args.length > 0) {
            logger.info("going to scan only first page");
            scanOnlyFirstPage = true;
        }
        AuctionBot auctionBot = new AuctionBot(scanOnlyFirstPage);
        if (scanOnlyFirstPage) {
            auctionBot.runBuyer();
        }
//        wowInstance.getObjectManager().scanForNew();
/*
        for (;;) {
            wowInstance.getObjectManager().scanForNewUnits();
            Map<Long, UnitObject> units = wowInstance.getObjectManager().getUnits();
            units.entrySet().forEach(e -> {
                Point3D coordinates = e.getValue().getCoordinates();
                Player player = wowInstance.getPlayer();
                double distance = player.getCoordinates().distance(coordinates);
                if (distance < 10) {
                    System.out.println(distance + " " + e.getValue().getLevel());
                }
            });
            System.out.println("!!!!!!!!!!!!!!!!!");
            System.out.println("!!!!!!!!!!!!!!!!!");
        }
        */
        auctionBot.runBuyerWithSelling();
    }

    private void runBuyer() throws InterruptedException, ParseException, IOException {
        boolean success = buyer.analyze();
        if (!success) {
//            telegramBot.sendMessageToShumik("your bot is dead, check it");
            Runtime.getRuntime().exit(0);
        }
    }

    private void runBuyerWithSelling() throws InterruptedException, ParseException, IOException {
        /*
        for (;;) {
            auctionMovement.goToMail();
            mailbox.getMail();
            auctionMovement.goToAuction();
            buyer.resetAuction();
        }
        */

        long now = System.currentTimeMillis();
        if (now - lastTelegramMessage > 1000 * 60 * 30) {
            if (telegramBot != null) {
                telegramBot.sendMessageToCharm("run bot for faction: " + player.getFaction().getFactionName());
            }
            lastTelegramMessage = now;
        }
        for (; ; ) {
            try {
//            auctionMovement.goToMail();
//            mailbox.getMail();
//            auctionMovement.goToAuction();
                int failed = 0;

                boolean wasSelling = false;
                for (int i = 0; i < FREQUENCY_FOR_SELLING; i++) {
                    boolean successAnalyze = buyer.analyze();
                    if (successAnalyze && !wasSelling) {
                        logger.info("calculate auction & sell items");
                        analyzer.calculate();
                        sellWithRetries(1);
                        wasSelling = true;
                    } else {
                        failed++;
                        logger.error("bot failed to analyze auction at iteration:{}", i);
                    }
                }
                if (failed == FREQUENCY_FOR_SELLING) {
                    if (telegramBot != null) {
                        telegramBot.sendMessageToCharm("bot didn't find auction " + failed + " times, " +
                                "faction:" + player.getFaction().getFactionName() + " check it");
                    }
                    //buyer.resetAuction();
                    buyer.resetOnFirstPage();
                    int sleepTime = 1000 * 60 * 5;
                    logger.info("sleep:{}, because we failed:{} times to analyze full auction", sleepTime, failed);
                    Utils.sleep(sleepTime);
                }
            } catch (Exception e) {
                logger.error("got exception:", e);
            }
        }
    }

    private void sellWithRetries(int retries) {
        for (int i = 0; i < retries; i++) {
            seller.sellAllItemsFromBag();
        }
    }
}
