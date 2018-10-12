package auction.dao;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;

public class BidManagerImpl implements BidManager {

    private final String path;

    public BidManagerImpl(String path) {
        this.path = path;
    }

    @Override
    public boolean shouldBid(
        int auctionId,
        int currentBid)
    {
        try {
            List<String> strings = Files.readAllLines(Paths.get(path));
            for (int i = strings.size() - 1; i >= 0; i--) {
                String s = strings.get(i);
                String[] split = s.split(" ");
                Integer parseAucId = Integer.valueOf(split[0]);
                int previousBid = Integer.parseInt(split[1]);
                if (parseAucId == auctionId && previousBid < currentBid) {
                    return true;
                } else if (parseAucId == auctionId) {
                    return false;
                }
            }
            return true;
        } catch (NoSuchFileException e) {
            e.printStackTrace();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void saveBid(
        int auctionId,
        int currentBid)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));
            bw.write(auctionId + " " + currentBid + "\n");
            bw.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
