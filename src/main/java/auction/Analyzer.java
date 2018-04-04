package auction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import winapi.components.WinKey;
import wow.WowInstance;

/**
 * @author alexlovkov
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);

    private final BidManager bidManager;
    private final String path;
    private Map<Integer, Statistic> itemIdToStatistics;
    private WowInstance wowInstance;
    private Map<Integer, List<Item>> currentItemsOnAuction = new HashMap<>();
    private int PROFIT = 20000;

    public Analyzer(
        WowInstance wowInstance,
        String path)
    {
        this.wowInstance = wowInstance;
        this.path = path;
        bidManager = new BidManager(path + File.separator + "bidHistory.txt");
    }

    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new Analyzer(null, "history_auction" + File.separator + "horde");
        analyzer.calculate();
        int itemId = 15084;
        Item item = new Item(41439123, itemId, 1, 0, 792201, 130000, 130000, 270103430);
        System.out.println(analyzer.getItemIdToStatistics().get(itemId) + " minPriceForSelling " + analyzer.getItemIdToStatistics().get(itemId).minBuyOut * 1.05);
    }

    // TODO: make some conditional, what do first, buyout or bid, depends on the profit.
    //run only after calculate method.
    public BuyType buyItem(
        Item item,
        int index)
    {
        Statistic statistic = itemIdToStatistics.get(item.getItemId());
        if (statistic == null || statistic.count < 100) {
            return BuyType.NONE;
        }
        double BID_PERCENT = 0.70;
        double BUYOUT_PERCENT = 0.70;
        int minBuyOutPrice = (int) (BUYOUT_PERCENT * statistic.getMinBuyOut());
        int minBidPrice = (int) (BID_PERCENT * statistic.getMinBid());

        Pair<BuyType, Integer> buyType = getBuyTypeAndProfit(item, minBuyOutPrice, minBidPrice);
        // check that current history don't have items which are cheap too
        if (buyType.getKey() == BuyType.BUYOUT) {
            List<Item> currentItems = currentItemsOnAuction.get(item.getItemId());
            List<Item> itemsOnAuction = new ArrayList<>();
            if (currentItems != null) {
                itemsOnAuction = currentItems.stream()
                    .filter(e -> e.getItemId() == item.getItemId())
                    .filter(e -> e.getBuyOut() != item.getBuyOut() && e.getCurrentBid() != item.getCurrentBid())
                    .collect(Collectors.toList());
            }
            if (currentItems != null) {
                currentItems.stream()
                    .filter(e -> e.getItemId() == item.getItemId())
                    .filter(e -> e.getBuyOut() != item.getBuyOut() && e.getCurrentBid() != item.getCurrentBid())
                    .collect(Collectors.toList());

                int count = 0;
                for (Item itemOnAuction : itemsOnAuction) {
                    if (getBuyTypeAndProfit(
                        item,
                        (int) (itemOnAuction.getBuyOut() * BUYOUT_PERCENT),
                        (int) (itemOnAuction.getCurrentBid() * BID_PERCENT)).getKey() == BuyType.NONE) {
                        count++;
                    }
                }
                int n = 10;
                if (count > n) {
                    logger.info("we have > " + n + " items on auction, with which you will not get profit, don't buy it: " + item);
                    return BuyType.NONE;
                }
            }
        }
        if (buyType.getKey() != BuyType.NONE) {
            logger.info("found item, which we should " + String.format("%-7s", buyType.getKey()) + " profit:~ " + String.format("%-8s", buyType.getValue()) +
                "| item: " + item + " | statistics: " + statistic);
            buyItem(item, buyType.getKey(), index);
        }
        return buyType.getKey();
    }

    private void buyItem(
        Item item,
        BuyType buyType,
        int index)
    {
        if (buyType == BuyType.BUYOUT) {
            buyItem(index, item.getBuyOut());
        } else {
            if (bidManager.shouldBid(item.getAuctionId(), item.getCurrentBid())) {
                // TODO: change it, (read startBid, and currentBid from memory, if currentBid != 0, the increase not by 1, by some % I think)
                int bidPrice = item.getCurrentBid() + 1;
                buyItem(index, bidPrice);
                bidManager.saveBid(item.getAuctionId(), bidPrice);
            }
        }
    }

    private boolean buyItem(
        int index,
        int price)
    {
        logger.info("buying item, index:" + index + " price:" + price);
        wowInstance.click(WinKey.ENTER);
        wowInstance.clickEditing(WinKey.SLASH);
        wowInstance.clickEditing(WinKey.r);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.SPACEBAR);
        wowInstance.clickEditing(WinKey.P);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.a);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.e);
        wowInstance.clickEditing(WinKey.A);
        wowInstance.clickEditing(WinKey.u);
        wowInstance.clickEditing(WinKey.c);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.o);
        wowInstance.clickEditing(WinKey.n);
        wowInstance.clickEditing(WinKey.B);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.d);
        wowInstance.clickEditing(WinKey.LEFT_BRACKET);
        wowInstance.clickEditing(WinKey.DOUBLE_QUOTES);
        wowInstance.clickEditing(WinKey.l);
        wowInstance.clickEditing(WinKey.i);
        wowInstance.clickEditing(WinKey.s);
        wowInstance.clickEditing(WinKey.t);
        wowInstance.clickEditing(WinKey.DOUBLE_QUOTES);
        wowInstance.clickEditing(WinKey.COMMA);
        writeNumber(index);
        wowInstance.clickEditing(WinKey.COMMA);
        writeNumber(price);
        wowInstance.clickEditing(WinKey.RIGHT_BRACKET);
        wowInstance.click(WinKey.ENTER);
        return true;
    }

    public void writeNumber(int x) {
        List<Integer> list = new ArrayList<>();
        while (x > 0) {
            list.add(x % 10);
            x /= 10;
        }
        Collections.reverse(list);
        for (Integer digit : list) {
            wowInstance.clickEditing(WinKey.mapIntToWinKey(digit));
        }
    }

    private Pair<BuyType, Integer> getBuyTypeAndProfit(
        Item item,
        int minBuyOutPrice,
        int minBidPrice)
    {
        int profit = 0;
        BuyType buyType = BuyType.NONE;
        if (item.getBuyOut() < minBuyOutPrice && item.getBuyOut() != 0) {
            profit = minBuyOutPrice - item.getBuyOut();
            buyType = BuyType.BUYOUT;
        } else if (item.getCurrentBid() < minBidPrice) {
            profit = minBidPrice - item.getCurrentBid();
            buyType = BuyType.BID;
        }

        if (profit < PROFIT) {
            return new Pair<>(BuyType.NONE, 0);
        }
        return new Pair<>(buyType, profit);
    }

    private List<Scan> readFiles() throws IOException {
        List<Scan> scans = new ArrayList<>();
        for (File file : new File(path).listFiles()) {
            if ("bidHistory.txt".equals(file.getName()) || "logPrices.txt".equals(file.getName())) {
                continue;
            }
            List<String> strings = Files.readAllLines(Paths.get(file.getPath()));
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
            if (strings.size() < 10000) {
                continue;
            }
            Date date = null;
            try {
                date = df.parse(strings.get(0));
            } catch (ParseException e) {
                logger.error("can't parse:" + strings.get(0) + " file:" + file.getAbsolutePath());
                System.exit(0);
            }
            String statistics = strings.get(1);
            ArrayList<Item> items = new ArrayList<>(strings.size() - 2);
            for (int i = 2; i < strings.size(); i++) {
                String s = strings.get(i);
                int[] ints = Arrays.stream(s.split("\\s+")).map(Integer::valueOf).mapToInt(x -> x).toArray();
                Item item = new Item(ints[0], ints[1], ints[2], ints[3], ints[4], ints[5], ints[6], ints[7]);
                items.add(item);
            }
            scans.add(new Scan(date, items, statistics));
            if ("tmp.txt".equals(file.getName())) {
                currentItemsOnAuction = mapItemIdToItems(items);
                normalizePrice(currentItemsOnAuction.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
            }
        }
        Collections.sort(scans);
        return scans;
    }

    private Map<Integer, Statistic> getMapWithStatistics(Set<Item> allItems) {
        Map<Integer, Statistic> map = new HashMap<>();
        for (Item item : allItems) {
            map.compute(item.getItemId(), (key, value) -> {
                if (value == null) {
                    return new Statistic(1, item.getBuyOut(), item.getCurrentBid());
                }
                value.setMinBuyOut(Math.min(value.getMinBuyOut(), item.getBuyOut()));
                value.setMinBid(Math.min(value.getMinBid(), item.getCurrentBid()));
                value.setCount(value.getCount() + 1);
                return value;
            });
        }
        return map;
    }

    void calculate() throws IOException, ParseException {
        List<Scan> scans = readFiles();
        Set<Item> allItems = new HashSet<>();
        for (Scan scan : scans) {
            allItems.addAll(scan.items);
        }
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
            int trash = (list.size() / 100) * 5;
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

    public Map<Integer, Statistic> getItemIdToStatistics() {
        return itemIdToStatistics;
    }

    enum BuyType {
        BUYOUT,
        BID,
        NONE
    }

    private static class Statistic {
        private int count;
        private int minBuyOut;
        private int minBid;

        public Statistic(
            int count,
            int minBuyOut,
            int minBid)
        {
            this.count = count;
            this.minBuyOut = minBuyOut;
            this.minBid = minBid;
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

        public int getMinBid() {
            return minBid;
        }

        public void setMinBid(int minBid) {
            this.minBid = minBid;
        }

        @Override
        public String toString() {
            return "Statistic{" +
                "count=" + count +
                ", minBuyOut=" + minBuyOut +
                ", minBid=" + minBid +
                '}';
        }
    }

    private static class Scan implements Comparable<Scan> {
        private final String statistics;
        private Date date;
        private List<Item> items;
        private int minAuctionId = Integer.MAX_VALUE;
        private int maxAuctionId = Integer.MIN_VALUE;
        private int minExpiredTime = Integer.MAX_VALUE;
        private int maxExpiredTime = Integer.MIN_VALUE;

        public Scan(
            Date date,
            List<Item> items,
            String statistics)
        {
            this.date = date;
            this.items = items;
            this.statistics = statistics;
            for (Item item : items) {
                minAuctionId = Math.min(minAuctionId, item.getAuctionId());
                maxAuctionId = Math.max(maxAuctionId, item.getAuctionId());
                minExpiredTime = Math.min(minExpiredTime, item.getAuctionId());
                maxExpiredTime = Math.max(maxExpiredTime, item.getAuctionId());
            }
        }

        @Override
        public int compareTo(Scan o) {
            return date.compareTo(o.date);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Scan scan = (Scan) o;

            return date != null ? date.equals(scan.date) : scan.date == null;
        }

        @Override
        public int hashCode() {
            return date != null ? date.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "Scan{" +
                "date=" + date +
                ", minAuctionId=" + minAuctionId +
                ", maxAuctionId=" + maxAuctionId +
                ", minExpiredTime=" + minExpiredTime +
                ", maxExpiredTime=" + maxExpiredTime +
                '}';
        }

        public Date getDate() {
            return date;
        }

        public List<Item> getItems() {
            return items;
        }

        public String getStatistics() {
            return statistics;
        }
    }
}
