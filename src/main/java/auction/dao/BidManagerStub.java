package auction.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * @author alexlovkov
 */
public class BidManagerStub implements BidManager {

    private final Map<Integer, Integer> map = new HashMap<>();

    @Override
    public boolean shouldBid(int auctionId, int currentBid) {
        Integer prevBid = map.get(auctionId);
        if (prevBid == null) {
            map.put(auctionId, currentBid);
            return true;
        }
        boolean ans;
        if (prevBid >= currentBid) {
            ans = false;
        } else {
            ans = true;
        }
        map.put(auctionId, currentBid);
        return ans;
    }

    @Override
    public void saveBid(int auctionId, int currentBid) {
        map.put(auctionId, currentBid);
    }
}
