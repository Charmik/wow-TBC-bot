package farmbot.launch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;

/**
 * @author alexlovkov
 */
public class CloseHandler implements Runnable {

    Logger logger = LoggerFactory.getLogger(CloseHandler.class);
    private Stopper stopper;
    private final int hours;

    public CloseHandler(
        Stopper stopper,
        int hours)
    {
        this.stopper = stopper;
        this.hours = hours;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        for (; ; ) {
            long now = System.currentTimeMillis();
            logger.info("###time - " + startTime + " " + now + " " + (now - startTime));
            //1*60*60*1000 = 1 hour
            if (now - startTime > hours * 60 * 60 * 1000) {
                logger.error("time to exit, startTime={} finishTime={}", startTime, now);
                /*
                try {
                    Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
                stopper.setStopped(true);
                break;
            }
            //10 min
            Utils.sleep(10 * 60 * 1000);
        }
    }
}
