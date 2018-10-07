package wow.components;

/**
 * @author alexlovkov
 */
public class Coordinates {

    private float x;
    private float y;
    private float z;

    public Coordinates(
        float x,
        float y,
        float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float distance(Coordinates coordinates) {
        double a = getX() - coordinates.x;
        double b = getY() - coordinates.y;
        double c = getZ() - coordinates.z;
        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    public Coordinates add(float x, float y, float z) {
        return add(new Coordinates(x, y, z));
    }

    public Coordinates add(Coordinates coordinates) {
        return new Coordinates(
            x + coordinates.getX(),
            y + coordinates.getY(),
            z + coordinates.getZ());
    }

    public Coordinates subtract(float x, float y, float z) {
        return add(new Coordinates(x, y, z));
    }

    public Coordinates subtract(Coordinates coordinates) {
        return new Coordinates(
            x - coordinates.getX(),
            y - coordinates.getY(),
            z - coordinates.getZ());
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
            "x=" + getX() +
            ", y=" + getY() +
            ", z=" + getZ() +
            '}';
    }
}
