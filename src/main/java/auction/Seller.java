package auction;

import java.util.HashMap;
import java.util.Map;

import auction.analyzer.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.Reconnect;
import wow.WowInstance;
import wow.memory.objects.AuctionManager;

public class Seller {

    private static final Logger logger = LoggerFactory.getLogger(Seller.class);

    private final AuctionManager auctionManager;
    private final WowInstance wowInstance;
    private PriceLogger priceLogger;
    private Analyzer analyzer;
    private final Reconnect reconnect;

    public Seller(
        AuctionManager auctionManager,
        WowInstance wowInstance,
        PriceLogger priceLogger,
        Analyzer analyzer,
        Reconnect reconnect)
    {
        this.auctionManager = auctionManager;
        this.wowInstance = wowInstance;
        this.priceLogger = priceLogger;
        this.analyzer = analyzer;
        this.reconnect = reconnect;
    }

    public void sellAllItemsFromBag() {

        Map<Integer, Boolean> items = new HashMap<>();

        for (int bag = 0; bag < 5; bag++) {
            reconnect.checkAndReconnect();
            int startSlot = 1;
            if (bag == 0) {
                // skip water
                startSlot = 2;
            }
            if (bag == 4) {
                // skip first line in the last bag
                startSlot = 5;
            }
            int maxSlots = 16;
            if (bag > 0) {
                maxSlots = 18;
            }
            for (int slot = startSlot; slot <= maxSlots; slot++) {
                try {
                    int itemId = auctionManager.getItemId(wowInstance, bag, slot);
                    // TODO: delete
                    if (itemId == 34837) {
                        continue;
                    }
                    logger.info("bag:{} slot:{} itemId:{}", bag, slot, itemId);
                    if (itemId != -1) {
                        int lastPriceForItem = priceLogger.getLastPriceForItem(itemId);
                        logger.info("item:{} lastPriceForItem: {}", itemId, lastPriceForItem);

                        int minBuyout = (int) (analyzer.getMinBuyoutByItemId(itemId) * 0.95);
                        logger.info("minBuyout:{}", minBuyout);

                        // SHOULD NEVER HAPPEN
                        if (lastPriceForItem == -1) {
                            logger.error("history for item:{} not found in history", itemId);
                            lastPriceForItem = minBuyout;
                        }
                        logger.info("lastPriceForItem:{}",
                            lastPriceForItem);

                        int currentMinPriceOnAuctionByItemId = analyzer.getCurrentMinPriceOnAuctionByItemId(itemId);
                        logger.info("currentMinPriceOnAuctionByItemId from analyzer:{}",
                            currentMinPriceOnAuctionByItemId);
                        if (currentMinPriceOnAuctionByItemId == 0) {
                            logger.info("didn't find current min price for this item");
                            currentMinPriceOnAuctionByItemId = Integer.MAX_VALUE;
                        }
                        logger.info("currentMinPriceOnAuctionByItemId:{}", currentMinPriceOnAuctionByItemId);
                        //current price on auc is very cheap, so miss it, sell later
                        if (currentMinPriceOnAuctionByItemId < minBuyout) {
                            logger.info("currentMinPriceOnAuctionByItemId={} < than minBuyout={} for item:{}",
                                currentMinPriceOnAuctionByItemId, minBuyout, itemId);
                            continue;
                        }
                        int priceForSelling;
                        //if zero items on auction with this id - set price minBuyout * C
                        if (currentMinPriceOnAuctionByItemId == Integer.MAX_VALUE) {
                            double coefForMinBuyout = 1.05;
                            priceForSelling = (int) (minBuyout * coefForMinBuyout);
                            logger.info("didn't find item:{} on auc, so set price minBuyout:{}*1.5, price:{}", itemId, minBuyout, priceForSelling);
                        } else {
                            if (currentMinPriceOnAuctionByItemId > minBuyout * 3) {
                                logger.info("currentMinPriceOnAuctionByItemId:{} and minBuyout:{} so skip this item, price is too big",
                                    currentMinPriceOnAuctionByItemId, minBuyout);
                                continue;
                            }
                            priceForSelling = (int) (currentMinPriceOnAuctionByItemId * 0.8);
                            logger.info("set price {} ", priceForSelling);
                        }

                        if (priceForSelling < lastPriceForItem * 0.95) {
                            logger.error("we try to sell item cheaper than we bought it, priceForSelling:{}, itemId:{}", priceForSelling, itemId);
                            continue;
                        }
                        Boolean sold = items.get(itemId);
                        logger.info("sold:{}", sold);
                        if (sold != null && sold && priceForSelling > 300000) {
                            logger.info("we sold item:{} already, so skip it");
                            continue;
                        }
                        items.put(itemId, true);
                        logger.info("sell item bag:{} slot:{} priceForSelling:{}", bag, slot, priceForSelling);
                        Writer.sellItem(wowInstance, bag, slot, priceForSelling);
                    }
                } catch (ItemNotFoundException ignore) {
                }
            }
        }
    }
}
