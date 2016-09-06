package com.clawsoftware.agentsimulator.lcs;

import java.util.ArrayList;

import com.clawsoftware.agentsimulator.Misc.Misc;
import com.clawsoftware.agentsimulator.agent.Configuration;
import com.clawsoftware.agentsimulator.agent.Sensors;

/**
 * The correctly rotated classifiers with the absolute direction Only used to
 * determine the action
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class AppliedClassifierSet {

	/**
	 * The actual sets of applied classifiers (i.e. pairs of the actual
	 * direction and the original classifier)
	 */
	private final ArrayList<Classifier> classifiers = new ArrayList<Classifier>();
	/**
	 * The cache for the prediction fitness product sum for each direction
	 */
	private final double[] predictionFitnessProductSum = new double[Action.MAX_DIRECTIONS];
	/**
	 * The cache for the sum of the fitnesses of classifiers for each direction
	 */
	private final double[] fitnessSum = new double[Action.MAX_DIRECTIONS];

	/**
	 * If we have matching classifiers in the original population then include
	 * it here so that they are not generated twice Include all classifiers and
	 * save their action
	 * 
	 * @param state
	 *            The current sensor state of the agent
	 * @param parent_pop
	 *            The classifier set of the agent
	 * @see Classifier#getMatchingActions
	 */
	public AppliedClassifierSet(final Sensors state,
			final ClassifierSet parent_pop) throws Exception {
		for (final Classifier c : parent_pop.getClassifiers()) {
			if (c.isMatchingState(state)) {
				classifiers.add(c);
			}
		}
		/**
		 * needs to be executed because choseAbsoluteDirection is called
		 * immediately after creation while 'getBestValue' will be called in a
		 * later time step In the mean-time the fitness will have changed
		 */
		initValues();
	}

	public double getValue(final int action) throws Exception {
		initValues();
		return predictionFitnessProductSum[action];
	}

	/**
	 * @return the highest value in the prediction array.
	 */
	public double getBestValue() throws Exception {
		initValues();

		int i;
		double max;
		for (i = 1, max = predictionFitnessProductSum[0]; i < predictionFitnessProductSum.length; i++) {
			if (max < predictionFitnessProductSum[i]) {
				max = predictionFitnessProductSum[i];
			}
		}
		return max;
	}

	public double getWorstValue() throws Exception {
		initValues();

		int i;
		double min;
		for (i = 1, min = predictionFitnessProductSum[0]; i < predictionFitnessProductSum.length; i++) {
			if (min > predictionFitnessProductSum[i]) {
				min = predictionFitnessProductSum[i];
			}
		}
		return min;

	}

	/**
	 * Choses an action from the matching set
	 * 
	 * @param do_explore
	 *            Whether to concentrate on random classifiers or the best
	 *            classifiers
	 * @return the winning action
	 */
	public int chooseAbsoluteDirection(final boolean do_explore) {
		// choose random classifier from classifierArray randomly by fitness *
		// prediction
		if (do_explore) {
			switch (Configuration.getExplorationMode()) {
			case Configuration.RANDOM_ROULETTE_SELECTION_MODE:
			case Configuration.RANDOM_TOURNAMENT_SELECTION_MODE:
			case Configuration.RANDOM_BEST_SELECTION_MODE:
				return randomActionWinner();
			case Configuration.ROULETTE_TOURNAMENT_SELECTION_MODE:
			case Configuration.ROULETTE_BEST_SELECTION_MODE:
				return rouletteActionWinner();
			case Configuration.TOURNAMENT_BEST_SELECTION_MODE:
			default:
				return tournamentActionWinner();
			}
		} else {
			switch (Configuration.getExplorationMode()) {
			case Configuration.RANDOM_ROULETTE_SELECTION_MODE:
				return rouletteActionWinner();
			case Configuration.RANDOM_TOURNAMENT_SELECTION_MODE:
			case Configuration.ROULETTE_TOURNAMENT_SELECTION_MODE:
				return tournamentActionWinner();
			case Configuration.RANDOM_BEST_SELECTION_MODE:
			case Configuration.ROULETTE_BEST_SELECTION_MODE:
			case Configuration.TOURNAMENT_BEST_SELECTION_MODE:
			default:
				return bestActionWinner();
			}
		}
	}

	/**
	 * @return The private classifier set
	 */
	public final ArrayList<Classifier> getClassifiers() {
		return classifiers;
	}

	/**
	 * re-calculates the fitness and predictionFitnessProduct cache
	 */
	private void initValues() throws Exception {
		for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
			predictionFitnessProductSum[i] = 0.0;
			fitnessSum[i] = 0.0;
		}
		for (final Classifier c : classifiers) {
			predictionFitnessProductSum[c.getDirection()] += c.getPrediction()
					* c.getFitness();
			fitnessSum[c.getDirection()] += c.getFitness();
			if (Double.isNaN(predictionFitnessProductSum[c.getDirection()])) {
				throw new Exception("predfit out of range " + c.toString());
			}
			if (Double.isNaN(fitnessSum[c.getDirection()])) {
				throw new Exception("fit out of range " + c.toString());
			}
		}

		for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
			if (Double.isNaN(predictionFitnessProductSum[i])) {
				throw new Exception("predfit out of range " + i);
			}
			if (Double.isNaN(fitnessSum[i])) {
				throw new Exception("fit out of range " + i);
			}
		}

		for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
			if (fitnessSum[i] != 0.0) {
				predictionFitnessProductSum[i] /= fitnessSum[i];
				if (Double.isNaN(predictionFitnessProductSum[i])) {
					throw new Exception("out of range " + fitnessSum[i]);
				}
			} else {
				predictionFitnessProductSum[i] = 0;
			}
		}
	}

	/**
	 * Selects an action randomly. The function assures that the chosen action
	 * is represented by at least one classifier.
	 * 
	 * @return The action id
	 */
	private int randomActionWinner() {
		int ret = 0;
		do {
			ret = Misc.nextInt(Action.MAX_DIRECTIONS);
		} while (fitnessSum[ret] == 0.0);
		return ret;
	}

	/**
	 * Selects the action in the prediction array with the best value.
	 * 
	 * @return The action id
	 */
	private int bestActionWinner() {
		int ret = 0;
		final int[] array = Misc.getRandomArray(Action.MAX_DIRECTIONS);
		for (final int element : array) {
			if (ret == -1
					|| predictionFitnessProductSum[ret] < predictionFitnessProductSum[element]) {
				ret = element;
			}
		}
		return ret;
	}

	/**
	 * Selects an action in the prediction array by roulette wheel selection.
	 * 
	 * @return -1 if no winner could be determined, the action id otherwise
	 */
	private int rouletteActionWinner() {
		double bidSum = 0.;
		for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
			bidSum += predictionFitnessProductSum[i];
		}

		bidSum *= Misc.nextDouble();

		double bidC = 0.;
		for (int i = 0; i < Action.MAX_DIRECTIONS; i++) {
			bidC += predictionFitnessProductSum[i];
			if (bidC >= bidSum) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Selects an action according to their rank, the first will be chosen with
	 * probability p, the next with p*(1-p), the third with p*(1-p)^2 etc. See
	 * M. V. Butz, K. Sastry, and D. E. Goldberg, “Tournament selection: Stable
	 * fitness pressure in XCS,” in Lecture Notes in Computer Science, Eds.
	 * Chicago, IL, Jul. 12–16, 2003, vol. 2724, Proc. Genetic and Evol.
	 * Comput., pp. 1857–1869.
	 *
	 *
	 * @return
	 */
	private int tournamentActionWinner() {
		int[] array = Misc.getRandomArray(Action.MAX_DIRECTIONS);

		while (array.length > 1) {
			int ret = -1;
			int j = 0;

			for (int i = 0; i < array.length; i++) {
				if (ret == -1
						|| predictionFitnessProductSum[ret] < predictionFitnessProductSum[array[i]]) {
					ret = array[i];
					j = i;
				}
			}
			if (Misc.nextDouble() <= Configuration.getTournamentProbability()) {
				return ret;
			}

			final int[] new_array = new int[array.length - 1];
			for (int i = 0; i < j; i++) {
				new_array[i] = array[i];
			}
			for (int i = j + 1; i < array.length; i++) {
				new_array[i - 1] = array[i];
			}
			array = new int[new_array.length];
			for (int i = 0; i < array.length; i++) {
				array[i] = new_array[i];
			}
		}
		return array[0];
	}

	public boolean isEmpty() {
		return classifiers.isEmpty();
	}

	@Override
	public String toString() {
		String output = new String();
		for (final Classifier c : getClassifiers()) {
			output += "     - " + c.toString() + "\n";
		}
		return output;
	}
}
