/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lcs;

import java.util.ArrayList;

/**
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
        // if reward == factor == 1 reward immediately
        this.reward.add(new RewardHelper(reward, factor));
    }

    public double getBestValue() {
        return actionClassifierSet.getMatchSet().getBestValue();
    }

    public void processReward(double max_prediction) throws Exception {
        if(reward.isEmpty()) {
            // empty results => Apply 0 reward
            // so that no 0-reward needs to be distributed when no agent is in sight in the LCS Agent code
            actionClassifierSet.updateReward(0.0, max_prediction, 1.0);
        } else {
            double max = 0.0;
            double max_factor = 0.0;
//            bloed
            for(RewardHelper r : reward) {
                //actionClassifierSet.updateReward(r.reward, max_prediction, r.factor);
                if(r.reward >= max && r.factor >= max_factor) {
                    max = r.reward;
                    max_factor = r.factor;
                }
            }
            actionClassifierSet.updateReward(max, max_prediction, max_factor);
        }
    }

    public void evolutionaryAlgorithm(MainClassifierSet main_classifier_set, long gaTimestep) throws Exception {
        actionClassifierSet.evolutionaryAlgorithm(main_classifier_set, gaTimestep);
    }

    public void destroy() {
        reward.clear();
        actionClassifierSet.destroy();
    }
}
