package farmbot.Pathing;

import java.util.List;

import wow.components.Coordinates;

/**
 * @author alexlovkov
 */
public class Path {

    private List<Coordinates> points;
    private String fileName;

    public Path(
        List<Coordinates> points,
        String fileName)
    {
//        for (int i = 0; i < points.size(); i++) {
//            Coordinates newPoint = points.get(i).add(new Coordinates(0, 1000, 0));
            //points.set(i, newPoint);
//        }
        this.points = points;
        this.fileName = fileName;
    }

    public List<Coordinates> getPoints() {
        return points;
    }

    public String getFileName() {
        return fileName;
    }
}
