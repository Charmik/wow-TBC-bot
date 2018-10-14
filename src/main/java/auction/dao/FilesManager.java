package auction.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import auction.Item;
import auction.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telegram.Client;
import wow.WowInstance;

/**
 * @author alexlovkov
 */
public class FilesManager implements AuctionDao {

    private static final Logger logger = LoggerFactory.getLogger(FilesManager.class);

    private static final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
    private static final int HOURS = 10;

    private final String path;
    private final String tmpFile;
    private final Client client;
    private BufferedWriter historyBufferedWriter;

    public FilesManager(String path, Client client) {
        this.path = path;
        this.tmpFile = path + File.separator + "tmp" + File.separator + "tmp.txt";
        this.client = client;
    }

    public static void main(String[] args) throws IOException, ParseException {
        FilesManager filesManager = new FilesManager(
            "history_auction" + File.separator + "alliance",
            new Client("FilesManager"));
        System.out.println(filesManager.getLastDateFromFiles().date);
    }

    public File[] listFiles() {
        return new File(path).listFiles();
    }

    public LastFile getLastDateFromFiles() throws IOException, ParseException {
        Date last = null;
        int index = -1;
        for (File file : listFiles()) {
            if (file.getName().startsWith("auc")) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    Date date = df.parse(br.readLine());
                    if (last == null || date.compareTo(last) > 0) {
                        last = date;
                        String fileName = file.getName().substring(4);
                        fileName = fileName.substring(0, fileName.indexOf(".txt"));
                        index = Integer.parseInt(fileName);
                    }
                }
            }
        }
        logger.info("found last file date:{}, index:{}", last, index);
        return new LastFile(last, index);
    }

    private boolean addToDataBase(
        String folder,
        String filePath) throws IOException, ParseException
    {
        logger.info("addToDataBase folder:{} filePath:{}", folder, filePath);
        LastFile lastFile = getLastDateFromFiles();
        Date lastDateFromFiles = lastFile.date;
        int newIndex = lastFile.index + 1;

        List<String> strings = Files.readAllLines(Paths.get(filePath));
        if (strings.size() < 10000) {
            logger.info("don't save file because it's size:{}", strings.size());
            return false;
        }
        Date currentDate = df.parse(strings.get(0));
        ZonedDateTime lastZonedDateTime = ZonedDateTime.ofInstant(lastDateFromFiles.toInstant(), ZoneId.systemDefault());
        ZonedDateTime currentZonedDateTime = ZonedDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        Duration between = Duration.between(lastZonedDateTime, currentZonedDateTime);

        if (between.toHours() > HOURS) {
            File file = new File(filePath);
            File out = new File(folder + File.separator + "auc_" + newIndex + ".txt");
            boolean b = file.renameTo(out);
            logger.info("renamed:{}, file:{} out:{}", b, file.getAbsolutePath(), out.getAbsolutePath());
            return true;
        } else {
            logger.info("don't save file because difference in time hours:{} lastZonedDateTime:{} currentZonedDateTime:{}",
                between.toHours(), lastZonedDateTime, currentZonedDateTime);
            return false;
        }
    }

    @Override
    public List<Scan> getScans() {
        List<Scan> scans = new ArrayList<>();
        File[] files = new File(path).listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        for (File file : files) {
            if (file == null
                || "bidHistory.txt".equals(file.getName())
                || "logPrices.txt".equals(file.getName())
                || ".DS_Store".equals(file.getName())) {
                continue;
            }
            boolean isTmpFile = file.getName().equals("tmp");
            if (isTmpFile && file.isDirectory()) {
                file = new File(path + File.separator + file.getName() + File.separator + "tmp.txt");
                if (!file.exists()) {
                    continue;
                }
            }
            try {
                List<String> strings = Files.readAllLines(Paths.get(file.getPath()));
                if (strings.size() < 10000) {
                    continue;
                }
                Date date;
                try {
                    date = df.parse(strings.get(0));
                } catch (ParseException e) {
                    logger.error("can't parse:" + strings.get(0) + " file:" + file.getAbsolutePath());
                    continue;
                }
                ArrayList<Item> items = new ArrayList<>(strings.size() - 2);
                for (int i = 2; i < strings.size(); i++) {
                    String s = strings.get(i);
                    try {
                        //int[] ints = Arrays.stream(s.split("\\s+")).map(Integer::valueOf).mapToInt(x -> x).toArray();

                        try {
                            StringTokenizer st = new StringTokenizer(s, " ");
                            int x1 = Integer.parseInt(st.nextToken());
                            int x2 = Integer.parseInt(st.nextToken());
                            int x3 = Integer.parseInt(st.nextToken());
                            int x4 = Integer.parseInt(st.nextToken());
                            int x5 = Integer.parseInt(st.nextToken());
                            int x6 = Integer.parseInt(st.nextToken());
                            int x7 = Integer.parseInt(st.nextToken());
                            int x8 = Integer.parseInt(st.nextToken());
                            int x9 = Integer.parseInt(st.nextToken());
                            Item item = new Item(x1, x2, x3, x4, x5, x6, x7, x8, x9);
                            items.add(item);
                        } catch (Throwable t) {
                            logger.error("couldn't read file:{}", file, t);
                        }
                    } catch (Throwable e) {
                        logger.info(e.toString() + " file:" + file);
                        throw e;
                    }
                }
                scans.add(new Scan(date, items, isTmpFile));
            } catch (IOException exception) {
                logger.info("file:{}", file);
            }
        }
        Collections.sort(scans);
        return scans;
    }


    @Override
    public boolean save(Collection<Item[]> items) {
        try {
            resetTmpFile();
            logger.info("write tmp file with {} items", items.size() * 50);
            for (Item[] itemsFromCurrentPage : items) {
                writeCurrentAuc(itemsFromCurrentPage);
            }
            historyBufferedWriter.flush();
            historyBufferedWriter.close();
            boolean savedNewFile = addToDataBase(path, tmpFile);
            logger.info("savedNewFile:{}", savedNewFile);
            if (savedNewFile) {
                client.sendMessage("charm", "successfully saved a new file to:" +
                    WowInstance.getInstance().getPlayer().getFaction());
            }
        } catch (Throwable t) {
            logger.error("couldn't save {} items to db");
            return false;
        }
        return true;
    }

    private void resetTmpFile() throws IOException {
        logger.info("reset tmp file");
        historyBufferedWriter = new BufferedWriter(new FileWriter(tmpFile));
        initWrite();
    }

    private void initWrite() throws IOException {
        historyBufferedWriter.write(new Date() + "\n");
    }

    private void writeCurrentAuc(Item[] items) throws IOException {
        for (Item item : items) {
            // TODO: fast write
            historyBufferedWriter.write(item.toString() + "\n");
        }
    }

    private static class LastFile {

        Date date;
        int index;

        LastFile(Date date, int index) {
            this.index = index;
            this.date = date;
        }
    }
}
