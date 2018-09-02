package auction;

/**
 * @author alexlovkov
 */

/**
 * class describes how we should buy item, what's price and index on the page
 */
public class BuyingItem {

    /**
     * index on the page 1..50
     */
    private int index;
    /**
     * type how to buy this item
     */
    private BuyType buyType;

    public BuyingItem(
        int index,
        BuyType buyType)
    {
        this.index = index;
        this.buyType = buyType;
    }

    public BuyingItem(BuyType none) {
        index = -1;
        this.buyType = BuyType.NONE;
    }

    public int getIndex() {
        return index;
    }

    public BuyType getBuyType() {
        return buyType;
    }
}
