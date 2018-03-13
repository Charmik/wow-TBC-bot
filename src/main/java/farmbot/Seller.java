package farmbot;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Point3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Utils;
import winapi.components.WinKey;
import wow.WowInstance;
import wow.memory.CtmManager;
import wow.memory.ObjectManager;
import wow.memory.objects.Player;
import wow.memory.objects.UnitObject;

class Seller {
    private static final Logger log = LoggerFactory.getLogger(Movement.class);
    private final List<Point3D> path;
    private final Movement movement;
    private final ObjectManager objectManager;
    private final CtmManager ctmManager;
    private final WowInstance wowInstance;
    private final Player player;
    private final Healer healer;

    Seller(
        List<Point3D> path,
        Movement movement,
        ObjectManager objectManager,
        CtmManager ctmManager,
        WowInstance wowInstance,
        Player player,
        Healer healer)
    {
        this.path = path;
        this.movement = movement;
        this.objectManager = objectManager;
        this.ctmManager = ctmManager;
        this.wowInstance = wowInstance;
        this.player = player;
        this.healer = healer;
    }

    void goToSellAndComeBack() {
        this.goToVendor();
        this.wowInstance.click(WinKey.K);
        Utils.sleep(300L);
        this.wowInstance.click(WinKey.L);
        this.sellItems();
        this.wowInstance.click(WinKey.K);
        this.backToFarm();
        Utils.sleep(10000L);
    }

    private void goToVendor() {
        log.info("go to vendor");
        Iterator var1 = this.path.iterator();

        while (var1.hasNext()) {
            Point3D point3D = (Point3D) var1.next();
            this.movement.goToNextPoint(point3D);
        }

    }

    private void sellItems() {
        log.info("sell items");
        this.findSellerAndSellItems();
        this.wowInstance.click(WinKey.W);
        this.wowInstance.click(WinKey.W);
        this.wowInstance.click(WinKey.W);
        this.wowInstance.click(WinKey.S);
        this.wowInstance.click(WinKey.S);
        Utils.sleep(1000L);
        this.healer.heal((UnitObject) null);
        Utils.sleep(1000L);
        this.wowInstance.click(WinKey.D3);
        this.findSellerAndSellItems();
    }

    private void findSellerAndSellItems() {
        this.objectManager.refillUnits();
        Optional<UnitObject> nearestUnitTo = this.objectManager.getNearestUnitTo(this.player);
        if (nearestUnitTo.isPresent()) {
            log.info("have seller unit");
            UnitObject unitObject = (UnitObject) nearestUnitTo.get();
            this.ctmManager.interactNpc(unitObject);
            Utils.sleep(1000L);
        } else {
            log.info("seller wasn't found");
        }

    }

    private void backToFarm() {
        log.info("back to farm");
        Collections.reverse(this.path);
        Iterator var1 = this.path.iterator();

        while (var1.hasNext()) {
            Point3D point3D = (Point3D) var1.next();
            this.movement.goToNextPoint(point3D);
        }

        Collections.reverse(this.path);
    }

    public List<Point3D> getPath() {
        return this.path;
    }
}
