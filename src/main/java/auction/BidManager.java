package auction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BidManager {

    private final String path;

    public BidManager(String path) {
        this.path = path;
    }

    public boolean shouldBid(
        int auctionId,
        int currentBid)
    {
        try {
            List<String> strings = Files.readAllLines(Paths.get(path));
            for (String s : strings) {
                String[] split = s.split(" ");
                Integer parseAucId = Integer.valueOf(split[0]);
                if (parseAucId == auctionId && Integer.valueOf(split[1]) < currentBid) {
                    return true;
                } else if (parseAucId == auctionId) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveBid(
        int auctionId,
        int currentBid)
    {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));
            bw.write(auctionId + " " + currentBid + "\n");
            bw.flush();
            bw.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
