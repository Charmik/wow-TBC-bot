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

    public boolean makeOneHeal() {
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

    private void run() {
        int i = 0;

        while (true) {
            if (i == 0 || i == 100000) {
                this.player.updatePlayer();
                this.objectManager.refillPlayers();
                i = 0;
            }

            Map<Long, PlayerObject> players = this.objectManager.getPlayers();
            PlayerObject playerWithMinimumHealth = this.getTargetForHeal(players);
            if (playerWithMinimumHealth != null) {
                this.makeHeal(playerWithMinimumHealth);
                Utils.sleep(50L);
            }
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

    private void makeHeal(PlayerObject playerWithMinimumHealth) {
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
                    previousHeal.list.add(new Cast(spell, System.currentTimeMillis()));

                    if (spell == Spell.LIFEBLOOM) {
                        this.wowInstance.click(WinKey.D1);
                    } else if (spell == Spell.REJUVENATION) {
                        this.wowInstance.click(WinKey.D4);
                    } else if (spell == Spell.REGROWTH) {
                        this.wowInstance.click(WinKey.D3);
                    }

                    this.timestampLastHeal = System.currentTimeMillis();
                }
            }
            while (previousHeal.list.size() > 10) {
                previousHeal.list.removeFirst();
            }
        }
    }

    enum Spell {
        REJUVENATION, REGROWTH, LIFEBLOOM, NONE

    }

    class PreviousHeal {

        public LinkedList<Cast> list;

        public Spell getSpell(int needHealthForFull) {
            if (needHealthForFull < 2000) {
                return Spell.LIFEBLOOM;
            }
            return null;
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
