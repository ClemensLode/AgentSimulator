package com.clawsoftware.agentsimulator.lcs;

import com.clawsoftware.agentsimulator.Misc.Misc;
import com.clawsoftware.agentsimulator.agent.Configuration;

/**
 * Base class of an action
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class Action {

    /**
     * The action to take
     */
    private int action;
   
    public static final int ACTION_SIZE = 1;
    public static final int MAX_DIRECTIONS = 4;
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    
    // special, only for goal agents and special random agents
    public static final int DO_JUMP = -1;

    public static final String[] shortDirectionString = {"N", "E", "S", "W", "-"};    // direction starts at 0 degrees (vertical)
    
    public static final int[] dx = {0, 1, 0, -1, 0};
    public static final int[] dy = {-1, 0, 1, 0, 0};

    /**
     * Generates a copy of an Action
     * @param a The action to copy
     */
    public Action(Action a) {
        action = a.action;
    }

    /**
     * Generates a new Action with the given action
     * @param action The action
     */
    public Action(final int action) {
        this.action = action;
    }


    public int getDirection() {
        return action;
    }

    public boolean mutateAction()
    {
        boolean changed = false;
        if(Misc.nextDouble() < Configuration.getMutationProbability()) {
            int act = 0;
            do {
                act = Misc.nextInt(Action.MAX_DIRECTIONS);
            } while(act == action);
            action = act;
            changed = true;
        }
        return changed;
	}

    @Override
    public Action clone() {
        return new Action(action);
    }
    
    public boolean equals(final Action a) {
        return (action == a.action);
    }

    @Override
    public String toString() {
        return Action.shortDirectionString[action];
    }
}
