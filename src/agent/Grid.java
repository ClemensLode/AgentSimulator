package agent;

import agents.BaseAgent;
import Misc.Statistics;
import Misc.Misc;
import Misc.Point;
import lcs.Action;
import java.util.ArrayList;
import lcs.ClassifierSet;

/**
 *
 * Provides routines for the main field
 * All movements and collisions of the agents will be registered here
 * This class also provides detection routines (sight range)
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Grid extends BaseGrid {

   
    /** 
     * pointer to actual agents, 
     * optimization in order to not have to search through the whole grid each time
     */ 
    private ArrayList<BaseAgent> agentList;
    private ArrayList<Point> obstacleList;
    private ArrayList<Point> sightPoints = new ArrayList<Point>();

    private void fillSightPoints() {
        int max = (int)Configuration.getSightRange();
        for(int i = max; i >= 1; i--) {
            for(int x = -i; x <= i; x++) {
                for(int y = -i; y <= i; y++) {
                    if(x == 0 && y == 0) {
                        continue;
                    }
                    double dist = Math.sqrt((double)(x*x + y*y));
                    if(dist > ((double)i) || (dist <= ((double)(i-1)))) {
                        continue;
                    }
                    Point p = new Point(x, y);
                    boolean found = false;
                    for(Point t : sightPoints) {
                        if(t.x == p.x && t.y == p.y) {
                            found = true;
                            break;
                        }
                    }
                    if(found) {                        
                        continue;
                    }
                    sightPoints.add(p);
                }
            }
        }
    }

    /**
     * Initialize an empty grid and an empty agent list
     */
    public Grid() {
        super();
        agentList = new ArrayList<BaseAgent>();
        obstacleList = new ArrayList<Point>();
        clearGrid();
        fillSightPoints();
        fillSavedLinePosition();
        fillSavedDistances();
    }

    private void fillSavedDistances() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        torusDistance = new double[max_x][max_y][max_x][max_y];
        for(int x = 0; x < max_x; x++) {
            for(int y = 0; y < max_y; y++) {
                for(int a = 0; a < max_x; a++) {
                    for(int b = 0; b < max_y; b++) {
                        int tdx = Geometry.getTorusDistanceX(x, a);
                        int tdy = Geometry.getTorusDistanceY(y, b);
                        torusDistance[x][y][a][b] = Math.sqrt(tdx * tdx + tdy * tdy);
                    }
                }
            }
        }
    }

    /**
     * Clears the grid and places a new configuration of obstacles
     */
    public void clearGrid() {
        clear();

            obstacleList.clear();
            switch(Configuration.getScenarioType()) {
                case Configuration.RANDOM_SCENARIO: {
                    int obstacle_count = (int)(Configuration.getObstaclePercentage() * (double) (Configuration.getMaxX() * Configuration.getMaxY()));
                    while(obstacle_count > 0) {
                        Point p = getFreeField();
                        obstacle_count = createConnectedObstacle(p, obstacle_count);
                    }
                }
                break;
                case Configuration.NON_TORUS_SCENARIO: {
                    for(int x = 0; x < Configuration.getMaxX(); x++) {
                        createObstacle(new Point(x, 0));
                    }
                    for(int y = 1; y < Configuration.getMaxY(); y++) {
                        createObstacle(new Point(0, y));
                    }
                }
                break;
                case Configuration.PILLAR_SCENARIO: {
                    int nx = Configuration.getMaxX() / 8;
                    int ny = Configuration.getMaxY() / 8;
                    for(int x = 0; x < nx; x++) {
                        for(int y = 0; y < ny; y++) {
                            createObstacle(new Point(x * 8 + 4, y * 8 + 4));
                        }
                    }
                }
                break;
                case Configuration.CROSS_SCENARIO: {
                    int x1 = Configuration.getMaxX() / 4;
                    int x2 = Configuration.getMaxX() - x1;
                    int y1 = Configuration.getMaxY() / 4;
                    int y2 = Configuration.getMaxY() - y1;
                    int cx = Configuration.getMaxX() / 2;
                    int cy = Configuration.getMaxY() / 2;
                    for(int x = x1; x <= x2; x++) {
                        createObstacle(new Point(x, cy));
                    }
                    for(int y = y1; y < cy; y++) {
                        createObstacle(new Point(cx, y));
                    }
                    for(int y = cy+1; y <= y2; y++) {
                        createObstacle(new Point(cx, y));
                    }
                }
                break;
                case Configuration.ROOM_SCENARIO: {
                    int x1 = Configuration.getMaxX() / 4;
                    int x2 = Configuration.getMaxX() - x1;
                    int y1 = Configuration.getMaxY() / 4;
                    int y2 = Configuration.getMaxY() - y1;
                    int c1 = Configuration.getMaxX() / 2 - 1;
                    int c2 = c1 + 3;
                    for(int x = x1; x <= x2; x++) {
                        createObstacle(new Point(x, y2));
                        if(x >= c1 && x <= c2) {
                            continue;
                        }
                        createObstacle(new Point(x, y1));
                    }
                    for(int y = y1+1; y < y2; y++) {
                        createObstacle(new Point(x1, y));
                        createObstacle(new Point(x2, y));
                    }
                }
                break;
                case Configuration.DIFFICULT_SCENARIO: {
                    int y1 = Configuration.getMaxY() / 4 - 1;
                    int y2 = Configuration.getMaxY() / 4 + 1;
                    int y3 = (3 * Configuration.getMaxY()) / 4 - 1;
                    int y4 = (3 * Configuration.getMaxY()) / 4 + 1;
                    int n = Configuration.getMaxX() / 4;
                    for(int i = 0; i < Configuration.getMaxY(); i++) {
                        createObstacle(new Point(Configuration.getMaxX() - 1, i));
                    }
                    for(int i = 1; i < n; i++) {
                        if(i%2 == 0) {
                            for(int j = 0; j < y1; j++) {
                                createObstacle(new Point(4*i, j));
                            }
                            for(int j = y2; j < Configuration.getMaxY(); j++) {
                                createObstacle(new Point(4*i, j));
                            }
                        } else {
                            for(int j = 0; j < y3; j++) {
                                createObstacle(new Point(4*i, j));
                            }
                            for(int j = y4; j < Configuration.getMaxY(); j++) {
                                createObstacle(new Point(4*i, j));
                            }
                        }
                    }
                }
                break;


        }        
    }
// TODO wenn kein torus, Hindernis erkennen?
    private void createObstacle(Point p) {
        grid[p.x][p.y].setContent(Field.OBSTACLE);
        obstacleList.add(new Point(p.x, p.y));
    }


    private int createConnectedObstacle(Point p, int obstacle_count) {
        if(isObstacleNear(p) && Misc.nextDouble() > Configuration.getObstacleConnectionFactor()) {
            return obstacle_count;
        }
        createObstacle(p);

        obstacle_count--;
        if(obstacle_count == 0) {
            return 0;
        }
        if(Misc.nextDouble() <= Configuration.getObstacleConnectionFactor()) {
            Point new_p = getFreeFieldNear(p);
            if(new_p != null) {
                obstacle_count = createConnectedObstacle(new_p, obstacle_count);
            }
        }
        return obstacle_count;
    }
    
    public void resetState() throws Exception {
        clearGrid();
        for(Point p : obstacleList) {
            grid[p.x][p.y].setContent(Field.OBSTACLE);
        }
        for(BaseAgent a : agentList) {
            Point p;
            if(a.isGoalAgent()) {
                p = getFreeGoalAgentField();
            } else {
                p = getFreeAgentField();
            }
            a.setPosition(p);
            grid[p.x][p.y].setContent(a.getID());
            a.resetBeforeNewProblem();
        }
        updateSight();
    }

    public void updateSight() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        int max = (int)Configuration.getSightRange();
        for (int i = 0; i < max_x; i++) {
            for (int j = 0; j < max_y; j++) {
                grid[i][j].clearSight();
            }
        }
        
        for(BaseAgent a : agentList) {
            int ax = a.getX();
            int ay = a.getY();
            for(Point sp : sightPoints) {
                
                int dx = Geometry.correctTorusX(ax + sp.x);
                int dy = Geometry.correctTorusY(ay + sp.y);
                double dist = Math.sqrt(sp.x * sp.x + sp.y * sp.y);

//                double dist = savedLinePosition[a.getX()][a.getY()].pos[dx][dy].distance;
                if(!Configuration.isObstaclesBlockSight()) {
                    grid[dx][dy].addSeen(a.getID());
                    if(dist <= Configuration.getRewardDistance()) {
                        grid[dx][dy].addRewarded(a.getID());
                    }
                    continue;
                }
                // already checked
                if(grid[dx][dy].isSeenBy(a.getID())) {
                    continue;
                }

                ArrayList<Point> line_points = savedLinePosition[ax][ay].pos[sp.x+max][sp.y+max].torus_line;
                for(Point p : line_points) {
                    grid[p.x][p.y].addSeen(a.getID());
                    if(torusDistance[ax][ay][p.x][p.y] <= Configuration.getRewardDistance()) {
                        grid[p.x][p.y].addRewarded(a.getID());
                    }
                    if(grid[p.x][p.y].isOccupied()) {
                        break;
                    }
                }
            }
        }
    }

    private class SavedLine {
        public ArrayList<Point> torus_line;
    }

    private class SavedLinePosition {
        public SavedLine[][] pos;
    }
    private SavedLinePosition[][] savedLinePosition;
    
    private double[][][][] torusDistance;

    private void fillSavedLinePosition() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        int max = (int)Configuration.getSightRange();

        savedLinePosition = new SavedLinePosition[max_x][max_y];

        for(int x = 0; x < max_x; x++) {
            for(int y = 0; y < max_y; y++) {
                savedLinePosition[x][y] = new SavedLinePosition();
                savedLinePosition[x][y].pos = new SavedLine[2*max+1][2*max+1];
                for(int dx = -max; dx <= max; dx++) {
                    for(int dy = -max; dy <= max; dy++) {
                        if(dx == 0 && dy == 0) {
                            continue;
                        }
                        double dist = Math.sqrt((double)(dx*dx+dy*dy));
                        if(dist > Configuration.getSightRange()) {
                            continue;
                        }
                        int tx = Geometry.correctTorusX(x + dx);
                        int ty = Geometry.correctTorusY(y + dy);
                        savedLinePosition[x][y].pos[dx+max][dy+max] = new SavedLine();
                        savedLinePosition[x][y].pos[dx+max][dy+max].torus_line = Geometry.getTorusLine(new Point(x, y), new Point(tx, ty));
                    }
                }
            }
        }
    }

    
    /**
     * Transfer reward to other agents
     * @param agent The agent that collected the reward
     * @param action_set_size The size of the action set of the original agent (i.e. number of steps that get rewarded)
     * @param reward The amount of reward
     * @throws java.lang.Exception If there was an error collecting the external reward
     * @see LCS_Agent#collectExternalReward
     */
    public void contactOtherAgents(BaseAgent agent, int start_index, int action_set_size, boolean reward, boolean is_event) throws Exception {
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent() || a.getID() == agent.getID()) {
                continue;
            }
            a.collectExternalReward(agent, start_index, action_set_size, reward, is_event);
        }
    }


    public Point getFreeAgentField() {
        switch(Configuration.getScenarioType()) {
            case Configuration.NON_TORUS_SCENARIO:
                return getFreeField(new Point(1, 1), new Point(Configuration.getMaxX(), Configuration.getMaxY()));
            case Configuration.PILLAR_SCENARIO:
            case Configuration.ROOM_SCENARIO:
            {
                Point p = new Point(0,0);
                do {

                    switch(Misc.nextInt(4)) {
                        case 0:p = new Point(Misc.nextInt(Configuration.getMaxX()), 0);break;
                        case 1:p = new Point(Misc.nextInt(Configuration.getMaxX()), Configuration.getMaxY()-1);break;
                        case 2:p = new Point(0, Misc.nextInt(Configuration.getMaxY()));break;
                        case 3:p = new Point(Configuration.getMaxX()-1, Misc.nextInt(Configuration.getMaxY()));break;
                        default:break;
                    }
                } while (grid[p.x][p.y].isOccupied());
                return p;
            }
            case Configuration.DIFFICULT_SCENARIO: {
                return getFreeField(new Point(0,0), new Point(1, Configuration.getMaxY()));
            }
            default: {
                return getFreeField();
            }
        }
    }

    public void maybeRemoveAgentDirections(final BaseAgent a, ArrayList<Integer> available_directions, double probability) {
        boolean[] direction_agent_in_sight_list = getDirectionAgentInSightList(a.getPosition(), a.getID());
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(Misc.nextDouble() < probability && direction_agent_in_sight_list[i]) {
                available_directions.remove(new Integer(i));
            }
        }
    }

    public void maybeRemoveObstacleDirections(final BaseAgent a, ArrayList<Integer> available_directions, double probability) {
        boolean[] direction_obstacle_in_sight_list = getDirectionObstacleInSightList(a.getPosition(), a.getID());
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(Misc.nextDouble() < probability && direction_obstacle_in_sight_list[i]) {
                available_directions.remove(new Integer(i));
            }
        }
    }

    public void maybeRemoveOpenDirections(final BaseAgent a, ArrayList<Integer> available_directions, double probability) {
        boolean[] direction_obstacle_in_sight_list = getDirectionObstacleInSightList(a.getPosition(), a.getID());
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(Misc.nextDouble() < probability && !direction_obstacle_in_sight_list[i]) {
                available_directions.remove(new Integer(i));
            }
        }
    }

    public Point getFreeGoalAgentField() {
        switch(Configuration.getScenarioType()) {
            case Configuration.NON_TORUS_SCENARIO:
                return getFreeField(new Point(1, 1), new Point(Configuration.getMaxX(), Configuration.getMaxY()));
            case Configuration.PILLAR_SCENARIO:
            case Configuration.ROOM_SCENARIO: {
                int x = Configuration.getHalfMaxX();
                int y = Configuration.getHalfMaxY();
                if(grid[x][y].isOccupied()) {
                    return new Point(x+1, y);
                } else {
                    return new Point(x, y);
                }
            }
            case Configuration.DIFFICULT_SCENARIO: {
                return new Point(Configuration.getMaxX() - 2, 0);
            }
            default:{
                return getFreeField();
            }
        }
    }
   
    
    /**
     * @param a Agent in question
     * @return true if this agent is in sight of the goal agent
     */
    public boolean isGoalAgentInRewardRange(final BaseAgent a) {
        if(a.isGoalAgent()) {
            return false;
        }
        return grid[a.getX()][a.getY()].isRewardFor(Field.GOAL_AGENT_ID);
    }

    
    /**
     * @return true if any agent sees the goal agent
     */
    public boolean isGoalAgentInRewardRangeByAnyAgent() {
        return grid[BaseAgent.goalAgent.getX()][BaseAgent.goalAgent.getY()].isRewardedForAgents();
    }

    public BaseAgent findRandomAgentNearby(Point position, int id) {
        ArrayList<BaseAgent> nearby_agents = new ArrayList<BaseAgent>();
        for(BaseAgent a : agentList) {
            if(a.getID() == id || a.isGoalAgent()) {
                continue;
            }
            if(a.getPosition().distance(position) <= 2.0*Configuration.getSightRange()) {
                nearby_agents.add(a);
            }
        }
        if(nearby_agents.isEmpty()) {
            return null;
        }
        return nearby_agents.get(Misc.nextInt(nearby_agents.size()));
    }


    /**
     * Determines the distance from position1 to position2, depending on the
     * type of field (torus, grid with borders) and the sight range
     * @param position1 base point
     * @param position2 target point
     * @return the direction if position2 is in sight range of position1,
     * -1 otherwise
     */
    private int getGoalAgentDirectionSightRange(final Point position, final int self_id) {
        if(self_id == Field.GOAL_AGENT_ID) {
            return -1;
        }
        Point goal_position = BaseAgent.goalAgent.getPosition();
        if(!grid[goal_position.x][goal_position.y].isSeenBy(self_id)) {
            return -1;
        }

        return Geometry.getDirection(position, goal_position);
    }
    
    /**
     * Calculates the distances to the nearest agents in each direction
     * @param position The position of the agent in question
     * @param self_id The ID of the agent in question
     * @return An array of minimal distances to other agents
     */
    private boolean[] getDirectionAgentInSightList(final Point position, final int self_id) {
        boolean[] absolute_direction_agent_in_sight = new boolean[Action.MAX_DIRECTIONS];
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            absolute_direction_agent_in_sight[i] = false;
        }
        
        for (BaseAgent a : agentList) {

            if (a.getID() == self_id || a.isGoalAgent() || !grid[a.getX()][a.getY()].isSeenBy(self_id)) {
                continue;
            }
            absolute_direction_agent_in_sight[Geometry.getDirection(position, a.getPosition())] = true;
        }        
        return absolute_direction_agent_in_sight;
    }
    
    /**
     * Calculates the distances to the nearest obstacles in each direction
     * @param position The position of the agent in question
     * @return An array of minimal distances to obstacles
     */
    private boolean[] getDirectionObstacleInSightList(final Point position, final int self_id) {
        boolean[] absolute_direction_obstacle_in_sight = new boolean[Action.MAX_DIRECTIONS];
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            absolute_direction_obstacle_in_sight[i] = false;
        }

        for (Point p : obstacleList) {
            
            if(!grid[p.x][p.y].isSeenBy(self_id)) {
                continue;
            }
            absolute_direction_obstacle_in_sight[Geometry.getDirection(position, p)] = true;
        }

        return absolute_direction_obstacle_in_sight;
    }
    
    /**
     * Determines the sensor bit field of an agent at 'position' with 
     * id 'self_id' depending on all other agents
     * The directions are absolute, i.e. [0] corresponds to NORTH
     * @param position the position of the agent
     * @param self_id the id of the agent
     * @return sensor bit field that the agent is seeing
     * @see Sensors#Sensors
     */
    
    public Sensors getAbsoluteSensorInformation(final Point position, final int self_id) {
        return new Sensors(getGoalAgentDirectionSightRange(position, self_id),
                getDirectionAgentInSightList(position, self_id),
                getDirectionObstacleInSightList(position, self_id) );
    }

    /**
     * Put an agent on the grid
     * @param a the BaseAgent
     */
    public void addAgent(final BaseAgent a) {
        agentList.add(a);
    }

    public void updateStatistics(long currentTimestep, ClassifierSet c_set) {
    /**
     * Check if any agent sees the goal agent
     */
        double average_agent_distance = getAverageAgentDistance();
        double average_goal_distance = getAverageGoalAgentDistance();
        double average_points = getAverageIndividualTotalPoints();

        Statistics.addStatisticEntry(
            currentTimestep,
            c_set,
            isGoalAgentInRewardRangeByAnyAgent(),
            average_agent_distance,
            getSpreadAgentDistance(average_agent_distance),
            average_goal_distance,
            getSpreadGoalAgentDistance(average_goal_distance),
            getCoveredAreaFactor(),
            average_points,
            getSpreadIndividualTotalPoints(average_points));

    }

    private double getAverageAgentDistance() {
        int count = 0;
        double dist = 0.0;
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent()) {
                continue;
            }
            int ax = a.getX();
            int ay = a.getY();
            for(BaseAgent b : agentList) {
                if(b.isGoalAgent() || a.getID() == b.getID()) {
                    continue;
                }
                dist += torusDistance[ax][ay][b.getX()][b.getY()];
                count++;
            }
        }
        dist /= (double)count;
        return dist;
    }

    private double getSpreadAgentDistance(double average_agent_distance) {
        int count = 0;
        double spread = 0.0;
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent()) {
                continue;
            }
            int ax = a.getX();
            int ay = a.getY();

            for(BaseAgent b : agentList) {
                if(b.isGoalAgent() || a.getID() == b.getID()) {
                    continue;
                }
                double diff = torusDistance[ax][ay][b.getX()][b.getY()] - average_agent_distance;
                spread += diff * diff;
                count++;
            }
        }
        spread /= (double)count;
        return Math.sqrt(spread);
    }

    private double getAverageGoalAgentDistance() {
        double dist = 0.0;
        int count = 0;
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent()) {
                int ax = a.getX();
                int ay = a.getY();
                for(BaseAgent b : agentList) {
                    if(b.isGoalAgent()) {
                        continue;
                    }
                    dist += torusDistance[ax][ay][b.getX()][b.getY()];
                    count++;
                }
                dist /= (double)count;
                return dist;
            }
        }
        return 0.0;
    }

    private double getSpreadGoalAgentDistance(double average_goal_distance) {
        int count = 0;
        double spread = 0.0;
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent()) {
                int ax = a.getX();
                int ay = a.getY();

                for(BaseAgent b : agentList) {
                    if(b.isGoalAgent()) {
                        continue;
                    }
                    double diff = torusDistance[ax][ay][b.getX()][b.getY()] - average_goal_distance;
                    spread += diff * diff;
                    count++;
                }
                spread /= (double)count;
                return Math.sqrt(spread);
            }
        }
        return 0.0;
    }


    private double getCoveredAreaFactor() {

        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        double max_covered = Math.PI * Configuration.getRewardDistance() * Configuration.getRewardDistance() * agentList.size();
        double total_cells = max_x * max_y;
        double free_percentage = 1.0 - ((double)(obstacleList.size() + agentList.size())) / total_cells;
        double max_cells = free_percentage * total_cells;
        if(max_covered > max_cells) {
            max_covered = max_cells;
        }

        int count = 0;

        for(int x = 0; x < max_x; x++) {
            for(int y = 0; y < max_y; y++) {
                if(grid[x][y].isEmpty() && grid[x][y].isRewardedForAgents()) {
                    count++;
                }
            }
        }

        return ((double)count) / max_covered;
    }

    private double getAverageIndividualTotalPoints() {
        int count = 0;
        int total_points = 0;
        double average_points = 0.0;
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent()) {
                continue;
            }
            total_points += a.getTotalPoints();
            count++;
        }
        average_points = (double)total_points / (double)count;
        return average_points;
    }

    public double getSpreadIndividualTotalPoints(double average_points) {
        int count = 0;
        double spread_individual_total_points = 0.0;
        for(BaseAgent a : agentList) {
            if(a.isGoalAgent()) {
                continue;
            }
            double diff = a.getTotalPoints() - average_points;
            spread_individual_total_points += diff * diff;
            count++;
        }
        spread_individual_total_points /= (double)count;
        return Math.sqrt(spread_individual_total_points);
    }

    public void printAgents() {
        for(BaseAgent a : agentList) {
            a.printHeader();
            a.printMove();
            a.printActionSet();
            a.printMatching();
            a.printProjectedReward();
        }
    }


}
