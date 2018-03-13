package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * @author Cargeh
 */
public class DbcParsers {

    /**
     * Takes a raw factionTemplate.dbc file, parses it and writes
     * in such format that's convenient to read from later on.
     * Unless you're changing the dbc file, you won't really need it.
     */
    private static class FactionTemplateParser {
        private static final String factionTemplateDbcFileName = "dbc/FactionTemplateDbc";
        private static final String factions = "Factions";

        private static final File factionTemplateDbc;
        private static final File factionTemplate;

        static {
            factionTemplateDbc = getFile(factionTemplateDbcFileName);
            factionTemplate = getFile(factions);
        }

        private static File getFile(String fileName) {
            URL resource = DbcParsers.class.getClassLoader().getResource(fileName);
            if (resource == null)
                throw new RuntimeException("No such file found: " + fileName);
            return new File(resource.getFile());
        }

        public static void main(String[] args) throws IOException {
            try (
                BufferedReader reader = new BufferedReader(new FileReader(factionTemplateDbc));
                BufferedWriter writer = new BufferedWriter(new FileWriter(factionTemplate))) {

                String line;
                String[] lineArgs;
                while ((line = reader.readLine()) != null) {
                    lineArgs = line.split("\\s+");

                    int id = toInt(lineArgs[0]);
                    int ourMask = toInt(lineArgs[2]);
                    int friendlyMask = toInt(lineArgs[3]);
                    int hostileMask = toInt(lineArgs[4]);

                    String newLine = String.format("F%1$d(%1$d, %2$d, %3$d, %4$d)," + System.lineSeparator(),
                        id, ourMask, friendlyMask, hostileMask);
                    System.out.print(newLine);
//                    writer.write(newLine); // doesn't work? Eh?
                }
            }
        }

        private static int toInt(String s) {
            return Integer.parseInt(s);
        }
    }
}
