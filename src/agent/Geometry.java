package com.clawsoftware.agentsimulator.agent;

import com.clawsoftware.agentsimulator.Misc.Point;
import java.util.ArrayList;
import com.clawsoftware.agentsimulator.lcs.Action;

/**
 * Basic geometry class (lines, distances etc.
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class Geometry {

    public static ArrayList<Point> sightPoints;
    public static int maxRewardCoverage = 0;

    public static class SavedLine {

        public ArrayList<Point> torus_line;
    }

    public static class SavedLinePosition {

        public SavedLine[][] pos;
    }
    public static SavedLinePosition[][] savedLinePosition;
    public static double[][][][] torusDistance;
    private static int[][] torusDistanceX;
    private static int[][] torusDistanceY;
    public static int[] correctX;
    public static int[] correctY;

    /**
     * Precompute the sightPoints
     */
    public static void fillSightPoints() {
        int max = (int) Configuration.getSightRange();
        sightPoints = new ArrayList<Point>(max * max);
        maxRewardCoverage = 0;

        for (int i = max; i >= 1; i--) {
            for (int x = -i; x <= i; x++) {
                for (int y = -i; y <= i; y++) {
                    if (x == 0 && y == 0) {
                        continue;
                    }
                    double dist = Math.sqrt((double) (x * x + y * y));
                    if (dist > ((double) i) || (dist <= ((double) (i - 1)))) {
                        continue;
                    }
                    Point p = new Point(x, y);
                    boolean found = false;
                    for (Point t : sightPoints) {
                        if (t.x == p.x && t.y == p.y) {
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    if (dist <= Configuration.getRewardDistance()) {
                        maxRewardCoverage++;
                    }
                    sightPoints.add(p);
                }
            }
        }
    }

    /**
     * Precompute the distances on the torus
     */
    public static void fillSavedDistances() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        torusDistanceX = new int[max_x][max_x];
        torusDistanceY = new int[max_y][max_y];
        correctX = new int[128 + max_x + 128];
        correctY = new int[128 + max_y + 128];

        for (int x = 0; x < 256 + max_x; x++) {
            correctX[x] = correctTorusX(x - 128);
        }
        for (int y = 0; y < 256 + max_y; y++) {
            correctY[y] = correctTorusY(y - 128);
        }

        for (int x1 = 0; x1 < max_x; x1++) {
            for (int x2 = 0; x2 < max_x; x2++) {
                torusDistanceX[x1][x2] = getTorusDistanceX(x1, x2);
            }
        }
        for (int y1 = 0; y1 < max_y; y1++) {
            for (int y2 = 0; y2 < max_y; y2++) {
                torusDistanceY[y1][y2] = getTorusDistanceY(y1, y2);
            }
        }
        torusDistance = new double[max_x][max_y][max_x][max_y];
        for (int x = 0; x < max_x; x++) {
            for (int y = 0; y < max_y; y++) {
                for (int a = 0; a < max_x; a++) {
                    for (int b = 0; b < max_y; b++) {
                        int tdx = torusDistanceX[x][a];
                        int tdy = torusDistanceY[y][b];
                        torusDistance[x][y][a][b] = Math.sqrt(tdx * tdx + tdy * tdy);
                    }
                }
            }
        }
    }

    /**
     * Precompute the lines
     */
    public static void fillSavedLinePosition() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        int max = (int) Configuration.getSightRange();

        savedLinePosition = new SavedLinePosition[max_x][max_y];

        for (int x = 0; x < max_x; x++) {
            for (int y = 0; y < max_y; y++) {
                savedLinePosition[x][y] = new SavedLinePosition();
                savedLinePosition[x][y].pos = new SavedLine[2 * max + 1][2 * max + 1];
                for (int dx = -max; dx <= max; dx++) {
                    for (int dy = -max; dy <= max; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }
                        double dist = Math.sqrt((double) (dx * dx + dy * dy));
                        if (dist > Configuration.getSightRange()) {
                            continue;
                        }
                        int tx = correctX[x + dx + 128];
                        int ty = correctY[y + dy + 128];
                        savedLinePosition[x][y].pos[dx + max][dy + max] = new SavedLine();
                        savedLinePosition[x][y].pos[dx + max][dy + max].torus_line = Geometry.getTorusLine(new Point(x, y), new Point(tx, ty));
                    }
                }
            }
        }
    }

    /**
     * Determine direction from position1 looking at position2, depending on
     * the type of field (Torus, grid with borders)
     * @param position1 base point
     * @param position2 point to look at
     * @return Direction (0=north, 1=east, 2=south, 3=west)
     */
    public static int getDirection(final Point position1, final Point position2) {
        return getGridDirection(torusDistanceX[position1.x][position2.x],
                torusDistanceY[position1.y][position2.y]);
    }

    /**
     * corrects a x-coordinate outside of the grid if it is a torus
     * @param x the x-coordinate in question
     * @return the corrected coordinate
     */
    public static int correctTorusX(final int x) {
        if (x < 0) {
            return Configuration.getMaxX() + x;
        } else if (x >= Configuration.getMaxX()) {
            return x - Configuration.getMaxX();
        }
        return x;
    }

    /**
     * corrects a y-coordinate outside of the grid if it is a torus
     * @param y the y-coordinate in question
     * @return the corrected coordinate
     */
    public static int correctTorusY(final int y) {
        if (y < 0) {
            return Configuration.getMaxY() + y;
        } else if (y >= Configuration.getMaxY()) {
            return y - Configuration.getMaxY();
        }
        return y;
    }

    /**
     * @param a starting point
     * @param b goal point
     * @return the array of points of a line in the torus
     */
    protected static ArrayList<Point> getTorusLine(Point a, Point b) {
        if (a.x == b.x && a.y == b.y) {
            return null;
        }

        float dx = torusDistanceX[a.x][b.x];
        float dy = torusDistanceY[a.y][b.y];

        ArrayList<Point> list;

        if (Math.abs(dx) > Math.abs(dy)) {
            list = new ArrayList<Point>(1 + (int) Math.abs(dx));
            dy /= Math.abs(dx);
            int tx = dx > 0.0 ? 1 : -1;

            int x = a.x;
            float y = a.y;

            do {
                x += tx;
                y += dy;
                x = correctTorusX(x);
                y = correctTorusFloatY(y);

                list.add(new Point(x, Math.round(y)));
            } while (x != b.x);


        } else {
            list = new ArrayList<Point>(1 + (int) Math.abs(dy));
            dx /= Math.abs(dy);
            int ty = dy > 0.0 ? 1 : -1;

            float x = a.x;
            int y = a.y;

            do {
                x += dx;
                y += ty;
                x = correctTorusFloatX(x);
                y = correctTorusY(y);

                list.add(new Point(Math.round(x), y));
            } while (y != b.y);
        }
        return list;

    }

    /**
     * @param x1 base point
     * @param x2 goal point
     * @return Absolute (minimal) X-Distance between the two points on a torus
     */
    public static int getTorusDistanceX(final int x1, final int x2) {
        int tdx = x2 - x1;
        if (tdx < -Configuration.getHalfMaxX()) {
            tdx += Configuration.getMaxX();
        } else if (tdx >= Configuration.getHalfMaxX()) {
            tdx -= Configuration.getMaxX();
        }
        return tdx;
    }

    /**
     * @param y1 base point
     * @param y2 goal point
     * @return Absolute (minimal) Y-Distance between the two points on a torus
     */
    public static int getTorusDistanceY(final int y1, final int y2) {
        int tdy = y2 - y1;
        if (tdy < -Configuration.getMaxY() / 2) {
            tdy += Configuration.getMaxY();
        } else if (tdy >= Configuration.getMaxY() / 2) {
            tdy -= Configuration.getMaxY();
        }
        return tdy;
    }

    /**
     * @param a base point
     * @param b point to look at
     * @return The relative direction of point b to point a
     */
    private static int getGridDirection(int dx, int dy) {
        // east or south
        if (dx >= 0 && dy >= 0) {
            if (dx > dy) {
                return Action.EAST;
            } else {
                return Action.SOUTH;
            }
        } // west or south
        else if (dx <= 0 && dy >= 0) {
            if (Math.abs(dx) >= Math.abs(dy)) {
                return Action.WEST;
            } else {
                return Action.SOUTH;
            }
        } // east or north
        else if (dx >= 0 && dy <= 0) {
            if (Math.abs(dx) < Math.abs(dy)) {
                return Action.NORTH;
            } else {
                return Action.EAST;
            }
        } // west or north
        else //if(dx <= 0 && dy <= 0)
        {
            if (Math.abs(dx) > Math.abs(dy)) {
                return Action.WEST;
            } else {
                return Action.NORTH;
            }
        }
    }

    /**
     * corrects a x-coordinate outside of the grid if it is a torus
     * @param x the x-coordinate in question
     * @return the corrected coordinate
     */
    private static float correctTorusFloatX(final float x) {
        if (Math.round(x) < 0) {
            return x + (float) Configuration.getMaxX();
        } else if (Math.round(x) >= Configuration.getMaxX()) {
            return x - (float) Configuration.getMaxX();
        }
        return x;
    }

    /**
     * corrects a y-coordinate outside of the grid if it is a torus
     * @param y the y-coordinate in question
     * @return the corrected coordinate
     */
    private static float correctTorusFloatY(final float y) {
        if (Math.round(y) < 0) {
            return y + (float) Configuration.getMaxY();
        } else if (Math.round(y) >= Configuration.getMaxY()) {
            return y - (float) Configuration.getMaxY();
        }
        return y;
    }
}
