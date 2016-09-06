package Misc;

/**
 * Base geometry class
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class Point {
    public int x;
    public int y;
    public Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
    
    public double distance(final Point p) {
        int dx = x - p.x;
        int dy = y - p.y;
        return Math.sqrt((double)(dx * dx + dy * dy));
    }

    @Override
    public String toString() {
        return new String("[" + x + "/" + y+ "]");
    }

}
