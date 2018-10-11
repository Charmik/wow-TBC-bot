package farmbot.launch;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Random;

import farmbot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;

/**
 * @author alexlovkov
 */
public class ScheduledLauncher {

    private static Logger logger = LoggerFactory.getLogger(ScheduledLauncher.class);
    private static Robot robot;
    private static Random random = new Random();

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        logger.info("args:{}", (Object) args);
        Thread.sleep(3000);
        //robot.keyPress(KeyEvent.VK_5);

        //logger.info("length=" + args.length + " " + args[0]);
        if (args.length < 1) {
            logger.error("you should give path to wow.exe in the argument, example: C://WoWWW/wow.exe");
            return;
        }
        boolean now = false;
        if (args.length > 1 && args[1].equals("now")) {
            now = true;
        }
        int hours = Integer.valueOf(args[2]);

        String pathToWow = args[0];

        File file = new File(pathToWow);
        if (!file.exists()) {
            logger.error("The file " + pathToWow + " does not exist");
        }

        LocalDateTime localNow = LocalDateTime.now();
        ZoneId currentZone = ZoneId.of("Europe/Moscow");
        ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
        ZonedDateTime zonedNext3 = zonedNow.withHour(3).withMinute(0).withSecond(0);
        ZonedDateTime zonedNext5 = zonedNow.withHour(5).withMinute(0).withSecond(0);
        if (zonedNow.compareTo(zonedNext3) > 0) {
            logger.info("zonedNow:{} > zonedNext3:{}", zonedNow, zonedNext3);
            zonedNext3 = zonedNext3.plusDays(1);
            zonedNext5 = zonedNext5.plusDays(1);
        }

        logger.info("now={}", now);
        //for (; ; ) {
        while (!now && (zonedNow.compareTo(zonedNext3) < 0 && zonedNext3.compareTo(zonedNext5) < 0)) {
            logger.info("wait until run... " + Duration.between(zonedNow, zonedNext3));
            Thread.sleep(1 * 10000);
            localNow = LocalDateTime.now();
            currentZone = ZoneId.of("Europe/Moscow");
            zonedNow = ZonedDateTime.of(localNow, currentZone);
        }
        Process wowProcess = Runtime.getRuntime().exec(file.getAbsolutePath());
        long startTimeLaunchWow = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTimeLaunchWow < 180 * 1000) {
            logger.info("wait launch wow...");
            Thread.sleep(3000);
        }
        enterToTheGame();
        for (int i = 0; i < 30; i++) {
            int r = random.nextInt(300) - 150;
            robot.mouseMove(600 + r, 400 + r); //center
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
            Thread.sleep(50);
        }
            /*
            String[] cmdArray = {"cmd", "C:\\Users\\charm\\Dropbox\\prog\\wow-bot\\gradlew", "fatJar"};
            Process build = Runtime.getRuntime().exec(cmdArray);
            logger.info("wait for building");
            build.waitFor();
            logger.info("building completed");
            logErrors(build);
            //Process bot = Runtime.getRuntime().exec("java -jar C:\\Users\\charm\Dropbox\prog\wow-bot\build\libs\run-wow-bot-1.0-SNAPSHOT.jar");
            //Process bot = Runtime.getRuntime().exec("java -jar C:\\Users\\charm\\Dropbox\\prog\\wow-bot\\build\\libs\\run-wow-bot-1.0-SNAPSHOT.jar");
            */
//            String[] cmdArray = {"C:\\Program Files\\Java\\jdk1.8.0_161\\bin\\java", "-jar", "C:\\Users\\charm\\Dropbox\\prog\\wow-bot\\build\\libs\\run-wow-bot-1.0-SNAPSHOT.jar"};
//            Process bot = Runtime.getRuntime().exec(cmdArray);
//            logger.info("wait for running bot");
        Bot bot = new Bot(new String[0]);
        bot.run(hours);
        Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0").waitFor();

        //bot.waitFor(); //with it bot freeze
        //logger.info("bot is working");
        //logErrors(bot);

        //Thread.sleep(60000000);
        //zonedNext5.plusDays(1);
        //System.exit(0);
        //}
    }

    private static void logErrors(Process bot) {
        java.util.Scanner s = new java.util.Scanner(bot.getInputStream()).useDelimiter("\\A");
        String q = s.hasNext() ? s.next() : "";
        logger.info("inputStream=" + q);

        s = new java.util.Scanner(bot.getErrorStream()).useDelimiter("\\A");
        q = s.hasNext() ? s.next() : "";
        logger.info("errorStream=" + q);
    }

    private static void enterToTheGame() throws InterruptedException {
        //skworew666
        pressButton(KeyEvent.VK_T);
        pressButton(KeyEvent.VK_O);
        pressButton(KeyEvent.VK_P);
        pressButton(KeyEvent.VK_P);
        pressButton(KeyEvent.VK_R);
        pressButton(KeyEvent.VK_I);
        pressButton(KeyEvent.VK_E);
        pressButton(KeyEvent.VK_S);
        pressButton(KeyEvent.VK_T);

        pressButton(KeyEvent.VK_TAB);

        pressButton(KeyEvent.VK_M);
        pressButton(KeyEvent.VK_A);
        pressButton(KeyEvent.VK_4);
        pressButton(KeyEvent.VK_A);
        pressButton(KeyEvent.VK_M);
        pressButton(KeyEvent.VK_A);
        pressButton(KeyEvent.VK_4);
        pressButton(KeyEvent.VK_A);

        pressButton(KeyEvent.VK_ENTER);
        Thread.sleep(15000);
        pressButton(KeyEvent.VK_ENTER);
        for (int i = 0; i < 180; i++) {
            // TODO: if level != 0 -> break;
            Utils.sleep(1000);
        }
        logger.info("finished logging to the character");
    }

    private static void pressButton(int key) throws InterruptedException {
        robot.keyPress(key);
        Thread.sleep(200);
        robot.keyRelease(key);
        Thread.sleep(200);
    }


}
