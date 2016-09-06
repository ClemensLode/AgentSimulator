package com.clawsoftware.agentsimulator.agents;

/**
 * This class provides basic functionality for any moving agents and the goal agent
 * This class cannot be instanciated
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
import com.clawsoftware.agentsimulator.agent.Sensors;
import com.clawsoftware.agentsimulator.agent.Field;
import com.clawsoftware.agentsimulator.agent.Grid;
import com.clawsoftware.agentsimulator.Misc.Log;
import com.clawsoftware.agentsimulator.Misc.Point;
import java.text.NumberFormat;
import com.clawsoftware.agentsimulator.lcs.Action;

public abstract class BaseAgent {

    /**
     * The action that was determined by calculateNextMove
     */
    protected int calculatedAction = 0;
    protected Sensors lastState = null;
    /**
     * statistical value
     */
    protected double totalPoints = 0;
    /**
     * instance of the grid
     */
    public static Grid grid;
    /**
     * instance of the single goal agent
     */
    public static BaseAgent goalAgent;
    /**
     * current position on the grid
     */
    private Point p;
    /**
     * unique id of the agent
     */
    private int id;
    /**
     * reset global id whenever a new grid with new agents is created
     */
    private static int global_id = Field.GOAL_AGENT_ID;

    /**
     * Create new agent and register the agent in the grid
     */
    protected BaseAgent() {
        id = getNewID();
        grid.addAgent(this);
    }

    public void acquireNewSensorData() {
        lastState = grid.getAbsoluteSensorInformation(p, id);
    }

    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     * @throws java.lang.Exception
     * @see LCS_Agent#calculateNextMove
     * @see AI_Agent#calculateNextMove
     * @see Good_AI_Agent#calculateNextMove
     * @see Random_Agent#calculateNextMove
     */
    public abstract void calculateNextMove(final long gaTimestep) throws Exception;

    /**
     * Only implemented by LCS_Agent
     * @param gaTimestep the current time step
     * @throws java.lang.Exception
     * @see LCS_Agent#calculateReward
     */
    public void calculateReward(final long gaTimestep) throws Exception {
        checkRewardPoints();
    }


    /**
     * counts the number of rounds the goal agent was in sight and calculates
     * the base reward
     * @return the base reward
     */
    public boolean checkRewardPoints() {
        if (lastState == null) {
            acquireNewSensorData();
        }

        // goal agent is in reward range?
        if (grid.isGoalAgentInRewardRange(this)) {
            totalPoints = totalPoints + 1.0;
            return true;
        }
        return false;
    }

    /**
     * Only implemented by LCS_Agent
     * @param other_agent The original agent
     * @param start_index Index of the action set in the historic action set where the reward update should begin
     * @param action_set_size Number of actions that the original agent has rewarded
     * @param reward The amount of reward that the original agent received
     * @throws java.lang.Exception If there was an error collecting the reward
     * @param is_event Does this reward come from an event or from a non-event (no change in reward for a long time)?
     * @see LCS_Agent#collectExternalReward
     * @see New_LCS_Agent#collectExternalReward
     * @see Multistep_LCS_Agent#collectExternalReward
     */
    public void collectExternalReward(BaseAgent other_agent, int start_index, int action_set_size, boolean reward, boolean is_event) throws Exception {
    }

    /**
     * Move the agent and put all classifiers with the same action in the action
     * set for later reward
     * @throws java.lang.Exception if there was an error moving the agent on the grid
     */
    public void doNextMove() throws Exception {
        if (grid.moveAgent(this, calculatedAction)) {
            printMove();
        }
    }

    public boolean isGoalAgentNear() {
        boolean[] sensor_goal = lastState.getSensorGoal();
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if ((sensor_goal[2 * i])) {
                return true;
            }
        }
        return false;
    }
    public boolean isGoalAgentVeryNear() {
        boolean[] sensor_goal = lastState.getSensorGoal();
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (sensor_goal[2 * i + 1]) {
                return true;
            }
        }
        return false;
    }
    public boolean isAgentNear() {
        boolean[] sensor_agent = lastState.getSensorAgent();
        int near = 0;
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (sensor_agent[2 * i]) {
                near++;
                
            }
        }
        return (near >= 2);
        //return false;
    }

    public boolean isAgentVeryNear() {
        boolean[] sensor_agent = lastState.getSensorAgent();
        for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if (sensor_agent[2 * i + 1]) {
                return true;
            }
        }
        return false;
    }



    /**
     * Called before each problem
     */
    public void resetBeforeNewProblem() throws Exception {
    }

    /**
     * @return true if this agent has the goal agent id
     */
    public boolean isGoalAgent() {
        return id == Field.GOAL_AGENT_ID;
    }

    /**
     * @return the number of rounds this particular agent has observed the goal agent
     */
    public double getTotalPoints() {
        return totalPoints;
    }

    public double getLastPredictionError() {
        return 0.0;
    }

    /**
     * @return the current position on the grid
     */
    public final Point getPosition() {
        return p;
    }

    /**
     * @param p the desired position, may only be called by Grid!
     */
    public void setPosition(Point p) {
        this.p = new Point(p.x, p.y);
    }

    /**
     * @return The current X position
     */
    public int getX() {
        return p.x;
    }

    /**
     * @return The current Y position
     */
    public int getY() {
        return p.y;
    }

    /**
     * @return The unique ID of this agent
     */
    public int getID() {
        return id;
    }

    /**
     * resets the global id so that we can generate new agents
     * call only before experiments!
     */
    public static void resetGlobalID() {
        global_id = Field.GOAL_AGENT_ID;
    }

    /**
     * @return a new unique index number for the agents
     */
    public static int getNewID() {
        int new_id = global_id;
        global_id++;
        return new_id;
    }

    /**
     * Prints the header of the LCS_Agent (the ID)
     */
    public void printHeader() {
        if (!Log.isDoLog()) {
            return;
        }

        if (this.isGoalAgent()) {
            Log.log("# GOAL AGENT");
        } else {
            Log.log("# AGENT");
        }
        NumberFormat nf = NumberFormat.getInstance(); // Get Instance of NumberFormat
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3

        String sb = "ID " + (nf.format((long) getID()));
        Log.log(sb);
        Log.log("# input");
        if (lastState == null) {
            acquireNewSensorData();
        }
        Log.log(lastState.getInputString());
    }

    public void printMove() {
        if (!Log.isDoLog()) {
            return;
        }
        Log.log(" - actual move");
        Log.log("    " + calculatedAction);
    }

    public void printActionSet() {
    }

    public void printMatching() {
    }

    public void printProjectedReward() {
    }

    /**
     * @return the calculatedAction
     */
    public int getCalculatedAction() {
        return calculatedAction;
    }
}
