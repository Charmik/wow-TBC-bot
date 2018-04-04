package wow.memory.objects;

import auction.Item;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.Address;
import wow.memory.MemoryAware;
import wow.memory.WowMemory;

public class AuctionManager extends MemoryAware {

    private static final Address AUCTION_POINTER_TO_ITEMS;

    static {
        AUCTION_POINTER_TO_ITEMS = Address.STATIC.AUCTION_POINTER_TO_ITEMS;
    }

    private final int pointer;
    private Item[] itemsFromPreviousPage = null;

    public AuctionManager(WowMemory memory) {
        super(memory);
        setBaseAddress(AUCTION_POINTER_TO_ITEMS.getValue());
        this.pointer = readInt(AUCTION_POINTER_TO_ITEMS);
    }

    public Item[] getItemsFromCurrentPageWithRetry() {
        boolean foundLastPage = false;

        int lastExpiredTime = -1;
        if (itemsFromPreviousPage != null) {
            lastExpiredTime = itemsFromPreviousPage[0].getExpireTime();
        }
        int countGetPreviousPage = 1;
        do {
            itemsFromPreviousPage = getItemsFromCurrentPage();
            countGetPreviousPage++;
            //try to force nextPage again, we will miss this page, doesn't matter, bug wow.
            if (countGetPreviousPage % 10000 == 0) {
                nextPage();
            }
            //found lastPage
            if (countGetPreviousPage == 30000) {
                foundLastPage = true;
                break;
            }
        } while (itemsFromPreviousPage[0].getExpireTime() == lastExpiredTime);
        if (foundLastPage) {
            return null;
        }
        return itemsFromPreviousPage;
    }

    public void nextPage() {
        WowInstance.getInstance().click(WinKey.D4);
    }

    public Item[] getItemsFromCurrentPage() {
        Item[] items = new Item[50];
        int localPointer = pointer;
        for (int i = 0; i < 50; i++) {
            int[] ints = readBlock(localPointer, Address.OFFSET.AUCTION_ITEM_INFORMATION);
            localPointer += Address.OFFSET.AUCTION_ITEM_INFORMATION.getBytes();
            Item item = new Item(ints);
            items[i] = item;
        }
        return items;
    }
}
