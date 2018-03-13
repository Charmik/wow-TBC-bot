package bgbot;

public class Movement {
    /*
    private static Logger log = LoggerFactory.getLogger(Movement.class);
    private Player player;
    private CtmManager ctmManager;
    private WowInstance wowInstance;

    Movement(Player player, CtmManager ctmManager, WowInstance wowInstance) {
        this.player = player;
        this.ctmManager = ctmManager;
        this.wowInstance = wowInstance;
    }

    boolean goToNextPoint(Coordinates3D point, boolean fromBase) {
        return this.goToNextPoint(new Point3D((double)point.x, (double)point.y, (double)point.z), fromBase);
    }

    private boolean goToNextPoint(Point3D nextPoint, boolean fromBase) {
        int count = 0;

        while(!Navigation.isNear(new Coordinates3D(this.player.getX(), this.player.getY(), this.player.getZ()), nextPoint)) {
            if (this.player.getZone().isShatrhCity()) {
                return false;
            }

            if ((this.player.isInCombat() || this.player.getHealthPercent() < 100) && !fromBase) {
                return false;
            }

            log.info("go to next point");
            ++count;
            log.info("count=" + count);
            if (count == 2) {
                this.ctmManager.stop();
                Utils.sleep(15000L);
                break;
            }

            if (this.player.isDead()) {
                this.ress();
                break;
            }

            boolean success = this.ctmManager.goTo(nextPoint);
            if (success) {
                return true;
            }
        }

        this.ctmManager.stop();
        return true;
    }

    public void ress() {
        log.info("try ress character");
        if (this.player.isDeadLyingDown()) {
            this.wowInstance.click(WinKey.D9, 0L);
            Utils.sleep(500L);
        } else if (this.player.isSpirit()) {
            this.wowInstance.click(WinKey.D0, 0L);
        }

    }
    */
}
