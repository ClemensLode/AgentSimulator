package com.clawsoftware.agentsimulator.agents;

/**
 * This class provides the functionality to access the classifier set, to move
 * the agents and to calculate the reward
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
import com.clawsoftware.agentsimulator.agent.Configuration;
import com.clawsoftware.agentsimulator.lcs.ActionClassifierSet;
import com.clawsoftware.agentsimulator.lcs.HistoryActionClassifierSet;
import com.clawsoftware.agentsimulator.lcs.AppliedClassifierSet;
import java.util.LinkedList;

public class DSXCS_Agent extends Base_XCS_Agent {

    public DSXCS_Agent(int n) throws Exception {
        super(n);
    }
    /**
     * Complete history of actionsets that were executed
     * Contains the history up to max stack size
     */
    private LinkedList<HistoryActionClassifierSet> historicActionSet = new LinkedList<HistoryActionClassifierSet>();

    /**
     * Determines the matching classifiers and chooses one action from this set
     * @param gaTimestep the current time step
     * @throws java.lang.Exception if there was an error covering all valid actions
     */
    @Override
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

        actionSetSize++;

        historicActionSet.addLast(new HistoryActionClassifierSet(lastActionSet));

        if (historicActionSet.size() > Configuration.getMaxStackSize()) {

            HistoryActionClassifierSet first = historicActionSet.getFirst();
            historicActionSet.removeFirst();
            first.processReward(classifierSet);

            /** Tries to insert two new children into the population
             * They are generated by using crossing over and mutation
             * The two parents are taken from the current population
             */
            if (Configuration.isDoEvolutionaryAlgorithm()) {
                first.evolutionaryAlgorithm(classifierSet, gaTimestep);
            }

            first.destroy();
        }

    }

    /**
     * @param other_agent The original agent
     * @param start_index Index of the action set in the historic action set where the reward update should begin
     * @param action_set_size Number of actions that the original agent has rewarded
     * @param reward The amount of reward that the original agent received
     * @throws java.lang.Exception If there was an error collecting the reward
     * @param is_event Does this reward come from an event or from a non-event (no change in reward for a long time)?
     * @see LCS_Agent#collectReward
     */
    @Override
    public void collectExternalReward(BaseAgent other_agent, int start_index, int action_set_size, boolean reward, boolean is_event) throws Exception {
        if (other_agent.getID() == getID()) {
            return;
        }
        if (start_index >= historicActionSet.size()) {
            return;
        }

        if (action_set_size + start_index > historicActionSet.size()) {
            action_set_size = historicActionSet.size() - start_index;
        }

        // u.U. sehr rechenaufwändig
        switch (Configuration.getExternalRewardMode()) {
            case Configuration.NO_EXTERNAL_REWARD:
                break;
            case Configuration.REWARD_ALL_EQUALLY:
                collectReward(start_index, action_set_size, reward, 1.0, is_event);
                break;
            case Configuration.REWARD_EGOISM:
                collectReward(start_index, action_set_size, reward, classifierSet.checkEgoisticDegreeOfRelationship(((DSXCS_Agent)other_agent).classifierSet), is_event);
                break;
        }

    }

    /**
     *
     * @param start_index Index of the action set in the historic action set where the reward update should begin
     * @param action_set_size Numerosity of the action set size from which the action was 
     * @param reward Positive reward (goal agent in sight or not)
     * @param is_external_reward true if this is an external reward received through communication
     * @param is_event Does this reward come from an event or from a non-event (no change in reward for a long time)?
     * @throws java.lang.Exception If there was an error updating the reward
     */
    private void collectReward(int start_index, int action_set_size, boolean reward, double factor, boolean is_event) throws Exception {
        double corrected_reward = reward ? 1.0 : 0.0;
        for (int i = 0; i < action_set_size; i++) {
            if (is_event) {
                corrected_reward = reward ? calculateReward(action_set_size - i, action_set_size) : calculateReward(i, action_set_size);
            }
            historicActionSet.get(start_index - i).addReward(corrected_reward, factor);
        }
    }

    /**
     * resets the historic action set and initialized lastReward
     */
    @Override
    public void resetBeforeNewProblem() throws Exception {
        super.resetBeforeNewProblem();
        actionSetSize = 0;
        while (!historicActionSet.isEmpty()) {
            HistoryActionClassifierSet first = historicActionSet.getFirst();
            historicActionSet.removeFirst();
            first.processReward(this.getClassifierSet());
            first.destroy();
        }
        historicActionSet.clear();

    }

    /**
     * is called in each step, determines the current reward and checks if the
     * reward has changed. If it has changed update the classifiers in the 
     * action set appropriately
     * @param gaTimestep
     * @throws java.lang.Exception
     */
    @Override
    public void calculateReward(final long gaTimestep) throws Exception {
        boolean reward = checkRewardPoints();
        lastExplore = testSwitchExplore(reward);

        classifierSet.updateEgoFactor();

        // event?
        if (reward != lastReward) {

            int start_index = historicActionSet.size() - 1;
            collectReward(start_index, actionSetSize, reward, 1.0, true);

            grid.contactOtherAgents(this, start_index, actionSetSize, reward, true);
            // remove all classifier sets
            actionSetSize = 0;
            lastReward = reward;
        } else if (actionSetSize >= Configuration.getMaxStackSize()) {

            int start_index = Configuration.getMaxStackSize() / 2;
            int length = actionSetSize - start_index;
            collectReward(start_index, length, reward, 1.0, false);

            if(reward) {
                grid.contactOtherAgents(this, start_index, length, reward, false);
            }
            actionSetSize = start_index;
            lastReward = reward;

        }

    }
}
