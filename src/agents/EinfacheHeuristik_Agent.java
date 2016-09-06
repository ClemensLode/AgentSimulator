package agents;

/**
 *
 * Simple agent implementation, moves around randomly if no goal agent is in sight
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */

import Misc.Misc;
import lcs.Action;
import agent.Configuration;

public class EinfacheHeuristik_Agent extends BaseAgent {

    
    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     */
    @Override
    public void calculateNextMove(final long gaTimestep) {
        boolean[] goal_sensor = lastState.getSensorGoal();
        calculatedAction = -1;
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(goal_sensor[2*i]) {
                calculatedAction = i;
                break;
            }
        }

        if(Misc.nextDouble() > Configuration.getTournamentProbability()) {
            int temp = Misc.nextInt(Action.MAX_DIRECTIONS - 1);
            if(temp == calculatedAction) {
                temp++;
            }
            calculatedAction = temp;
        }

        if(calculatedAction == -1) {
            calculatedAction = Misc.nextInt(Action.MAX_DIRECTIONS);
        }      
    }

}
