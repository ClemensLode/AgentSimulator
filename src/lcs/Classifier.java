package lcs;

import agents.LCS_Agent;
import agent.Configuration;
import agent.Sensors;
import Misc.Misc;

import java.util.ArrayList;

/**
 * Main class for the XCS Classifier
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Classifier {

    /**
     * Difference between the actual reward and the predicted payoff
     */
    private double predictionError;
    /**
     * predicted payoff
     */
    private double prediction;

    // TODO problem mit possiblesubsumer Aufrufe, experience 0 sonst...
    /**
     * Number of time this classifier was updated
     */
    private double experience = 1.0;
    /**
     * The action set size estimate of the classifier.
     */
    private double actionSetSize;
    /**
     * The timestamp a genetic algorithm was executed on an action set this classifier was part of
     */
    private long gaTimestamp;
    /**
     * Accuracy of the classifier
     */
    private double fitness;
    /**
     * Action to take when the condition matches. Depends on the rotation of the condition.
     */
    private Action action;
    /**
     * The condition that has to match the current state in order for the classifier to be selected / activated
     */
    private Condition condition;
    /**
     * The number of micro-classifiers 
     */
    private int numerosity = 1;
    /**
     * list of all classifier sets that contain this classifier, important to 
     * keep track of the numerosity sum
     */
    private ArrayList<ClassifierSet> parents = new ArrayList<ClassifierSet>(1 + Configuration.getMaxStackSize());

// nicht set_size! Ansonsten sind die ActionSetSizes der ersten Classifier 1, 2, 3, 4, 5 statt 5, 5, 5, 5, 5
    // Action set size wird seperat zugewiesen, sobald die Zahl der hinzugefügten Classifier bekannt sind
    /**
     * Create a new classifier that covers the state and action
     * @param state The current state
     * @param action The action this classifier should cover
     * @param gaTimestamp The current time step
     * @see MainClassifierSet#coverAllValidActions
     * @throws java.lang.Exception if there was an error setting prediction, fitness or action set size
     */
    public Classifier(final Sensors state, final Action action, final long gaTimestamp, double action_set_size) throws Exception {
        setGaTimestamp(gaTimestamp);

        setPrediction(Configuration.getPredictionInitialization());
        setPredictionError(Configuration.getPredictionErrorInitialization());
        setFitness(Configuration.getFitnessInitialization());
        // will later (in the actionClassifierSet) be resetted to the actual value
        setActionSetSize(action_set_size);

        this.condition = new Condition(state);
        this.action = new Action(action);
    }

    /**
     * Constructs an identical XClassifier.
     * However, the experience of the copy is set to 0 and the numerosity is set to 1 since this is indeed 
     * a new individual in a population.
     * @param old_classifier The classifier we want to copy
     * @throws java.lang.Exception if there was an error setting prediction, fitness or action set size
     */
    public Classifier(final Classifier old_classifier) {

        setGaTimestamp(old_classifier.gaTimestamp);
        condition = old_classifier.condition.clone();
        action = new Action(old_classifier.action);

        prediction = old_classifier.prediction;
        predictionError = old_classifier.predictionError;
        // Here we should divide the fitness by the numerosity to get a accurate value for the new one!
        fitness = old_classifier.fitness / ((double) old_classifier.getNumerosity());
        actionSetSize = old_classifier.actionSetSize;
    }

    public Classifier clone(ClassifierSet cs) {
        Classifier new_cl = new Classifier(this);
        new_cl.addParent(cs);
        new_cl.fitness *= getNumerosity();
        new_cl.experience = experience;
        return new_cl;
    }

    /**
     * Crossing over with one or two points depending if there are obstacle sensors
     * @param childA First child with parental genetic data
     * @param childB Second child with parental genetic data
     */
    public static void crossOverClassifiers(Classifier childA, Classifier childB) {
        // combine condition and action parts of classifier to form strings
        final int[] childAStr = childA.getCondition().getData();
        final int[] childBStr = childB.getCondition().getData();

        // two fixed crossover points, dividing goal agent, agents and obstacles
        int crossoverIndex1 = Condition.AGENT_DISTANCE_INDEX;
        int crossoverIndex2 = Condition.OBSTACLE_DISTANCE_INDEX;

        // do the crossover
        int[] newChildAStr = new int[childAStr.length];
        int[] newChildBStr = new int[childBStr.length];

        for (int i = 0; i < crossoverIndex1; i++) {
            newChildAStr[i] = childAStr[i];
            newChildBStr[i] = childBStr[i];
        }

        switch (Misc.nextInt(3)) {
            case 0:
                for (int i = crossoverIndex1; i < crossoverIndex2; i++) {
                    newChildAStr[i] = childBStr[i];
                    newChildBStr[i] = childAStr[i];
                }
                for (int i = crossoverIndex2; i < childAStr.length; i++) {
                    newChildAStr[i] = childAStr[i];
                    newChildBStr[i] = childBStr[i];
                }
                break;
            case 1:
                for (int i = crossoverIndex1; i < crossoverIndex2; i++) {
                    newChildAStr[i] = childAStr[i];
                    newChildBStr[i] = childBStr[i];
                }
                for (int i = crossoverIndex2; i < childAStr.length; i++) {
                    newChildAStr[i] = childBStr[i];
                    newChildBStr[i] = childAStr[i];
                }
                break;
            case 2:
                for (int i = crossoverIndex1; i < crossoverIndex2; i++) {
                    newChildAStr[i] = childBStr[i];
                    newChildBStr[i] = childAStr[i];
                }
                for (int i = crossoverIndex2; i < childAStr.length; i++) {
                    newChildAStr[i] = childBStr[i];
                    newChildBStr[i] = childAStr[i];
                }
                break;
        }


        childA.condition.setData(newChildAStr);
        childB.condition.setData(newChildBStr);
    }

    /**
     * @param s The current sensor state
     * @return all absolute directions this classifier matches the sensor state
     * @see MainClassifierSet#coverAllActions
     * @see AppliedClassifierSet#AppliedClassifierSet
     */
    public ArrayList<Integer> getMatchingActions(Sensors s) {
        ArrayList<Integer> correct_list = new ArrayList<Integer>(1 + Action.MAX_DIRECTIONS);

        if (action.getDirection() == Action.NO_DIRECTION) {
            correct_list.add(new Integer(Action.NO_DIRECTION));
        } else {
            // determine the rotation list of the condition
            ArrayList<Integer> list = condition.getMatchingDirections(s);

            // apply the rotation list on the action
            for (Integer i : list) {
                correct_list.add(new Integer(action.getRotated(i)));
            }
        }
        return correct_list;
    }

    public void setMatchingActions(boolean[] actions, Sensors s) {
        if (action.getDirection() == Action.NO_DIRECTION) {
            actions[Action.NO_DIRECTION] = true;
        } else {
            // determine the rotation list of the condition
            ArrayList<Integer> list = condition.getMatchingDirections(s);

            // apply the rotation list on the action
            for (Integer i : list) {
                actions[action.getRotated(i)] = true;
            }
        }
    }

    public double getEgoFactor() {
        return getCondition().getEgoFactor(action.getDirection()) * this.getFitness() * this.getPrediction();
    }

    /**
     * @return true if the classifier is experienced and accurate enough
     * @see Configuration#getThetaSubsumer
     * @see Configuration#getEpsilon0
     */
    public boolean isPossibleSubsumer() throws Exception {
        if (getExperience() < Configuration.getThetaSubsumer() || getPredictionError() >= Configuration.getEpsilon0()) {
            return false;
        }
        return true;
    }

    /**
     * @param c The classifier we want to compare
     * @return true if this classifier is a subsumer of the other classifier
     * @see Classifier#isPossibleSubsumer
     * @see Classifier#isMoreGeneral
     */
    public boolean subsumes(Classifier c) {
        for (int rotation = 0; rotation < Action.MAX_DIRECTIONS; rotation++) {
            if (condition.isMoreGeneral(c.getCondition(), rotation)) {
                if (action.getDirection() == c.getAction().getRotated(rotation)) {
                    return true;
                }
            }
            if (!Configuration.isDoAllowRotation()) {
                return false;
            }
        }

        return false;
    }

    /**
     * Test all rotations to determine (phenotype) equality
     * @param c the classifier to compare to
     * @return true if both classifiers are phenotypical equal
     */
    public boolean equals(Classifier c) {
        for (int rotation = 0; rotation < Action.MAX_DIRECTIONS; rotation++) {
            if (condition.equals(c.getCondition(), rotation)) {
                if (action.getDirection() == c.getAction().getRotated(rotation)) {
                    return true;
                }
            }
            if (!Configuration.isDoAllowRotation()) {
                return false;
            }
        }
        return false;
    }

    /**
     * @see Configuration#delta
     * @param mean_fitness The mean fitness in the population.
     * @return Probability for deletion of the classifier.
     * @throws java.lang.Exception If the resulting deletion probability is out of range
     */
    public double getDelProp(double mean_fitness) throws Exception {
        double del_prop = 0.0;
        if ((getFitness() / ((double) getNumerosity())) >= Configuration.getDelta() * mean_fitness || (getExperience()) < Configuration.getThetaDel()) {
            del_prop = getActionSetSize() * ((double) getNumerosity());
        } else {
            del_prop = ((double) (getNumerosity() * getNumerosity())) * getActionSetSize() * mean_fitness / getFitness();
        }
        return del_prop;
    }

    /**
     * The accuracy is determined from the prediction error of the classifier using Wilson's 
     * power function as published in 'Get Real! XCS with continuous-valued inputs' (1999)
     * @return The accuracy of the classifier
     * @see Configuration#getEpsilon0
     * @see Configuration#getAlpha
     * @see Configuration#getNu
     */
    public double getAccuracy() {
        if (getPredictionError() <= Configuration.getEpsilon0()) {
            return 1.;
        } else {
            return Configuration.getAlpha() * Math.pow(getPredictionError() / Configuration.getEpsilon0(), -Configuration.getNu());
        }
    }

    /**
     * Updates the fitness of the classifier according to the relative accuracy.
     * @param accSum The sum of all the accuracies in the action set
     * @param accuracy The accuracy of the classifier.
     * @param factor Weight of the change, change fitness by a smaller amount if it was an external reward
     * @throws java.lang.Exception if the fitness is out of range
     * @see Configuration#beta
     */
    public void updateFitness(double accSum, double accuracy, double factor) throws Exception {
        setFitness(getFitness() + factor * Configuration.getBeta() * ((accuracy * (double) getNumerosity()) / accSum - getFitness()));
    }

    /**
     * Updates the prediction error of the classifier according to P.
     * @param P The actual Q-payoff value (actual reward + max of predicted reward in the following situation).
     * @param factor Weight of the change, change prediction error by a smaller amount if it was an external reward
     * @see Configuration#beta
     */
    public void updatePredictionError(double P, double factor) throws Exception {
        if (getExperience() < 1. / Configuration.getBeta()) {
            setPredictionError((getPredictionError() * (getExperience() - 1.0) + Math.abs(P - prediction)) / getExperience());
        } else {
            setPredictionError(getPredictionError() + factor * Configuration.getBeta() * (Math.abs(P - prediction) - getPredictionError()));
        }
    }

    /**
     * Updates the prediction of the classifier according to P.
     * @param P The actual Q-payoff value (actual reward + max of predicted reward in the following situation).
     * @param factor Weight of the change, change prediction by a smaller amount if it was an external reward
     * @see Configuration#beta
     */
    public void updatePrediction(double P, double factor) throws Exception {
        if (getExperience() < 1. / Configuration.getBeta()) {
            setPrediction((prediction * (getExperience() - 1.0) + P) / getExperience());
        } else {
            setPrediction(prediction + factor * Configuration.getBeta() * (P - prediction));
        }
    }

    /**
     * Updates the action set size to find the average of the action set sizes this classifier is part of
     * @param numerosity_sum Numerosity of the action classifier set in question
     * @param factor Weight of the change, change action set size by a smaller amount if it was an external reward
     * @throws java.lang.Exception If the action set size is out of bounds
     * @see Configuration#beta
     */
    public void updateActionSetSize(int numerosity_sum, double factor) throws Exception {
        if (Configuration.getBeta() * getExperience() < 1.0) {
            try {
                setActionSetSize((getActionSetSize() * (getExperience() - 1.0) + (double) numerosity_sum) / getExperience());
            } catch (Exception e) {
                throw new Exception(e + " : " + getActionSetSize() + " * (" + getExperience() + " - 1.0) + " + numerosity_sum + ") / " + getExperience());
            }

        } else {
            try {
                setActionSetSize(getActionSetSize() + factor * Configuration.getBeta() * (((double) numerosity_sum) - getActionSetSize()));
            } catch (Exception e) {
                throw new Exception(e + " : " + getActionSetSize() + " + " + factor + " * " + Configuration.getBeta() + " * (" + numerosity_sum + " - " + getActionSetSize() + ")");
            }

        }
    }

    public void testActionSetSize(double new_value) {
        if (actionSetSize == 0.0) {
            actionSetSize = new_value;
        }
    }

    /**
     * @param predicted_payoff The predicted average payoff of this classifier
     * @throws java.lang.Exception If the prediction is out of range
     */
    public void setPrediction(double predicted_payoff) throws Exception {
    //    if(predicted_payoff < 0.0 || predicted_payoff > (10.0 * LCS_Agent.MAX_REWARD * getNumerosity())) {
//            throw new Exception("Prediction out of range: " + predicted_payoff + " [num: " + getNumerosity() + "] from " + prediction + ")");
//        }
        prediction = predicted_payoff;
    }

    /**
     * Applies a niche mutation to the classifier. 
     * This method calls mutateCondition(state) and mutateAction(numberOfActions) and returns 
     * if at least one bit or the action was mutated.
     * @param state The current state
     * @param direction The direction of the action set
     * @return true if the condition was changed
     */
    public boolean applyMutation(Sensors state, int direction) {
        if (Configuration.getCoveringWildcardProbability() == 0.0) {
            return false;
        }
        /**
         * rotation denotes the margin by what the condition of the classifier
         * has to be rotated in order to match the actual action
         */
        // works for NO_DIRECTION, too, because in that case action.direction is equal to direction
        int rotation = (Action.MAX_DIRECTIONS + direction - this.action.getDirection()) % Action.MAX_DIRECTIONS;
        return condition.mutateCondition(state, rotation);
    }

    /**
     * Adds to the numerosity of the classifier.
     * @param num The added numerosity (can be negative!).
     * @see ClassifierSet#changeNumerositySum
     * @see ClassifierSet#removeClassifier
     */
    public void addNumerosity(int num) {
        numerosity += num;
        for (ClassifierSet p : parents) {
            p.changeNumerositySum(num);
            if (numerosity == 0) {
                p.removeClassifier(this);
            }
        }
    }

    /**
     * Register a parent to the classifier (important to update numerosity)
     * @param p The parent
     */
    public void addParent(ClassifierSet p) {
        parents.add(p);
        p.changeNumerositySum(numerosity);
    }

    /**
     * Unregister parent from the classifier (to free resources)
     * @param p The parent
     */
    public void removeParent(ClassifierSet p) {
        parents.remove(p);
    }

    /**
     * @param fitness The new fitness value
     * @throws java.lang.Exception If the fitness was out of range
     */
    public void setFitness(double fitness) throws Exception {
        if (fitness > 0.0 && fitness <= getNumerosity()) {
            this.fitness = fitness;
            if (this.fitness < 0.01) {
                this.fitness = 0.01;
            }
        } else {
            throw new Exception("Fitness out of range: " + fitness + " [num: " + getNumerosity() + "] from " + this.fitness + ")");
        }
    }

    /**
     * @param actionSetSize The new actionSetSize value
     * @throws java.lang.Exception If the actionSetSize was out of range
     */
    public void setActionSetSize(double actionSetSize) throws Exception {
        if (actionSetSize < 0.0 || actionSetSize > 100 * Configuration.getMaxPopSize()) {
            throw new Exception("Action set size out of range (" + actionSetSize + ")");
        }
        this.actionSetSize = actionSetSize;
    }

    /**
     * @param factor Increases the Experience of the classifier by this amount
     * @throws java.lang.Exception if the factor is out of range
     */
    public void increaseExperience(double factor) throws Exception {
        if (factor < 0.0 || factor > 1.0) {
            throw new Exception("Factor out of range (" + factor + ")");
        }
        experience += factor;
    }

    public void setPredictionError(double error) {
        this.predictionError = error;
    }

    public void setGaTimestamp(long gaTimestamp) {
        this.gaTimestamp = gaTimestamp;
    }

    public final int getDirection(int goal_direction) {
        return action.getRotated(goal_direction);
    }

    public final Action getAction() {
        return action;
    }

    public final Condition getCondition() {
        return condition;
    }

    public double getFitness() {
        return fitness;
    }

    public double getActionSetSize() {
        return actionSetSize;
    }

    /**
     * @return number of times the classifier was in the action set TODO
     */
    public double getExperience() throws Exception {
        /*if(experience == 0.0) {
        throw new Exception("Experience was 0.0 when called.");
        }*/
        return experience;
    }

    public long getGaTimestamp() {
        return gaTimestamp;
    }

    public int getNumerosity() {
        return numerosity;
    }

    public double getPredictionError() {
        return predictionError;
    }

    public double getPrediction() {
        return prediction;
    }

    @Override
    public String toString() {
        String output = new String();
        output += condition.toString();

        output += "-";
        output += action.toString();
        output += " :";

        output += " [Pr: " + Misc.round(getPrediction(), 0.01) + "]";
        output += " [PE: " + Misc.round(getPredictionError(), 0.01) + "]";
        output += " [Fi: " + Misc.round(getFitness(), 0.01) + "]";
        output += " [AS: " + Misc.round(getActionSetSize(), 0.01) + "]";
        output += " [Ti: " + getGaTimestamp() + "]";
        output += " [Nu: " + getNumerosity() + "]";
        output += " [Par=" + parents.size() + "]";


        return output;
    }
}
