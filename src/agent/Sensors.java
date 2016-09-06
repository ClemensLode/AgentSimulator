package agent;

import lcs.Action;

/**
 * This class provides a container for formatted sensor information
 * New instances are created by the grid class
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Sensors {


    private boolean[] sensorGoal = new boolean[Action.MAX_DIRECTIONS*2];
    private boolean[] sensorAgent = new boolean[Action.MAX_DIRECTIONS*2];
    private boolean[] sensorObstacle = new boolean[Action.MAX_DIRECTIONS*2];


    /**
     * Creates a sensor information object out of the given information from the grid
     * @param goal_agent_direction The absolute direction of the goal agent (-1 if no goal agent is in sight)
     * @param absolute_direction_agent_distance The absolute distances to the nearest agent for each direction in an array 
     * @param absolute_direction_obstacle_distance The absolute distances to the nearest ovstacle for each direction in an array 
     * @see Grid#getAbsoluteSensorInformation
     */
    public Sensors(boolean[] absolute_direction_goal_in_sight, boolean[] absolute_direction_agent_in_sight, boolean[] absolute_direction_obstacle_in_sight) {

        for (int i = 0; i < Action.MAX_DIRECTIONS*2; i++) {
            sensorGoal[i] = absolute_direction_goal_in_sight[i];
            sensorAgent[i] = absolute_direction_agent_in_sight[i];
            sensorObstacle[i] = absolute_direction_obstacle_in_sight[i];
        }
    }

    /**
     * @return the binary sensor data relative to the goal direction, first bit is goal direction
     */
    public boolean[] getCompressedSensorData() {
        boolean[] new_data = new boolean[24];

        for (int i = 0; i < Action.MAX_DIRECTIONS*2; i++) {
            new_data[i] = sensorGoal[i];
            new_data[8 + i] = sensorAgent[i];
            new_data[16 + i] = sensorObstacle[i];
        }

        return new_data;
    }

    private Sensors() {
    }

    @Override
    public Sensors clone() {
        Sensors s = new Sensors();
        for (int i = 0; i < 2*Action.MAX_DIRECTIONS; i++) {
            s.sensorGoal[i] = sensorGoal[i];
            s.sensorAgent[i] = sensorAgent[i];
            s.sensorObstacle[i] = sensorObstacle[i];

        }
        return s;
    }

    public boolean isGoalInRewardRange() {
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(sensorGoal[2*i+1]) {
                return true;
            }
        }
        return false;
    }


    public boolean[] getSensorGoal() {
        return sensorGoal;
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

        for (int i = 0; i < 2*Action.MAX_DIRECTIONS; i++) {
            input += sensorGoal[i] ? "1" : "0";
        }

        input += ".";
        for (int i = 0; i < 2*Action.MAX_DIRECTIONS; i++) {
            input += sensorAgent[i] ? "1" : "0";
        }

        input += ".";
        for (int i = 0; i < 2*Action.MAX_DIRECTIONS; i++) {
            input += sensorObstacle[i] ? "1" : "0";
        }

        return input;
    }
}
