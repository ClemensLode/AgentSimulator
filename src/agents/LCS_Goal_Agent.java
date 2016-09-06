package agents;

/**
 * This class provides the functionality to access the classifier set, to move
 * the agents and to calculate the reward
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
import agent.Configuration;
import lcs.ActionClassifierSet;
import lcs.AppliedClassifierSet;
import lcs.Action;

public class LCS_Goal_Agent extends LCS_Agent {

    public LCS_Goal_Agent(int n) throws Exception {
        super(n);
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
        boolean reward = !grid.isGoalAgentInRewardRangeByAnyAgent();
        if(reward && Configuration.getExplorationMode() == Configuration.SWITCH_EXPLORATION_MODE) {
            // new problem!
            lastExplore = !lastExplore;
        }

        // event?
        if (reward != lastReward) {
            int start_index = historicActionSet.size() - 1;
            collectReward(start_index, actionSetSize, reward, 1.0, true);
            // remove all classifier sets
            actionSetSize = 0;
            lastReward = reward;
        }
        // ausschliesslich
        else if(actionSetSize >= Configuration.getMaxStackSize())
        {
            int start_index = Configuration.getMaxStackSize() / 2;
            int length = actionSetSize - start_index;
            collectReward(start_index, length, reward, 1.0, false);
            actionSetSize = start_index;
            lastReward = reward;
        }
        evolutionaryAlgorithm(gaTimestep);
    }


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
        boolean[] sensor_agent = lastState.getSensorAgent();
        lastExplore = checkIfExplore(sensor_agent[0] || sensor_agent[1] || sensor_agent[2] || sensor_agent[3], lastExplore, gaTimestep);

        calculatedAction = lastMatchSet.chooseAbsoluteDirection(lastExplore);

        lastPrediction = lastMatchSet.getValue(calculatedAction);

        // wir holen uns alle passenden Classifier, die ebenfalls diese Action
        // (im gedrehten Zustand) gewählt hätten
        lastActionSet = new ActionClassifierSet(lastState, lastMatchSet, calculatedAction);

        actionSetSize++;

        historicActionSet.addLast(lastActionSet);

        if (historicActionSet.size() > Configuration.getMaxStackSize()) {
            historicActionSet.getFirst().destroy();
            historicActionSet.removeFirst();
        }


        if(BaseAgent.grid.getAvailableDirections(getPosition()).isEmpty()) {
            calculatedAction = Action.DO_JUMP;
            System.out.println("DO_JUMP called");
        }
    }

}
