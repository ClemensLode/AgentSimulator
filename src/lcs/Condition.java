package lcs;

import agent.Sensors;
import agent.Configuration;
import Misc.Misc;
import java.util.ArrayList;

/**
 * Provides functionality to store a state and a matching, to compare and mutate
 * a condition and to create a covering matching to a state
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Condition {

    /**
     * Index values of data
     */
    private static final int GOAL_AGENT_DISTANCE_INDEX = 0;
    public static final int AGENT_DISTANCE_INDEX = 1;
    public static final int OBSTACLE_DISTANCE_INDEX = 5;
    /**
     * wildcard symbol
     */
    private static final int DONTCARE = -1;
    /**
     * Raw data, matching condition
     */
    protected int[] data;

    /**
     * Creates a new condition based on the given data
     * @param data Matching condition of the condition
     */
    public Condition(final int[] data) {
        this.data = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }

    /**
     * Generates a new randomized instance with the given size
     * @param size size of the data
     * @see randomize
     */
    public Condition(final int size) {
        data = new int[size];
        randomize();
    }

    /**
     * Creates a covering condition based on the sensor data, adding randomly 
     * DONTCARE symbols
     * @param s The current sensor data
     */
    public Condition(Sensors s) {
        boolean[] sensor_data = s.getCompressedSensorData();
        data = new int[sensor_data.length];

        // goal in sight?
        data[0] = sensor_data[0] ? 1 : 0;

        // flip some of the condition bits to # with a probability p
        for (int i = Condition.AGENT_DISTANCE_INDEX; i < data.length; i++) {
            data[i] = sensor_data[i] ? 1 : 0;
            Double tempr = Misc.nextDouble();
            if (tempr < Configuration.getCoveringWildcardProbability()) {
                data[i] = Condition.DONTCARE;
            }
        }
    }

    public double getEgoFactor(final int action) {
        if (action == Action.NO_DIRECTION) {
            double t = 0.0;
            for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                switch (data[i]) {
                    case DONTCARE:
                        t += 0.5;
                        break;
                    case 1:
                        t += 1.0;
                        break;
                    default:
                        break;
                }
            }
            return t / 4.0;
        } else {
            switch (data[action]) {
                case DONTCARE:
                    return 0.5;
                case 1:
                    return 1.0;
                default:
                    return 0.0;
            }
        }
    }

    /**
     * Mutates the condition of the classifier. If one allele is mutated depends on the constant mutationProbability. 
     * This mutation is a niche mutation. It assures that the resulting classifier
     * still matches the current situation.
     * @param state current state
     * @param rotation difference between the actual action and the classifier action
     * @return true if something was changed
     */
    public boolean mutateCondition(final Sensors state, final int rotation) {
        boolean changed = false;
        // determine in which direction the sensor data relative to the condition data is rotated
        boolean[] agent_sensor = state.getSensorAgent();
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (Misc.nextDouble() < Configuration.getMutationProbability()) {
                changed = true;
                int real_direction = (i + rotation) % Action.MAX_DIRECTIONS;
                // TODO Check
                if (data[Condition.AGENT_DISTANCE_INDEX + real_direction] == DONTCARE) {
                    data[Condition.AGENT_DISTANCE_INDEX + real_direction] = agent_sensor[i] ? 1 : 0;
                } else {
                    data[Condition.AGENT_DISTANCE_INDEX + real_direction] = DONTCARE;
                }
            }
        }

        boolean[] obstacle_sensor = state.getSensorObstacle();
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (Misc.nextDouble() < Configuration.getMutationProbability()) {
                changed = true;
                int real_direction = (i + rotation) % Action.MAX_DIRECTIONS;
                if (data[Condition.OBSTACLE_DISTANCE_INDEX + real_direction] == DONTCARE) {
                    data[Condition.OBSTACLE_DISTANCE_INDEX + real_direction] = obstacle_sensor[i] ? 1 : 0;
                } else {
                    data[Condition.OBSTACLE_DISTANCE_INDEX + real_direction] = DONTCARE;
                }
            }
        }


        return changed;
    }

    /**
     * @param s Sensor state
     * @return rotation list consisting of all rotations that this condition matches the sensor state 
     */
    public ArrayList<Integer> getMatchingDirections(final Sensors s) {
        ArrayList<Integer> direction_list = new ArrayList<Integer>(Action.MAX_DIRECTIONS);
        int goal_direction = s.getSensorGoalAgentDirection();
        int rotation = 0;

        // condition bezeichnet min..max Werte für Distanz von Agenten
        // no goal agent near =>rotate all matchings

        if (goal_direction == -1) {
            if (data[GOAL_AGENT_DISTANCE_INDEX] == 1) {
                return direction_list;
            }
            goal_direction = 0;
            if (!Configuration.isDoAllowRotation()) {
                rotation = Action.MAX_DIRECTIONS;
            }
        } else {
            if (data[GOAL_AGENT_DISTANCE_INDEX] == 0) {
                return direction_list;
            }
            // no rotations => set it to the maximal value
            rotation = Action.MAX_DIRECTIONS;
        }

        boolean[] sensor_bit = s.getCompressedSensorData();
        do {
            boolean matched = true;
            for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                int index = (i + rotation + Action.MAX_DIRECTIONS - goal_direction) % Action.MAX_DIRECTIONS;
                if ((sensor_bit[AGENT_DISTANCE_INDEX + i] != (data[AGENT_DISTANCE_INDEX + index] == 1)) && (data[AGENT_DISTANCE_INDEX + index] != Condition.DONTCARE)) {
                    matched = false;
                    break;
                }

                if ((sensor_bit[OBSTACLE_DISTANCE_INDEX + i] != (data[OBSTACLE_DISTANCE_INDEX + index] == 1)) && (data[AGENT_DISTANCE_INDEX + index] != Condition.DONTCARE)) {
                    matched = false;
                    break;
                }

            }

            if (matched) {
                direction_list.add(rotation % Action.MAX_DIRECTIONS);
            }
            rotation++;
        } while (rotation < Action.MAX_DIRECTIONS);
        return direction_list;
    }

    /**
     * Assigns a new data array
     * @param data the data array we want to assign
     */
    public void setData(final int[] data) {
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }

    /**
     * @param c The other classifier
     * @param rotation The rotation of the other condition
     * @return True if this classifier is identical with the other classifier with the given rotation
     */
    public boolean equals(final Condition c, final int rotation) {
        // if goal agent is visible only allow rotation 0
        if ((data[0] == 1 && rotation != 0) || (data[0] != c.data[0])) {
            return false;
        } else {
            for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                if (data[AGENT_DISTANCE_INDEX + i] != c.data[AGENT_DISTANCE_INDEX + ((i + rotation) % Action.MAX_DIRECTIONS)]) {
                    return false;
                }

                if (data[OBSTACLE_DISTANCE_INDEX + i] != c.data[OBSTACLE_DISTANCE_INDEX + ((i + rotation) % Action.MAX_DIRECTIONS)]) {
                    return false;
                }

            }
        }
        // data[0] == c.data[0], data[0] != 1 || rotation == 0, data[*] = c.data[*+rotation]
        return true;
    }

    /**
     * @param c The condition to compare to
     * @param rotation The rotation of the other condition
     * @return true if this classifier is more general (i.e. equal or equal and more wildcards)
     */
    public boolean isMoreGeneral(final Condition c, final int rotation) {
        boolean really_more_general = false;
        // if goal agent is visible only allow rotation 0
        if ((data[0] == 1 && rotation != 0) || ((data[0] != DONTCARE) && (data[0] != c.data[0]))) {
            return false;
        } else {
            for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                if ((data[AGENT_DISTANCE_INDEX + i] != DONTCARE) && (data[AGENT_DISTANCE_INDEX + i] != c.data[AGENT_DISTANCE_INDEX + ((i + rotation) % Action.MAX_DIRECTIONS)])) {
                    return false;
                } else if (data[AGENT_DISTANCE_INDEX + i] != c.data[AGENT_DISTANCE_INDEX + ((i + rotation) % Action.MAX_DIRECTIONS)]) {
                    really_more_general = true;
                }

                if ((data[OBSTACLE_DISTANCE_INDEX + i] != DONTCARE) && (data[OBSTACLE_DISTANCE_INDEX + i] != c.data[OBSTACLE_DISTANCE_INDEX + ((i + rotation) % Action.MAX_DIRECTIONS)])) {
                    return false;
                } else if (data[OBSTACLE_DISTANCE_INDEX + i] != c.data[OBSTACLE_DISTANCE_INDEX + ((i + rotation) % Action.MAX_DIRECTIONS)]) {
                    really_more_general = true;
                }

            }
        }
        // data[0] == c.data[0], data[0] != 1 || rotation == 0, data[*] = c.data[*+rotation]
        return really_more_general;
    /*
    boolean ret = true;
    for (int i = 0; i < data.length; i++) {
    if ((data[i] != DONTCARE) && (data[i] != c.data[i])) {
    return false;
    } else if (data[i] != c.data[i]) {
    ret = true;
    }
    }
    return ret;*/
    }

    /**
     * Randomize the matching condition of the condition
     */
    private void randomize() {
        final int with_wild_cards = Configuration.getCoveringWildcardProbability() > 0.0 ? 1 : 0;
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            data[i + AGENT_DISTANCE_INDEX] = Misc.nextInt(2 + with_wild_cards) - with_wild_cards;

            data[i + OBSTACLE_DISTANCE_INDEX] = Misc.nextInt(2 + with_wild_cards) - with_wild_cards;

        }
        // goal agent in sight / not in sight
        data[GOAL_AGENT_DISTANCE_INDEX] = Misc.nextInt(2);
    }

    /**
     * @return The basic data of the condition
     */
    public final int[] getData() {
        return data;
    }

    @Override
    public Condition clone() {
        Condition new_condition = new Condition(data);
        return new_condition;
    }

    @Override
    public String toString() {
        String output = new String();

        for (int i = 0; i < data.length; i++) {
            if (data[i] == DONTCARE) {
                output += "#";
            } else {
                output += "" + data[i];
            }
        }
        return output;
    }
}
