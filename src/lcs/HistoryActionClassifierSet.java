package lcs;

import java.util.ArrayList;
import agent.Sensors;

/**
 * This class provides extra memory to the action classifier set in order to 
 * allow the late distribution of the reward
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
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

    public void rewardPrematurely(double max_prediction) throws Exception {
        actionClassifierSet.updateReward(1.0, max_prediction, 1.0);
    }

    public double getBestValue() throws Exception {
        return actionClassifierSet.getMatchSet().getBestValue();
    }

    public void processReward(MainClassifierSet main, double max_prediction) throws Exception {
        if (reward.isEmpty()) {
            return;
        }

        int calculatedAction = actionClassifierSet.getAction();
        Sensors lastState = actionClassifierSet.getLastState();
        AppliedClassifierSet lastMatchSet = new AppliedClassifierSet(lastState, main);
        actionClassifierSet = new ActionClassifierSet(lastState, lastMatchSet, calculatedAction);


        // empty results => Apply 0 reward
        // so that no 0-reward needs to be distributed when no agent is in sight in the LCS Agent code
        //    actionClassifierSet.updateReward(0.0, max_prediction, 1.0);
        //} else {
        double max = 0.0;
        double max_factor = 0.0;
        for (RewardHelper r : reward) {
            // we already gave reward for this case
            if (r.reward == 1.0 && r.factor == 1.0) {
                return;
            }

            if (r.reward * r.factor >= max_factor) {
                max = r.reward;
                max_factor = r.reward * r.factor;
            }
        }
        actionClassifierSet.updateReward(max, max_prediction, 1.0);
    // }
    }

    public void evolutionaryAlgorithm(MainClassifierSet main_classifier_set, long gaTimestep) throws Exception {
        actionClassifierSet.evolutionaryAlgorithm(main_classifier_set, gaTimestep);
    }

    public void destroy() {
        reward.clear();
        actionClassifierSet.destroy();
    }
}
