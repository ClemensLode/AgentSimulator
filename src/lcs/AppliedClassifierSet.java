package lcs;

import agent.*;
import Misc.*;
import java.util.ArrayList;

/**
 * The correctly rotated classifiers with the absolute direction
 * Only used to determine the action
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class AppliedClassifierSet {

    /**
     * The actual sets of applied classifiers (i.e. pairs of the actual direction and the original classifier)
     */
    private ArrayList<AppliedClassifier> classifiers = new ArrayList<AppliedClassifier>();
    
    /**
     * The cache for the prediction fitness product sum for each direction
     */
    private double[] predictionFitnessProductSum = new double[Action.MAX_ACTIONS];
    
    /**
     * The cache for the sum of the fitnesses of classifiers for each direction
     */
    private double[] fitnessSum = new double[Action.MAX_ACTIONS];

    /**
     * If we have matching classifiers in the original population then include it here so that they are not generated twice
     * Include all classifiers and save their action
     * @param state The current sensor state of the agent
     * @param parent_pop The classifier set of the agent
     * @see Classifier#getMatchingActions
     */
    public AppliedClassifierSet(final Sensors state, ClassifierSet parent_pop) {
        for (Classifier c : parent_pop.getClassifiers()) {
            ArrayList<Integer> action_list = c.getMatchingActions(state);
            for (Integer i : action_list) {
                classifiers.add(new AppliedClassifier(c, i));
            }
        }
        //TODO doch Originalversion nehmen... dort aber mit richtigem Wert evtl...kA
        /**
         * needs to be executed because choseAbsoluteDirection is called immediately after
         * creation while 'getBestValue' will be called in a later time step
         * In the mean-time the fitness will have changed
         */
        initValues();
    }

    /**
     * @return the highest value in the prediction array.
     */
    public double getBestValue() {
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
    

    /**
     * Choses an action from the matching set
     * @param do_explore Whether to concentrate on random classifiers or the best classifiers
     * @return the winning action
     */
    public int chooseAbsoluteDirection(boolean do_explore) {
        // choose random classifier from classifierArray randomly by fitness * prediction
        if (do_explore) {
            return rouletteActionWinner();
        } else {
            // choose best classifier determined by fitness * prediction
            return bestActionWinner();
        }
    }

    /**
     * @return The private classifier set
     */
    public final ArrayList<AppliedClassifier> getClassifiers() {
        return classifiers;
    }    
    
    /**
     * re-calculates the fitness and predictionFitnessProduct cache
     */
    private void initValues() {
        for (int i = 0; i < Action.MAX_ACTIONS; i++) {
            predictionFitnessProductSum[i] = 0.;
            fitnessSum[i] = 0.;
        }
        for (AppliedClassifier c : classifiers) {
            predictionFitnessProductSum[c.getAbsoluteDirection()] += c.getOriginalClassifier().getPrediction() * c.getOriginalClassifier().getFitness();
            fitnessSum[c.getAbsoluteDirection()] += c.getOriginalClassifier().getFitness();
        }

        for (int i = 0; i < Action.MAX_ACTIONS; i++) {
            if (fitnessSum[i] != 0.0) {
                predictionFitnessProductSum[i] /= fitnessSum[i];
            } else {
                predictionFitnessProductSum[i] = 0;
            }
        }
    }
    

    /**
     * Selects an action randomly. 
     * The function assures that the chosen action is represented by at least one classifier.
     * @return The action id
     */
    private int randomActionWinner() {
        int ret = 0;
        do {
            ret = Misc.nextInt(Action.MAX_ACTIONS);
        } while (fitnessSum[ret] == 0.0);
        return ret;
    }

    /**
     * Selects the action in the prediction array with the best value.
     * @return The action id
     */
    private int bestActionWinner() {
        int ret = 0;
        for (int i = 1; i < predictionFitnessProductSum.length; i++) {
            if (predictionFitnessProductSum[ret] < predictionFitnessProductSum[i]) {
                ret = i;
            }
        }
        return ret;
    }

    /**
     * Selects an action in the prediction array by roulette wheel selection.
     * @return -1 if no winner could be determined, the action id otherwise
     */
    private int rouletteActionWinner() {
        double bidSum = 0.;
        for (int i = 0; i < Action.MAX_ACTIONS; i++) {
            bidSum += predictionFitnessProductSum[i];
        }

        bidSum *= Misc.nextDouble();

        double bidC = 0.;
        for (int i = 0; i < Action.MAX_ACTIONS; i++) {
            bidC += predictionFitnessProductSum[i];
            if (bidC >= bidSum) {
                return i;
            }
        }
        return -1;
    }

    public boolean isEmpty() {
        return classifiers.isEmpty();
    }

    @Override
    public String toString() {
        String output = new String();
        for (AppliedClassifier c : getClassifiers()) {
            output += "     - " + c.toString() + "\n";
        }
        return output;
    }
}
