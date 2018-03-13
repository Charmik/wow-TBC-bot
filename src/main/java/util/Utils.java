package util;

import wow.memory.objects.Player;

/**
 * @author Cargeh
 */
public class Utils {

    public static void sleep(long millis) {
        try {
            long beforeSleep = System.currentTimeMillis();
            Thread.sleep(millis);
            long afterSleep = System.currentTimeMillis();
            if ((afterSleep - beforeSleep) < (millis - 50)) {
                System.err.println("Wanted to sleep for " + millis + " but slept for " + (afterSleep - beforeSleep));
                sleep(millis - (afterSleep - beforeSleep));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Exception while sleeping. ", e);
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
