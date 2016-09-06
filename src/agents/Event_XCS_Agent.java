package com.clawsoftware.agentsimulator.agents;

/**
 * This class provides the functionality to access the classifier set, to move
 * the agents and to calculate the reward
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */

import com.clawsoftware.agentsimulator.Misc.Log;
import com.clawsoftware.agentsimulator.lcs.ActionClassifierSet;
import com.clawsoftware.agentsimulator.lcs.AppliedClassifierSet;

public class Event_XCS_Agent extends Base_XCS_Agent {

    public Event_XCS_Agent(int n) throws Exception {
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
        calculatedAction = lastMatchSet.chooseAbsoluteDirection(lastExplore);

        lastPrediction = lastMatchSet.getValue(calculatedAction);

        // wir holen uns alle passenden Classifier, die ebenfalls diese Action
        // (im gedrehten Zustand) gewählt hätten
        lastActionSet = new ActionClassifierSet(lastState, lastMatchSet, calculatedAction);
    }

    /**
     * @param reward Positive reward (goal agent in sight or not)
     * @param best_value best value of the previous action set
     * @param is_event If this function was called because of an event, i.e. a positive reward
     * @throws java.lang.Exception If there was an error updating the reward
     */
    public void collectReward(boolean reward, double best_value, boolean is_event) throws Exception {

        double corrected_reward = reward?1.0:0.0;
        if(!is_event) {
            if(prevActionSet != null) {
                prevActionSet.updateReward(corrected_reward, best_value, 1.0);
            }
        } else {
            if(lastActionSet != null) {
                lastActionSet.updateReward(corrected_reward, best_value, 1.0);
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
        lastExplore = testSwitchExplore(reward);

        if(prevActionSet!=null){
            double max_prediction = lastMatchSet.getBestValue();
            collectReward(/*lastReward*/false, max_prediction, false);
            prevActionSet.evolutionaryAlgorithm(classifierSet, gaTimestep);
        }

        // Ziel erreicht?
        if(reward != lastReward) {
            collectReward(reward, 0.0, true);
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
