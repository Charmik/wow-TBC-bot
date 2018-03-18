package healbot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.components.Navigation;
import wow.memory.ObjectManager;
import wow.memory.objects.Player;
import wow.memory.objects.PlayerObject;

public class HealBot {


    private WowInstance wowInstance = new WowInstance("World of Warcraft");
    private Player player;
    private ObjectManager objectManager;
    private long timestampLastHeal;
    private Map<Long, PreviousHeal> map;

    public HealBot() {
        this.player = this.wowInstance.getPlayer();
        this.objectManager = this.wowInstance.getObjectManager();
        this.timestampLastHeal = 0L;
        this.map = new HashMap<>();
    }

    public static void main(String[] args) throws InterruptedException {
        HealBot healBot = new HealBot();
        healBot.run();
    }

    public boolean makeOneHeal() throws InterruptedException {
        this.player.updatePlayer();
        this.objectManager.refillPlayers();
        Map<Long, PlayerObject> players = this.objectManager.getPlayers();
        PlayerObject playerWithMinimumHealth = this.getTargetForHeal(players);
        if (playerWithMinimumHealth == null) {
            return false;
        } else {
            this.makeHeal(playerWithMinimumHealth);
            Utils.sleep(50L);
            return true;
        }
    }

    private void run() throws InterruptedException {
        int i = 0;

        while (true) {
            makeOneHeal();
            ++i;
        }
    }

    private PlayerObject getTargetForHeal(Map<Long, PlayerObject> players) {
        PlayerObject playerWithMinimumHealth = null;
        Iterator var3 = players.entrySet().iterator();

        while (true) {
            PlayerObject victim;
            int needHealthForFull;
            do {
                do {
                    do {
                        if (!var3.hasNext()) {
                            return playerWithMinimumHealth;
                        }
                        Map.Entry entry = (Map.Entry) var3.next();
                        victim = (PlayerObject) entry.getValue();
                        needHealthForFull = victim.needHealthForFull();
                    } while (needHealthForFull < 2);
                } while (needHealthForFull > 30000);
            }
            while (playerWithMinimumHealth != null && playerWithMinimumHealth.needHealthForFull() >= needHealthForFull);

            playerWithMinimumHealth = victim;
        }
    }

    private boolean isTooFarAway(PlayerObject victim) {
        double v = Navigation.evaluateDistanceFromTo(this.player, victim);
        return v > 1000.0D;
    }

    private void makeHeal(PlayerObject playerWithMinimumHealth) throws InterruptedException {
        int needHealthForFull = playerWithMinimumHealth.needHealthForFull();
        if (needHealthForFull >= 1150) {
            player.target(playerWithMinimumHealth);
            PreviousHeal previousHeal = this.map.computeIfAbsent(playerWithMinimumHealth.getGuid(), (k) -> new PreviousHeal());
            Spell spell = previousHeal.getSpell(needHealthForFull);
            long time = 1500L;
            if (previousHeal.list.size() > 0 && (previousHeal.list.getLast()).spell == Spell.REGROWTH) {
                time = 2000L;
            }

            if (System.currentTimeMillis() - this.timestampLastHeal >= time) {
                if (spell != Spell.NONE) {
                    if (spell != Spell.SWIFTMEND) {
                        previousHeal.list.add(new Cast(spell, System.currentTimeMillis()));
                    }

                    if (spell == Spell.LIFEBLOOM) {
                        this.wowInstance.click(WinKey.D1);
                    } else if (spell == Spell.REJUVENATION) {
                        this.wowInstance.click(WinKey.D4);
                    } else if (spell == Spell.REGROWTH) {
                        this.wowInstance.click(WinKey.D3);
                    } else if (spell == Spell.SWIFTMEND) {
                        this.wowInstance.click(WinKey.D2);
                    }

                    this.timestampLastHeal = System.currentTimeMillis();
                }
                //Thread.sleep(800);
            }
            while (previousHeal.list.size() > 10) {
                previousHeal.list.removeFirst();
            }
        }
    }

    enum Spell {
        REJUVENATION, REGROWTH, LIFEBLOOM, SWIFTMEND, NONE

    }

    class PreviousHeal {

        public LinkedList<Cast> list;

        public PreviousHeal() {
            this.list = new LinkedList<>();
        }

        public Spell getSpell(int needHealthForFull) {
            if (list.isEmpty()) {
                if (needHealthForFull < 1500) {
                    System.out.println("return LIFEBLOOM because list is empty for this player");
                    return Spell.LIFEBLOOM;
                } else if (needHealthForFull < 4000) {
                    System.out.println("return REJUVENATION because list is empty for this player");
                    return Spell.REJUVENATION;
                } else {
                    System.out.println("return REGROWTH because list is empty for this player");
                    return Spell.REGROWTH;
                }
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

            //didn't use lifebloom, or it will over soon, need update.
            if (lifebloomDifference == -1 || lifebloomDifference > 4500) {
                System.out.println("return LIFEBLOOM lifebloomDifference:" + lifebloomDifference);
                return Spell.LIFEBLOOM;
            }
            if (regrowthDifference == -1) {
                return Spell.REGROWTH;
            }
            if (rejuvenationDifference == -1 || rejuvenationDifference > 12000) {
                return Spell.REJUVENATION;
            }
            if (needHealthForFull > 8000 && regrowthDifference > 10000) {
                return Spell.REGROWTH;
            } else {
                return Spell.SWIFTMEND;
            }
            //return null;
        }
    }

    private class Cast {
        private final Spell spell;
        private final long timeCast;

        public Cast(
            Spell spell,
            long timeCast)
        {
            this.spell = spell;
            this.timeCast = timeCast;
        }
    }
}
