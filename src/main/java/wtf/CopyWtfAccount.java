package wtf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CopyWtfAccount {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("you should give folder to (WOW/WTF/Account FROM_ACCOUNT DESTINATION_ACCOUNT), EXAMPLE: D:\\wow\\BOT\\WTF\\Account HKLERK KOROCHHKA256");
            return;
        }
        String pathToWowWtfAccount = args[0];
        String originalAccount = args[1];
        String targetAccount = args[2];


        copyMacroses(
            pathToWowWtfAccount + "\\" + originalAccount,
            pathToWowWtfAccount + "\\" + targetAccount + "\\");

        final Path destination = Files.list(Paths.get(pathToWowWtfAccount + "\\" + targetAccount + "\\Outland")).collect(Collectors.toList()).get(0);
        copyMacroses(
            pathToWowWtfAccount + "\\" + originalAccount + "\\Outland" + "\\Krivoydru",
            destination + "\\");
    }

    private static void copyMacroses(
        String from,
        String target) throws IOException
    {
        try {
            Files.list(Paths.get(target)).forEach(e -> {
                try {
                    if (!e.toString().endsWith("Outland")) {
                        delete(e.toFile());
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stream<Path> list = Files.list(Paths.get(from));
        list.forEach(e -> {
            if (!"Outland".equals(e.getFileName().toString())) {
                try {

                    Files.copy(e, Paths.get(target + e.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                    if (e.toFile().isDirectory()) {
                        String folder = e.getFileName().toString();
                        copyMacroses(from + "\\" + folder, target + folder + "\\");
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });

    }

    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
