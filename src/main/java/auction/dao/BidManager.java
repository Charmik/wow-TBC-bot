package auction.dao;

/**
 * @author alexlovkov
 */
public interface BidManager {

    /**
     * @param auctionId  id of the auction item
     * @param currentBid current bid for this auction-item
     * @return true if last bid wasn't made by us
     */
    boolean shouldBid(int auctionId, int currentBid);

    /**
     * save our bid for specific item on the auction
     *
     * @param auctionId  id of the auction-item
     * @param currentBid our bid for this item
     */
    void saveBid(int auctionId, int currentBid);
}
