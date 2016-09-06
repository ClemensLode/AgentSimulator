package agents;

import lcs.MainClassifierSet;
import Misc.Log;
import lcs.ActionClassifierSet;
import lcs.AppliedClassifierSet;
import Misc.Misc;
import agent.Configuration;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
abstract public class Base_LCS_Agent extends BaseAgent {
    /**
     * Reward of the last time step (in order to recognize events)
     */
    protected boolean lastReward = false;

    /**
     * maximal reward given in each round
     */
    //public final static double MAX_REWARD = 1000.0;

    protected boolean lastExplore = false;

    /**
     * current rule set
     */
    protected MainClassifierSet classifierSet;
    
    
    private Base_LCS_Agent() {}
    
    public Base_LCS_Agent(int n) throws Exception {
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

    protected void tryToExchangeRuleWithNeighbor() throws Exception {
        if(!Configuration.isExchangeClassifiers()) {
            return;
        }
        Base_LCS_Agent a = (Base_LCS_Agent)(grid.findRandomAgentNearby(getPosition(), getID()));
        if(a == null) {
            return;
        }
        classifierSet.exchangeRules(a.classifierSet);
    }
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


    public static boolean checkIfExplore(boolean reward, boolean last_explore, long gaTimestep) {
            /**
         * Exploration probability modes:
         * - no exploration (0/100) (0)
         * - always exploration (100/0) (1)
         * - switch between explore and exploit (50/50) (3)
         * - first explore, then exploit (50/50) (2)
         * - linear reduction of exploration probability (100..0/0..100)
         */
        double exploration_probability = 0.0;
        switch (Configuration.getExplorationMode()) {
            case Configuration.NO_EXPLORATION_MODE:
                return false;
            case Configuration.ALWAYS_EXPLORATION_MODE:
                return true;
                /**
                 * will be switched by the agent if the reward was positive
                 */
            case Configuration.SWITCH_EXPLORATION_MODE:
                return last_explore;
            case Configuration.RANDOM_EXPLORATION_MODE:
                exploration_probability = 0.5;
                break;
            case Configuration.EXPLORE_THEN_EXPLOIT_MODE:
                if (gaTimestep % Configuration.getNumberOfSteps() < Configuration.getNumberOfSteps() / 2.0) {
                    exploration_probability = 1.0;
                } else {
                    exploration_probability = 0.05;
                }
                break;
            case Configuration.LINEAR_REDUCTION_EXPLORE_MODE:
                 {
                    exploration_probability = 1.0 - (double) (gaTimestep % Configuration.getNumberOfSteps()) / (double) Configuration.getNumberOfSteps();
                }
                break;
            case Configuration.GOAL_DIRECTED_EXPLOITATION_MODE:
            {
                return !reward;
            }
        }
        if (Misc.nextDouble() <= exploration_probability) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Resets the lastReward before a new problem
     */
    @Override
    public void resetBeforeNewProblem() throws Exception {
        lastReward = grid.isGoalAgentInRewardRange(this);
        lastExplore = lastReward;
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

    public double getFitnessNumerosity() throws Exception {
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

//TODO random-explore miteinbeziehen! Und alle mal durchtesten evtl.
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
