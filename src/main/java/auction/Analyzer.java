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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alexlovkov
 */
public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class);

    private final String path;
    private Map<Integer, Statistic> itemIdToStatistics;


    public Analyzer(String path) {
        this.path = path;
    }

    /*
    public static void main(String[] args) throws IOException, ParseException {
        Analyzer analyzer = new Analyzer("history_auction" + File.separator + "alliance");
        analyzer.calculate();

        analyzer.buyItem(new Item(1, 21886, 1, 1, 1, 190000, 190000, 1));
        analyzer.buyItem(new Item(1, 21886, 1, 1, 1, 200000, 200000, 1));
        analyzer.buyItem(new Item(1, 21886, 1, 1, 1, 210000, 210000, 1));
        analyzer.buyItem(new Item(1, 21886, 1, 1, 1, 220000, 220000, 1));
        analyzer.buyItem(new Item(1, 21886, 1, 1, 1, 230000, 230000, 1));
    }
    */

    public boolean buyItem(Item item) {
        Statistic statistic = itemIdToStatistics.get(item.getItemId());
        if (statistic == null) {
            return false;
        }
        if (statistic.count < 50) {
            return false;
        }
        if (item.getBuyOut() == 0) {
            return false;
        }
        int minPrice = (int) (0.90 * statistic.min);
        if (item.getBuyOut() < minPrice) {
            int profit = minPrice - item.getBuyOut();
            if (profit < 10000) {
                return false;
            }
            logger.info("found item, which we should buy profit:~ " + String.format("%-8s", profit) + "| item: " + item + " | statistics: " + statistic);
            return true;
        }
        return false;
    }

    private List<Scan> readFiles() throws IOException, ParseException {
        List<Scan> scans = new ArrayList<>();
        for (File file : new File(path).listFiles()) {
            List<String> strings = Files.readAllLines(Paths.get(file.getPath()));
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
            Date date = df.parse(strings.get(0));
            String statistics = strings.get(1);
            ArrayList<Item> items = new ArrayList<>(strings.size() - 2);
            for (int i = 2; i < strings.size(); i++) {
                String s = strings.get(i);
                int[] ints = Arrays.stream(s.split("\\s+")).map(Integer::valueOf).mapToInt(x -> x).toArray();
                Item item = new Item(ints[0], ints[1], ints[2], ints[3], ints[4], ints[5], ints[6], ints[7]);
                items.add(item);
            }
            scans.add(new Scan(date, items, statistics));
        }
        Collections.sort(scans);
        return scans;
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

    private Map<Integer, Statistic> getMapWithStatistics(Set<Item> allItems) {
        Map<Integer, Statistic> map = new HashMap<>();
        System.out.println("allItems.size():" + allItems.size());
        for (Item item : allItems) {
            map.compute(item.getItemId(), (key, value) -> {
                if (value == null) {
                    return new Statistic(1, item.getBuyOut());
                }
                value.min = Math.min(value.min, item.getBuyOut());
                value.count++;
                return value;
            });
        }
        return map;
    }

    private Map<Integer, List<Item>> savePercentileOfItems(Set<Item> allItems) {
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

        for (Map.Entry<Integer, List<Item>> entry : itemIdToItems.entrySet()) {
            List<Item> list = entry.getValue();
            list.sort(Comparator.comparingInt(Item::getBuyOut));
            //percentile
            int trash = (list.size() / 100) * 5;
            // TODO: don't remove items from last 48 hours
            entry.setValue(list.subList(trash, list.size()));
        }
        return itemIdToItems;
    }

    private void normalizePrice(Set<Item> allItems) {
        for (Item item : allItems) {
            if (item.getCount() > 1) {
                item.setCurrentBid(item.getCurrentBid() / item.getCount());
                item.setBuyOut(item.getBuyOut() / item.getCount());
                item.setCurrentBid(item.getCurrentBid() / item.getCount());
                item.setCount(1);
            }
        }
    }

    private class Statistic {
        int count;
        int min;

        public Statistic(
            int count,
            int min)
        {
            this.count = count;
            this.min = min;
        }

        @Override
        public String toString() {
            return "Statistic{" +
                "count=" + count +
                ", min=" + min +
                '}';
        }
    }

    private class Scan implements Comparable<Scan> {
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
