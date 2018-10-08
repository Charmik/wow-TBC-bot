package auction;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

import auction.analyzer.Analyzer;
import auction.dao.FilesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.Client;
import util.LoggerConfiguration;
import util.Utils;
import wow.Reconnect;
import wow.WowInstance;
import wow.memory.objects.AuctionManager;
import wow.memory.objects.Player;

public class AuctionBot {

    private static final Logger logger = LoggerFactory.getLogger(AuctionBot.class);
    private static final int FREQUENCY_FOR_SELLING = 50;

    private static final WowInstance wowInstance = new WowInstance("World of Warcraft");
    private final Player player;
    private final Buyer buyer;
    private final Seller seller;
    private final Analyzer analyzer;
    private final AuctionMovement auctionMovement;
    private final Mailbox mailbox;
    private final Reconnect reconnect;
    private final Client client;

    private AuctionBot(Account account, boolean scanOnlyFirstPage) throws IOException {
        this.client = new Client();
        this.reconnect = new Reconnect(wowInstance, account, client);
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

        AuctionBot auctionBot = new AuctionBot(account, scanOnlyFirstPage);
        if (scanOnlyFirstPage) {
            auctionBot.runBuyer();
        }
        auctionBot.runBuyerWithSelling();
    }

    private void runBuyer() throws InterruptedException, ParseException, IOException {
        boolean success = buyer.analyze();
        if (!success) {
            Runtime.getRuntime().exit(0);
        }
    }

    private void runBuyerWithSelling() {
        /*
        testing mail
        for (;;) {
            auctionMovement.goToMail();
            mailbox.getMail();
            auctionMovement.goToAuction();
            buyer.resetAuction();
        }
        */
        this.client.sendMessageToCharm("started bot " + player.getAccountName());
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
                    int sleepTime = 1000 * 60 * 5;
                    logger.info("sleep:{}, because we failed:{} times to analyze full auction", sleepTime, failed);
                    client.sendMessageToCharm(player.getAccountName() +
                        " failed to analyze auction " + FREQUENCY_FOR_SELLING + " times");
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
