/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;
import java.awt.Point;
import java.util.ArrayList;
import java.text.NumberFormat;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Grid {

    
    public static final int MAX_DIRECTIONS = 4;
    

    // direction starts at 0 degrees (vertical)
    private static final int[] dx = {0, 1, 0, -1, 0};
    private static final int[] dy = {-1, 0, 1, 0, 0};

    public static String[] shortDirectionString = {"N","E","S","W","r","s"};
    
    private int[][] grid;
    private ArrayList<Agent> agent;
    private int timeGoalAgentObserved;
    private int totalTimeGoalAgentObserved;
    
    public Grid() {
        grid = new int[Configuration.getMaxX()][Configuration.getMaxY()];
        timeGoalAgentObserved = 0;
        totalTimeGoalAgentObserved = 0;
        for(int i = 0; i < Configuration.getMaxX(); i++) {
            for(int j = 0; j < Configuration.getMaxY(); j++) {
                grid[i][j] = 0;
            }
        }
        agent = new ArrayList<Agent>();
    }
    
    void checkGoalAgentInSight() {
        totalTimeGoalAgentObserved++;
        for(Agent a : agent) {
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
    
    public String getInputStrings() throws Exception {
        String input_strings = new String();
        NumberFormat nf=NumberFormat.getInstance(); // Get Instance of NumberFormat
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3
        
        for(int i = 1; i < agent.size(); i++) {
            String sb="ID " + (nf.format((long)i)) + " ";
            input_strings += sb + agent.get(i).getInputString() + "\n";
        }   
        return input_strings;
    }
    
    public String getGridString() {
        String grid_string = new String();
        NumberFormat nf=NumberFormat.getInstance(); // Get Instance of NumberFormat
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3
        
        for(int i = 0; i < Configuration.getMaxY(); i++) {
            for(int j = 0; j < Configuration.getMaxX(); j++) {
                grid_string += " " + (nf.format(grid[j][i]));
            }
            grid_string += "\n";
        }
        grid_string += "\n";
        return grid_string;
    }
    
    public String getAgentStrings() {
        String agent_strings = new String();
        NumberFormat nf=NumberFormat.getInstance(); // Get Instance of NumberFormat
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3
        
        for(int i = 1; i < agent.size(); i++) {
            String sb="ID " + (nf.format((long)i)) + "\n";
            agent_strings += sb + " " + agent.get(i).toString() + "\n\n";
        }       
        
        return agent_strings;
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
