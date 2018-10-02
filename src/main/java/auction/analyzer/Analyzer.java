package auction.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import auction.BuyType;
import auction.BuyingItem;
import auction.Item;
import auction.PriceLogger;
import auction.Scan;
import auction.Writer;
import auction.dao.BidManager;
import auction.dao.FilesManager;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wow.WowInstance;

/**
 * @author alexlovkov
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);

    private final BidManager bidManager;
    private final PriceLogger priceLogger;
    private final FilesManager filesManager;
    private Map<Integer, Statistic> itemIdToStatistics;
    private final WowInstance wowInstance;
    private Map<Integer, List<Item>> currentItemsOnAuction = new HashMap<>();

    private int profit = 50000;
    private static final double BUYOUT_PERCENT = 0.70;
    private static final int REMOVE_ITEMS_PERCENT = 2;

    public Analyzer(
        WowInstance wowInstance,
        String path,
        PriceLogger priceLogger,
        FilesManager filesManager,
        boolean scanOnlyFirstPage)
    {
        this.wowInstance = wowInstance;
        this.bidManager = new BidManager(path + File.separator + "bidHistory.txt");
        this.priceLogger = priceLogger;
        this.filesManager = filesManager;
        if (scanOnlyFirstPage) {
            profit = 250000;
        }
    }

    public static void main(String[] args) throws IOException {
        String path = "history_auction/alliance";
        Analyzer analyzer = new Analyzer(null, path, null, new FilesManager(path), false);
        analyzer.calculate();
        Statistic statistic = analyzer.itemIdToStatistics.get(22451);
        System.out.println(statistic);
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
            if (item.getBuyOut() < 70000000) {
                return new BuyingItem(BuyType.BUYOUT);
            }
            if (item.getCurrentBid() < 70000000) {
                return new BuyingItem(BuyType.BID);
            }
        }

        if (uselessItem(item)) {
            return new BuyingItem(BuyType.NONE);
        }

        Statistic statistic = itemIdToStatistics.get(item.getItemId());
        if (statistic == null || statistic.count < 20) {
            return new BuyingItem(BuyType.NONE);
        }
        int minBuyOutPrice = (int) (BUYOUT_PERCENT * statistic.getMinBuyOut());
        Pair<BuyType, Integer> buyType = getBuyTypeAndProfit(item, minBuyOutPrice);
        //if (!scanOnlyFirstPage) {
        // almost never happens
        if (checkFallingPrices(item, buyType)) {
            return new BuyingItem(BuyType.NONE);
        }
        //}

        if (buyType.getKey() != BuyType.NONE) {
            /*
            logger.info("found item, which we should " + String.format("%-7s", buyType.getKey())
                + " profit:~ " + String.format("%-8s", buyType.getValue())
                + "| item: " + item + " | statistics: " + statistic);
                */
        }
        return new BuyingItem(index, buyType.getKey());
    }

    private boolean uselessItem(Item item) {
        int[] itemsForSkip = {2576, 10036, 4601, 25679, 851};
        if (item.getItemId() == 38082 && (item.getBuyOut() > 1200 || item.getCurrentBid() > 1200)) {
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
            return new Pair<>(BuyType.NONE, 0);
        }
        return new Pair<>(buyType, profit);
    }

    private Map<Integer, Statistic> getMapWithStatistics(Set<Item> allItems) {
        Map<Integer, Statistic> map = new HashMap<>();
        for (Item item : allItems) {
            map.compute(item.getItemId(), (key, value) -> {
                if (value == null) {
                    return new Statistic(1, item.getBuyOut());
                }
                value.setMinBuyOut(Math.min(value.getMinBuyOut(), item.getBuyOut()));
                value.setCount(value.getCount() + 1);
                return value;
            });
        }
        return map;
    }

    public void calculate() throws IOException {
        List<Scan> scans = filesManager.readFiles();
        for (Scan scan : scans) {
            if (scan.isCurrentAuction()) {
                currentItemsOnAuction = mapItemIdToItems(scan.getItems());
                normalizePrice(currentItemsOnAuction.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
            }
        }
        Set<Item> allItems = new HashSet<>();
        for (Scan scan : scans) {
            allItems.addAll(scan.getItems());
        }
        // delete many items with bid price, but without buyout
        allItems = allItems.stream().filter(e -> e.getBuyOut() != 0).collect(Collectors.toSet());
        normalizePrice(allItems);
        Map<Integer, List<Item>> itemIdToItems = savePercentileOfItems(allItems);
        allItems.clear();
        for (Map.Entry<Integer, List<Item>> entry : itemIdToItems.entrySet()) {
            allItems.addAll(entry.getValue());
        }
        itemIdToStatistics = getMapWithStatistics(allItems);
    }

    private Map<Integer, List<Item>> savePercentileOfItems(Set<Item> allItems) {
        Map<Integer, List<Item>> itemIdToItems = mapItemIdToItems(allItems);
        for (Map.Entry<Integer, List<Item>> entry : itemIdToItems.entrySet()) {
            List<Item> list = entry.getValue();
            list.sort(Comparator.comparingInt(Item::getBuyOut));
            // TODO: add filter buyout 0
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

    private Map<Integer, List<Item>> mapItemIdToItems(Collection<Item> allItems) {
        Map<Integer, List<Item>> itemIdToItems = new HashMap<>();
        for (Item item : allItems) {
            itemIdToItems.compute(item.getItemId(), (key, value) -> {
                if (value == null) {
                    List<Item> l = new ArrayList<>();
                    l.add(item);
                    return l;
                }
                value.add(item);
                return value;
            });
        }
        return itemIdToItems;
    }

    private void normalizePrice(Collection<Item> allItems) {
        for (Item item : allItems) {
            if (item.getCount() > 1) {
                item.setCurrentBid(item.getCurrentBid() / item.getCount());
                item.setBuyOut(item.getBuyOut() / item.getCount());
                item.setCurrentBid(item.getCurrentBid() / item.getCount());
                item.setCount(1);
            }
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
