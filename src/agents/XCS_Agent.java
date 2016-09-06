package agents;

/**
 * This class provides the functionality to access the classifier set, to move
 * the agents and to calculate the reward
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */

import agent.Configuration;
import Misc.Log;
import lcs.ActionClassifierSet;
import lcs.AppliedClassifierSet;

public class XCS_Agent extends Base_XCS_Agent {

    public XCS_Agent(int n) throws Exception {
        super(n);
    }

    private ActionClassifierSet prevActionSet = null;

    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     * @throws java.lang.Exception if there was an error covering all valid actions
     */
    public void calculateNextMove(long gaTimestep) throws Exception {
        // Überdecke zur aktuellen Situation fehlende Aktionen
        classifierSet.coverAllValidActions(lastState, getPosition(), gaTimestep);
        /**
         * Match set muss Bezug auf die Sensoren haben, damit das Action Set
         * korrekt konstruiert werden kann!
         * holt sich alle classifier die auf die aktuelle Situation passen
         * und merkt sich jeweils ihre Rotation (bzw. Aktion) in dieser gedrehten
         * Situation 
         */
        lastMatchSet = new AppliedClassifierSet(lastState, classifierSet);
        // Wir holen uns einen zufälligen / den besten Classifier
        lastExplore = checkIfExplore(lastState.isGoalInRewardRange(), lastExplore, gaTimestep);

        calculatedAction = lastMatchSet.chooseAbsoluteDirection(lastExplore);

        lastPrediction = lastMatchSet.getValue(calculatedAction);

        // wir holen uns alle passenden Classifier, die ebenfalls diese Action
        // (im gedrehten Zustand) gewählt hätten
        lastActionSet = new ActionClassifierSet(lastState, lastMatchSet, calculatedAction);
    }



    /**
     * @param other_agent The original agent
     * @param action_set_size Number of actions that the original agent has rewarded
     * @param reward The amount of reward that the original agent received
     * @throws java.lang.Exception If there was an error collecting the reward
     * @see LCS_Agent#collectReward
     * TODO raus
     */
    @Override
    public void collectExternalReward(BaseAgent other_agent, int start_index, int action_set_size, boolean reward, boolean is_event) throws Exception {
        double best_value = is_event?0.0:lastMatchSet.getBestValue();
        // u.U. sehr rechenaufwändig
        switch (Configuration.getExternalRewardMode()) {
            case Configuration.NO_EXTERNAL_REWARD:
                break;
            case Configuration.REWARD_ALL_EQUALLY:
                collectReward(reward, best_value, 1.0, is_event);
                break;
            case Configuration.REWARD_SIMPLE:
                collectReward(reward, best_value, classifierSet.checkDegreeOfRelationship(((XCS_Agent)other_agent).classifierSet), is_event);
                break;
            case Configuration.REWARD_COMPLEX:
                //collectReward(reward, best_value, classifierSet.checkComplexDegreeOfRelationship(((XCS_Agent)other_agent).classifierSet), is_event);
                break;
            case Configuration.REWARD_NEW:
                collectReward(reward, best_value, classifierSet.checkDegreeOfRelationshipNew(((XCS_Agent)other_agent).classifierSet), is_event);
                break;
            case Configuration.REWARD_EGOISM:
                collectReward(reward, best_value, classifierSet.checkEgoisticDegreeOfRelationship(((XCS_Agent)other_agent).classifierSet), is_event);
                break;
        }

    }

    /**
     * @param reward Positive reward (goal agent in sight or not)
     * @param best_value best value of the previous action set
     * @param factor Weight of the update
     * @param is_event If this function was called because of an event, i.e. a positive reward
     * @throws java.lang.Exception If there was an error updating the reward
     */
    public void collectReward(boolean reward, double best_value, double factor, boolean is_event) throws Exception {
        if(factor == 0.0) {
            return;
        }
        double corrected_reward = reward?1.0:0.0;
        if(!is_event) {
            if(prevActionSet != null) {
                prevActionSet.updateReward(corrected_reward, best_value, factor);
            }
        } else {
            if(lastActionSet != null) {
                lastActionSet.updateReward(corrected_reward, best_value, factor);
                prevActionSet = null;
            }
        }
    }


    /**
     * is called in each step, determines the current reward and checks if the
     * reward has changed. If it has changed update the classifiers in the 
     * action set appropriately
     * @param gaTimestep current time step
     * @throws java.lang.Exception If there was an error collecting the reward, executing the evolutionary algorithm or contacting other agents
     */
    @Override
    public void calculateReward(final long gaTimestep) throws Exception {
        boolean reward = checkRewardPoints();
        if(reward != lastReward) {
            if(Configuration.getExplorationMode() == Configuration.SWITCH_EXPLORATION_START_EXPLORE_MODE ||
               Configuration.getExplorationMode() == Configuration.SWITCH_EXPLORATION_START_EXPLOIT_MODE) {
            // new problem!
                lastExplore = !lastExplore;
            }
        }

        if(prevActionSet!=null){
            double max_prediction = 0.0;
            if(Configuration.isUseMaxPrediction()) {
                max_prediction = lastMatchSet.getBestValue();
            }
            collectReward(lastReward, max_prediction, 1.0, false);
            prevActionSet.evolutionaryAlgorithm(classifierSet, gaTimestep);
        }

        // Ziel erreicht?
        if(reward) {
            collectReward(reward, 0.0, 1.0, true);
            lastActionSet.evolutionaryAlgorithm(classifierSet, gaTimestep);
            prevActionSet = null;
            return;
        }
        prevActionSet = lastActionSet;
        lastReward = reward;
        
    }


    /**
     * projected reward if the next action will cause an event
     */
    @Override
    public void printProjectedReward() {
    }    


    /**
     * Prints the action set
     */
    @Override
    public void printActionSet() {
        Log.log("# action set");
        Log.log(" - ActionSet:  [ total action set size: " + lastActionSet.size() + " ]");
        Log.log(lastActionSet.toString());
    }
}
