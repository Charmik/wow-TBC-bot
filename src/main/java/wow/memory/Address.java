package wow.memory;

import com.sun.jna.Pointer;

/**
 * @author Cargeh
 */
public interface Address {

    Pointer getPointer();

    int getBytes();

    long getValue();

    enum STATIC implements Address {
        /* ------- GENERAL  ------- */
        CLIENT_CONNECTION(0x00D43318, 4),
        CHAR_BASE_POINTER(0x00E29D28, 4),

        /* ------- CHARACTER ------- */
        CHAR_ACCNAME(0x00D43128, 12), // TODO [cargeh]: Test bytes
        CHAR_ISINGAME(0x00C6EA08, 1),
        CHAR_NAME(0x00D43348, 12), // TODO [cargeh]: Test bytes
        CHAR_ZONEID(0x00C6E9AC, 4),
        CHAR_LVL(0x00CEF5D0, 4),
        CHAR_ANGLE(0x00C71C2C, 4),

        /* ------- TARGET ------- */
        TARGET_GUID(0x0C6E960, 8),
        TEST(0x0C6E960, 320),
        TARGET_VENDOR_WINDOW_ID(0x00C8920C, 4),
        TARGET_COMBO_POINTS(0x00C6E9E1, 1),

        /* ------- CURSOR ------- */
        CURSOR_STATE(0x00CF5750, 1),
        CURSOR_HOVER_OVER_OBJECT_GUID(0x00C6E950, 8),

        /* ------- CTM ------- */
        CTM_X(0x00D68A18, 4),
        CTM_Y(0x00D68A1C, 4),
        CTM_Z(0x00D68A20, 4),
        CTM_PUSH(0x00D689BC, 4),
        CTM_GUID(0x00D689C0, 8),
        CTM_MYSTERY(0x00D689AE, 4),
        CTM_MYSTERY_GATHERING(0x4090, 4),
        CTM_MYSTERY_NOTHING(0, 4),
        CTM_MYSTERY_MOVING(0x3F00, 4),
        CTM_MYSTERY_ATTACKING(0x406A, 4),
        CTM_MYSTERY_INTERACT_UNIT(0x4031, 4),
        CTM_MYSTERY_INTERACT_OBJECT(0x4090, 4),

        /* ------- AUCTION ------- */
        AUCTION_POINTER_TO_ITEMS(0x00CE0CC8, 4),

        // USE ONLY you wrote /charm ID_BAG ID_ITEM_IN_BAG in chat by addon
        ITEM_ID_IN_BAG_BY_BAG_ID_AND_SLOT(0x00CF4231,10);

        private final long address;
        private final int bytes;
        private Pointer pointer;

        STATIC(
            long address,
            int bytes)
        {
            this.address = address;
            this.bytes = bytes;
        }

        @Override
        public Pointer getPointer() {
            if (pointer == null)
                pointer = new Pointer(address);
            return pointer;
        }

        @Override
        public int getBytes() {
            return bytes;
        }

        @Override
        public long getValue() {
            return address;
        }
    }

    enum OFFSET implements Address {

        /* ------- CHARACTER  ------- */
        CHAR_BLOCK(0x2698, 320),
        CHAR_HEALTH(0x2698, 4),

        CHAR_HEALTH_HEALTH_BLOCK(0x2698, 40),
        CHAR_MANA(0x269C, 4),
        CHAR_RAGE(0x26A0, 4),
        CHAR_MAXIMUM_HEALTH(0x26B0, 4),
        CHAR_MAXIMUM_MANA(0x26B4, 4),
        CHAR_ENERGY(0x26A8, 4),
        CHAR_CASTING_SPELL_ID(0xF40, 4),
        CHAR_X(0xBF0, 4),
        CHAR_Y(0xBF4, 4),
        CHAR_Z(0xBF8, 4),
        CHAR_STATE(0x26F8, 4),

        CHAR_MOVEMENT_STATE(0xC20, 2),
        /* ------- OBJECTS ------- */
        OBJ_MANAGER(0x2218, 4),
        OBJ_FIRST(0xAC, 4),
        OBJ_NEXT(0x3C, 4),

        /* ------- BASE ------- */
        OBJ_BASE_GUID(0x30, 8),
        OBJ_BASE_TYPE(0x14, 4),
        OBJ_BASE_X(0xBF0, 4),
        OBJ_BASE_Y(0xBF4, 4),
        OBJ_BASE_Z(0xBF8, 4),

        /* ------- PLAYERS ------- */
        OBJ_PLAYER_ISCASTING(0xf3c, 4),

        /* ------- AUCTION ------- */
        AUCTION_ITEM_INFORMATION(0x00, 144),
        AUCTION_ITEM_INFORMATION_PAGE(0x00, 7200);

        private final long address;
        private final int bytes;
        private Pointer pointer;

        OFFSET(
            long address,
            int bytes)
        {
            this.address = address;
            this.bytes = bytes;
        }

        @Override
        public Pointer getPointer() {
            if (pointer == null)
                pointer = new Pointer(address);
            return pointer;
        }

        @Override
        public int getBytes() {
            return bytes;
        }

        @Override
        public long getValue() {
            return address;
        }
    }

    enum DESCRIPTOR implements Address {
        DESCRIPTOR_OFFSET(0x120, 8),

        CHAR_COMBAT_STATE(0xA2, 1),

        OBJ_BLOCK(0x28, 320),
        OBJ_TARGET(0x28, 4),
        OBJ_UNIT_HP(0x40, 4),
        OBJ_MAX_UNIT_HP(0x58, 4),
        OBJ_UNIT_MANA(0x44, 4),
        OBJ_UNIT_FACTION(0x74, 4),
        OBJ_UNIT_LEVEL(0x70, 4);

        private final long address;
        private final int bytes;
        private Pointer pointer;

        DESCRIPTOR(
            long address,
            int bytes)
        {
            this.address = address;
            this.bytes = bytes;
        }

        @Override
        public Pointer getPointer() {
            if (pointer == null)
                pointer = new Pointer(address);
            return pointer;
        }

        @Override
        public int getBytes() {
            return bytes;
        }

        public long getValue() {
            return address;
        }
    }
}
