package bgbot;

public class BgBot {
    /*
    private static Logger log = LoggerFactory.getLogger(BgBot.class);
    private WowInstance wowInstance = new WowInstance("World of Warcraft");
    private CtmManager ctmManager;
    private Player player;
    private ObjectManager objectManager;
    private HealBot healBot;
    private Healer healer;
    private Movement movement;
    private Path path;
    private static final int HOW_OFTEN_MOONFIRE_COST = 100;
    private static final int HOW_OFTEN_TO_MOVE = 2000;

    public BgBot() {
        this.ctmManager = this.wowInstance.getCtmManager();
        this.player = this.wowInstance.getPlayer();
        this.objectManager = this.wowInstance.getObjectManager();
        this.healBot = new HealBot();
        this.healer = new Healer(this.player, this.wowInstance);
        this.movement = new Movement(this.player, this.ctmManager, this.wowInstance);
        this.path = new Path();
    }

    public static void main(String[] args) throws IOException {
        BgBot bgBot = new BgBot();
        bgBot.run();
    }

    private void run() throws IOException {
        for(int i = 0; i < 1; ++i) {
            this.player.updatePlayer();
            log.info("step=" + i);
            this.regBg();
            this.player.updatePlayer();
            log.info("not in shatr");
            this.getPaths(this.player);
            this.player.updatePlayer();
            this.goToTheNearestPoint();
            this.player.updatePlayer();
            this.defencePoint();
            this.player.updatePlayer();
        }

    }

    private void defencePoint() {
        boolean wasDead = false;

        while(true) {
            while(!this.player.getZone().isShatrhCity()) {
                this.player.updatePlayer();

                while(this.player.isDead()) {
                    this.player.updatePlayer();
                    log.info("playerIsDead");
                    Utils.sleep(3000L);
                    this.movement.ress();
                    wasDead = true;
                }

                while(this.player.isInCombat()) {
                    this.player.updatePlayer();
                    this.healBot.makeOneHeal();
                }

                this.healBot.makeOneHeal();
                if (wasDead) {
                    this.goToTheNearestPoint();
                }

                int count = 0;
                long timeForCombat = System.currentTimeMillis();

                while(!this.player.isDead()) {
                    if (this.player.getZone().isShatrhCity()) {
                        this.ctmManager.stop();
                        break;
                    }

                    if (this.player.getZone().isEye()) {
                        if (this.player.isInCombat()) {
                            timeForCombat = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - timeForCombat > 15000L && this.player.getHealthPercent() == 100) {
                            this.wowInstance.click(WinKey.R);
                            timeForCombat = System.currentTimeMillis();
                        }
                    }

                    this.player.updatePlayer();
                    this.objectManager.refillPlayers();
                    boolean healed = this.healBot.makeOneHeal();
                    if (!this.player.getZone().isEye()) {
                        this.wowInstance.click(WinKey.MINUS);
                    }

                    this.wowInstance.click(WinKey.PLUS);
                    ++count;
                    if (count % 100 == 0) {
                        this.castMoonfireToNearestTarget();
                    }

                    if (count % 2000 == 0) {
                        this.wowInstance.click(WinKey.W);
                        this.wowInstance.click(WinKey.S);
                    }

                    if (this.player.getManaPercent() < 10) {
                        this.castInnervate();
                    }
                }
            }

            return;
        }
    }

    private void castInnervate() {
        Utils.sleep(500L);

        for(int i = 0; i < 5; ++i) {
            this.wowInstance.click(WinKey.D5);
            this.wowInstance.click(WinKey.D6);
        }

    }

    private void castMoonfireToNearestTarget() {
        this.objectManager.refillPlayers();
        this.objectManager.getPlayers().remove(this.player.getGuid());
        Optional<PlayerObject> nearestPlayerTo = this.objectManager.getNearestPlayerTo(this.player);
        if (nearestPlayerTo.isPresent()) {
            PlayerObject unitObject = (PlayerObject)nearestPlayerTo.get();
            double distance = Navigation.evaluateDistanceFromTo(this.player, unitObject);
            boolean isHorde = unitObject.getFaction().isHorde();
            if (isHorde && distance < 1500.0D) {
                log.info("face to unit and cast moonfire");
                this.player.target(unitObject);
                this.ctmManager.face(unitObject);
                this.wowInstance.click(WinKey.PLUS);
                this.ctmManager.stop();
            }
        }

    }

    private void goToTheNearestPoint() {
        log.info("player was dead so go to the nearest point!");
        List<Coordinates3D> startPoints = (List)this.path.getPoints().stream().map((e) -> {
            return (Coordinates3D)e.get(0);
        }).collect(Collectors.toList());
        this.setNearestPath(this.player, startPoints);
        List<Coordinates3D> pathToDefence = this.path.getNearestPoint();
        int countUnsuccess = 0;
        if (!this.player.getZone().isShatrhCity()) {
            if (this.path.isFromBase()) {
                log.info("go fromBase, cast mount");
                if (this.player.getZone().isArathiBasin()) {
                    this.movement.goToNextPoint((Coordinates3D)pathToDefence.get(0), true);
                }

                this.castMount();
            } else {
                this.wowInstance.click(WinKey.D7);
            }

            for(int i = 0; i < pathToDefence.size(); ++i) {
                System.out.println("pathToDefence i=" + i);
                this.castMoonfireToNearestTarget();
                boolean success = this.movement.goToNextPoint((Coordinates3D)pathToDefence.get(i), this.path.isFromBase());
                log.info("success to point=" + success);
                if (!success) {
                    ++countUnsuccess;
                    if (countUnsuccess == 50) {
                        Utils.sleep(10000L);
                        continue;
                    }
                }

                if (this.player.isDead()) {
                    break;
                }

                while(this.player.isInCombat() && !this.path.isFromBase()) {
                    this.player.updatePlayer();
                    this.healBot.makeOneHeal();
                }

                if (this.path.isFromBase() && i == 25 && this.player.getZone().isAlterac()) {
                    Utils.sleep(2000L);
                    this.castMount();
                }
            }

        }
    }

    private void castMount() {
        log.info("went to 1 point, try to cast mount");
        Utils.sleep(1000L);
        this.wowInstance.click(WinKey.D0);
        Utils.sleep(4000L);
        log.info("mount caster, go to the point");
    }

    private void regBg() {
        while(this.player.getZone().isShatrhCity()) {
            this.player.updatePlayer();

            int i;
            for(i = 0; i < 5 && this.player.getZone().isShatrhCity(); ++i) {
                this.objectManager.refillUnits();
                Optional<UnitObject> unit = this.objectManager.getNearestUnitTo(this.player);
                if (unit.isPresent()) {
                    log.info("found bg register");
                    UnitObject unitObject = (UnitObject)unit.get();
                    this.ctmManager.interactNpc(unitObject);
                } else {
                    log.info("didnt find a register master");
                }

                this.wowInstance.click(WinKey.ESC);
                Utils.sleep(100L);
                this.wowInstance.click(WinKey.W);
                this.wowInstance.click(WinKey.W);
                this.wowInstance.click(WinKey.S);
                this.wowInstance.click(WinKey.S);
                this.wowInstance.click(WinKey.S);
                Utils.sleep(1000L);
                this.wowInstance.click(WinKey.D2);
            }

            log.info("waiting in shatr");

            for(i = 0; i < 120; ++i) {
                if (this.player.getZone().isShatrhCity()) {
                    Utils.sleep(1000L);
                }
            }
        }

    }

    private String addBgFolder(String fileName) {
        return "bgsRoutes\\" + fileName;
    }

    private void getPaths(Player player) throws IOException {
        List<List<Point3D>> paths3D = new ArrayList();
        if (player.getZone().isArathiBasin()) {
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_fromBaseToST")));
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_BsRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_farmRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_LmRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_MinesRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_StRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("bg_fromBaseRespawnToSt")));
        } else if (player.getZone().isAlterac()) {
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\fromBaseToStoneheartGraveyard")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressAndDefFrostwolfGraveyard")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressAndDefFrostwolfReliefHut")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressAndDefIceBloodGraveyard")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressAndDefStoneheartGraveyard")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressAndDefStormpikeAidStation")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressStormpikeGraveyardAndDefStoneheartGraveyard")));
            paths3D.add(BotPath.getPath(this.addBgFolder("alt\\ressSnowFallGravyyardAndDeffIceblood")));
        } else if (player.getZone().isEye()) {
            paths3D.add(BotPath.getPath(this.addBgFolder("eye\\fromBaseToDraeneiRuins")));
            paths3D.add(BotPath.getPath(this.addBgFolder("eye\\bloodElfRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("eye\\draeneiRuinsRespawn")));
            paths3D.add(BotPath.getPath(this.addBgFolder("eye\\FelReaverRuins")));
            paths3D.add(BotPath.getPath(this.addBgFolder("eye\\mageTowerRespawn")));
        }

        List<List<Coordinates3D>> paths = new ArrayList();

        for(int i = 0; i < paths3D.size(); ++i) {
            paths.add(new ArrayList());

            for(int j = 0; j < ((List)paths3D.get(i)).size(); ++j) {
                ((List)paths.get(i)).add(new Coordinates3D((float)((Point3D)((List)paths3D.get(i)).get(j)).getX(), (float)((Point3D)((List)paths3D.get(i)).get(j)).getY(), (float)((Point3D)((List)paths3D.get(i)).get(j)).getZ()));
            }
        }

        this.path.setPoints(paths);
    }

    public void setNearestPath(Player player, List<Coordinates3D> points) {
        Coordinates3D playerCoordinates = Navigation.get3DCoordsFor(player);
        double min = Navigation.evaluateDistanceFromTo((Coordinates3D)points.get(0), playerCoordinates);
        int index = 0;

        for(int i = 1; i < points.size(); ++i) {
            double distance = Navigation.evaluateDistanceFromTo((Coordinates3D)points.get(i), playerCoordinates);
            log.info(((Coordinates3D)points.get(i)).toString());
            log.info("i=" + i + " distance from point to player=" + distance);
            if (distance < min) {
                min = distance;
                index = i;
            }
        }

        log.info("indexNearest=" + index);
        this.path.setNearestPoint((List)this.path.getPoints().get(index));
        this.path.setFromBase(index == 0);
    }
    */
}
