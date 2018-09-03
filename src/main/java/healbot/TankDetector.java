package healbot;

import java.util.HashMap;
import java.util.Map;

import wow.memory.ObjectManager;
import wow.memory.objects.PlayerObject;
import wow.memory.objects.UnitObject;

public class TankDetector {

    private ObjectManager objectManager;

    public TankDetector(ObjectManager objectManager) {
        this.objectManager = objectManager;
    }

    public PlayerObject guessTank() {
        objectManager.refillPlayers();
        objectManager.refillUnits();

        Map<Long, UnitObject> units = objectManager.getUnits();

        HashMap<Long, Long> playerGuidIsTargetting = new HashMap<>();
        for (Map.Entry<Long,UnitObject> entry: units.entrySet()) {
            UnitObject mob = entry.getValue();
            if (mob.getTargetGuid() == 0) {
                continue;
            }
            playerGuidIsTargetting.compute(mob.getTargetGuid(), (key,value) -> {
                if (value == null) {
                    return 1L;
                } else {
                    return value + 1;
                }
            });
        }
        long maxTargets = -1;
        long tankGuid = -1;
        for (Map.Entry<Long,Long> entry : playerGuidIsTargetting.entrySet()) {
            if (entry.getValue() > maxTargets) {
                maxTargets = entry.getValue();
                tankGuid = entry.getKey();
            }
        }
        Map<Long, PlayerObject> players = objectManager.getPlayers();
        if (tankGuid != -1) {
            PlayerObject tank = players.get(tankGuid);
//            System.out.println("found tank, maxHp" + tank.getMaximumHealth() + " his maxHealth: " + tank.getMaximumHealth());
            return tank;
        }
        return null;
    }
}
