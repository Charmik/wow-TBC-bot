package telegram;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author alexlovkov
 */
public class Screenshot {

    public static BufferedImage get_screen() throws AWTException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        return robot.createScreenCapture(screenRectangle);
    }
}
