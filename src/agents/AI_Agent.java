package agents;

/**
 *
 * Simple agent implementation, moves around randomly if no goal agent is in sight
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */

import Misc.Misc;
import lcs.Action;

public class AI_Agent extends BaseAgent {

    
    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     */
    @Override
    public void calculateNextMove(final long gaTimestep) {
        calculatedAction = lastState.getSensorGoalAgentDirection();
        
        if(calculatedAction == -1) {
            calculatedAction = Misc.nextInt(Action.MAX_ACTIONS);
        }      
    }

}
