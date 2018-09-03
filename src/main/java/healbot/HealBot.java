package healbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Navigation;
import wow.memory.ObjectManager;
import wow.memory.objects.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import static wow.components.UnitReaction.FRIENDLY;

public class HealBot {

    private static final Logger logger = LoggerFactory.getLogger(HealBot.class);

    private WowInstance wowInstance;
    private Player player;
    private ObjectManager objectManager;
    private long timestampLastHeal;
    private Spell lastSpell = null;
    private Map<Long, PreviousHeal> map;
    private static final int MIN_HEALT_FOR_HEAL = 1250;
    private TankDetector tankDetector;
    // try to decrease time if tank lose blooms too often
    private final int UPDATE_TIME_FOR_LIFEBLOOM = 5600;

    public HealBot() {
        reset();
    }

    public void reset() {
        try {
            this.wowInstance = new WowInstance("World of Warcraft");
            this.player = wowInstance.getPlayer();
            this.objectManager = wowInstance.getObjectManager();
            this.timestampLastHeal = 0L;
            this.map = new HashMap<>();
            tankDetector = new TankDetector(objectManager);
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
    }

    public void reset2() {
        try {
            this.player = wowInstance.getPlayer();
            this.objectManager = wowInstance.getObjectManager();
            this.timestampLastHeal = 0L;
            this.map = new HashMap<>();
            tankDetector = new TankDetector(objectManager);
        } catch (Throwable e) {
            logger.info(e.getMessage());
        }
    }

    public static void main(String[] args) {
        HealBot healBot = new HealBot();
        healBot.run();
    }

    public boolean makeOnePlayerHeal() {
        CreatureObject playerWithMinimumHealth = getPlayerForHeal();
        if (playerWithMinimumHealth == null
                || playerWithMinimumHealth.needHealthForFull() < MIN_HEALT_FOR_HEAL
                || playerWithMinimumHealth.getHealth() == 0) {
            return false;
        } else {
            if (playerWithMinimumHealth.needHealthForFull() >= MIN_HEALT_FOR_HEAL) {
                logger.info("heal player needHp:{}", playerWithMinimumHealth.needHealthForFull());
                makeHeal(playerWithMinimumHealth);
            }
            Utils.sleep(50L);
            return true;
        }
    }

    private boolean makeOnePetHeal() {
        logger.info("makeOnePetHeal");
        CreatureObject petWithMinimumHealth = getPetForHeal();
        if (petWithMinimumHealth == null || petWithMinimumHealth.needHealthForFull() < MIN_HEALT_FOR_HEAL) {
            return false;
        } else {
            if (petWithMinimumHealth.needHealthForFull() >= MIN_HEALT_FOR_HEAL) {
                makeHeal(petWithMinimumHealth);
            }
            Utils.sleep(50L);
            return true;
        }
    }

    private void run() {
        int notFoundPlayerToHealCount = 0;

        long lastSleep = 0;

        while (true) {
            //reset();
            // for swap cc only
            /*
            if (System.currentTimeMillis() > lastSleep + 17 * 1000) {
                Utils.sleep(2100);
                wowInstance.click(WinKey.B);
                boolean wasCastring = false;
                while (player.isCasting()) {
                    wasCastring = true;
                }
                Utils.sleep(2000);
                if (player.isInCombat()) {
                    wowInstance.click(WinKey.D2);
                }
                lastSleep = System.currentTimeMillis();
                Utils.sleep(2000);
                logger.info("trying CC focus target, wasCasting:" + wasCastring);
            }
            */


            //AND EVERYONE in raid is not in combat -> regen mana.
            boolean inCombat = player.isInCombat();
            if (!inCombat && player.getManaPercent() < 20) {
                continue;
            }
            try {
                if (!makeOnePlayerHeal()) {
                    notFoundPlayerToHealCount++;
                    PlayerObject tank = tankDetector.guessTank();
                    if (tank != null) {
                        PreviousHeal previousHeal = map.get(tank.getGuid());
                        if (previousHeal == null) {
                            makeHeal(tank, Spell.LIFEBLOOM);
                            continue;
                        }
                        UpdateSkill updateSkill = previousHeal.needUpdateLifebloom(tank);
                        if (updateSkill.needUpdateLifebloom) {
                            makeHeal(tank, Spell.LIFEBLOOM);
                        } else if (updateSkill.needUpdateRejuvenation) {
                            makeHeal(tank, Spell.REJUVENATION);
                        }
                    }
                } else {
                    notFoundPlayerToHealCount = 0;
                }
                if (notFoundPlayerToHealCount == 5) {
                    logger.info("dont have players for healing, check pets " + System.currentTimeMillis());
                    makeOnePetHeal();
                    notFoundPlayerToHealCount = 0;
                }
                /*
                if (!player.isInCombat()) {
                    int MANA_PERCENT_WHEN_DRINK = 80;
                    if (player.getManaPercent() < MANA_PERCENT_WHEN_DRINK) {
                        wowInstance.click(WinKey.D6);
                        while (!player.isInCombat() && player.getManaPercent() < 99) {
                            Utils.sleep(1000);
                        }
                    }
                }
                */
            } catch (Throwable e) {
                //reset2();
                System.out.println(e);
            }
            if (player.isDead()) {
                //reset2();
            }
            if (!inCombat) {
                logger.info("we are out of combat, so reset & sleep");
                reset2();
                Utils.sleep(3000);
            }
        }
    }

    private PlayerObject getPlayerForHeal() {
        player.updatePlayer();
        try {
            objectManager.refillPlayers();
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        PlayerObject playerWithMinimumHealth = null;
        Map<Long, PlayerObject> players = objectManager.getPlayers();
        for (Map.Entry<Long, PlayerObject> entry : players.entrySet()) {
            PlayerObject currentPlayer = entry.getValue();
            if (currentPlayer.getFaction().isAlliance() && !isTooFarAway(currentPlayer) && !currentPlayer.isDead()) {
                int needHealthForFull = entry.getValue().needHealthForFull();
                if (playerWithMinimumHealth == null || needHealthForFull > playerWithMinimumHealth.needHealthForFull()) {
                    playerWithMinimumHealth = currentPlayer;
                }
            }
        }
        return playerWithMinimumHealth;
    }

    private PlayerObject getPlayersSortByHealth() {
        player.updatePlayer();
        try {
            objectManager.refillPlayers();
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        PlayerObject playerWithMinimumHealth = null;
        Map<Long, PlayerObject> players = objectManager.getPlayers();
        for (Map.Entry<Long, PlayerObject> entry : players.entrySet()) {
            PlayerObject currentPlayer = entry.getValue();
            if (currentPlayer.getFaction().isAlliance() && !isTooFarAway(currentPlayer) && !currentPlayer.isDead()) {
                int needHealthForFull = entry.getValue().needHealthForFull();
                if (playerWithMinimumHealth == null || needHealthForFull > playerWithMinimumHealth.needHealthForFull()) {
                    playerWithMinimumHealth = currentPlayer;
                }
            }
        }
        return playerWithMinimumHealth;
    }

    private UnitObject getPetForHeal() {
        try {
            objectManager.refillUnits();
        } catch (ArrayIndexOutOfBoundsException ignore) {
        }
        UnitObject petWithMinimumHealth = null;
        Map<Long, UnitObject> units = objectManager.getUnits();
        for (Map.Entry<Long, UnitObject> entry : units.entrySet()) {
            UnitObject currentMob = entry.getValue();
            if (currentMob.getUnitReaction() != FRIENDLY) {
                continue;
            }
            if (!isTooFarAway(currentMob)) {
                int needHealthForFull = currentMob.needHealthForFull();
                if (petWithMinimumHealth == null || needHealthForFull > petWithMinimumHealth.needHealthForFull()) {
                    petWithMinimumHealth = currentMob;
                }
            }
        }
        return petWithMinimumHealth;
    }

    private boolean isTooFarAway(WowObject victim) {
        double v = Navigation.evaluateDistanceFromTo(player, victim);
        return v > 1500.0D;
    }

    private void makeHeal(CreatureObject playerWithMinimumHealth) {
        makeHeal(playerWithMinimumHealth, null);
    }

    private void makeHeal(CreatureObject playerWithMinimumHealth, Spell spell) {
        player.target(playerWithMinimumHealth);
        PreviousHeal previousHeal = map.computeIfAbsent(playerWithMinimumHealth.getGuid(), (k) -> new PreviousHeal());
        if (spell == null) {
            spell = previousHeal.getSpell(playerWithMinimumHealth);
        }
        long time = 1510L;
        if (lastSpell == Spell.REGROWTH) {
            time = 2010L;
        }

        logger.info("spell:{}", spell);

        if (System.currentTimeMillis() - timestampLastHeal >= time) {
            if (spell != Spell.NONE) {
                if (spell != Spell.SWIFTMEND) {
                    previousHeal.list.add(new Cast(spell, System.currentTimeMillis()));
                }
                if (spell == Spell.LIFEBLOOM) {
                    wowInstance.click(WinKey.D1);
                } else if (spell == Spell.REJUVENATION) {
                    wowInstance.click(WinKey.D4);
                } else if (spell == Spell.REGROWTH) {
                    wowInstance.click(WinKey.D3);
                } else if (spell == Spell.SWIFTMEND) {
                    wowInstance.click(WinKey.T);
                    boolean wasRejuvenation = false;
                    for (Iterator<Cast> iterator = previousHeal.list.iterator(); iterator.hasNext(); ) {
                        Cast cast = iterator.next();
                        if (cast.spell == Spell.REJUVENATION) {
                            if (System.currentTimeMillis() - cast.timeCast < 12000) {
                                wasRejuvenation = true;
                            }
                            iterator.remove();
                        }
                    }
                    if (!wasRejuvenation) {
                        previousHeal.list.removeIf(cast -> cast.spell == Spell.REGROWTH);
                    }

                    for (Cast cast : previousHeal.list) {
                        if (cast.spell == Spell.REJUVENATION) {
                            // if for last 12 secs were rej -> delete it from list, if not, then delete regrowth.
                            // because swiftmend always remove rej->reg
                            //previousHeal.list.remove(cast);
                        }
                    }
                }

                timestampLastHeal = System.currentTimeMillis();
                lastSpell = spell;
            }
            //Thread.sleep(800);
        }
        while (previousHeal.list.size() > 10) {
            previousHeal.list.removeFirst();
        }
    }

    enum Spell {
        REJUVENATION, REGROWTH, LIFEBLOOM, SWIFTMEND, NONE
    }

    class PreviousHeal {
        public LinkedList<Cast> list;

        public PreviousHeal() {
            list = new LinkedList<>();
        }

        public Spell getSpell(CreatureObject needHealthForFull) {
            if (list.isEmpty()) {
                if (needHealthForFull.needHealthForFull() < 3000) {
                    return Spell.LIFEBLOOM;
                }
                return Spell.REGROWTH;
            }
            UpdateSkill updateSkill = needUpdateLifebloom(needHealthForFull);
            return updateSkill.spell;
        }

        private UpdateSkill needUpdateLifebloom(CreatureObject target) {
            UpdateSkill updateSkill = new UpdateSkill();
            int needHealthForFull = target.needHealthForFull();
            // if we are OOM - cast bloom
            if (player.getManaPercent() <= 3) {
                updateSkill.needUpdateLifebloom = true;
                updateSkill.updateSpellIfNotNull(Spell.LIFEBLOOM);
            }
            long lastTimeLifebloom = -1;
            long lastTimeRejuvenation = -1;
            long lastTimeRegrowth = -1;
            for (Cast c : list) {
                if (c.spell == Spell.LIFEBLOOM) {
                    lastTimeLifebloom = c.timeCast;
                }
                if (c.spell == Spell.REJUVENATION) {
                    lastTimeRejuvenation = c.timeCast;
                }
                if (c.spell == Spell.REGROWTH) {
                    lastTimeRegrowth = c.timeCast;
                }
            }
            long now = System.currentTimeMillis();
            long lifebloomDifference = now - lastTimeLifebloom;
            if (lastTimeLifebloom == -1) {
                lifebloomDifference = -1;
            }
            long rejuvenationDifference = now - lastTimeRejuvenation;
            if (lastTimeRejuvenation == -1) {
                rejuvenationDifference = -1;
            }
            long regrowthDifference = now - lastTimeRegrowth;
            if (lastTimeRegrowth == -1) {
                regrowthDifference = -1;
            }

            if (lifebloomDifference == -1 || lifebloomDifference > UPDATE_TIME_FOR_LIFEBLOOM) {
                updateSkill.needUpdateLifebloom = true;
                updateSkill.updateSpellIfNotNull(Spell.LIFEBLOOM);
            }
            if (regrowthDifference == -1 && needHealthForFull > 2000) {
                updateSkill.needUpdateRegrowth = true;
                updateSkill.updateSpellIfNotNull(Spell.REGROWTH);
            }
            if (rejuvenationDifference == -1 || rejuvenationDifference > 14000) {
                // PVE: use rejuvement only for tank, because for dps better to use just bloom, he will get max hp faster then we heal him.
                if (target.getMaximumHealth() > 13000 || player.onBg()) {
                    updateSkill.needUpdateRejuvenation = true;
                    updateSkill.updateSpellIfNotNull(Spell.REJUVENATION);
                }
            }
            if (needHealthForFull > 8000 && regrowthDifference > 21000) {
                updateSkill.needUpdateRegrowth = true;
                updateSkill.updateSpellIfNotNull(Spell.REGROWTH);
            } else if (needHealthForFull > 10000) {
                updateSkill.needSwift = true;
                updateSkill.updateSpellIfNotNull(Spell.SWIFTMEND);
            }
            return updateSkill;
        }
    }

    private boolean regenMana() {
        objectManager.refillPlayers();
        Map<Long, PlayerObject> players = objectManager.getPlayers();
        // TODO method is combat for PlayerObject
        return false;
    }

    private class Cast {
        private final Spell spell;
        private final long timeCast;

        public Cast(
                Spell spell,
                long timeCast) {
            this.spell = spell;
            this.timeCast = timeCast;
        }
    }

    private class UpdateSkill {
        boolean needUpdateLifebloom;
        boolean needUpdateRejuvenation;
        boolean needUpdateRegrowth;
        boolean needSwift;
        Spell spell;

        void updateSpellIfNotNull(Spell spell) {
            if (this.spell == null) {
                this.spell = spell;
            }
        }
    }
}
