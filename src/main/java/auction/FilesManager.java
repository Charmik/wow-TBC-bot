package auction;

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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author alexlovkov
 */
public class FilesManager {

    private static final DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);

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

    public Date getLastDateFromFiles() throws IOException, ParseException {
        Date last = null;
        for (File file : listFiles()) {
            if (file.getName().startsWith("auc")) {
                List<String> strings = Files.readAllLines(Paths.get(file.getPath()));
                Date date = df.parse(strings.get(0));
                if (last == null || date.compareTo(last) > 0) {
                    last = date;
                }
            }
        }
        return last;
    }

    public void addToDataBase(
        String folder,
        String filePath) throws IOException, ParseException
    {
        Date lastDateFromFiles = getLastDateFromFiles();
        List<String> strings = Files.readAllLines(Paths.get(filePath));
        if (strings.size() < 20000) {
            return;
        }
        Date currentDate = df.parse(strings.get(0));
        ZonedDateTime lastZonedDateTime = ZonedDateTime.ofInstant(lastDateFromFiles.toInstant(), ZoneId.systemDefault());
        ZonedDateTime currentZonedDateTime = ZonedDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
        Duration between = Duration.between(lastZonedDateTime, currentZonedDateTime);
        if (between.toHours() > 12) {
            int newIndex = listFiles().length - 1;
            File file = new File(filePath);
            File out = new File(folder + File.separator + "auc_" + newIndex + ".txt");
            boolean b = file.renameTo(out);
            System.out.println("renamed:" + b);
        }
    }
}
