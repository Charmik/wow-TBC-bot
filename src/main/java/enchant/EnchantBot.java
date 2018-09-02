package enchant;

import java.awt.*;
import java.awt.event.InputEvent;

import util.Utils;

public class EnchantBot {

    static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        while (1==1) {
            robot.mouseMove(500,500);
            Utils.sleep(200);
            rightClick();
        }

        /*
        if (args.length > 0) {
            for(;;) {
                Point location = MouseInfo.getPointerInfo().getLocation();
                System.out.println(location);
                Utils.sleep(1000);
            }
        }

        int x1 = 266;
        int y1 = 799;
        int x2 = 1484;
        int y2 = 810;
        int x3 = 933;
        int y3 = 237;
        /*
        {
            x1 = 1103;
            y1 = 803;
            x2 = 1700;
            y2 = 821;
            x3 = 1427;
            y3 = 242;
        }

        for(int i = 0; i < 30; i++) {
            //button enchant
            robot.mouseMove(x1,y1);
            Utils.sleep(500);
            click();
            //first item in the bag
            robot.mouseMove(x2,y2);
            Utils.sleep(500);
            click();
            //accept enchanting
            robot.mouseMove(x3,y3);
            Utils.sleep(500);
            click();
            Utils.sleep(5100);
        }
        */
    }

    static void leftClick() {
        Utils.sleep(150);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Utils.sleep(150);
    }

    static void rightClick() {
        Utils.sleep(150);
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        Utils.sleep(150);
    }
}
