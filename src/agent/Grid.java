package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Grid {

    
    private static final int EMPTY = 0;
    

   
    private int[][] grid;
    private ArrayList<Agent> agentList;
    private int timeGoalAgentObserved;
    private int totalTimeGoalAgentObserved;
    
    public Grid() {
        grid = new int[Configuration.getMaxX()][Configuration.getMaxY()];
        timeGoalAgentObserved = 0;
        totalTimeGoalAgentObserved = 0;
        for(int i = 0; i < Configuration.getMaxX(); i++) {
            for(int j = 0; j < Configuration.getMaxY(); j++) {
                grid[i][j] = EMPTY;
            }
        }
        agentList = new ArrayList<Agent>();
    }
    
    void checkGoalAgentInSight() {
        totalTimeGoalAgentObserved++;
        for(Agent a : agentList) {
            if(a.isGoalAgent()) {
                continue;
            }
            if(a.isGoalAgentInSight()) {
                timeGoalAgentObserved++;
                return;
            }
        }
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
        int tdx = x1 - x2;
        if(tdx < -Configuration.getMaxX()/2) {
            tdx += Configuration.getMaxX();
        }
        else if(tdx >= Configuration.getMaxX()/2) {
            tdx -= Configuration.getMaxX();
        }
        return tdx;
    }
    
    public int getAbsoluteTorusDistanceY(int y1, int y2) {
        int tdy = y1 - y2;
        if(tdy < -Configuration.getMaxY()/2) {
           tdy += Configuration.getMaxY();
        }
        else if(tdy >= Configuration.getMaxY()/2) {
            tdy -= Configuration.getMaxY();
        }
        return tdy;
    }    
    
    public int getAbsoluteTorusDirection(int a, int b) {
        int tdx = a - b;
        if(tdx < -Configuration.getMaxX()/2) {
            return 0;
        }
        else if(tdx >= Configuration.getMaxX()/2) {
            return 1;
        } 
        else if(tdx < 0) {
            return 1;
        } else  {
            return 0;
        }
    }
    
    public double getAbsoluteTorusDistance(Point a, Point b) {
        int tdx = getAbsoluteTorusDistanceX(a.x, b.x);
        int tdy = getAbsoluteTorusDistanceY(a.y, b.y);
        return Math.sqrt(tdx*tdx + tdy*tdy);
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
        double[] min_distance = new double[Action.MAX_DIRECTIONS];
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            min_distance[i] = Configuration.getMaxX() + Configuration.getMaxY();
        }
        
        for(Agent a : agentList) {
            
            if(a.getID() == self_id || a.isGoalAgent()) {
                continue;
            }
            
            double distance = getAgentDistance(position, a.getPosition());
            int direction = getAgentDirection(position, a.getPosition());
            
            if(min_distance[direction] > distance ) {
                min_distance[direction] = distance;
            }
        }
        return min_distance;
    }
    
    public void addAgent(Agent a) throws Exception {
        if(grid[a.getX()][a.getY()] != EMPTY) {
            throw new Exception("Grid at position " + a.getX() + "/" + a.getY() + " already used by ID " + a.getID());
        }
        grid[a.getX()][a.getY()] = a.getID();
        agentList.add(a);
    }
    
    public boolean isFreeField(Point p) {
        return ( grid[p.x][p.y] == EMPTY );
    }
    
    public boolean isDirectionInvalid(Agent a, int direction) {
        // 'no action' always succeeds
        if(direction == Action.NO_DIRECTION) {
            return false;
        }
        
        if(direction < 0 || direction >= Action.MAX_DIRECTIONS) {
            Log.errorLog("Grid.isDirectionInvalid(): Direction " + direction + " of Agent " + a.getID() + " at " + a.getX() + "/" + a.getY() + " out of range.");
        }
        int goal_x = a.getX() + Action.dx[direction];
        int goal_y = a.getY() + Action.dy[direction];
        
        if(Configuration.isIsTorus()) {
            goal_x = correctTorusX(goal_x);
            goal_y = correctTorusY(goal_y);
        }
        
        return isMovementInvalid(a, goal_x, goal_y);
    }
    
    public boolean isMovementInvalid(Agent a, int goal_x, int goal_y) {
        // did we hit a wall?
        if(goal_x < 0 || goal_x >= Configuration.getMaxX() ||
           goal_y < 0 || goal_y >= Configuration.getMaxY()) {
           return true;
        }
        if(grid[goal_x][goal_y] == EMPTY) {
            return false;
        }
        return true;
    }
    
    public static int correctTorusX(int x) {
        if(x < 0) {
            return Configuration.getMaxX() - 1;
        } else if(x >= Configuration.getMaxX()) {
            return 0;
        }
        return x;
    }
    
    public static int correctTorusY(int y) {
        if(y < 0) {
            return Configuration.getMaxY() - 1;
        } else if(y >= Configuration.getMaxY()) {
            return 0;
        }
        return y;
    }  
    
    public boolean moveAgent(Agent a, int direction) throws Exception {
/*        if(direction == Action.RANDOM_DIRECTION) {
            direction = Misc.nextInt(MAX_DIRECTIONS);
        }*/
        
        if(isDirectionInvalid(a, direction)) {
            return false;
        }
       
        int goal_x = a.getX() + Action.dx[direction];
        int goal_y = a.getY() + Action.dy[direction];
// TORUS        
        if(Configuration.isIsTorus()) {
            goal_x = correctTorusX(goal_x);
            goal_y = correctTorusY(goal_y);
        } 

        if(grid[goal_x][goal_y] == EMPTY) {
            grid[a.getX()][a.getY()] = EMPTY;
            grid[goal_x][goal_y] = a.getID();
            a.setPosition(new Point(goal_x, goal_y));
            return true;
        } else {
            return false;
        }
    }
    
    
    public String getGridString() {
        String grid_string = new String();
        NumberFormat nf=NumberFormat.getInstance(); // Get Instance of NumberFormat
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3
        
        for(int i = 0; i < Configuration.getMaxY(); i++) {
            for(int j = 0; j < Configuration.getMaxX(); j++) {
                if(grid[j][i] == EMPTY) {
                    grid_string += "  . ";
                } else if(grid[j][i] == Agent.GOAL_AGENT_ID) {
                    grid_string += " [X]";
                } else {
                    grid_string += " " + (nf.format(grid[j][i]));
                }
            }
            grid_string += "\n";
        }
        grid_string += "\n";
        return grid_string;
    }

    public int getTimeGoalAgentObserved() {
        return timeGoalAgentObserved;
    }

    public int getTotalTimeGoalAgentObserved() {
        return totalTimeGoalAgentObserved;
    }
    
    public double getPercentageGoalAgentObserved() {
        return ((double)timeGoalAgentObserved) / ((double)totalTimeGoalAgentObserved);
    }
}
