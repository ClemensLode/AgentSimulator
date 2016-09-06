package lcs;

import agent.Sensors;
import agent.Configuration;
import Misc.Misc;
import java.util.ArrayList;

/**
 *
 * This class provides the action classifier set, it is needed to save the history of sets of
 * classifiers that were activated so far
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class ActionClassifierSet extends ClassifierSet {

    /**
     * Sensor status at that time step
     */
    private Sensors state;
    
    /**
     * chosen action at that time step
     */
    private int action;
    
    /**
     * The matchset from which this set of classifiers was selected
     */
    private AppliedClassifierSet matchSet;

    public Sensors getLastState() {
        return state;
    }

    /**
     * Constructs an action set out of the given match set. 
     * @param current_state The current state
     * @param current_match_set The match set that related to the current state
     * @param action Action that was taken in that state
     * @see Classifier#addParent
     */
    public ActionClassifierSet(final Sensors current_state, final AppliedClassifierSet current_match_set, final int action) throws Exception {
        super(10);
        for (Classifier c : current_match_set.getClassifiers()) {
            if (c.getDirection() == action) {
                getClassifiers().add(c);
                c.addParent(this);
            }
        }

        this.state = current_state.clone();
        this.matchSet = current_match_set;
        this.action = action;
    }


    /**
     * The Genetic Discovery in XCS takes place here. If a GA takes place, two classifiers are selected
     * by roulette wheel selection, possibly crossed and mutated and then inserted.
     * @param main_classifier_set The main classifier set of the agent
     * @param gaTimestep The actual number of instances the XCS learned from so far.
     * @throws java.lang.Exception if there was an error crossing over both selected parents
     * @see MainClassifierSet#crossOverClassifiers
     */
    public void evolutionaryAlgorithm(MainClassifierSet main_classifier_set, long gaTimestep) throws Exception {

        // Don't do a GA if the theta_GA threshold is not reached, yet
        if (isEmpty() || gaTimestep - getTimeStampAverage() < Configuration.getThetaGA()) {
            return;
        }

        setTimeStamps(gaTimestep); // ?? Warum werden GA Timestamps aller auf die aktuelle Zeit gesetzt?
        // TODO sollte nicht z.B. Experience auch eine Rolle spielen?? mmmh

        // Select two parent Classifiers with roulette wheel selection

        double fitness_sum = getFitnessSum();
        Classifier cl1P = chooseRandomClassifier(Misc.nextDouble(), fitness_sum);
        Classifier cl2P = chooseRandomClassifier(Misc.nextDouble(), fitness_sum);

        /**
         * don't add classifier to this classifier set (which is an action set)
         * but to the actual population (classifier_set)
         * */
        main_classifier_set.crossOverClassifiers(cl1P, cl2P, state);
    }


//TODO Problem: irgendwie gibts identische Classifier... alle Einfügefunktionen prüfen ???
    
    /**
     * Sets the time stamp of all classifiers in the set to the current time. The current time 
     * is the number of exploration steps executed so far.
     * @param time The actual number of instances the XCS learned from so far.
     */
    private void setTimeStamps(long time) {
        for (Classifier c : getClassifiers()) {
            c.setGaTimestamp(time);
        }
    }

    /**
     * @return The average of the time stamps in the set.
     */
    private double getTimeStampAverage() throws Exception {
        return getTimeStampSum() / getNumerositySum();
    }

    /**
     * @return The sum of the time stamps of all classifiers in the set.
     */
    private double getTimeStampSum() throws Exception {
        double sum = 0.;
        for (Classifier c : getClassifiers()) {
            sum += c.getGaTimestamp() * c.getNumerosity();
        }
        return sum;
    }

    /**
     * Updates the values of a classifier according to the reward received
     * @param reward The amount of reward
     * @param max_prediction Max predicted value for the next match set
     * @param factor Weight of the change, change the appropriate values by a smaller amount if it was an external reward
     * @throws java.lang.Exception If there was an error with the reward or updating prediction, action set size and fitness
     */
    public void updateReward(double reward, double max_prediction, double factor) throws Exception {
        if (getClassifiers().isEmpty()) {
            return;
        }
        if(factor == 0.0) {
            return;
        }

        double[] accuracies = new double[size()];


        int i = 0;
        double accuracy_sum = 0.;
        for (Classifier c : getClassifiers()) {
            accuracies[i] = c.getAccuracy();
            if(Double.isNaN(accuracies[i])) {
                throw new Exception("" + Configuration.getAlpha() + " * pow(" + c.getPredictionError() + " / " + Configuration.getEpsilon0() + " , " + (-Configuration.getNu()));
            }
            accuracy_sum += accuracies[i] * c.getNumerosity();
        }
        double P = (reward + Configuration.getGamma() * max_prediction);
        if(Double.isNaN(P)) {
            throw new Exception("" + reward + " + " + max_prediction);
        }

        for (Classifier c : getClassifiers()) {
            c.increaseExperience(factor);
            // faster convergence if switched, only for simpler problems so don't
            c.updatePrediction(P, factor);
            c.updatePredictionError(P, factor);
            c.updateActionSetSize(getNumerositySum(), factor);
            c.updateFitness(accuracy_sum, accuracies[i], factor);
        }

        if (Configuration.isDoActionSetSubsumption()) {
            doActionSetSubsumption();
        }
    }

    /**
     * Executes action set subsumption. 
     * The action set subsumption looks for the most general subsumer classifier in the action set 
     * and subsumes all classifiers that are more specific than the selected one.
     * @see Classifier#subsumes
     * @see Classifier#addNumerosity
     */
    private void doActionSetSubsumption() throws Exception {
        // search most general subsumer
        Classifier subsumer = null;
        ArrayList<Classifier> non_subsumer = new ArrayList<Classifier>();
        
        for (Classifier c : getClassifiers()) {
            if(!c.isPossibleSubsumer()) {
                non_subsumer.add(c);
                continue;
            }
            if(subsumer == null) {
                subsumer = c;
            } else if(c.subsumes(subsumer)) {
                non_subsumer.add(subsumer);
                subsumer = c;
            }
        }
        
        // no most general subsumer found
        if(subsumer == null) {
            return;
        }
        
        //If a subsumer was found, subsume all more specific classifiers in the action set
        for(Classifier c : non_subsumer) {
            if(subsumer.subsumes(c)) {
                int num = c.getNumerosity();
                subsumer.addNumerosity(num);
                c.addNumerosity(-num);
            }
        }
    }

    /**
     * @return the matchSet
     */
    public AppliedClassifierSet getMatchSet() {
        return matchSet;
    }

    public int getAction() {
        return action;
    }
}
