package wow.memory;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import winapi.MemoryApi;

/**
 * @author Cargeh
 */
public abstract class MemoryAware {
    protected final WowMemory memoryManager;
    private final WinNT.HANDLE processMemoryHandle;
    private volatile long baseAddress = -1L;
    private volatile long descriptorAddress = -1L;

    protected MemoryAware(WowMemory memory) {
        this.memoryManager = memory;
        this.processMemoryHandle = memory.getProcessMemoryHandle();
    }

    /* ------- STATIC READS ------- */

    protected int readInt(Address address) {
        Memory memory = readMemory(address.getPointer(), address.getBytes());
        int value = memory.getInt(0);
        memory.clear();
        return value;
    }

    protected long readLong(Address address) {
        Memory memory = readMemory(address.getPointer(), address.getBytes());
        long value = memory.getLong(0);
        memory.clear();
        return value;
    }

    protected float readFloat(Address address) {
        Memory memory = readMemory(address.getPointer(), address.getBytes());
        float value = memory.getFloat(0);
        memory.clear();
        return value;
    }

    protected String readString(Address address) {
        Memory memory = readMemory(address.getPointer(), address.getBytes());
        String value = memory.getString(0);
        memory.clear();
        return value;
    }

    protected byte readByte(Address address) {
        Memory memory = readMemory(address.getPointer(), address.getBytes());
        byte value = memory.getByte(0);
        memory.clear();
        return value;
    }

    /* ------- OFFSET READS WITH SET BASEADDESS ------- */

    protected short readShortOffset(Address offset) {
        checkBaseAddress();
        Memory memory = readMemory(baseAddress, offset);
        short value = memory.getShort(0);
        memory.clear();
        return value;
    }

    protected int readIntOffset(Address offset) {
        checkBaseAddress();
        Memory memory = readMemory(baseAddress, offset);
        int value = memory.getInt(0);
        memory.clear();
        return value;
    }

    protected long readLongOffset(Address offset) {
        checkBaseAddress();
        Memory memory = readMemory(baseAddress, offset);
        long value = memory.getLong(0);
        memory.clear();
        return value;
    }

    protected float readFloatOffset(Address offset) {
        checkBaseAddress();
        Memory memory = readMemory(baseAddress, offset);
        float value = memory.getFloat(0);
        memory.clear();
        return value;
    }

    private void checkBaseAddress() {
        if (baseAddress == -1L)
            throw new RuntimeException("Reading an offset without having set the base address");
    }

    /* ------- OFFSET READS WITH CUSTOM BASEADDRESS ------- */

    protected int readIntOffset(
        long base,
        Address offset)
    {
        Memory memory = readMemory(base, offset);
        int value = memory.getInt(0);
        memory.clear();
        return value;
    }

    protected long readLongOffset(
        long base,
        Address offset)
    {
        Memory memory = readMemory(base, offset);
        long value = memory.getLong(0);
        memory.clear();
        return value;
    }

    public byte readByteDescriptor(Address address) {
        checkDescriptorAddress();
        Memory memory = readMemory(descriptorAddress, address);
        byte value = memory.getByte(0);
        memory.clear();
        return value;
    }

    public int readIntDescriptor(Address address) {
        checkDescriptorAddress();
        Memory memory = readMemory(descriptorAddress, address);
        int value = memory.getInt(0);
        memory.clear();
        return value;
    }

    public int[] readIntUnitBlock(Address address) {
        return readBlock(address, descriptorAddress);
    }

    public int[] readIntPlayerBlock(Address address) {
        return readBlock(address, baseAddress);
    }

    public int[] readBlock(
        Address address,
        long addr)
    {
        Memory memory = readMemory(descriptorAddress, address);
        int[] arr = new int[address.getBytes() / 4];
        memory.read(0, arr, 0, address.getBytes() / 4);
        memory.clear();
        return arr;
    }

    private void checkDescriptorAddress() {
        if (descriptorAddress == -1L)
            throw new RuntimeException("Reading an offset without having set the base address");
    }

    /* ------- MEMORY READS ------- */

    protected Memory readMemory(
        long base,
        Address offset)
    {
        Pointer pointer = offset.getPointer().share(base);
        return readMemory(pointer, offset.getBytes());
    }

    protected Memory readMemory(
        Pointer address,
        int bytesToRead)
    {
        return MemoryApi.readMemory(processMemoryHandle, address, bytesToRead);
    }

    /* ------- MEMORY WRITES API ------- */

    protected void writeFloat(
        Address address,
        float data)
    {
        writeFloat(address.getPointer(), data, address.getBytes());
    }

    protected void writeInt(
        Address address,
        int data)
    {
        writeInt(address.getPointer(), data, address.getBytes());
    }

    protected void writeLong(
        Address address,
        long data)
    {
        writeLong(address.getPointer(), data, address.getBytes());
    }

    /* ------- MEMORY WRITES SYSTEM METHODS ------- */

    private void writeFloat(
        Pointer address,
        float data,
        int bytesToWrite)
    {
        MemoryApi.writeFloat(processMemoryHandle, address, data, bytesToWrite);
    }

    private void writeInt(
        Pointer address,
        int data,
        int bytesToWrite)
    {
        MemoryApi.writeInt(processMemoryHandle, address, data, bytesToWrite);
    }

    private void writeLong(
        Pointer address,
        long data,
        int bytesToWrite)
    {
        MemoryApi.writeLong(processMemoryHandle, address, data, bytesToWrite);
    }

    protected void setBaseAddress(long baseAddress) {
        this.baseAddress = baseAddress;
    }

    public void setDescriptorAddress(long descriptorAddress) {
        this.descriptorAddress = descriptorAddress;
    }
}
