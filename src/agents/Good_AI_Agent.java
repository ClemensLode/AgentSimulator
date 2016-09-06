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
        
        calculatedAction = lastState.getSensorGoalAgentDirection();
        boolean[] agent_sensors = lastState.getSensorAgent();
        
        boolean one_free = false;
        for(boolean b : agent_sensors) {
            if(!b) {
                one_free = true;
                break;
            } 
        }

        // no goal agent in sight
        if(calculatedAction == -1 || agent_sensors[calculatedAction]) {
            calculatedAction = Misc.nextInt(Action.MAX_DIRECTIONS);
            if(one_free) {
                while(agent_sensors[calculatedAction]) {
                    calculatedAction = Misc.nextInt(Action.MAX_DIRECTIONS);
                } 
            }
        } 
    }   
}
