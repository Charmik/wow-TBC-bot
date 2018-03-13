package farmbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;

/**
 * @author alexlovkov
 */
public class CloseHandler implements Runnable {

    Logger logger = LoggerFactory.getLogger(CloseHandler.class);

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        for (; ; ) {
            long now = System.currentTimeMillis();
            System.out.println("###time - " + startTime + " " + now + " " + (now - startTime));
            //1*60*60*1000 = 1 hour
            if (now - startTime > 3 * 60 * 60 * 1000) {
                logger.info("time to exit, startTime={} finishTime={}", startTime, now);
                /*
                try {
                    Runtime.getRuntime().exec("rundll32.exe powrprof.dll,SetSuspendState 0,1,0");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */
                System.exit(0);
                startTime = now;
            }
            //10 min
            Utils.sleep(10 * 60 * 1000);
        }
    }
}
