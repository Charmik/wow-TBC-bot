package farmbot.launch;

/**
 * @author alexlovkov
 */
public class Stopper {

    private volatile boolean stopped;

    public Stopper() {
        this.stopped = false;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isStop() {
        return stopped;
    }
}
