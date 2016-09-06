/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Grid {

    
    public static final int MAX_DIRECTIONS = 5;
    

    // direction starts at 0 degrees (vertical)
    private static final int[] dx = {0, 1, 0, -1, 0};
    private static final int[] dy = {-1, 0, 1, 0, 0};

    public static String[] shortDirectionString = {"N","E","S","W","r","s"};
    
    private int[][] grid;
    private ArrayList<Agent> agent;
    
    public Grid() {
        grid = new int[Configuration.getMaxX()][Configuration.getMaxY()];
        for(int i = 0; i < Configuration.getMaxX(); i++) {
            for(int j = 0; j < Configuration.getMaxY(); j++) {
                grid[i][j] = 0;
            }
        }
        agent = new ArrayList<Agent>();
    }
    
    // Punkt b relativ zu Punkt a
    public int getDirection(Point a, Point b) {
        if(Math.abs(a.x - b.x) > Math.abs(a.y - b.y)) {
            if(b.x < a.x) {
                return Action.WEST;
            } else {
                return Action.EAST;
            }
        } else {
            if(b.y < a.y) {
                return Action.NORTH;
            } else {
                return Action.SOUTH;
            }
        }
    }
    
    public int getAbsoluteTorusDistanceX(int x1, int x2) {
        int dx = x1 - x2;
        if(dx < -Configuration.getMaxX()/2) {
            dx += Configuration.getMaxX();
        }
        else if(dx >= Configuration.getMaxX()/2) {
            dx -= Configuration.getMaxX();
        }
        return dx;
    }
    
    public int getAbsoluteTorusDistanceY(int y1, int y2) {
        int dy = y1 - y2;
        if(dy < -Configuration.getMaxY()/2) {
            dy += Configuration.getMaxY();
        }
        else if(dy >= Configuration.getMaxY()/2) {
            dy -= Configuration.getMaxY();
        }
        return dy;
    }    
    
    public int getAbsoluteTorusDirection(int a, int b) {
        int dx = a - b;
        if(dx < -Configuration.getMaxX()/2) {
            return 0;
        }
        else if(dx >= Configuration.getMaxX()/2) {
            return 1;
        } 
        else if(dx < 0) {
            return 1;
        } else  {
            return 0;
        }
    }
    
    public double getAbsoluteTorusDistance(Point a, Point b) {
        int dx = getAbsoluteTorusDistanceX(a.x, b.x);
        int dy = getAbsoluteTorusDistanceY(a.y, b.y);
        return Math.sqrt(dx*dx + dy*dy);
    }
    
    // wo steht b von der Sicht von a aus?
    public int getTorusDirection(Point a, Point b) {
        int absolute_x = getAbsoluteTorusDistanceX(a.x, b.x);
        int absolute_y = getAbsoluteTorusDistanceY(a.y, b.y);
        
        if(absolute_x > absolute_y) {
            switch(getAbsoluteTorusDirection(a.x, b.x)) {
                case 0: return Action.WEST;
                case 1: return Action.EAST;
                default:return 0;
            }
        } else {
            switch(getAbsoluteTorusDirection(a.y, b.y)) {
                case 0: return Action.NORTH;
                case 1: return Action.SOUTH;
                default:return 0;
            }
        }
    }
    
    public int getAgentDirection(final Point position1, final Point position2) {
        if(Configuration.isIsTorus()) {
            return getTorusDirection(position1, position2);
        } else {
            return getDirection(position1, position2);
        }
    }
    
    public int getAgentDirectionRange(final Point position1, final Point position2) {
        if(getAgentDistance(position1, position2) > Configuration.getSightRange()) {
            return -1;
        } else {
            return getAgentDirection(position1, position2);
        }
    }
    
    public double getAgentDistance(final Point position1, final Point position2) {
        if(Configuration.isIsTorus()) {
            return getAbsoluteTorusDistance(position1, position2);
        } else {
            return Math.abs(position2.distance(position1));
        }        
    }
    
    public double[] getDirectionAgentDistances(final Point position, int self_id) {
        double[] min_distance = new double[MAX_DIRECTIONS];
        for(int i = 0; i < MAX_DIRECTIONS; i++) {
            min_distance[i] = Configuration.getMaxX() + Configuration.getMaxY();
        }
        
        for(int i=0; i < agent.size(); i++) {
            if(self_id == agent.get(i).getID()) {
                continue;
            }
            
            double distance = getAgentDistance(position, agent.get(i).getPosition());
            int direction = getAgentDirection(position, agent.get(i).getPosition());
            
            if(min_distance[direction] > distance ) {
                min_distance[direction] = distance;
            }
        }
        return min_distance;
    }
    
    public void addAgent(Agent a) throws Exception {
        if(grid[a.getX()][a.getY()] != 0) {
            throw new Exception("Grid at position " + a.getX() + "/" + a.getY() + " already used by ID " + a.getID());
        }
        grid[a.getX()][a.getY()] = a.getID();
        agent.add(a);
    }
    
    public boolean isFreeField(Point p) {
        return ( grid[p.x][p.y] == 0 );
    }
    
    public boolean moveAgent(Agent a, int direction) throws Exception {
        if(direction == Action.RANDOM_DIRECTION) {
            direction = Misc.nextInt(MAX_DIRECTIONS);
        }
        if(direction == Action.NO_DIRECTION) {
            return true;
        }

        if(direction < 0 || direction >= MAX_DIRECTIONS) {
            throw new Exception("Grid.moveAgent(): Direction " + direction + " of Agent " + a.getID() + " at " + a.getX() + "/" + a.getY() + " out of range.");
        }

        
        int goal_x = a.getX() + dx[direction];
        int goal_y = a.getY() + dy[direction];
// TORUS        
        if(goal_x < 0) {
            goal_x = Configuration.getMaxX() - 1;
        } else if(goal_x >= Configuration.getMaxX()) {
            goal_x = 0;
        }
        if(goal_y < 0) {
            goal_y = Configuration.getMaxY() - 1;
        } else if(goal_y >= Configuration.getMaxY()) {
            goal_y = 0;
        }

        if(grid[goal_x][goal_y] == 0) {
            grid[a.getX()][a.getY()] = 0;
            grid[goal_x][goal_y] = a.getID();
            a.setPosition(new Point(goal_x, goal_y));
            return true;
        } else {
            return false;
        }
    }
    
    public void printGrid() {
        String grid_string = new String();
        for(int i = 0; i < Configuration.getMaxY(); i++) {
            for(int j = 0; j < Configuration.getMaxX(); j++) {
                grid_string += grid[j][i];
            }
            grid_string += "\n";
        }
        grid_string += "\n";
        Log.gridLog(grid_string);
    }
}
