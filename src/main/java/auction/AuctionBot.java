package auction;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import auction.analyzer.Analyzer;
import auction.dao.FilesManager;
import farmbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.TelegramBot;
import util.LoggerConfiguration;
import util.Utils;
import wow.Reconnect;
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
    private final Reconnect reconnect;

    private long lastTelegramMessage = 0;

    private AuctionBot(Reconnect reconnect, boolean scanOnlyFirstPage) throws IOException {
        this.reconnect = reconnect;
        if (this.reconnect.isDisconnected()) {
            this.reconnect.reconnect();
        }
        this.player = wowInstance.getPlayer();
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
        this.analyzer = new Analyzer(wowInstance, folder, priceLogger, filesManager, scanOnlyFirstPage);
        this.analyzer.calculate();
        this.auctionMovement = new AuctionMovement(wowInstance);
        this.buyer = new Buyer(scanOnlyFirstPage, folder, analyzer, filesManager, auctionMovement);
        AuctionManager auctionManager = wowInstance.getAuctionManager();
        this.seller = new Seller(auctionManager, wowInstance, priceLogger, analyzer, reconnect);
        if (this.player.getFaction().isHorde()) {
//            telegramBot = new TelegramBot();
            this.telegramBot = null;
        } else {
            this.telegramBot = null;
        }

        this.mailbox = new Mailbox(wowInstance);
    }

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        boolean scanOnlyFirstPage = false;
        logger.info("Started, args:{}", Arrays.toString(args));
        if (args.length < 2) {
            logger.info("need 2 or 3 arguments: name password neutral(optional)");
        }
        Account account = new Account(args[0], args[1]);
        if (args.length == 3 && "neutral".equals(args[2])) {
            logger.info("going to scan only first page for neutral auction");
            scanOnlyFirstPage = true;
        }

        Reconnect reconnect = new Reconnect(wowInstance, account);
        AuctionBot auctionBot = new AuctionBot(reconnect, scanOnlyFirstPage);
        if (scanOnlyFirstPage) {
            auctionBot.runBuyer();
        }
        auctionBot.runBuyerWithSelling();
    }

    private void runBuyer() throws InterruptedException, ParseException, IOException {
        boolean success = buyer.analyze();
        if (!success) {
//            telegramBot.sendMessageToShumik("your bot is dead, check it");
            Runtime.getRuntime().exit(0);
        }
    }

    private void runBuyerWithSelling() {
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

                reconnect.checkAndReconnect();
                int failed = 0;

                boolean wasSelling = false;
                for (int i = 0; i < FREQUENCY_FOR_SELLING; i++) {
                    boolean successAnalyze = buyer.analyze();
                    reconnect.checkAndReconnect();
                    if (successAnalyze && !wasSelling) {
                        logger.info("calculate auction & sell items");
                        analyzer.calculate();
                        sellWithRetries(1);
                        wasSelling = true;
                    } else {
                        failed++;
                        logger.error("bot failed to analyze auction at iteration:{} failed:{} of {}", i, failed, FREQUENCY_FOR_SELLING);
                    }

                    if (failed % 10 == 0 && failed != 0) {
                        auctionMovement.goToAuction();
                        buyer.resetAuction();
                    }
                }
                if (failed == FREQUENCY_FOR_SELLING) {
                    if (telegramBot != null) {
                        telegramBot.sendMessageToCharm("bot didn't find auction " + failed + " times, " +
                                "faction:" + player.getFaction().getFactionName() + " check it");
                    }
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
