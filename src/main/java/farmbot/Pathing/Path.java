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
