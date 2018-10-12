package auction.dao;

import java.util.Collection;
import java.util.List;

import auction.Item;
import auction.Scan;

/**
 * @author alexlovkov
 */
public interface AuctionDao {

    List<Scan> getScans();

    boolean save(Collection<Item[]> items);
}
