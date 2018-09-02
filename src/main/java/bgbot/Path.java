package bgbot;

import java.util.ArrayList;
import java.util.List;

import wow.components.Navigation;

public class Path {

    private List<List<Navigation.Coordinates3D>> points = new ArrayList<>();
    private boolean fromBase = false;
    private List<Navigation.Coordinates3D> nearestPoint;

    public Path() {
    }

    public void setPoints(List<List<Navigation.Coordinates3D>> points) {
        this.points = points;
    }

    public void setFromBase(boolean fromBase) {
        this.fromBase = fromBase;
    }

    public void setNearestPoint(List<Navigation.Coordinates3D> nearestPoint) {
        this.nearestPoint = nearestPoint;
    }

    public List<List<Navigation.Coordinates3D>> getPoints() {
        return this.points;
    }

    public boolean isFromBase() {
        return this.fromBase;
    }

    public List<Navigation.Coordinates3D> getNearestPoint() {
        return this.nearestPoint;
    }

}
