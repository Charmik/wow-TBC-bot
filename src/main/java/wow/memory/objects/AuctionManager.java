package wow.memory.objects;

import auction.Buyer;
import auction.Item;
import auction.ItemNotFoundException;
import auction.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.Address;
import wow.memory.MemoryAware;
import wow.memory.WowMemory;

public class AuctionManager extends MemoryAware {

    private static final Logger logger = LoggerFactory.getLogger(AuctionManager.class);

    private static final Address AUCTION_POINTER_TO_ITEMS;
    private static final Address ITEM_ID_IN_BAG_BY_BAG_ID_AND_SLOT;

    static {
        AUCTION_POINTER_TO_ITEMS = Address.STATIC.AUCTION_POINTER_TO_ITEMS;
        ITEM_ID_IN_BAG_BY_BAG_ID_AND_SLOT = Address.STATIC.ITEM_ID_IN_BAG_BY_BAG_ID_AND_SLOT;
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
        int lastAuctionId = -1;
        if (itemsFromPreviousPage != null) {
            lastAuctionId = itemsFromPreviousPage[0].getAuctionId();
        }
        int countGetPreviousPage = 1;
        do {
            itemsFromPreviousPage = getItemsFromCurrentPage();
            countGetPreviousPage++;
            //try to force nextPage again, we will miss this page, doesn't matter, bug wow.
            if (countGetPreviousPage % 10000 == 0) {
                nextPage();
                Utils.sleep(Buyer.SLEEP2);
            }
            //found lastPage
            if (countGetPreviousPage == 30000) {
                logger.info("found last page");
                foundLastPage = true;
                break;
            }
        } while (itemsFromPreviousPage[0].getAuctionId() == lastAuctionId);
        if (foundLastPage) {
            return null;
        }
        return itemsFromPreviousPage;
    }

    public void nextPage() {
        WowInstance.getInstance().click(WinKey.D4);
    }

    public Item[] getItemsFromCurrentPage() {
//        Item[] itemsByManyReads = getItemsByManyReads();
        Item[] itemsByOneRead = getItemsByOneRead();
        /*
        for (int i = 0; i < itemsByManyReads.length; i++) {
            Item item1 = itemsByManyReads[i];
            Item item2 = itemsByOneRead[i];
            if (!item1.compareAllFieldsExceptTime(item2)) {
                logger.error(item1 + " not equals i=" + i + " " + item2);
                Runtime.getRuntime().exit(1);
//                throw new RuntimeException(item1 + " not equals i=" + i + " " + item2);
            }
        }
        */
        return itemsByOneRead;
    }

    private Item[] getItemsByManyReads() {
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

    private Item[] getItemsByOneRead() {
        Item[] items = new Item[50];
        int arrayElementForItem = 36;
        int index = 0;
        int[] ints = readBlock(pointer, Address.OFFSET.AUCTION_ITEM_INFORMATION_PAGE);
        for (int i = 0; i < 50; i++) {
            int[] currentItem = new int[arrayElementForItem];
            System.arraycopy(ints, index, currentItem, 0, arrayElementForItem);
            items[i] = new Item(currentItem);
            index += arrayElementForItem;
        }
        return items;
    }

    public int getTickCount() {
        return super.getTickCount();
    }

    /**
     * return itemId in the bad or -1 if it's empty slot
     */
    public int getItemId(WowInstance wowInstance, int bag, int slot) throws ItemNotFoundException {
        // TODO: check disconnect
        Writer.useMacroForGettingItemId(wowInstance, 0, 1);
        int waterId = getIdFromString(readString(ITEM_ID_IN_BAG_BY_BAG_ID_AND_SLOT));
        if (waterId == 159) {
            Writer.useMacroForGettingItemId(wowInstance, bag, slot);
            String string = readString(ITEM_ID_IN_BAG_BY_BAG_ID_AND_SLOT);
            int itemId = getIdFromString(string);
            if (itemId == 159) {
                itemId = -1;
            }
            return itemId;
        } else {
            logger.error("you don't have water with id=159 in 1 slot of bag");
            throw new ItemNotFoundException("first item in the bag is not a water with itemId = 159");
        }
    }

    private int getIdFromString(String s) {
        return Integer.valueOf(s.substring(0, s.indexOf(':')));
    }
}
