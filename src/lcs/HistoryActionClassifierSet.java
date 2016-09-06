package com.clawsoftware.agentsimulator.lcs;

import java.util.ArrayList;

import com.clawsoftware.agentsimulator.agent.Sensors;

/**
 * This class provides extra memory to the action classifier set in order to
 * allow the late distribution of the reward
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class HistoryActionClassifierSet {

	private class RewardHelper {

		public RewardHelper(final double reward, final double factor) {
			this.reward = reward;
			this.factor = factor;
		}

		public double reward;
		public double factor;
	}

	private final ArrayList<RewardHelper> reward = new ArrayList<RewardHelper>();
	private ActionClassifierSet actionClassifierSet;

	public HistoryActionClassifierSet(
			final ActionClassifierSet action_classifier_set) {
		actionClassifierSet = action_classifier_set;
	}

	public void addReward(final double reward, final double factor) {
		this.reward.add(new RewardHelper(reward, factor));
	}

	/**
	 * Processes the saved rewards and factors and updates the action sets
	 *
	 * @param main
	 *            The corresponding classifier set of the agent
	 * @throws java.lang.Exception
	 *             If there was an error creating the match set
	 */
	public void processReward(final MainClassifierSet main) throws Exception {
		final int calculatedAction = actionClassifierSet.getAction();
		final Sensors lastState = actionClassifierSet.getLastState();
		final AppliedClassifierSet lastMatchSet = new AppliedClassifierSet(
				lastState, main);
		actionClassifierSet = new ActionClassifierSet(lastState, lastMatchSet,
				calculatedAction);

		double max_reward = 0.0;
		double max = 0.0;

		for (final RewardHelper r : reward) {
			if (r.reward * r.factor > max) {
				max = r.reward * r.factor;
				max_reward = r.reward;
			}
		}
		actionClassifierSet.updateReward(max_reward, 0.0, 1.0);
	}

	public void evolutionaryAlgorithm(
			final MainClassifierSet main_classifier_set, final long gaTimestep)
			throws Exception {
		actionClassifierSet.evolutionaryAlgorithm(main_classifier_set,
				gaTimestep);
	}

	public void destroy() {
		reward.clear();
		actionClassifierSet.destroy();
	}
}
