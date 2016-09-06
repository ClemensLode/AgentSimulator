package agents;

import Misc.Misc;
import lcs.Action;

/**
 *
 * Complex agent implementation, moves around randomly if no goal agent is in sight, moves away from other agents
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Good_AI_Agent extends BaseAgent {
    
    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     */
    @Override
    public void calculateNextMove(final long gaTimestep) throws Exception {
        /**
         * no agents in sight, no goal agent in sight => Random movement
         * agent and goal agent in the same direction => move randomly away from agents
         * goal agent not
         */
        boolean[] goal_sensor = lastState.getSensorGoal();
        calculatedAction = -1;
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(goal_sensor[2*i]) {
                calculatedAction = i;
                break;
            }
        }

        if(calculatedAction == -1) {
            calculatedAction = Misc.nextInt(Action.MAX_DIRECTIONS);

            boolean[] agent_sensors = lastState.getSensorAgent();
            boolean one_free = false;
            for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                if(!agent_sensors[2*i]) {
                    one_free = true;
                    break;
                }
            }

            if(one_free) {
                while(agent_sensors[2*calculatedAction]) {
                    calculatedAction = Misc.nextInt(Action.MAX_DIRECTIONS);
                }
            }
        } 
    }   
}
