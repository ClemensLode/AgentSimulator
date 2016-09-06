package lcs;

import agent.Sensors;
import agent.Configuration;
import Misc.Misc;


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
    private static final int GOAL_DISTANCE_INDEX = 0;
    public static final int AGENT_DISTANCE_INDEX = 8;
    public static final int OBSTACLE_DISTANCE_INDEX = 16;

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
        assignData(data);
    }
    
    private void assignData(final int[] data) {
        this.data = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }

    public Condition(final int dir) {
        switch(dir) {
            case 0:{int[] data2 = {-1,1, -1,-1, -1,-1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1  };assignData(data2);break;}
            case 1:{int[] data2 = {-1,-1, -1,1, -1,-1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1  };assignData(data2);break;}
            case 2:{int[] data2 = {-1,-1, -1,-1, -1,1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1  };assignData(data2);break;}
            case 3:{int[] data2 = {-1,-1, -1,-1, -1,-1, -1,1,  -1,-1, -1,-1, -1,-1, -1,-1,  -1,-1, -1,-1, -1,-1, -1,-1  };assignData(data2);break;}
        }
    }

    /**
     * Generates a new randomized instance
     * @see randomize
     */
    public Condition() {
        data = new int[24];
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

        // flip some of the condition bits to # with a probability p
        for (int i = 0; i < sensor_data.length; i++) {
            data[i] = sensor_data[i] ? 1 : 0;
            if (Misc.nextDouble() < Configuration.getCoveringWildcardProbability()) {
                data[i] = Condition.DONTCARE;
            }
        }
    }

    public double getEgoFactor(final int action) {

            switch (data[8 + 2*action]) {
                case DONTCARE:
                    return 0.5;
                case 1:
                    return 1.0;
                default:
                    return 0.0;
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
    public boolean mutateCondition(final Sensors state) {
        boolean changed = false;
        // determine in which direction the sensor data relative to the condition data is rotated

        
        boolean[] sensors = state.getCompressedSensorData();
        for (int i = 0; i < sensors.length; i++) {
            if (Misc.nextDouble() < Configuration.getMutationProbability()) {
                changed = true;
                // TODO Check
                if (data[i] == DONTCARE) {
                    data[i] = sensors[i] ? 1 : 0;
                } else {
                    data[i] = DONTCARE;
                }
            }
        }

        return changed;
    }

    /**
     * @param s Sensor state
     * @return rotation list consisting of all rotations that this condition matches the sensor state 
     */
    public boolean isMatchingState(final Sensors s) {

        boolean[] sensors = s.getCompressedSensorData();
            boolean matched = true;
            for (int i = 0; i < sensors.length; i++) {
                if(i%2 == 1 && sensors[i-1]) {
                    continue;
                }
                if( (sensors[i] && (data[i] == 0)) ||
                    ((!sensors[i]) && (data[i] == 1))) {
                    matched = false;
                    break;
                }
            }
            return matched;

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
    public boolean equals(final Condition c) {
        for(int i = 0; i < data.length; i++) {
            if(data[i] != c.data[i]) {
                return false;
            }
        }
        return true;
    }

   /* public boolean isGoalCondition() {
        return data[GOAL_DISTANCE_INDEX] == 1;
    }*/


    /**
     * @param c The condition to compare to
     * @param rotation The rotation of the other condition
     * @return true if this classifier is more general (i.e. equal or equal and more wildcards)
     */
    public boolean isMoreGeneral(final Condition c) {
        boolean really_more_general = false;
        
        for (int i = 0; i < data.length; i++) {
            if ((data[i] != DONTCARE) && (data[i] != c.data[i])) {
                return false;
            } else
            if (data[i] != c.data[i]) {
                really_more_general = true;
            }
        }
        return really_more_general;
    }

    /**
     * Randomize the matching condition of the condition
     */
    private void randomize() {
        for (int i = 0; i < data.length; i++) {
            /*if(Misc.nextDouble() < Configuration.getCoveringWildcardProbability()) {
                data[i] = DONTCARE;
            } else {*/
                data[i] = Misc.nextInt(3)-1;
            //}
        }
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
            if((i % 8) == 0) {
                output += ".";
            }
            if (data[i] == DONTCARE) {
                output += "#";
            } else {
                output += "" + data[i];
            }
        }
        return output;
    }
}
