package com.clawsoftware.agentsimulator.agents;

/**
 * This class provides the functionality to access the classifier set, to move
 * the agents and to calculate the reward
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
import com.clawsoftware.agentsimulator.agent.Configuration;
import com.clawsoftware.agentsimulator.lcs.ActionClassifierSet;
import com.clawsoftware.agentsimulator.lcs.AppliedClassifierSet;
import com.clawsoftware.agentsimulator.lcs.Action;

public class SXCS_Goal_Agent extends SXCS_Agent {

    public SXCS_Goal_Agent(int n) throws Exception {
        super(n);
    }

    /**
     * @return true if the other agents currently is not in reward range
     */
    @Override
    public boolean checkRewardPoints() {
        if (lastState == null) {
            acquireNewSensorData();
        }
        boolean[] sensor_agent = lastState.getSensorAgent();
        boolean reward = true;
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(sensor_agent[2*i+1]) {
                reward = false;
            }
        }

        // goal agent is in sight?
        if (grid.isGoalAgentInRewardRange(this)) {
            totalPoints = totalPoints + 1.0;
        }

        return reward;
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
