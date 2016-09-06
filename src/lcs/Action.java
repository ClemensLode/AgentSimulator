package lcs;

/**
 * Base class of an action
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Action {

    /**
     * The action to take
     */
    private int action;
   
    public static final int ACTION_SIZE = 1;
    public static final int MAX_DIRECTIONS = 4;
    public static final int MAX_ACTIONS = 5;
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int NO_DIRECTION = 4;
    
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

    /**
     * @param rotation Amount of rotation
     * @return The rotated version of the action
     */
    public final int getRotated(final int rotation) {
        if(action == NO_DIRECTION) {
            return NO_DIRECTION;
        }
        return ((action + rotation) % MAX_DIRECTIONS);
    }

    public int getDirection() {
        return action;
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
