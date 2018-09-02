package auction.dao;

import java.io.File;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import auction.Item;
import auction.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author alexlovkov
 */
public class FilesManager {

    private static final Logger logger = LoggerFactory.getLogger(FilesManager.class);

    private static final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
    private static final int HOURS = 10;

    private final String path;

    public FilesManager(String path) {
        this.path = path;
    }

    public static void main(String[] args) throws IOException, ParseException {
        FilesManager filesManager = new FilesManager("history_auction" + File.separator + "alliance");
        System.out.println(filesManager.getLastDateFromFiles());
        filesManager.addToDataBase("history_auction" + File.separator + "alliance", "ttt.txt");
    }

    public File[] listFiles() {
        return new File(path).listFiles();
    }

    public LastFile getLastDateFromFiles() throws IOException, ParseException {
        Date last = null;
        int index = -1;
        for (File file : listFiles()) {
            if (file.getName().startsWith("auc")) {
                List<String> strings = Files.readAllLines(Paths.get(file.getPath()));
                Date date = df.parse(strings.get(0));
                if (last == null || date.compareTo(last) > 0) {
                    last = date;
                    String fileName = file.getName().substring(4);
                    fileName = fileName.substring(0, fileName.indexOf(".txt"));
                    index = Integer.valueOf(fileName);
                }
            }
        }
        logger.info("found last file date:{}, index:{}", last, index);
        return new LastFile(last, index);
    }

    public void addToDataBase(
        String folder,
        String filePath) throws IOException, ParseException
    {
        logger.info("addToDataBase folder:{} filePath:{}", folder, filePath);
        LastFile lastFile = getLastDateFromFiles();
        Date lastDateFromFiles = lastFile.date;
        int newIndex = lastFile.index + 1;

        List<String> strings = Files.readAllLines(Paths.get(filePath));
        if (strings.size() < 20000) {
            logger.info("don't save file because it's size:{}", strings.size());
            return;
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
        } else {
            logger.info("don't save file because difference in time hours:{} lastZonedDateTime:{} currentZonedDateTime:{}",
                between.toHours(), lastZonedDateTime, currentZonedDateTime);
        }
    }

    public List<Scan> readFiles() throws IOException {
        List<Scan> scans = new ArrayList<>();
        for (File file : new File(path).listFiles()) {
            if ("bidHistory.txt".equals(file.getName()) || "logPrices.txt".equals(file.getName())
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
                DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
                if (strings.size() < 10000) {
                    continue;
                }
                Date date;
                try {
                    date = df.parse(strings.get(0));
                } catch (ParseException e) {
                    logger.error("can't parse:" + strings.get(0) + " file:" + file.getAbsolutePath());
                    //throw e;
                    continue;
                }
                ArrayList<Item> items = new ArrayList<>(strings.size() - 2);
                for (int i = 2; i < strings.size(); i++) {
                    String s = strings.get(i);
                    try {
                        int[] ints = Arrays.stream(s.split("\\s+")).map(Integer::valueOf).mapToInt(x -> x).toArray();
                        Item item;
                        if (ints.length == 8) {
                            item = new Item(ints[0], ints[1], ints[2], ints[3], ints[4], ints[5], ints[6], ints[7]);
                        } else if (ints.length == 9) {
                            item = new Item(ints[0], ints[1], ints[2], ints[3], ints[4], ints[5], ints[6], ints[7], ints[8]);
                        } else {
                            if ("tmp.txt".equals(file.getName())) {
                                logger.info("file is not correct, delete it:{}", file);
                                file.delete();
                                continue;
                            }
                            throw new IllegalArgumentException("can't parse file: " + file);
                        }
                        items.add(item);
                    } catch (Throwable e) {
                        logger.info(e.toString() + " file:" + file);
                        throw e;
                    }
                }
                scans.add(new Scan(date, items, isTmpFile));
            } catch (IOException exception) {
                logger.info("file:{}", file);
                throw exception;
            }
        }
        Collections.sort(scans);
        return scans;

    }

    private class LastFile {

        Date date;
        int index;

        public LastFile(Date date, int index) {
            this.index = index;
            this.date = date;
        }
    }
}
