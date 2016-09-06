package com.clawsoftware.agentsimulator.agents;

/**
 *
 * Simple agent implementation, moves around randomly if no goal agent is in sight
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */

import com.clawsoftware.agentsimulator.Misc.Misc;
import com.clawsoftware.agentsimulator.lcs.Action;
import com.clawsoftware.agentsimulator.agent.Configuration;
import java.util.ArrayList;

public class EinfacheHeuristik_Agent extends BaseAgent {

    
    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     */
    @Override
    public void calculateNextMove(final long gaTimestep) {
        boolean[] goal_sensor = lastState.getSensorGoal();

        calculatedAction = -1;

        switch(Configuration.getGoalMode()) {
            case Configuration.GOAL_OBS_MODE:
            case Configuration.GOAL_OBS_AGENTS_OBS_MODE:
            case Configuration.GOAL_OBS_AGENTS_SIGHT_MODE:
                for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                    if(goal_sensor[2*i+1]) {
                        calculatedAction = i;
                        return;
                    }
                }break;

            case Configuration.GOAL_SIGHT_MODE:
            case Configuration.GOAL_SIGHT_AGENTS_OBS_MODE:
            case Configuration.GOAL_SIGHT_AGENTS_SIGHT_MODE:
                for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                    if(goal_sensor[2*i]) {
                        calculatedAction = i;
                        return;
                    }
                }break;
        }

        boolean[] agent_sensors = lastState.getSensorAgent();

        calculatedAction = Misc.nextInt(Action.MAX_DIRECTIONS);

        switch(Configuration.getGoalMode()) {
            case Configuration.GOAL_OBS_MODE:
            case Configuration.GOAL_SIGHT_MODE:
                return;
        }

        ArrayList<Integer> free_directions = new ArrayList<Integer>();

        switch(Configuration.getGoalMode()) {
            case Configuration.GOAL_OBS_AGENTS_OBS_MODE:
            case Configuration.GOAL_SIGHT_AGENTS_OBS_MODE:
                for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                    if(!agent_sensors[2*i+1]) {
                        free_directions.add(i);
                        break;
                    }
                }break;

            case Configuration.GOAL_OBS_AGENTS_SIGHT_MODE:
            case Configuration.GOAL_SIGHT_AGENTS_SIGHT_MODE:
                for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
                    if(!agent_sensors[2*i]) {
                        free_directions.add(i);
                        break;
                    }
                }break;
        }

        if(free_directions.isEmpty()) {
            return;
        }

        calculatedAction = free_directions.get(Misc.nextInt(free_directions.size()));
    }

}
