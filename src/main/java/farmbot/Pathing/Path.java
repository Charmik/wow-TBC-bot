package farmbot.Pathing;

import java.util.List;

import javafx.geometry.Point3D;

/**
 * @author alexlovkov
 */
public class Path {

    private List<Point3D> points;
    private String fileName;

    public Path(
        List<Point3D> points,
        String fileName)
    {
//        for (int i = 0; i < points.size(); i++) {
//            Point3D newPoint = points.get(i).add(new Point3D(0, 1000, 0));
            //points.set(i, newPoint);
//        }
        this.points = points;
        this.fileName = fileName;
    }

    public List<Point3D> getPoints() {
        return points;
    }

    public String getFileName() {
        return fileName;
    }
}
