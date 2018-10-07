package bgbot;

import java.util.ArrayList;
import java.util.List;

import wow.components.Coordinates;

public class Path {

    private List<List<Coordinates>> points = new ArrayList<>();
    private boolean fromBase = false;
    private List<Coordinates> nearestPoint;

    public Path() {
    }

    public void setPoints(List<List<Coordinates>> points) {
        this.points = points;
    }

    public void setFromBase(boolean fromBase) {
        this.fromBase = fromBase;
    }

    public void setNearestPoint(List<Coordinates> nearestPoint) {
        this.nearestPoint = nearestPoint;
    }

    public List<List<Coordinates>> getPoints() {
        return this.points;
    }

    public boolean isFromBase() {
        return this.fromBase;
    }

    public List<Coordinates> getNearestPoint() {
        return this.nearestPoint;
    }

}
