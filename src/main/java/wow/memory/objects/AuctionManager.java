package wow.memory.objects;

import auction.Item;
import wow.memory.Address;
import wow.memory.MemoryAware;
import wow.memory.WowMemory;

public class AuctionManager extends MemoryAware {

    private static final Address AUCTION_POINTER_TO_ITEMS;

    static {
        AUCTION_POINTER_TO_ITEMS = Address.STATIC.AUCTION_POINTER_TO_ITEMS;
    }

    private final int pointer;

    public AuctionManager(WowMemory memory) {
        super(memory);
        setBaseAddress(AUCTION_POINTER_TO_ITEMS.getValue());
        this.pointer = readInt(AUCTION_POINTER_TO_ITEMS);
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
