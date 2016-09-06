/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.awt.Point;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Condition extends GeneticData {
    public static final int CONDITION_SIZE = Grid.MAX_DIRECTIONS+1; // *4
    
    private static final int AGENT_DISTANCE_INDEX = 1;
    private static final int GOAL_AGENT_DISTANCE_INDEX = 0;
    
    public static final int DONTCARE = -1;

    public Condition() {
        super(CONDITION_SIZE);
    }
    
    public Condition(int[] data) {
        super(data);
    }    
    
    public Condition(Condition c) {
        super(c.data);
    }
    
    public Condition(final int id, final Point position) {
        super(calculateConditionArray(id, position));
    }


    private static int[] calculateConditionArray(final int id, final Point position) {
        int[] condition = new int[CONDITION_SIZE];

        
        double[] direction_agent_distance = Agent.grid.getDirectionAgentDistances(position, id);
        int goal_agent_direction = Agent.grid.getAgentDirectionRange(position, Agent.goalAgent.getPosition());   
        
             
        for(int i = 0; i < Grid.MAX_DIRECTIONS; i++) {            
            if(direction_agent_distance[i] <= Configuration.getSightRange()) {
                condition[(goal_agent_direction + i)%Grid.MAX_DIRECTIONS + AGENT_DISTANCE_INDEX] = 1;
            } else {
                condition[(goal_agent_direction + i)%Grid.MAX_DIRECTIONS + AGENT_DISTANCE_INDEX] = 0;
            }
        }
        
        if(goal_agent_direction == -1) {
            condition[GOAL_AGENT_DISTANCE_INDEX] = 0;
        } else {
            condition[GOAL_AGENT_DISTANCE_INDEX] = 1;
        }
        
        return condition;
    }
    
    /**
     * Creates a covering condition
     * @param direction_agent_distance
     * @param goal_agent_direction
     * @return
     */
    public static Condition createCoveringCondition(final int id, final Point position) {
        int[] condition = calculateConditionArray(id, position);
        
                // flip some of the condition bits to # with a probability p
        for(int i = AGENT_DISTANCE_INDEX; i < condition.length; i++) {
            if(Misc.nextDouble() < Configuration.getCoveringWildcardProbability()) {
                condition[i] = Condition.DONTCARE;
            }
        }        
        return new Condition(condition);
    }    
    
    @Override
    public Condition clone() {
        Condition new_condition = new Condition(data);
        return new_condition;
    }    
    
    public void randomize() {
        for(int i = 0; i < Grid.MAX_DIRECTIONS; i++) {
            data[i+AGENT_DISTANCE_INDEX] = Misc.nextInt(3) - 1;
        }
        // goal agent in sight / not in sight
        data[GOAL_AGENT_DISTANCE_INDEX] = Misc.nextInt(2);
    }
    
    
    /**
     * Mutates the condition of the classifier. If one allele is mutated depends on the constant pM. 
     * This mutation is a niche mutation. It assures that the resulting classifier
     * still matches the current situation.
     *
     * @see XCSConstants#pM
     * @param state The current situation/problem instance.
     */
    public boolean mutateCondition(Condition state)
    {
	boolean changed=false;
        for(int i = 0; i < data.length; i++) {
            if(Misc.nextDouble() < Configuration.getMutationProbability()) {
                changed = true;
                if(data[i] == DONTCARE) {
                    data[i] = state.data[i];
                } else {
                    data[i] = DONTCARE;
                }
            }
        }
        return changed;
    }
        
    
    public boolean isMoreGeneral(final Condition c) {
        boolean ret = false;        
        for(int i = 0; i < data.length; i++) {
            if((data[i] != DONTCARE) && (data[i] != c.data[i])) {
                return false;
            } else if(data[i] != c.data[i]) {
                ret = true;
            } 
        }
        return ret;
    }
    
   
    public boolean isMatched(double[] direction_agent_distance, int goal_agent_direction) {
        // condition bezeichnet min..max Werte für Distanz von Agenten
        // no goal agent near => rotate all matchings
        if(goal_agent_direction == -1) {
            if(data[GOAL_AGENT_DISTANCE_INDEX] == 1) {
                return false;
            }

            for(int i = 0; i < Grid.MAX_DIRECTIONS; i++) {
                boolean matched = true;
                for(int j = 0; j < Grid.MAX_DIRECTIONS; j++) {
                    switch(data[AGENT_DISTANCE_INDEX + ((i + j)%Grid.MAX_DIRECTIONS)]) {
                        case Condition.DONTCARE:break;
                        case 0:if(direction_agent_distance[i] <= Configuration.getSightRange()) {
                            matched = false;
                        }break;
                        case 1:if(direction_agent_distance[i] > Configuration.getSightRange()) {
                            matched = false;
                        }break;
                    }
                }
                if(matched) {
                    return true;
                }
            }
            return false;
        } else {
            if(data[GOAL_AGENT_DISTANCE_INDEX] == 0) {
                return false;
            }
            
            for(int j = 0; j < Grid.MAX_DIRECTIONS; j++) {
                switch(data[AGENT_DISTANCE_INDEX + ((goal_agent_direction + j)%Grid.MAX_DIRECTIONS)]) {
                    case Condition.DONTCARE:break;
                    case 0:if(direction_agent_distance[j] <= Configuration.getSightRange()) {
                        return false;
                    }break;
                    case 1:if(direction_agent_distance[j] > Configuration.getSightRange()) {
                        return false;
                    }break;
                }
            }
            return true;        
        }
    }
    

    
    @Override
    public String toString() {
        String output = new String();
        
        for(int i = 0; i < data.length; i++) {
            if(data[i] == DONTCARE) {
                output += "#";
            } else {
                output += "" + data[i];
            }
        }
        return output;
    }
    
    /*@Override
    public String toString() {
        String output = new String();
        
        for(int i = 0; i < Grid.MAX_DIRECTIONS; i++) {
            
            output += Grid.shortDirectionString[i];
            
            if(data[i + AGENT_DISTANCE_INDEX] == Condition.DONTCARE) {
                output += "#";
            } else {
                output += data[i + AGENT_DISTANCE_INDEX];
            }
            
            if(data[i + GOAL_AGENT_DISTANCE_INDEX] == Condition.DONTCARE) {
                output += "#";
            } else {
                output += data[i + GOAL_AGENT_DISTANCE_INDEX];
            }
     */

    public static String getInputString(double[] direction_agent_distance, int goal_direction) {
        String input = new String();

        if(goal_direction == -1) {
            input += "0";
            goal_direction = 0;
        } else {
            input += "1";
        }
        
      //  ???
        for(int i = 0; i < Grid.MAX_DIRECTIONS; i++) {
            if(direction_agent_distance[(i+goal_direction)%Grid.MAX_DIRECTIONS] <= Configuration.getSightRange()) {
                input += "1";
            } else {
                input += "0";
            }
        }

        return input;
    }
    
}
