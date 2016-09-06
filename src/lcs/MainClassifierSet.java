package com.clawsoftware.agentsimulator.lcs;

import java.util.ArrayList;

import com.clawsoftware.agentsimulator.Misc.Misc;
import com.clawsoftware.agentsimulator.Misc.Point;
import com.clawsoftware.agentsimulator.agent.Configuration;
import com.clawsoftware.agentsimulator.agent.Sensors;
import com.clawsoftware.agentsimulator.agents.Base_XCS_Agent;

/**
 * Main classifier set of each agent, provides covering, crossing over,
 * subsumation, adding/removing and relationship functionality
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class MainClassifierSet extends ClassifierSet {

	public MainClassifierSet(final int n) throws Exception {
		super(n);
		if (Configuration.isRandomStart()) {
			for (int i = 0; i < Configuration.getMaxPopSize(); i++) {
				this.addClassifier(new Classifier());
			}
		}
	}

	/**
	 * Add classifiers that match the current state to this set so that the
	 * overall classifier set covers all possible actions
	 * 
	 * @param state
	 *            The current sensor state
	 * @param position
	 *            The position of the agent whose classifier set is this
	 * @param gaTime
	 *            The current time step
	 * @throws java.lang.Exception
	 *             If there was an error creating or adding the classifiers
	 */
	public void coverAllValidActions(final Sensors state, final Point position,
			final long gaTime) throws Exception {
		final boolean[] action_covered = new boolean[Action.MAX_DIRECTIONS];

		for (int i = 0; i < action_covered.length; i++) {
			action_covered[i] = false;
		}
		// check all classifiers that match to the current sensor state
		// rotated variants will be tested, too

		for (final Classifier c : getClassifiers()) {
			action_covered[c.getDirection()] = true;
		}

		double prediction_initialization;
		if (Configuration.isPredictionInitializationAdaption()) {
			prediction_initialization = getAveragePrediction();
		} else {
			prediction_initialization = Configuration
					.getPredictionInitialization();
		}

		/**
		 * loop until all actions are covered
		 */
		boolean all_actions_covered;
		do {
			for (int i = 0; i < action_covered.length; i++) {
				if (!action_covered[i]) {
					final Classifier newCl = new Classifier(state,
							new Action(i), gaTime, getNumerositySum() + 1,
							prediction_initialization);
					addClassifier(newCl);
					// wenn ein neuer Classifier spaeter gepruefte actions schon
					// abdeckt, muessen insgesamt weniger Classifier
					// hinzugefuegt werden
					Base_XCS_Agent.cover_actions++;
				}
			}

			/**
			 * Test which actions are covered
			 */
			for (int i = 0; i < action_covered.length; i++) {
				action_covered[i] = false;
			}
			for (final Classifier c : getClassifiers()) {
				if (c.isMatchingState(state)) {
					action_covered[c.getDirection()] = true;
				}
			}

			/**
			 * repeat if there is an action that is not yet covered
			 */
			all_actions_covered = true;
			for (int i = 0; i < action_covered.length; i++) {
				if (!action_covered[i]) {
					all_actions_covered = false;
					break;
				}
			}
		} while (!all_actions_covered);
	}

	/**
	 * Creates and adds children to the classifier set, constructed out of the
	 * parents
	 * 
	 * @param cl1P
	 *            First parent classifier
	 * @param cl2P
	 *            Second parent classifier
	 * @param state
	 *            Current sensor state
	 * @throws java.lang.Exception
	 *             For various reasons (error creating classifier, setting
	 *             fitness and prediction, subsumation and addition of
	 *             classifiers)
	 */
	protected void crossOverClassifiers(final Classifier cl1P,
			final Classifier cl2P, final Sensors state) throws Exception {
		// children
		final Classifier cl1 = new Classifier(cl1P);
		final Classifier cl2 = new Classifier(cl2P);

		Classifier.crossOverClassifiers(cl2, cl2);

		cl1.applyMutation(state);
		cl2.applyMutation(state);

		cl1.setPrediction((cl1.getPrediction() + cl2.getPrediction()) / 2.0);
		cl1.setPredictionError(Configuration.getPredictionErrorReduction()
				* (cl1.getPredictionError() + cl2.getPredictionError()) / 2.0);
		cl1.setFitness(Configuration.getFitnessReduction()
				* (cl1.getFitness() + cl2.getFitness()) / 2.0);

		cl2.setPrediction(cl1.getPrediction());
		cl2.setPredictionError(cl1.getPredictionError());
		cl2.setFitness(cl1.getFitness());

		/**
		 * Inserts both discovered classifiers keeping the maximal size of the
		 * population and possibly doing GA subsumption.
		 */
		if (Configuration.isDoGASubsumption()) {
			subsumeClassifier(cl1, cl1P, cl2P);
			subsumeClassifier(cl2, cl1P, cl2P);
		} else {
			addClassifier(cl1);
			addClassifier(cl2);
		}
	}

	/**
	 * Tries to subsume a classifier in the parents. If no subsumption is
	 * possible it tries to subsume it in the current set. If no subsumption is
	 * possible the classifier is simply added to the population considering the
	 * possibility that there exists an identical classifier.
	 * 
	 * @param cl
	 *            Classifier in question
	 * @param cl1P
	 *            First parent
	 * @param cl2P
	 *            Second parent
	 * @throws java.lang.Exception
	 *             if there was an error subsuming the classifier
	 * @see Classifier#subsumes
	 */
	protected void subsumeClassifier(final Classifier cl,
			final Classifier cl1P, final Classifier cl2P) throws Exception {
		if (cl1P != null && cl1P.isPossibleSubsumer() && cl1P.subsumes(cl)) {
			cl1P.addNumerosity(1);
		} else if (cl2P != null && cl2P.isPossibleSubsumer()
				&& cl2P.subsumes(cl)) {
			cl2P.addNumerosity(1);
		} else {
			// Open up a new Vector in order to chose the subsumer candidates
			// randomly
			final ArrayList<Classifier> choices = new ArrayList<Classifier>();
			for (final Classifier c : getClassifiers()) {
				if (c.isPossibleSubsumer() && c.subsumes(cl)) {
					choices.add(c);
				}
			}

			// If no subsumer was found, add the classifier to the population
			if (choices.isEmpty()) {
				addClassifier(cl);
			} else {
				choices.get(Misc.nextInt(choices.size())).addNumerosity(1);
			}
		}
	}

	/**
	 * Adds a classifier to the set and increases the numerositySum value
	 * accordingly.
	 * 
	 * @param classifier
	 *            The to be added classifier.
	 * @throws java.lang.Exception
	 *             if there was an error removing classifiers because of a too
	 *             large classifier set
	 */
	public void addClassifier(final Classifier classifier) throws Exception {
		final Classifier identical = getIdenticalClassifier(classifier);
		if (identical != null) {
			identical.addNumerosity(classifier.getNumerosity());
		} else {
			getClassifiers().add(classifier);
			classifier.addParent(this);
		}

		while (getNumerositySum() > Configuration.getMaxPopSize()
				+ Action.MAX_DIRECTIONS) {
			final Classifier c = getDeleteCandidate();
			removeMicroClassifier(c);
		}
	}

	/**
	 * @param c
	 *            The macro classifier we want to remove a micro classifier from
	 * @throws java.lang.Exception
	 *             if the classifier was already empty
	 */
	protected void removeMicroClassifier(final Classifier c) throws Exception {
		if (c == null) {
			return;
		}
		if (c.getNumerosity() > 0) {
			c.addNumerosity(-1);
		} else {
			throw new Exception(
					"Numerosity of Microclassifier was already 0 when we tried to remove it.");
		}
	}

	/**
	 * Looks for an identical classifier in the population.
	 * 
	 * @param newCl
	 *            The new classifier.
	 * @return Returns the identical classifier if found, null otherwise.
	 * @see Classifier#equals
	 */
	private Classifier getIdenticalClassifier(final Classifier newCl) {
		for (final Classifier c : getClassifiers()) {
			if (c.equals(newCl)) {
				return c;
			}
		}
		return null;
	}

	public double getAveragePrediction() throws Exception {
		double pred = 0.0;
		int count = 0;
		for (final Classifier c : getClassifiers()) {
			pred += c.getPrediction();
			count++;
		}
		if (count == 0) {
			return Configuration.getPredictionInitialization();
		}
		pred /= count;
		return pred;
	}

	/**
	 * @return A randomly selected classifier from the set (using roulette wheel
	 *         selection)
	 * @throws java.lang.Exception
	 *             When there was an error selecting from the roulette wheel
	 * @see Classifier#getDelProp
	 */
	private Classifier getDeleteCandidate() throws Exception {
		// get average fitness of classifiers
		final double mean_fitness = getFitnessSum() / getNumerositySum();
		double sum = 0.;

		for (final Classifier c : getClassifiers()) {
			sum += c.getDelProp(mean_fitness);
		}

		// roulette
		final double choicePoint = sum * Misc.nextDouble();

		sum = 0.;
		for (final Classifier c : getClassifiers()) {
			sum += c.getDelProp(mean_fitness);
			if (sum > choicePoint) {
				return c;
			}
		}

		throw new Exception("Error finding proper roulette wheel selection");
	}

	/**
	 * Relation of this classifier set (the active agent classifier set, e.g.
	 * the set that received a reward) to another classifier set
	 * 
	 * @param other
	 *            The other set we want to compare with
	 * @return degree of relationship (0.0 - 1.0)
	 */
	public double checkEgoisticDegreeOfRelationship(
			final MainClassifierSet other) {
		if (ego_factor == 0.0 || other.ego_factor == 0.0) {
			return 0.0;
		}
		return 1.0 - Math.abs(ego_factor - other.ego_factor)
				* Math.abs(ego_factor - other.ego_factor);
	}

	private double ego_factor = 0.0;

	public void updateEgoFactor() throws Exception {
		if (Configuration.getExternalRewardMode() == Configuration.REWARD_EGOISM) {
			ego_factor = getEgoisticFactor();
		}
	}

}
