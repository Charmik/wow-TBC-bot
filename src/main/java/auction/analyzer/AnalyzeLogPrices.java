package auction.analyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnalyzeLogPrices {

    private String fileName;

    public AnalyzeLogPrices(String fileName) {
        this.fileName = fileName;
    }

    public static void main(String[] args) throws IOException {
        AnalyzeLogPrices analyzeLogPrices = new AnalyzeLogPrices("history_auction" + File.separator + "alliance" + File.separator + "logPrices.txt");
        analyzeLogPrices.analyze();
    }

    private void analyze() throws IOException {
        List<String> strings = Files.readAllLines(Paths.get(fileName));

        Set<Integer> itemsOnFirstPage = new HashSet<>();
        Set<Integer> allItemsOnAuction = new HashSet<>();

        for (String s : strings) {
            String[] split = s.split(" ");
            if (s.contains("true")) {
                itemsOnFirstPage.add(Integer.valueOf(split[7]));
            } else {
                allItemsOnAuction.add(Integer.valueOf(split[7]));
            }
        }
        System.out.println("itemsOnFirstPage.size():" + itemsOnFirstPage.size() + " itemsOnFirstPage: " + itemsOnFirstPage);
        System.out.println("allItemsOnAuction.size():" + allItemsOnAuction.size() + " allItemsOnAuction: " + allItemsOnAuction);
        for (Integer aucId : allItemsOnAuction) {
            if (itemsOnFirstPage.contains(aucId)) {
                itemsOnFirstPage.remove(aucId);
            }
        }
        System.out.println("itemsOnFirstPage.size():" + itemsOnFirstPage.size() + " " + itemsOnFirstPage);
    }
}
