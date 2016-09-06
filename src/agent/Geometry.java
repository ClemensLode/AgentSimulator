package agent;

import Misc.Point;
import java.util.ArrayList;
import lcs.Action;

/**
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Geometry {

    /**
     * Determine direction from position1 looking at position2, depending on
     * the type of field (Torus, grid with borders)
     * @param position1 base point
     * @param position2 point to look at
     * @return Direction (0=north, 1=east, 2=south, 3=west)
     */
    public static int getDirection(final Point position1, final Point position2) {
        return getGridDirection(getTorusDistanceX(position1.x, position2.x), getTorusDistanceY(position1.y, position2.y));
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


    protected static ArrayList<Point> getTorusLine(Point a, Point b) {
        /*if(a.x == b.x) {
            return getVerticalTorusLine(a.x, a.y, b.y);
        }
        if(a.y == b.y) {
            return getHorizontalTorusLine(a.y, a.x, b.x);
        }*/

        ArrayList<Point> list = new ArrayList<Point>();
        if(a.x == b.x && a.y == b.y) {
            return list;
        }

        float dx = getTorusDistanceX(a.x, b.x);
        float dy = getTorusDistanceY(a.y, b.y);

        if(Math.abs(dx) > Math.abs(dy)) {
            dy /= Math.abs(dx);
            int tx = dx>0.0?1:-1;

            int x = a.x;
            float y = a.y;

            do {
                x += tx;
                y += dy;
                x = correctTorusX(x);
                y = correctTorusFloatY(y);
                
                list.add(new Point(x, Math.round(y)));
            } while(x != b.x);


        } else {
            dx /= Math.abs(dy);
            int ty = dy>0.0?1:-1;

            float x = a.x;
            int y = a.y;

            do {
                x += dx;
                y += ty;
                x = correctTorusFloatX(x);
                y = correctTorusY(y);

                list.add(new Point(Math.round(x), y));
            } while(y != b.y);
        }
        return list;

    }

    private static ArrayList<Point> getVerticalTorusLine(int x, int y1, int y2) {
        if(getAbsoluteTorusIsSouthOf(y1, y2)) {
            if(y2 > y1) {
                return getVerticalLine(x, y1, y2);
            } else {
                ArrayList<Point> list = getVerticalLine(x, y1, 0);
                list.addAll(getVerticalLine(x, Configuration.getMaxY()-1, y2));
                return list;
            }
        } else {
            if(y1 > y2) {
                return getVerticalLine(x, y1, y2);
            } else {
                ArrayList<Point> list = getVerticalLine(x, y1, Configuration.getMaxY()-1);
                list.addAll(getVerticalLine(x, 0, y2));
                return list;
            }
        }
    }

    private static ArrayList<Point> getHorizontalTorusLine(int y, int x1, int x2) {
        if(getAbsoluteTorusIsEastOf(x1, x2)) {
            if(x2 > x1) {
                return getHorizontalLine(y, x1, x2);
            } else {
                ArrayList<Point> list = getHorizontalLine(y, x1, 0);
                list.addAll(getHorizontalLine(y, Configuration.getMaxX()-1, x2));
                return list;
            }
        } else {
            if(x1 > x2) {
                return getHorizontalLine(y, x1, x2);
            } else {
                ArrayList<Point> list = getHorizontalLine(y, x1, Configuration.getMaxX()-1);
                list.addAll(getHorizontalLine(y, 0, x2));
                return list;
            }
        }
    }

    private static ArrayList<Point> getVerticalLine(int x, int y1, int y2) {
        ArrayList<Point> list = new ArrayList<Point>();
        int dy = y2>y1?1:-1;
        int y = y1;
        while(y != y2) {
            y+=dy;
            list.add(new Point(x, y));
        }
        return list;
    }

    private static ArrayList<Point> getHorizontalLine(int y, int x1, int x2) {
        ArrayList<Point> list = new ArrayList<Point>();
        int dx = x2>x1?1:-1;
        int x = x1;
        while(x != x2) {
            x+=dx;
            list.add(new Point(x, y));
        }
        return list;
    }

    private static ArrayList<Point> getLine(Point a, Point b) {
        if(a.x == b.x) {
            return getVerticalLine(a.x, a.y, b.y);
        }
        if(a.y == b.y) {
            return getHorizontalLine(a.y, a.x, b.x);
        }

        ArrayList<Point> list = new ArrayList<Point>();
        float dx = (float)(b.x - a.x);
        float dy = (float)(b.y - a.y);

        if(dx > dy) {
            dy /= Math.abs(dx);
            int tx = dx>0.0?1:-1;

            int x = a.x;
            float y = a.y;

            while(x != b.x) {
                list.add(new Point(x, Math.round(y)));
                x += tx;
                y += dy;
            }
        } else {
            dx /= Math.abs(dy);
            int ty = dy>0.0?1:-1;

            float x = a.x;
            int y = a.y;

            while(y != b.y) {
                list.add(new Point(Math.round(x), y));
                x += dx;
                y += ty;
            }
        }
        return list;
    }

    /**
     * @param x1 base point
     * @param x2 point to look at
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
     * @param y2 point to look at
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
     * @param x1 base x-coordinate
     * @param x2 x-coordinate we want relate to x1
     * @return Direction (left/right: false/true) of the vector with the minmal distance from a to b on the torus
     */
    private static boolean getAbsoluteTorusIsEastOf(final int x1, final int x2) {
        int tdx = x1 - x2;
        if (tdx < -Configuration.getHalfMaxX()) {
            return false;
        } else if (tdx >= Configuration.getHalfMaxX()) {
            return true;
        } else if (tdx < 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param y1 base y-coordinate
     * @param y2 y-coordinate we want relate to y1
     * @return Direction (above/below: false/true) of the vector with the minmal distance from a to b on the torus
     */
    private static boolean getAbsoluteTorusIsSouthOf(final int y1, final int y2) {
        int tdx = y1 - y2;
        if (tdx < -Configuration.getMaxY() / 2) {
            return false;
        } else if (tdx >= Configuration.getMaxY() / 2) {
            return true;
        } else if (tdx < 0) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * @param a base point
     * @param b point to look at
     * @return The relative direction of point b to point a
     */
    private static int getGridDirection(int dx, int dy) {
        // east or south
        if(dx > 0 && dy > 0) {
            if(Math.abs(dx) > Math.abs(dy)) {
                return Action.EAST;
            } else {
                return Action.SOUTH;
            }
        }
        // west or south
        else if(dx < 0 && dy > 0) {
            if(Math.abs(dx) < Math.abs(dy)) {
                return Action.WEST;
            } else {
                return Action.SOUTH;
            }
        }
        // east or north
        else if(dx > 0 && dy < 0) {
            if(Math.abs(dx) < Math.abs(dy)) {
                return Action.NORTH;
            } else {
                return Action.EAST;
            }
        }
        // west or north
        else //if(dx < 0 && dy < 0)
        {
            if(Math.abs(dx) > Math.abs(dy)) {
                return Action.WEST;
            } else {
                return Action.NORTH;
            }
        }


    // east west, horizontal difference greater than vertical difference
    /*    if (Math.abs(dx) > Math.abs(dy)) {
            if(dx > 0) {
                    return Action.EAST;
            } else {
                    return Action.WEST;
            }
        } else {
            if(dy > 0) {
                return Action.SOUTH;
            } else {
                return Action.NORTH;
            }
        }      */
    }

    /**
     * corrects a x-coordinate outside of the grid if it is a torus
     * @param x the x-coordinate in question
     * @return the corrected coordinate
     */
    private static float correctTorusFloatX(final float x) {
        if (Math.round(x) < 0) {
            return x + (float)Configuration.getMaxX();
        } else if (Math.round(x) >= Configuration.getMaxX()) {
            return x - (float)Configuration.getMaxX();
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
            return y + (float)Configuration.getMaxY();
        } else if (Math.round(y) >= Configuration.getMaxY()) {
            return y - (float)Configuration.getMaxY();
        }
        return y;
    }


}
