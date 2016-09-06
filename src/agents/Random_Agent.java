package agents;

import agent.Configuration;
import Misc.Misc;
import lcs.Action;
import java.util.ArrayList;

/**
 *
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Random_Agent extends BaseAgent {

    /**
     * only for goal agent
     */
    private int lastDirection = 0;

    private boolean is_goal_agent = false;
    
    /**
     * Movement type
     * @see Configuration#TOTAL_RANDOM_MOVEMENT
     * @see Configuration#RANDOM_MOVEMENT
     * @see Configuration#INTELLIGENT_MOVEMENT_OPEN
     * @see Configuration#INTELLIGENT_MOVEMENT_HIDE
     * @see Configuration#RANDOM_DIRECTION_CHANGE
     * @see Configuration#ALWAYS_SAME_DIRECTION
     */
    private int movementType = 0;

    private int timeout = 0;
    
    public Random_Agent(int movement_type, boolean is_goal_agent) throws Exception {
        super();
        movementType = movement_type;
        this.is_goal_agent = is_goal_agent;
    }


    /**
     * 
     * @param gaTimestep not used
     * @throws java.lang.Exception If there was an error determining the available directions
     */
    @Override
    public void calculateNextMove(final long gaTimestep) throws Exception {
        if(movementType == Configuration.TOTAL_RANDOM_MOVEMENT) {
            calculatedAction = Action.DO_JUMP;
            return;
        }

        if(is_goal_agent && BaseAgent.grid.getAvailableDirections(getPosition()).isEmpty()) {
            calculatedAction = Action.DO_JUMP;
            System.out.println("DO_JUMP called");
            return;
        }
        
        ArrayList<Integer> available_actions = BaseAgent.grid.getAllDirections();

            switch(movementType) {
                case Configuration.RANDOM_MOVEMENT:
                    break;
                case Configuration.RANDOM_DIRECTION_CHANGE:
                        Integer opposing_dir = (lastDirection + (Action.MAX_DIRECTIONS / 2)) % Action.MAX_DIRECTIONS;
                        available_actions.remove(opposing_dir);
                    break;
                case Configuration.INTELLIGENT_MOVEMENT_OPEN:
                    BaseAgent.grid.maybeRemoveAgentDirections(this, available_actions, 0.5);
                    BaseAgent.grid.maybeRemoveObstacleDirections(this, available_actions, 0.2);
                   
                    // move away from agents
                    // tend to move away from walls
                    break;
                case Configuration.INTELLIGENT_MOVEMENT_HIDE:
                    BaseAgent.grid.maybeRemoveAgentDirections(this, available_actions, 0.5);
                    BaseAgent.grid.maybeRemoveOpenDirections(this, available_actions, 0.2);
                    // move away from agents
                    // tend to move to walls
                    break;
                case Configuration.ALWAYS_SAME_DIRECTION:
                        BaseAgent.grid.removeExceptThisDirection(lastDirection, available_actions);
                        if(available_actions.isEmpty()) {
                            available_actions = BaseAgent.grid.getSideDirections(lastDirection);
                            timeout++;
                        }
                        if(available_actions.isEmpty() || timeout > Configuration.getMaxX()) {
                            timeout = 0;
                            calculatedAction = Action.DO_JUMP;
                            return;
                        }
                    break;
                case Configuration.RANDOM_HIDE:
                    BaseAgent.grid.maybeRemoveOpenDirections(this, available_actions, 0.5);
                    // tend to move to walls
                    break;
            }

        if(available_actions.isEmpty()) {
            available_actions = BaseAgent.grid.getAllDirections();
        }

        calculatedAction = available_actions.get(Misc.nextInt(available_actions.size()));

        if(movementType != Configuration.ALWAYS_SAME_DIRECTION) {
            lastDirection = calculatedAction;
        }
    }
}
