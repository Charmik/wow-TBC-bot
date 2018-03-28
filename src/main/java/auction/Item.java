package auction;

import java.util.Objects;

public class Item implements Comparable<Item> {
    private final int auctionId;
    private final int itemId;
    private final int count;
    //-1 if you can make "right click to item", 0 otherwise
    private final int usableItem;
    private final int xxx;
    private final int currentBid;
    private final int buyOut;
    private final int expireTime;


    //from 3.0.9
    /*
    unsigned int Unk00;                // 0x00
    unsigned int AuctionId;            // 0x04
    unsigned int ItemEntry;            // 0x08
    WoWAucEnchantInfo EnchantInfo[7];    // 0x0C
    unsigned int RandomPropertyID;    // 0x60
    unsigned int ItemSuffixFactor;    // 0x64
    unsigned int Count;                // 0x68
    unsigned int SpellCharges;        // 0x6C
    unsigned int Unk70;                // 0x70
    unsigned int Unk74;                // 0x74
    unsigned int SellerGuidB;        // 0x78
    unsigned int SellerGuidA;        // 0x7C
    unsigned int StartBid;            // 0x80
    unsigned int MinBidInc;            // 0x84
    unsigned int BuyOut;            // 0x88
    unsigned int ExpireTime;        // 0x8C
    unsigned int BidderGuidB;        // 0x90
    unsigned int BidderGuidA;        // 0x94
    unsigned int CurrentBid;        // 0x98
    unsigned int SaleStatus;        // 0x9C
     */

    public Item(
        int auctionId,
        int itemId,
        int count,
        int usableItem,
        int xxx,
        int currentBid,
        int buyOut,
        int expireTime)
    {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.count = count;
        this.usableItem = usableItem;
        this.xxx = xxx;
        this.currentBid = currentBid;
        this.buyOut = buyOut;
        this.expireTime = expireTime;
    }

    public Item(int[] arr) {
        this.auctionId = arr[1];
        this.itemId = arr[2];
        this.count = arr[23];
        this.usableItem = arr[24];
        // TODO: understand what is it
        this.xxx = arr[26];
        this.currentBid = arr[28];
        this.buyOut = arr[30];
        this.expireTime = arr[31];
    }

    @Override
    public String toString() {
        return auctionId + " " + itemId + " " + count + " " + usableItem + " " + xxx + " " + currentBid + " " + buyOut + " " + expireTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return auctionId == item.auctionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(auctionId);
    }

    @Override
    public int compareTo(Item o) {
        return Integer.compare(auctionId, o.auctionId);
    }
}
