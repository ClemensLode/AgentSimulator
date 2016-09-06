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
    /**
     * Initialize an empty grid and an empty agent list
     */
    public Grid() {
        super();
        Geometry.fillSightPoints();
        Geometry.fillSavedDistances();
        Geometry.fillSavedLinePosition();

        agentList = new ArrayList<BaseAgent>(Configuration.getMaxAgents() + 1);
        obstacleList = new ArrayList<Point>(1 + (int)(Configuration.getObstaclePercentage() * Configuration.getMaxX() * Configuration.getMaxY()));
        clearGrid();
   }



    /**
     * Clears the grid and places a new configuration of obstacles
     */
    public void clearGrid() {
        clear();

        obstacleList.clear();
        switch (Configuration.getScenarioType()) {
            case Configuration.RANDOM_SCENARIO:
                 {
                    int obstacle_count = (int) (Configuration.getObstaclePercentage() * (double) (Configuration.getMaxX() * Configuration.getMaxY()));
                    while (obstacle_count > 0) {
                        Point p = getFreeField();
                        obstacle_count = createConnectedObstacle(p, obstacle_count, Configuration.getObstacleConnectionFactor());
                    }
                }
                break;
            case Configuration.MAZE_SCENARIO:
                fillMaze();
                break;
            case Configuration.NON_TORUS_SCENARIO:
                 {
                    for (int x = 0; x < Configuration.getMaxX(); x++) {
                        createObstacle(new Point(x, 0));
                    }
                    for (int y = 1; y < Configuration.getMaxY(); y++) {
                        createObstacle(new Point(0, y));
                    }
                }
                break;
            case Configuration.PILLAR_SCENARIO:
                 {
                    int nx = Configuration.getMaxX() / 8;
                    int ny = Configuration.getMaxY() / 8;
                    for (int x = 0; x < nx; x++) {
                        for (int y = 0; y < ny; y++) {
                            createObstacle(new Point(x * 8 + 4, y * 8 + 4));
                        }
                    }
                }
                break;
            case Configuration.CROSS_SCENARIO:
                 {
                    int x1 = Configuration.getMaxX() / 4;
                    int x2 = Configuration.getMaxX() - x1;
                    int y1 = Configuration.getMaxY() / 4;
                    int y2 = Configuration.getMaxY() - y1;
                    int cx = Configuration.getMaxX() / 2;
                    int cy = Configuration.getMaxY() / 2;
                    for (int x = x1; x <= x2; x++) {
                        createObstacle(new Point(x, cy));
                    }
                    for (int y = y1; y < cy; y++) {
                        createObstacle(new Point(cx, y));
                    }
                    for (int y = cy + 1; y <= y2; y++) {
                        createObstacle(new Point(cx, y));
                    }
                }
                break;
            case Configuration.ROOM_SCENARIO:
                 {
                    int x1 = Configuration.getMaxX() / 4;
                    int x2 = Configuration.getMaxX() - x1;
                    int y1 = Configuration.getMaxY() / 4;
                    int y2 = Configuration.getMaxY() - y1;
                    int c1 = Configuration.getMaxX() / 2 - 1;
                    int c2 = c1 + 3;
                    for (int x = x1; x <= x2; x++) {
                        createObstacle(new Point(x, y2));
                        if (x >= c1 && x <= c2) {
                            continue;
                        }
                        createObstacle(new Point(x, y1));
                    }
                    for (int y = y1 + 1; y < y2; y++) {
                        createObstacle(new Point(x1, y));
                        createObstacle(new Point(x2, y));
                    }
                }
                break;
            case Configuration.DIFFICULT_SCENARIO:
                 {
                    int y1 = Configuration.getMaxY() / 4 - 1;
                    int y2 = Configuration.getMaxY() / 4 + 1;
                    int y3 = (3 * Configuration.getMaxY()) / 4 - 1;
                    int y4 = (3 * Configuration.getMaxY()) / 4 + 1;
                    int n = Configuration.getMaxX() / 4;
                    for (int i = 0; i < Configuration.getMaxY(); i++) {
                        createObstacle(new Point(Configuration.getMaxX() - 1, i));
                    }
                    for (int i = 1; i < n; i++) {
                        if (i % 2 == 0) {
                            for (int j = 0; j < y1; j++) {
                                createObstacle(new Point(4 * i, j));
                            }
                            for (int j = y2; j < Configuration.getMaxY(); j++) {
                                createObstacle(new Point(4 * i, j));
                            }
                        } else {
                            for (int j = 0; j < y3; j++) {
                                createObstacle(new Point(4 * i, j));
                            }
                            for (int j = y4; j < Configuration.getMaxY(); j++) {
                                createObstacle(new Point(4 * i, j));
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

    private int createConnectedObstacle(Point p, int obstacle_count, double factor) {
        ArrayList<Integer> list = getAvailableDirections(p);
        if (list.size() > 0 && Misc.nextDouble() >= Configuration.getObstacleConnectionFactor()) {
            return obstacle_count;
        }
        createObstacle(p);

        obstacle_count--;

        if (list.isEmpty()) {
            return obstacle_count;
        }

        int[] rand_array = Misc.getRandomArray(list.size());
        for (int i = 0; i < list.size(); i++) {
            if (obstacle_count == 0) {
                return 0;
            }
            Point t = getNeighborField(p, rand_array[i]);
            if (this.grid[t.x][t.y].isOccupied()) {
                continue;
            }

            if (Misc.nextDouble() <= factor) {
                obstacle_count = createConnectedObstacle(t, obstacle_count, factor / 2.0);
            }
        }
        return obstacle_count;
    }

    public void resetState() throws Exception {
        clearGrid();
        for (Point p : obstacleList) {
            grid[p.x][p.y].setContent(Field.OBSTACLE);
        }
        for (BaseAgent a : agentList) {
            Point p;
            if (a.isGoalAgent()) {
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
        int max = (int) Configuration.getSightRange();
        for (int i = 0; i < max_x; i++) {
            for (int j = 0; j < max_y; j++) {
                grid[i][j].clearSight();
            }
        }

        for (BaseAgent a : agentList) {
            int ax = a.getX();
            int ay = a.getY();
            for (Point sp : Geometry.sightPoints) {

                int dx = Geometry.correctX[128 + ax + sp.x];
                int dy = Geometry.correctY[128 + ay + sp.y];

                // already checked
                if (grid[dx][dy].isSeenBy(a.getID())) {
                    continue;
                }

                ArrayList<Point> line_points = Geometry.savedLinePosition[ax][ay].pos[sp.x + max][sp.y + max].torus_line;
                if(line_points == null) {
                    continue;
                }
                for (Point p : line_points) {
                    grid[p.x][p.y].addSeen(a.getID());
                    if (Geometry.torusDistance[ax][ay][p.x][p.y] <= Configuration.getRewardDistance()) {
                        grid[p.x][p.y].addRewarded(a.getID());
                    }
                    if (grid[p.x][p.y].isOccupied()) {
                        break;
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
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent() || a.getID() == agent.getID()) {
                continue;
            }
            a.collectExternalReward(agent, start_index, action_set_size, reward, is_event);
        }
    }

    public Point getFreeAgentField() {
        switch (Configuration.getScenarioType()) {
            case Configuration.NON_TORUS_SCENARIO:
                return getFreeField(new Point(1, 1), new Point(Configuration.getMaxX(), Configuration.getMaxY()));
            case Configuration.PILLAR_SCENARIO:
            case Configuration.ROOM_SCENARIO: {
                Point p = new Point(0, 0);
                do {

                    switch (Misc.nextInt(4)) {
                        case 0:
                            p = new Point(Misc.nextInt(Configuration.getMaxX()), 0);
                            break;
                        case 1:
                            p = new Point(Misc.nextInt(Configuration.getMaxX()), Configuration.getMaxY() - 1);
                            break;
                        case 2:
                            p = new Point(0, Misc.nextInt(Configuration.getMaxY()));
                            break;
                        case 3:
                            p = new Point(Configuration.getMaxX() - 1, Misc.nextInt(Configuration.getMaxY()));
                            break;
                        default:
                            break;
                    }
                } while (grid[p.x][p.y].isOccupied());
                return p;
            }
            case Configuration.DIFFICULT_SCENARIO: {
                return getFreeField(new Point(0, 0), new Point(1, Configuration.getMaxY()));
            }
            default: {
                return getFreeField();
            }
        }
    }

    public void maybeRemoveAgentDirections(final BaseAgent a, ArrayList<Integer> available_directions, double probability_reward, double probability_sight) {
        boolean[] direction_agent_in_sight_list = getDirectionAgentInSightList(a.getPosition(), a.getID());
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (Misc.nextDouble() < probability_reward && direction_agent_in_sight_list[2*i+1]) {
                available_directions.remove(new Integer(i));
            }
            if (Misc.nextDouble() < probability_sight && direction_agent_in_sight_list[2*i]) {
                available_directions.remove(new Integer(i));
            }
        }
    }


    public void maybeRemoveObstacleDirections(final BaseAgent a, ArrayList<Integer> available_directions, double probability) {
        boolean[] direction_obstacle_in_sight_list = getDirectionObstacleInSightList(a.getPosition(), a.getID());
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (Misc.nextDouble() < probability && direction_obstacle_in_sight_list[2*i]) {
                available_directions.remove(new Integer(i));
            }
        }
    }

    public void maybeRemoveOpenDirections(final BaseAgent a, ArrayList<Integer> available_directions, double probability) {
        boolean[] direction_obstacle_in_sight_list = getDirectionObstacleInSightList(a.getPosition(), a.getID());
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (Misc.nextDouble() < probability && !direction_obstacle_in_sight_list[2*i]) {
                available_directions.remove(new Integer(i));
            }
        }
    }

    public Point getFreeGoalAgentField() {
        switch (Configuration.getScenarioType()) {
            case Configuration.NON_TORUS_SCENARIO:
            case Configuration.PILLAR_SCENARIO:
            case Configuration.ROOM_SCENARIO: {
                int x = Configuration.getHalfMaxX();
                int y = Configuration.getHalfMaxY();
                if (grid[x][y].isOccupied()) {
                    return new Point(x + 1, y);
                } else {
                    return new Point(x, y);
                }
            }
            case Configuration.DIFFICULT_SCENARIO: {
                return new Point(Configuration.getMaxX() - 2, 0);
            }
            default: {
                return getFreeField();
            }
        }
    }

    /**
     * @param a Agent in question
     * @return true if this agent is in sight of the goal agent
     */
    public boolean isGoalAgentInRewardRange(final BaseAgent a) {
        if (a.isGoalAgent()) {
            return false;
        }
        //return grid[a.getX()][a.getY()].isRewardFor(Field.GOAL_AGENT_ID);// && grid[BaseAgent.goalAgent.getX()][BaseAgent.goalAgent.getY()].
        
        
        return grid[BaseAgent.goalAgent.getX()][BaseAgent.goalAgent.getY()].isRewardFor(a.getID());
    }

    /**
     * @return true if any agent sees the goal agent
     */
    public boolean isGoalAgentInRewardRangeByAnyAgent() {
        return grid[BaseAgent.goalAgent.getX()][BaseAgent.goalAgent.getY()].isRewardedForAgents();
    }

    public BaseAgent findRandomAgentNearby(Point position, int id) {
        ArrayList<BaseAgent> nearby_agents = new ArrayList<BaseAgent>(Configuration.getMaxAgents());
        for (BaseAgent a : agentList) {
            if (a.getID() == id || a.isGoalAgent()) {
                continue;
            }
            if (a.getPosition().distance(position) <= 2.0 * Configuration.getSightRange()) {
                nearby_agents.add(a);
            }
        }
        if (nearby_agents.isEmpty()) {
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
    private boolean[] getDirectionGoalInSightList(final Point position, final int self_id) {
        boolean[] goal_in_sight = new boolean[2*Action.MAX_DIRECTIONS];
        for (int i = 0; i < goal_in_sight.length; i++) {
            goal_in_sight[i] = false;
        }
// TODO
        if (self_id == Field.GOAL_AGENT_ID) {
            return goal_in_sight;
        }

        Point goal_position = BaseAgent.goalAgent.getPosition();
        if (grid[goal_position.x][goal_position.y].isSeenBy(self_id)) {
            int direction_index = 2*Geometry.getDirection(position, goal_position);
            goal_in_sight[direction_index] = true;

            if (grid[goal_position.x][goal_position.y].isRewardFor(self_id)) {
                goal_in_sight[direction_index+1] = true;
            }
        }

        return goal_in_sight;
    }

    /**
     * Calculates the distances to the nearest agents in each direction
     * @param position The position of the agent in question
     * @param self_id The ID of the agent in question
     * @return An array of minimal distances to other agents
     */
    private boolean[] getDirectionAgentInSightList(final Point position, final int self_id) {
        boolean[] agent_in_sight = new boolean[2*Action.MAX_DIRECTIONS];
        for (int i = 0; i < agent_in_sight.length; i++) {
            agent_in_sight[i] = false;
        }

        for (BaseAgent a : agentList) {
            if (a.getID() == self_id || a.isGoalAgent()) {
                continue;
            }
            Point agent_position = a.getPosition();

            if (grid[agent_position.x][agent_position.y].isSeenBy(self_id)) {
                int direction_index = 2*Geometry.getDirection(position, agent_position);
                agent_in_sight[direction_index] = true;

                if (grid[agent_position.x][agent_position.y].isRewardFor(self_id)) {
                    agent_in_sight[direction_index+1] = true;
                }
            }
        }
        return agent_in_sight;
    }

    /**
     * Calculates the distances to the nearest obstacles in each direction
     * @param position The position of the agent in question
     * @return An array of minimal distances to obstacles
     */
    private boolean[] getDirectionObstacleInSightList(final Point position, final int self_id) {
        boolean[] obstacle_in_sight = new boolean[2*Action.MAX_DIRECTIONS];
        for (int i = 0; i < obstacle_in_sight.length; i++) {
            obstacle_in_sight[i] = false;
        }

        for (Point obstacle_position : obstacleList) {
            if (grid[obstacle_position.x][obstacle_position.y].isSeenBy(self_id)) {
                int direction_index = 2*Geometry.getDirection(position, obstacle_position);
                obstacle_in_sight[direction_index] = true;

                if (grid[obstacle_position.x][obstacle_position.y].isRewardFor(self_id)) {
                    obstacle_in_sight[direction_index+1] = true;
                }
            }
        }
        return obstacle_in_sight;
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
        return new Sensors(getDirectionGoalInSightList(position, self_id),
                getDirectionAgentInSightList(position, self_id),
                getDirectionObstacleInSightList(position, self_id));
    }

    /**
     * Put an agent on the grid
     * @param a the BaseAgent
     */
    public void addAgent(final BaseAgent a) {
        agentList.add(a);
    }

    public void updateStatistics(long currentTimestep, ClassifierSet c_set) throws Exception {
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
                getCoveredAreaFactor()*100.0,
                getWastedCoverage(),
                getGoalJumps(),
                getWastedMovements(),
                average_points,
                getSpreadIndividualTotalPoints(average_points),
                getAveragePredictionError());

    }

    private double getAverageAgentDistance() {
        int count = 0;
        double dist = 0.0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                continue;
            }
            int ax = a.getX();
            int ay = a.getY();
            for (BaseAgent b : agentList) {
                if (b.isGoalAgent() || a.getID() == b.getID()) {
                    continue;
                }
                dist += Geometry.torusDistance[ax][ay][b.getX()][b.getY()];
                count++;
            }
        }
        dist /= (double) count;
        return dist;
    }

    private double getSpreadAgentDistance(double average_agent_distance) {
        int count = 0;
        double spread = 0.0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                continue;
            }
            int ax = a.getX();
            int ay = a.getY();

            for (BaseAgent b : agentList) {
                if (b.isGoalAgent() || a.getID() == b.getID()) {
                    continue;
                }
                double diff = Geometry.torusDistance[ax][ay][b.getX()][b.getY()] - average_agent_distance;
                spread += diff * diff;
                count++;
            }
        }
        spread /= (double) count;
        return Math.sqrt(spread);
    }

    private double getAverageGoalAgentDistance() {
        double dist = 0.0;
        int count = 0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                int ax = a.getX();
                int ay = a.getY();
                for (BaseAgent b : agentList) {
                    if (b.isGoalAgent()) {
                        continue;
                    }
                    dist += Geometry.torusDistance[ax][ay][b.getX()][b.getY()];
                    count++;
                }
                dist /= (double) count;
                return dist;
            }
        }
        return 0.0;
    }

    private double getSpreadGoalAgentDistance(double average_goal_distance) {
        int count = 0;
        double spread = 0.0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                int ax = a.getX();
                int ay = a.getY();

                for (BaseAgent b : agentList) {
                    if (b.isGoalAgent()) {
                        continue;
                    }
                    double diff = Geometry.torusDistance[ax][ay][b.getX()][b.getY()] - average_goal_distance;
                    spread += diff * diff;
                    count++;
                }
                spread /= (double) count;
                return Math.sqrt(spread);
            }
        }
        return 0.0;
    }

    private double getWastedMovements() {
        double movements = ((double)BaseGrid.invalidActions) / ((double)Configuration.getMaxAgents());
        BaseGrid.invalidActions = 0;
        return movements;
    }

    private double getGoalJumps() {
        double movements = BaseGrid.goalJumps;
        BaseGrid.goalJumps = 0;
        return movements;
    }

    private double getWastedCoverage() {

        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        int count = 0;
        int n = 0;
        for (int x = 0; x < max_x; x++) {
            for (int y = 0; y < max_y; y++) {
                int t = grid[x][y].rewardedByCount();
                count += t;
                if(t > 0) {
                    n++;
                }
            }
        }
        return (double)(count - n) / (max_x * max_y);
    }

    private double getCoveredAreaFactor() {

        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        double max_covered = Geometry.maxRewardCoverage * agentList.size();
        double total_cells = max_x * max_y;
        double free_percentage = 1.0 - ((double) (obstacleList.size() + agentList.size())) / total_cells;
        double max_cells = free_percentage * total_cells;
        if (max_covered > max_cells) {
            max_covered = max_cells;
        }

        int count = 0;

        for (int x = 0; x < max_x; x++) {
            for (int y = 0; y < max_y; y++) {
                if (grid[x][y].isEmpty() && grid[x][y].isRewardedForAgents()) {
                    count++;
                }
            }
        }

        return ((double) count) / max_covered;
    }

    private double getAverageIndividualTotalPoints() {
        int count = 0;
        int total_points = 0;
        double average_points = 0.0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                continue;
            }
            total_points += a.getTotalPoints();
            count++;
        }
        average_points = (double) total_points / (double) count;
        return average_points;
    }

    public double getSpreadIndividualTotalPoints(double average_points) {
        int count = 0;
        double spread_individual_total_points = 0.0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                continue;
            }
            double diff = a.getTotalPoints() - average_points;
            spread_individual_total_points += diff * diff;
            count++;
        }
        spread_individual_total_points /= (double) count;
        return Math.sqrt(spread_individual_total_points);
    }

    public double getAveragePredictionError() {
        double average_prediction_error = 0.0;
        for (BaseAgent a : agentList) {
            if (a.isGoalAgent()) {
                continue;
            }
            average_prediction_error += a.getLastPredictionError();
        }
        average_prediction_error /= (double)(agentList.size()-1);
        return average_prediction_error;
    }


    private void fillMaze() {
        {
            int zoom = (int) (Configuration.getObstaclePercentage() * 20.0);
            int max_x = 2 + Configuration.getMaxX() / zoom;
            int max_y = 2 + Configuration.getMaxY() / zoom;
            int[][] my_squares = new int[max_x][max_y];
            for (int x = 0; x < max_x; x++) {
                for (int y = 0; y < max_y; y++) {
                    my_squares[x][y] = 0;
                }
            }
            for (int x = 1; x < max_x; x += 2) {
                for (int y = 1; y < max_y; y += 2) {
                    my_squares[x][y] = 3;
                }
            }
            ArrayList possible_squares = new ArrayList(max_x * max_y);
            int[] start_square = new int[2];
            start_square[0] = Misc.nextInt(max_x / 2) * 2 + 1;
            start_square[1] = Misc.nextInt(max_y / 2) * 2 + 1;
            my_squares[start_square[0]][start_square[1]] = 2;
            possible_squares.add(start_square);
            while (possible_squares.size() > 0) {
                int chosen_index = Misc.nextInt(possible_squares.size());
                int[] chosen_square = (int[]) possible_squares.get(chosen_index);
                my_squares[chosen_square[0]][chosen_square[1]] = 1;
                possible_squares.remove(chosen_index);
                int linkCount = 0;
                int i = chosen_square[0];
                int j = chosen_square[1];
                int[] links = new int[8];
                if (i >= 3) {
                    if (my_squares[i - 2][j] == 1) {
                        links[2 * linkCount] = i - 1;
                        links[2 * linkCount + 1] = j;
                        linkCount++;
                    } else if (my_squares[i - 2][j] == 3) {
                        my_squares[i - 2][j] = 2;
                        int[] newSquare = new int[2];
                        newSquare[0] = i - 2;
                        newSquare[1] = j;
                        possible_squares.add(newSquare);
                    }
                }
                if (j + 3 <= my_squares[i].length) {
                    if (my_squares[i][j + 2] == 3) {
                        my_squares[i][j + 2] = 2;
                        int[] newSquare = new int[2];
                        newSquare[0] = i;
                        newSquare[1] = j + 2;
                        possible_squares.add(newSquare);
                    } else if (my_squares[i][j + 2] == 1) {
                        links[2 * linkCount] = i;
                        links[2 * linkCount + 1] = j + 1;
                        linkCount++;
                    }
                }
                if (j >= 3) {
                    if (my_squares[i][j - 2] == 3) {
                        my_squares[i][j - 2] = 2;
                        int[] newSquare = new int[2];
                        newSquare[0] = i;
                        newSquare[1] = j - 2;
                        possible_squares.add(newSquare);
                    } else if (my_squares[i][j - 2] == 1) {
                        links[2 * linkCount] = i;
                        links[2 * linkCount + 1] = j - 1;
                        linkCount++;
                    }
                }
                if (i + 3 <= my_squares.length) {
                    if (my_squares[i + 2][j] == 3) {
                        my_squares[i + 2][j] = 2;
                        int[] newSquare = new int[2];
                        newSquare[0] = i + 2;
                        newSquare[1] = j;
                        possible_squares.add(newSquare);
                    } else if (my_squares[i + 2][j] == 1) {
                        links[2 * linkCount] = i + 1;
                        links[2 * linkCount + 1] = j;
                        linkCount++;
                    }
                }
                if (linkCount > 0) {
                    int linkChoice = Misc.nextInt(linkCount);
                    int linkX = links[2 * linkChoice];
                    int linkY = links[2 * linkChoice + 1];
                    my_squares[linkX][linkY] = 1;
                    int[] removeSquare = new int[2];
                    removeSquare[0] = linkX;
                    removeSquare[1] = linkY;
                    possible_squares.remove(removeSquare);
                }
            }
            for (int x = 1; x < max_x - 1; x++) {
                for (int y = 1; y < max_y - 1; y++) {
                    if (my_squares[x][y] == 0) {
                        for (int dx = 0; dx < zoom; dx++) {
                            for (int dy = 0; dy < zoom; dy++) {
                                createObstacle(new Point((x - 1) * zoom + dx, (y - 1) * zoom + dy));
                            }
                        }
                    }
                }
            }

        }
    }

    public void printAgents() {
        for (BaseAgent a : agentList) {
            a.printHeader();
            a.printMatching();
            a.printActionSet();
            a.printMove();
            //a.printProjectedReward();
        }
    }
}
