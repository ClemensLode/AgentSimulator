package com.clawsoftware.agentsimulator.agents;

import com.clawsoftware.agentsimulator.lcs.MainClassifierSet;
import com.clawsoftware.agentsimulator.Misc.Log;
import com.clawsoftware.agentsimulator.lcs.ActionClassifierSet;
import com.clawsoftware.agentsimulator.lcs.AppliedClassifierSet;
import com.clawsoftware.agentsimulator.agent.Configuration;

/**
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
abstract public class Base_XCS_Agent extends BaseAgent {

    static public long cover_actions = 0;

    /**
     * Reward of the last time step (in order to recognize events)
     */
    protected boolean lastReward = false;

    protected boolean lastGoalObs = false;
    protected boolean lastGoalSight = false;


    protected boolean lastExplore = false;

    /**
     * current rule set
     */
    protected MainClassifierSet classifierSet;
    
    
    private Base_XCS_Agent() {}
    
    public Base_XCS_Agent(int n) throws Exception {
        classifierSet = new MainClassifierSet(n);
    }


    @Override
    public double getLastPredictionError() {
        double value = lastPredictionError;
        lastPredictionError = 0.0;
        return value;
    }

    /**
     * number of entries in the action set since the last event
     */
    protected int actionSetSize = 0;

    /**
     * Last set of matchings
     * Not of type ClassifierSet because it holds applied classifiers
     * An applied classifier holds information about how this classifier
     * was actually used.
     * This is necessary because potentially every classifier can execute
     * any action
     */
    protected AppliedClassifierSet lastMatchSet = null;

    /**
     * Last action set, for logging issues
     */
    protected ActionClassifierSet lastActionSet = null;

    protected double lastPrediction = 0.0;
    public double lastPredictionError = 0.0;

    /**
     * Calculates the positive reward, i.e. classifiers will be rewarded higher
     * the LATER they were executed
     * @param step the index of the reward
     * @param size The total number of steps we want to reward
     * @return the reward for the action set at the provided index
     */
    protected static double calculateReward(int step, int size) {
        if(Configuration.isUseQuadraticReward()) {
            return ((double)(step*step)) / ((double)(size*size));
        } else {
            return ((double)step) / ((double) size);
        }
    }


    /**
     * counts the number of rounds the goal agent was in sight and calculates
     * the base reward
     * @return the base reward
     */
    public boolean checkRewardPoints() {
        super.checkRewardPoints();

        boolean reward = false;

        switch(Configuration.getGoalMode()) {
            case Configuration.GOAL_OBS_MODE:
                reward = isGoalAgentVeryNear();break;
            case Configuration.GOAL_SIGHT_MODE:
                reward = isGoalAgentNear();break;
            case Configuration.GOAL_OBS_AGENTS_OBS_MODE:
                reward = isGoalAgentVeryNear();
                if(!reward) {
                    reward = !isAgentVeryNear();
                }
                break;
            case Configuration.GOAL_OBS_AGENTS_SIGHT_MODE:
                reward = isGoalAgentVeryNear();
                if(!reward) {
                    reward = !isAgentNear();
                }
                break;
            case Configuration.GOAL_SIGHT_AGENTS_OBS_MODE:
                reward = isGoalAgentNear();
                if(!reward) {
                    reward = !isAgentVeryNear();
                }
                break;
            case Configuration.GOAL_SIGHT_AGENTS_SIGHT_MODE:
                reward = isGoalAgentNear();
                if(!reward) {
                    reward = !isAgentNear();
                }
                break;
        }

        return reward;
    }

    public boolean testSwitchExplore(boolean reward) {
        if (lastState == null) {
            acquireNewSensorData();
        }
        switch(Configuration.getSwitchMode()) {
            case Configuration.SWITCH_GOAL_OBS_MODE:
                return isGoalAgentVeryNear();
            case Configuration.SWITCH_GOAL_SIGHT_MODE:
                return isGoalAgentNear();
            case Configuration.SWITCH_REWARD_MODE:
                return reward;
            case Configuration.SWITCH_NO_MODE:
            default:return false;
        }
    }

    /**
     * Resets the lastReward before a new problem
     */
    @Override
    public void resetBeforeNewProblem() throws Exception {
        lastReward = checkRewardPoints();
        lastExplore = testSwitchExplore(lastReward);
        lastMatchSet = null;
        lastActionSet = null;
        lastPredictionError = 0.0;
    }




    /**
     * Prints the current state of the grid, the input data from the sensors
     * and the matching classifiers
     */
    @Override
    public void printMatching() {
        try {
            Log.log("# classifiers");
            Log.log(" - Population:");
            Log.log(classifierSet.toString());
            if(lastMatchSet != null) {
                Log.log(" - MatchSet:");
                Log.log(lastMatchSet.toString());
            }
        } catch (Exception e) {
            Log.errorLog("Error creating input string for log file: ", e);
        }
    }

    public double getFitnessNumerosity() {
        return classifierSet.getAverageFitness();
    }
    
    public MainClassifierSet getClassifierSet() {
        return classifierSet;
    }

    /**
     * projected reward if the next action will cause an event
     */
    @Override
    public void printProjectedReward() {
        /*Log.log("# history");
        for(ClassifierSet a : actionSet) {
        Log.log(a.chooseRandomClassifier().toString());
        }*/
        Log.log("# projected reward");
        if (!lastReward) {
            Log.log(" 0 ==> 1");
            for (int i = 0; i < actionSetSize; i++) {
                double corrected_reward = calculateReward(i, actionSetSize);
                Log.log(corrected_reward);
            }
        } else {
            Log.log(" 1 ==> 0");
            for (int i = 0; i < actionSetSize; i++) {
                double corrected_reward = calculateReward(actionSetSize - i, actionSetSize);
                Log.log(corrected_reward);
            }
        }
    }

    /**
     * Prints the action set
     */
    @Override
    public void printActionSet() {
        if(lastActionSet == null) {
            return;
        }
        Log.log("# action set");
        Log.log(" - ActionSet:  [ total action set size: " + actionSetSize + " ]");
        Log.log(lastActionSet.toString());
    }

}
