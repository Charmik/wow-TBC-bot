package auction;

public class Item implements Comparable<Item> {

    private int auctionId;
    private int itemId;
    private int count;
    //-1 if you can make "right click to item", 0 otherwise
    private int usableItem;

    private int playerId;
    private int startBid;
    private int currentBid;
    private int buyOut;
    private int expireTime;

    public Item(
        int auctionId,
        int itemId,
        int count,
        int usableItem,
        int playerId,
        int startBid,
        int currentBid,
        int buyOut,
        int expireTime)
    {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.count = count;
        this.usableItem = usableItem;
        this.playerId = playerId;
        this.startBid = startBid;
        this.currentBid = currentBid;
        this.buyOut = buyOut;
        this.expireTime = expireTime;
    }

    public Item(int[] arr) {
        this.auctionId = arr[1];
        this.itemId = arr[2];
        this.count = arr[23];
        this.usableItem = arr[24];
        this.playerId = arr[26];
        this.startBid = arr[28];
        this.currentBid = arr[34];
        this.buyOut = arr[30];
        this.expireTime = arr[31];
    }

    @Override
    public String toString() {
        return auctionId + " " +
            String.format("%-5s", itemId) + " " +
            String.format("%-2s", count) + " " +
            String.format("%-2s", usableItem) + " " +
            String.format("%-7s", playerId) + " " +
            String.format("%-8s", startBid) + " " +
            String.format("%-8s", currentBid) + " " +
            String.format("%-8s", buyOut) + " " +
            expireTime;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public int getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }


    public int getUsableItem() {
        return usableItem;
    }

    public int getCurrentBid() {
        return currentBid;
    }

    public int getBuyOut() {
        return buyOut;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setUsableItem(int usableItem) {
        this.usableItem = usableItem;
    }

    public void setCurrentBid(int currentBid) {
        this.currentBid = currentBid;
    }

    public void setBuyOut(int buyOut) {
        this.buyOut = buyOut;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public int getStartBid() {
        return startBid;
    }

    public void setStartBid(int startBid) {
        this.startBid = startBid;
    }

    public int getRealCurrentBid() {
        if (currentBid == 0) {
            return startBid;
        }
        return currentBid;
    }

    @Override
    // TODO: add more fields?
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        return auctionId == ((Item) o).auctionId;
    }

    @Override
    public int hashCode() {
        return auctionId;
    }

    @Override
    public int compareTo(Item o) {
        return Integer.compare(auctionId, o.auctionId);
    }

    boolean compareFields(Item o) {
        return //auctionId == o.auctionId &&
            itemId == o.itemId &&
            count == o.count &&
            //usableItem == o.usableItem &&
            //playerId == o.playerId &&
            currentBid == o.currentBid &&
            buyOut == o.buyOut;
//            expireTime == o.expireTime;
    }
}
