package lcs;


import agent.*;
import Misc.*;
import java.util.ArrayList;

/**
 * Main classifier set of each agent, provides covering, crossing over, subsumation, adding/removing and relationship functionality
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class MainClassifierSet extends ClassifierSet {


    public MainClassifierSet(int n) throws Exception {
        super(n);
        if(Configuration.isRandomStart()) {
            for(int i = 0; i < Configuration.getMaxPopSize(); i++) {
                this.addClassifier(new Classifier());
            }
            /*addClassifier(new Classifier(0));
            addClassifier(new Classifier(1));
            addClassifier(new Classifier(2));
            addClassifier(new Classifier(3));*/
            //this.addClassifier(new Classifier(false));
            //this.addClassifier(new Classifier(true));
        }
    }

    /**
     * Add classifiers that match the current state to this set so that the
     * overall classifier set covers all possible actions
     * @param state The current sensor state
     * @param position The position of the agent whose classifier set is this
     * @param gaTime The current time step
     * @throws java.lang.Exception If there was an error creating or adding the classifiers
     */
    public void coverAllValidActions(final Sensors state, final Point position, final long gaTime) throws Exception {
        boolean[] action_covered = new boolean[Action.MAX_DIRECTIONS];

        for(int i = 0; i < action_covered.length; i++) {
            action_covered[i] = false;
        }
        // check all classifiers that match to the current sensor state
        // rotated variants will be tested, too

        for (Classifier c : getClassifiers()) {
            action_covered[c.getDirection()] = true;
        }
        /**
         * loop until all actions are covered 
         */
        boolean all_actions_covered;
        do {
            for (int i = 0; i < action_covered.length; i++) {
                if (!action_covered[i]) {
                    Classifier newCl = new Classifier(state, new Action(i), gaTime, getNumerositySum()+1);
                    addClassifier(newCl);
                    // newCl.setMatchingActions(action_covered, state); TODO maybe optimize....
                    // wenn ein neuer Classifier spaeter gepruefte actions schon abdeckt, muessen insgesamt weniger Classifier hinzugefuegt werden
                }
            }

            /**
             * Test which actions are covered
             */
            for (int i = 0; i < action_covered.length; i++) {
                action_covered[i] = false;
            }
            for (Classifier c : getClassifiers()) {
                if(c.isMatchingState(state)) {
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
     * Creates and adds children to the classifier set, constructed out of the parents
     * @param cl1P First parent classifier
     * @param cl2P Second parent classifier
     * @param state Current sensor state 
     * @throws java.lang.Exception For various reasons (error creating classifier, setting fitness and prediction, subsumation and addition of classifiers)
     */
    protected void crossOverClassifiers(Classifier cl1P, Classifier cl2P, Sensors state) throws Exception {
        // children
        Classifier cl1 = new Classifier(cl1P);
        Classifier cl2 = new Classifier(cl2P);

        Classifier.crossOverClassifiers(cl2, cl2);

        cl1.applyMutation(state);
        cl2.applyMutation(state);

        cl1.setPrediction((cl1.getPrediction() + cl2.getPrediction()) / 2.0);
        cl1.setPredictionError(Configuration.getPredictionErrorReduction() * (cl1.getPredictionError() + cl2.getPredictionError()) / 2.0);
        cl1.setFitness(Configuration.getFitnessReduction() * (cl1.getFitness() + cl2.getFitness()) / 2.0);

        cl2.setPrediction(cl1.getPrediction());
        cl2.setPredictionError(cl1.getPredictionError());
        cl2.setFitness(cl1.getFitness());

        /**
         * Inserts both discovered classifiers keeping the maximal size of the population and possibly doing GA subsumption.
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
     * Tries to subsume a classifier in the parents. 
     * If no subsumption is possible it tries to subsume it in the current set. 
     * If no subsumption is possible the classifier is simply added to the population considering 
     * the possibility that there exists an identical classifier.
     * @param cl Classifier in question
     * @param cl1P First parent
     * @param cl2P Second parent
     * @throws java.lang.Exception if there was an error subsuming the classifier
     * @see Classifier#subsumes
     */
    protected void subsumeClassifier(Classifier cl, Classifier cl1P, Classifier cl2P) throws Exception {
        if (cl1P != null && cl1P.isPossibleSubsumer() && cl1P.subsumes(cl)) {
            cl1P.addNumerosity(1);
        } else if (cl2P != null && cl2P.isPossibleSubsumer() && cl2P.subsumes(cl)) {
            cl2P.addNumerosity(1);
        } else {
            //Open up a new Vector in order to chose the subsumer candidates randomly
            ArrayList<Classifier> choices = new ArrayList<Classifier>();
            for (Classifier c : getClassifiers()) {
                if(c.isPossibleSubsumer() && c.subsumes(cl)) {
                    choices.add(c);
                }
            }

            //If no subsumer was found, add the classifier to the population
            if (choices.isEmpty()) {
                addClassifier(cl);
            } else {
                choices.get(Misc.nextInt(choices.size())).addNumerosity(1);
            }
        }
    }

    /** 
     * Adds a classifier to the set and increases the numerositySum value accordingly.
     * @param classifier The to be added classifier.
     * @throws java.lang.Exception if there was an error removing classifiers because of a too large classifier set
     */
    public void addClassifier(Classifier classifier) throws Exception {
        Classifier identical = getIdenticalClassifier(classifier);
        if (identical != null) {
            identical.addNumerosity(classifier.getNumerosity());
        } else {
            getClassifiers().add(classifier);
            classifier.addParent(this);
        }

        while (getNumerositySum() > Configuration.getMaxPopSize() + Action.MAX_DIRECTIONS) {
            Classifier c = getDeleteCandidate();
            removeMicroClassifier(c);
        }
    }

    /**
     * @param c The macro classifier we want to remove a micro classifier from
     * @throws java.lang.Exception if the classifier was already empty
     */
    protected void removeMicroClassifier(Classifier c) throws Exception {
        if (c == null) {
            return;
        }
        if (c.getNumerosity() > 0) {
            c.addNumerosity(-1);
        } else {
            throw new Exception("Numerosity of Microclassifier was already 0 when we tried to remove it.");
        }
    }

    /**
     * Looks for an identical classifier in the population.
     * @param newCl The new classifier.
     * @return Returns the identical classifier if found, null otherwise.
     * @see Classifier#equals
     */
    private Classifier getIdenticalClassifier(Classifier newCl) {
        for (Classifier c : getClassifiers()) {
            if (c.equals(newCl)) {
                return c;
            }
        }
        return null;
    }

    /**
     * @return A randomly selected classifier from the set (using roulette wheel selection)
     * @throws java.lang.Exception When there was an error selecting from the roulette wheel
     * @see Classifier#getDelProp
     */
    private Classifier getDeleteCandidate() throws Exception {
        // get average fitness of classifiers
        double mean_fitness = getFitnessSum() / (double) getNumerositySum();
        double sum = 0.;

        for (Classifier c : getClassifiers()) {
            sum += c.getDelProp(mean_fitness);
        }
        
        // roulette
        double choicePoint = sum * Misc.nextDouble();

        sum = 0.;
        for (Classifier c : getClassifiers()) {
            sum += c.getDelProp(mean_fitness);
            if (sum > choicePoint) {
                return c;
            }
        }
        
        throw new Exception("Error finding proper roulette wheel selection");
    }

    /**
     * Relation of this classifier set (the active agent classifier set,
     * e.g. the set that received a reward) to another classifier set
     * @param other The other set we want to compare with
     * @return degree of relationship (0.0 - 1.0)
     */
    public double checkDegreeOfRelationship(final MainClassifierSet other) throws Exception {
        double degree = 0.0;
        int size = 0;
        ArrayList<Classifier> matched = new ArrayList<Classifier>();

        for (Classifier c : getClassifiers()) {
            if(!c.isPossibleSubsumer()) {
                continue;
            }

            Classifier cl = other.getBestIdenticalClassifier(matched, c);
            if (cl != null) {
                matched.add(cl);



                double div = c.getPrediction();
                if(cl.getPrediction() > div) {
                    div = cl.getPrediction();
                }
                if(div != 0.0) {
                    double difference =
                            1.0 - Math.abs(c.getFitness() * c.getPrediction() - cl.getFitness() * cl.getPrediction()) / div;
                    if(difference > 1.0) {
                        difference = 1.0;
                    } else
                    if(difference < 0.0) {
                        difference = 0.0;
                    }
                    degree += difference;
                }
            }
// TODO            size hoch?
            size++;
        }

        if(size == 0) {
            return 0.0;
        }

        degree /= (double)size;

        if (degree >= 0.01 && degree <= 1.0) {
            return degree;
        } else {
            return 0.0;
        }
    }

    /**
     * Relation of this classifier set (the active agent classifier set,
     * e.g. the set that received a reward) to another classifier set
     * @param other The other set we want to compare with
     * @return degree of relationship (0.0 - 1.0)
     */
    public double checkDegreeOfRelationshipNew(final MainClassifierSet other) throws Exception {
        double degree = 0.0;
        int size = 0;
        ArrayList<Classifier> matched = new ArrayList<Classifier>();

        for (Classifier c : getClassifiers()) {
            if (c.getExperience() < Configuration.getThetaSubsumer()) {
                continue;
            }
//            if(!c.isPossibleSubsumer()) {
//                continue;
//            }

            Classifier cl = other.getBestIdenticalClassifierNew(matched, c);
            if (cl != null) {
                matched.add(cl);

                double div = c.getPrediction();
                if(cl.getPrediction() > div) {
                    div = cl.getPrediction();
                }
                if(div != 0.0) {
                    double difference =
                            1.0 - Math.abs(c.getPrediction() - cl.getPrediction()) / div;
                    //difference *= c.getFitness() * cl.getFitness();
                    if(difference > 1.0) {
                        difference = 1.0;
                    } else
                    if(difference < 0.0) {
                        difference = 0.0;
                    }
                    degree += difference;
                }
            }
            size++;
        }

        if(size == 0) {
            return 0.0;
        }

        degree /= (double)size;

        if (degree >= 0.01 && degree <= 1.0) {
            return degree;
        } else {
            return 0.0;
        }
    }


    // alle SITUATIONEN durchlaufen und Verhalten vergleichen!
    // Configuration Option für possible subsumer einführen
    //

    /**
     * Relation of this classifier set (the active agent classifier set,
     * e.g. the set that received a reward) to another classifier set
     * @param other The other set we want to compare with
     * @return degree of relationship (0.0 - 1.0)
     */
    public double checkEgoisticDegreeOfRelationship(final MainClassifierSet other) throws Exception {
        if(ego_factor == 0.0 || other.ego_factor == 0.0) {
            return 0.0;
        }
        return(1.0 - Math.abs(ego_factor - other.ego_factor)*Math.abs(ego_factor - other.ego_factor));//getEgoisticFactor() - other.getEgoisticFactor()));
    }

    private double ego_factor = 0.0;

    public void updateEgoFactor() throws Exception {
        if(Configuration.getExternalRewardMode() == Configuration.REWARD_EGOISM) {
            ego_factor = getEgoisticFactor();
        }
    }

    public double getEgoisticFactor() throws Exception {
        double factor = 0.0;
        double pred_sum = 0.0;
        for(Classifier c : getClassifiers()) {
//            if(!c.isPossibleSubsumer()) {
//                continue;
//            }
            // ignore behavior if goal agent is near
//            if(c.getCondition().isGoalCondition()) {
//                continue;
 //           }
            factor += c.getEgoFactor();
            pred_sum += c.getFitness() * c.getPrediction();
        }
        if(pred_sum > 0.0) {
            factor /= pred_sum;
        } else {
            factor = 0.0;
        }

        return factor;
    }

    /**
     * Looks for an identical classifier in the population.
     * @param newCl The new classifier.
     * @return Returns the identical classifier if found, null otherwise.
     */
    private Classifier getBestIdenticalClassifier(ArrayList<Classifier> already_matched, Classifier newCl) throws Exception {

// TODO        da auch noch possible subsumer rein?

        ArrayList<Classifier> identical_classifiers = new ArrayList<Classifier>();
        for (Classifier c : getClassifiers()) {
            if (c.equals(newCl)) {
                identical_classifiers.add(c);
            }
        }
        identical_classifiers.removeAll(already_matched);
        if (identical_classifiers.isEmpty()) {
            return null;
        } else {
            double dist = -1.0;
            Classifier best = null;
            for (Classifier c : identical_classifiers) {
                double temp_dist = Math.abs(newCl.getFitness() - c.getFitness());
// TODO                fitness * prediction?
                if (temp_dist < dist || dist == -1.0) {
                    dist = temp_dist;
                    best = c;
                }
            }
            return best;
        }
    }

    /**
     * Looks for an identical classifier in the population.
     * @param newCl The new classifier.
     * @return Returns the identical classifier if found, null otherwise.
     */
    private Classifier getBestIdenticalClassifierNew(ArrayList<Classifier> already_matched, Classifier newCl) throws Exception {
        ArrayList<Classifier> identical_classifiers = new ArrayList<Classifier>();
        for (Classifier c : getClassifiers()) {
            if (c.equals(newCl)) {
                identical_classifiers.add(c);
            }
        }
        identical_classifiers.removeAll(already_matched);
        if (identical_classifiers.isEmpty()) {
            return null;
        } else {
            double best_fitness = -1.0;
            Classifier best = null;
            for (Classifier c : identical_classifiers) {
                if(c.getFitness() > best_fitness) {
                    best_fitness = c.getFitness();
                    best = c;
                }
            }
            return best;
        }
    }

    /**
     * Looks for an identical classifier in the population.
     * @param newCl The new classifier.
     * @return Returns the identical classifier if found, null otherwise.
     */
    private Classifier getBestIdenticalClassifier(Classifier newCl) throws Exception {
        ArrayList<Classifier> identical_classifiers = new ArrayList<Classifier>();
        for (Classifier c : getClassifiers()) {
            if (c.equals(newCl)) {
                identical_classifiers.add(c);
            }
        }
        if (identical_classifiers.isEmpty()) {
            return null;
        } else {
            double best_fitness = -1.0;
            Classifier best = null;
            for (Classifier c : identical_classifiers) {
                if(c.getFitness() > best_fitness) {
                    best_fitness = c.getFitness();
                    best = c;
                }
            }
            return best;
        }
    }

    public void exchangeRules(MainClassifierSet other) throws Exception {
        Classifier c1 = chooseRandomClassifier(Misc.nextDouble(), getFitnessSum());
        if(c1 != null) {
            Classifier ic2 = other.getBestIdenticalClassifier(c1);
            if(ic2 == null || ic2.getFitness() < c1.getFitness()) {
                other.addClassifier(new Classifier(c1));
            }
        }

        Classifier c2 = other.chooseRandomClassifier(Misc.nextDouble(), other.getFitnessSum());
        if(c2 != null) {
            Classifier ic1 = getBestIdenticalClassifier(c2);
            if(ic1 == null || ic1.getFitness() < c2.getFitness()) {
                addClassifier(new Classifier(c2));
            }
        }

    }
}
