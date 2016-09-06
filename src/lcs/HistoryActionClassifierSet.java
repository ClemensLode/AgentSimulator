package lcs;

import java.util.ArrayList;
import agent.Sensors;

/**
 * This class provides extra memory to the action classifier set in order to 
 * allow the late distribution of the reward
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class HistoryActionClassifierSet {

    private class RewardHelper {

        public RewardHelper(double reward, double factor) {
            this.reward = reward;
            this.factor = factor;
        }
        public double reward;
        public double factor;
    }
    private ArrayList<RewardHelper> reward = new ArrayList<RewardHelper>();
    private ActionClassifierSet actionClassifierSet;

    public HistoryActionClassifierSet(ActionClassifierSet action_classifier_set) {
        actionClassifierSet = action_classifier_set;
    }

    public void addReward(double reward, double factor) {
        this.reward.add(new RewardHelper(reward, factor));
    }

    /**
     * Processes the saved rewards and factors and updates the action sets
     *
     * @param main The corresponding classifier set of the agent
     * @throws java.lang.Exception If there was an error creating the match set
     */
    public void processReward(MainClassifierSet main) throws Exception {
        int calculatedAction = actionClassifierSet.getAction();
        Sensors lastState = actionClassifierSet.getLastState();
        AppliedClassifierSet lastMatchSet = new AppliedClassifierSet(lastState, main);
        actionClassifierSet = new ActionClassifierSet(lastState, lastMatchSet, calculatedAction);

        double max_reward = 0.0;
        double max = 0.0;

        for (RewardHelper r : reward) {
            if (r.reward * r.factor > max) {
                max = r.reward * r.factor;
                max_reward = r.reward;
            }
        }
        actionClassifierSet.updateReward(max_reward, 0.0, 1.0);
    }


    public void evolutionaryAlgorithm(MainClassifierSet main_classifier_set, long gaTimestep) throws Exception {
        actionClassifierSet.evolutionaryAlgorithm(main_classifier_set, gaTimestep);
    }

    public void destroy() {
        reward.clear();
        actionClassifierSet.destroy();
    }
}
