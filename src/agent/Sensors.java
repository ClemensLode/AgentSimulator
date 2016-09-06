package agent;

import lcs.Action;

/**
 * This class provides a container for formatted sensor information
 * New instances are created by the grid class
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Sensors {

    private int sensorGoalAgentDirection = -1;
    private boolean[] sensorAgent = new boolean[Action.MAX_DIRECTIONS];
    private boolean[] sensorObstacle = new boolean[Action.MAX_DIRECTIONS];

    public Sensors(int value) {
        if (value % 2 == 1) {
            sensorGoalAgentDirection = 0;
            value -= 1;
        }
        int t = 4;
        for (int i = 0; i < 4; i++) {
            sensorAgent[i] = (value % t != 0);
            value -= value % t;
            t *= 2;
        }

        for (int i = 0; i < 4; i++) {
            sensorObstacle[i] = (value % t != 0);
            value -= value % t;
            t *= 2;
        }
    }

    /**
     * Creates a sensor information object out of the given information from the grid
     * @param goal_agent_direction The absolute direction of the goal agent (-1 if no goal agent is in sight)
     * @param absolute_direction_agent_distance The absolute distances to the nearest agent for each direction in an array 
     * @param absolute_direction_obstacle_distance The absolute distances to the nearest ovstacle for each direction in an array 
     * @see Grid#getAbsoluteSensorInformation
     */
    public Sensors(int goal_agent_direction, boolean[] absolute_direction_agent_in_sight, boolean[] absolute_direction_obstacle_in_sight) {

        sensorGoalAgentDirection = goal_agent_direction;

        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            sensorAgent[i] = absolute_direction_agent_in_sight[i];
            sensorObstacle[i] = absolute_direction_obstacle_in_sight[i];
        }
    }

    /**
     * @return the binary sensor data relative to the goal direction, first bit is goal direction
     */
    public boolean[] getCompressedSensorData() {
        int data_size = 0;

        data_size = 5 + Action.MAX_DIRECTIONS;

        boolean[] new_data = new boolean[data_size];
        new_data[0] = (sensorGoalAgentDirection != -1);

        int goal_direction;
        if (sensorGoalAgentDirection != -1) {
            goal_direction = sensorGoalAgentDirection;
            new_data[0] = true;
        } else {
            goal_direction = 0;
            new_data[0] = false;
        }

        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            new_data[1 + ((i + Action.MAX_DIRECTIONS - goal_direction) % Action.MAX_DIRECTIONS)] = sensorAgent[i];
            new_data[5 + ((i + Action.MAX_DIRECTIONS - goal_direction) % Action.MAX_DIRECTIONS)] = sensorObstacle[i];
        }

        return new_data;
    }

    private Sensors() {
    }

    @Override
    public Sensors clone() {
        Sensors s = new Sensors();
        s.sensorGoalAgentDirection = sensorGoalAgentDirection;
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            s.sensorAgent[i] = sensorAgent[i];

            s.sensorObstacle[i] = sensorObstacle[i];

        }
        return s;
    }

    /**
     * @return The direction of the goal agent
     */
    public int getSensorGoalAgentDirection() {
        return sensorGoalAgentDirection;
    }

    /**
     * @return true if the goal agent is in sight
     */
    public boolean getSensorGoalAgent() {
        return sensorGoalAgentDirection != -1;
    }

    /**
     * @return The absolute binary sensor data of nearby agents
     */
    public boolean[] getSensorAgent() {
        return sensorAgent;
    }

    /**
     * @return The absolute binary sensor data of nearby obstacles
     */
    public boolean[] getSensorObstacle() {
        return sensorObstacle;
    }

    /**
     * @return Formatted string of the sensor input for log output
     */
    public String getInputString() {
        String input = new String();
        int goal_direction = sensorGoalAgentDirection;
        input += (goal_direction == -1 ? "-" : goal_direction) + ".";
        if (goal_direction == -1) {
            goal_direction = 0;
        }
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            input += sensorAgent[(i + goal_direction) % Action.MAX_DIRECTIONS] ? "1" : "0";
        }

        input += ".";
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            input += sensorObstacle[(i + goal_direction) % Action.MAX_DIRECTIONS] ? "1" : "0";
        }

        return input;
    }
}
