package util;

import java.util.Calendar;

import wow.memory.objects.Player;

/**
 * @author Cargeh
 */
public class Utils {

    private static final long tickDay = 1000 * 60 * 60 * 24;
    private static final long tickHour = 1000 * 60 * 60;
    private static final long tickMinute = 1000 * 60;
    private static final long tickSecond = 1000;

    public static Calendar getCalendarFromTime(long time) {
        Calendar calendar = Calendar.getInstance();

        int d = (int)(time / tickDay);
        time = time - (d * tickDay);
        int h = (int)(time / tickHour);
        time = time - (h * tickHour);
        int m = (int)(time / tickMinute);
        time = time - (m * tickMinute);
        int s = (int)(time / tickSecond);
        System.out.println("d = " + d +  " h = " + h + " m =" + m + " s=" + s);

        calendar.set(Calendar.DAY_OF_MONTH, d);
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        calendar.set(Calendar.SECOND, s);

        return calendar;
    }

    public static void sleep(long millis) {
        try {
            long beforeSleep = System.currentTimeMillis();
            Thread.sleep(millis);
            long afterSleep = System.currentTimeMillis();
            if ((afterSleep - beforeSleep) < (millis - 50)) {
                System.err.println("Wanted to sleep for " + millis + " but slept for " + (afterSleep - beforeSleep));
                sleep(millis - (afterSleep - beforeSleep));
            }
        } catch (InterruptedException ignore) {
        }
    }

    public static void sleeping(
        Player player,
        long millis)
    {
        int n = (int) millis / 100;
        for (int i = 0; i < n; i++) {
            if (player.isInCombat()) {
                return;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static boolean isPassedEnoughTime(
        long prevBuff,
        long now,
        long l)
    {
        return false;
    }
}
