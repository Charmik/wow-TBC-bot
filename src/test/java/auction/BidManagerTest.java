package auction;

import java.io.File;

import auction.dao.BidManager;
import auction.dao.BidManagerImpl;
import auction.dao.BidManagerStub;
import org.junit.Assert;
import org.junit.Test;

public class BidManagerTest {

    @Test
    public void testBidManagers() {
        String fileName = "history_auction" + File.separator + "testBidHistory.txt";
        BidManager bidManager = new BidManagerImpl(fileName);
        testBidManager(bidManager);
        testBidManager(new BidManagerStub());
        new File(fileName).delete();
    }

    private void testBidManager(BidManager bidManager) {
        bidManager.saveBid(0, 0);
        bidManager.saveBid(4, 4);
        bidManager.saveBid(2, 2);
        bidManager.saveBid(6, 6);

        Assert.assertFalse(bidManager.shouldBid(0, 0));

        Assert.assertTrue(bidManager.shouldBid(1, 1));
        Assert.assertFalse(bidManager.shouldBid(2, 1));
        Assert.assertTrue(bidManager.shouldBid(3, 3));
        Assert.assertFalse(bidManager.shouldBid(4, 2));
        Assert.assertTrue(bidManager.shouldBid(5, 4));

        Assert.assertTrue(bidManager.shouldBid(2, 3));
        Assert.assertFalse(bidManager.shouldBid(2, 1));
    }

}
