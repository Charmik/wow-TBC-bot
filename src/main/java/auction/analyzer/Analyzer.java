package auction.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import auction.BuyType;
import auction.BuyingItem;
import auction.Item;
import auction.PriceLogger;
import auction.Scan;
import auction.Writer;
import auction.dao.BidManager;
import auction.dao.BidManagerImpl;
import auction.dao.FilesManager;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.Client;
import wow.WowInstance;

/**
 * @author alexlovkov
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);
    private static final int ITEMS_TO_SAVE_FOR_ITEM_ID = 1000;
    private static final int REMOVE_ITEMS_PERCENT = 10;
    private static final int MIN_COUNT_IN_HISTORY = 50;
    private static final double BUYOUT_PERCENT = 0.75;

    private final BidManager bidManager;
    private final PriceLogger priceLogger;
    private final FilesManager filesManager;
    private Map<Integer, Statistic> itemIdToStatistics;
    private final WowInstance wowInstance;
    private Map<Integer, List<Item>> currentItemsOnAuction = new HashMap<>();

    private int profit = 5_00_00;

    public Analyzer(
        WowInstance wowInstance,
        BidManager bidManager,
        PriceLogger priceLogger,
        FilesManager filesManager,
        boolean scanOnlyFirstPage)
    {
        this.wowInstance = wowInstance;
        this.bidManager = bidManager;
        this.priceLogger = priceLogger;
        this.filesManager = filesManager;
        if (scanOnlyFirstPage) {
            profit = 25_00_00;
        }
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        String folder = "history_auction/horde";
        Analyzer analyzer = new Analyzer(
            null,
            new BidManagerImpl(folder + File.separator + "bidHistory.txt"),
            null,
            new FilesManager(folder, new Client()),
            false);
        analyzer.calculate();
        Statistic statistic = analyzer.itemIdToStatistics.get(21886);
        System.out.println(statistic);
        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
    }

    public int getMinBuyoutByItemId(int itemId) {
        Statistic statistic = itemIdToStatistics.get(itemId);
        if (statistic == null) {
            return Integer.MAX_VALUE / 2;
        }
        return statistic.getMinBuyOut();
    }

    public int getCurrentMinPriceOnAuctionByItemId(int itemId) {
        List<Item> items = currentItemsOnAuction.get(itemId);
        int min = Integer.MAX_VALUE;
        if (items != null) {
            for (Item item : items) {
                if (item.getBuyOut() < min && min != 0) {
                    min = item.getBuyOut();
                }
            }
        }
        return min;
    }

    //run only after calculate method.
    public BuyingItem buyItem(
        Item item,
        int index)
    {
        // TODO: delete
        if (item.getItemId() == 34837) {
            logger.info("found the ring, item:{}", item);
            if (item.getBuyOut() < 3000_00_00) {
                logger.info("buyout is:{}, so we are going to buy it", item.getBuyOut());
                return new BuyingItem(index, BuyType.BUYOUT);
            }
            if (item.getCurrentBid() < 3000_00_00) {
                logger.info("bid is:{}, so we are going to buy it", item.getCurrentBid());
                return new BuyingItem(index, BuyType.BID);
            }
        }

        if (uselessItem(item)) {
            return new BuyingItem();
        }

        Statistic statistic = itemIdToStatistics.get(item.getItemId());

        // TODO: delete this if
        if (item.getItemId() != 34837) {
            // don't buy too expensive items if you don't have enough statistics
            if (statistic != null && statistic.getMinBuyOut() > 3000_00_00 && statistic.getCount() < 50) {
                return new BuyingItem();
            }
        }

        if (statistic == null || statistic.getCount() < MIN_COUNT_IN_HISTORY) {
            return new BuyingItem();
        }
        int minBuyOutPrice = (int) (BUYOUT_PERCENT * statistic.getMinBuyOut());
        Pair<BuyType, Integer> buyType = getBuyTypeAndProfit(item, minBuyOutPrice);
        //if (!scanOnlyFirstPage) {
        // almost never happens
        if (checkFallingPrices(item, buyType)) {
            return new BuyingItem();
        }
        //}
        return new BuyingItem(index, buyType.getKey());
    }

    private boolean uselessItem(Item item) {
        int[] itemsForSkip = {2576, 10036, 4601, 25679, 851};
        if (item.getItemId() == 38082 && (item.getBuyOut() > 12_00 || item.getCurrentBid() > 12_00)) {
            return false;
        }
        for (int skip : itemsForSkip) {
            if (skip == item.getItemId()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFallingPrices(
        Item item,
        Pair<BuyType, Integer> buyType)
    {
        // check that current history don't have items which are cheap too
        if (buyType.getKey() == BuyType.BUYOUT) {
            List<Item> currentItems = currentItemsOnAuction.get(item.getItemId());
            if (currentItems != null) {
                currentItems = currentItems.stream()
                    .filter(e -> e.getItemId() == item.getItemId())
                    .filter(e -> e.getBuyOut() != item.getBuyOut())
                    .collect(Collectors.toList());
                int count = 0;
                for (Item itemOnAuction : currentItems) {
                    if (getBuyTypeAndProfit(item, (int) (itemOnAuction.getBuyOut() * BUYOUT_PERCENT)).getKey() == BuyType.NONE) {
                        count++;
                    }
                }
                int n = 5;
                if (count > n) {
                    logger.info("we have > " + n + " items on auction, with which you will not get profit, don't buy it: " + item);
                    return true;
                }
            }
        }
        return false;
    }

    // TODO: move from here, analyzer only analyzes
    public void buyItem(
        Item item,
        BuyType buyType,
        int index,
        int page,
        boolean scanFullAuction)
    {
        if (buyType == BuyType.BUYOUT) {
            buyItem(item, index, item.getBuyOut(), scanFullAuction);
            logger.info("item: " + item + " buyType: " + buyType + " " + itemIdToStatistics.get(item.getItemId()));
        } else {
            if (bidManager.shouldBid(item.getAuctionId(), item.getCurrentBid())) {
                int bidPrice;
                if (item.getCurrentBid() == 0) {
                    // first bid, we can set the initial bid
                    logger.info("it's the first bid, so we set bidPrice=startBid: " + item.getStartBid() + " page=" + page);
                    bidPrice = item.getStartBid();
                } else {
                    // TODO: by this I think we can bid more expensive item than buyout
                    // somebody (maybe me) already set bid, so we need to increase it at 5%
                    bidPrice = (int) (item.getCurrentBid() * 1.05);
                    if (bidPrice <= item.getCurrentBid()) {
                        bidPrice = item.getCurrentBid() + 1;
                    }
                    logger.info("somebody bid already, so we increase current bid at 5%, currentBid: " + item.getCurrentBid() + " newBid:" + bidPrice + " page=" + page);
                }
                logger.info("item: " + item + " buyType: " + buyType + " " + itemIdToStatistics.get(item.getItemId()));
                buyItem(item, index, bidPrice, scanFullAuction);
                bidManager.saveBid(item.getAuctionId(), bidPrice);
            }
        }
    }

    private Pair<BuyType, Integer> getBuyTypeAndProfit(
        Item item,
        int minBuyOutPrice)
    {
        int profit = 0;
        BuyType buyType = BuyType.NONE;
        int currentBid = item.getRealCurrentBid();
        if (item.getBuyOut() < minBuyOutPrice && item.getBuyOut() != 0) {
            profit = minBuyOutPrice - item.getBuyOut();
            buyType = BuyType.BUYOUT;
        } else if (currentBid < minBuyOutPrice) {
            profit = minBuyOutPrice - currentBid;
            buyType = BuyType.BID;
        }
        if (profit < this.profit) {
            return Pair.of(BuyType.NONE, 0);
        }
        return Pair.of(buyType, profit);
    }

    private Map<Integer, Statistic> getMapWithStatistics(Map<Integer, List<Item>> itemIdToItems) {
        return itemIdToItems.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            v -> {
                List<Item> items = v.getValue();
                int minBuyout =
                    items.stream().map(Item::getBuyOut).mapToInt(e -> e).min().getAsInt();
                return new Statistic(items.size(), minBuyout);
            }
        ));
    }

    public void calculate() {
        List<Scan> scans = filesManager.getScans();

        saveCurrentItemsOnAuction(scans);

        filterItems(scans);

        Map<Integer, List<Item>> idToItems = getItemIdToItems(scans);
        idToItems = savePercentileOfItems(idToItems);
        itemIdToStatistics = getMapWithStatistics(idToItems);
    }

    private Map<Integer, List<Item>> getItemIdToItems(List<Scan> scans) {
        Map<Integer, List<Item>> idToItems = new HashMap<>();
        for (int i = scans.size() - 1; i >= 0; i--) {
            Scan scan = scans.get(i);
            List<Item> items = scan.getItems();
            mapItemIdToItems(items, idToItems);
        }
        return idToItems;
    }

    private void filterItems(Collection<Scan> scans) {
        Map<Integer, Item> auctionIdToItem = new HashMap<>();
        for (Scan scan : scans) {
            List<Item> items = scan.getItems();

            Iterator<Item> iterator = items.iterator();
            while (iterator.hasNext()) {
                Item item = iterator.next();
                normalizePrice(item);
                // delete many items with bid price, but without buyout
                if (item.getBuyOut() == 0) {
                    iterator.remove();
                    continue;
                }
                // here Integer.value ? how?
                Item prev = auctionIdToItem.putIfAbsent(item.getAuctionId(), item);
                if (prev == null) {
                    continue;
                }
                if (prev.getBuyOut() == item.getBuyOut()) {
                    iterator.remove();
                }
            }
        }
    }

    private void saveCurrentItemsOnAuction(Collection<Scan> scans) {
        for (Scan scan : scans) {
            if (scan.isCurrentAuction()) {
                currentItemsOnAuction = mapItemIdToItems(scan.getItems(), new HashMap<>());
                // TODO: delete?
                currentItemsOnAuction.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .forEach(this::normalizePrice);
            }
        }
    }

    private Map<Integer, List<Item>> savePercentileOfItems(Map<Integer, List<Item>> itemIdToItems) {
        for (Map.Entry<Integer, List<Item>> entry : itemIdToItems.entrySet()) {
            List<Item> list = entry.getValue();
            list.sort(Comparator.comparingInt(Item::getBuyOut));
            // TODO: save another list for bid
            //percentile
            int trash = (list.size() / 100) * REMOVE_ITEMS_PERCENT;
            // TODO: don't remove items from last scan
            // TODO: can make it much faster with heap for O(percentile)
            List<Item> subList = list.subList(trash, list.size());
            entry.setValue(subList);
        }
        return itemIdToItems;
    }

    private Map<Integer, List<Item>> mapItemIdToItems(
        Collection<Item> allItems,
        Map<Integer, List<Item>> itemIdToItems)
    {
        for (Item item : allItems) {
            itemIdToItems.compute(item.getItemId(), (key, value) -> {
                if (value == null) {
                    List<Item> l = new ArrayList<>();
                    l.add(item);
                    return l;
                }
                if (value.size() < ITEMS_TO_SAVE_FOR_ITEM_ID) {
                    value.add(item);
                }
                return value;
            });
        }
        return itemIdToItems;
    }

    private void normalizePrice(Item item) {
        if (item.getCount() > 1) {
            item.setStartBid(item.getStartBid() / item.getCount());
            item.setCurrentBid(item.getCurrentBid() / item.getCount());
            item.setBuyOut(item.getBuyOut() / item.getCount());
            item.setCount(1);
        }
    }

    private static class Statistic {
        private int count;
        private int minBuyOut;

        public Statistic(
            int count,
            int minBuyOut)
        {
            this.count = count;
            this.minBuyOut = minBuyOut;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getMinBuyOut() {
            return minBuyOut;
        }

        public void setMinBuyOut(int minBuyOut) {
            this.minBuyOut = minBuyOut;
        }


        @Override
        public String toString() {
            return "Statistic{" +
                "count=" + count +
                ", minBuyOut=" + minBuyOut +
                '}';
        }
    }

    private void buyItem(
        Item item,
        int index,
        int price,
        boolean scanFullAuction)
    {
        if (price < 0) {
            logger.error("don't buy because price:{} for item:{}", price, item);
            return;
        }
        if (scanFullAuction) {
            priceLogger.logBuyingPrice(item, price);
        }
        Writer.buyItem(wowInstance, index, price, scanFullAuction);
    }

    public void setProfit(int profit) {
        this.profit = profit;
    }
}
