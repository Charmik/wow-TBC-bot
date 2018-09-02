package auction;

import java.util.Date;
import java.util.List;

/**
 * @author alexlovkov
 */
public class Scan implements Comparable<Scan> {

    private Date date;
    private List<Item> items;
    private boolean currentAuction;
    private int minAuctionId = Integer.MAX_VALUE;
    private int maxAuctionId = Integer.MIN_VALUE;
    private int minExpiredTime = Integer.MAX_VALUE;
    private int maxExpiredTime = Integer.MIN_VALUE;

    public Scan(
        Date date,
        List<Item> items,
        boolean currentAuction)
    {
        this.date = date;
        this.items = items;
        this.currentAuction = currentAuction;
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

    public List<Item> getItems() {
        return items;
    }

    public boolean isCurrentAuction() {
        return currentAuction;
    }
}
