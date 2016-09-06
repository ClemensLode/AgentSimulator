package lcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class provides basic set/get/creation/add/remove routines, all higher
 * functions are in MainClassifierSet, ActionClassifierSet and AppliedClassifierSet
 * This class cannot be instanciated
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class ClassifierSet {

    /**
     * The currently numer of micro classifiers held in this classifier set
     */
    private int numerositySum = 0;
    
    /**
     * The main set of classifiers
     */
    private ArrayList<Classifier> classifiers = new ArrayList<Classifier>();

    /**
     * May not be instanciated
     */
    private ClassifierSet() {
    }

    protected ClassifierSet(int n) {
        classifiers = new ArrayList<Classifier>(n);
    }
    
    /**
     * Frees up any reference to this classifier set in any classifier
     * @see Classifier#removeParent
     */
    public void destroy() {
        for(Classifier c : classifiers) {
            c.removeParent(this);
        }
    }
    
    /**
     * removes an classifier from the classifier set
     * ONLY be called by the classifier itself!
     * @param c The classifier
     */
    public void removeClassifier(Classifier c) {
        classifiers.remove(c);
    }

    /**
     * @return the sum of the fitnesses of all classifiers in the set.
     */
    protected double getFitnessSum() throws Exception {
        double sum = 0.;

        for (Classifier c : getClassifiers()) {
            sum += c.getFitness();
        }

        return sum;
    }

    public ClassifierSet clone_it() throws Exception {
        ClassifierSet cs = new ClassifierSet();
        for(Classifier c : getClassifiers()) {
            cs.classifiers.add(c.clone(cs));
        }
        return cs;
    }

    /**
     * choose a random classifier from the set, with a roulette selection
     * @param fitness_sum The sum of all fitness values
     * @return random classifier, classifiers with higher fitness are more probable
     */
    public Classifier chooseRandomClassifier(double random, double fitness_sum) throws Exception {
        double random_fitness = random * fitness_sum;
        double fitness = 0.0;
        for (Classifier c : getClassifiers()) {
            fitness += c.getFitness();
            if (fitness >= random_fitness) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * @return the classifier set
     */
    protected final ArrayList<Classifier> getClassifiers() {
        return classifiers;
    }
    

    /**
     * @return true if classifier set contains no classifiers
     */
    public boolean isEmpty() {
        return classifiers.isEmpty();
    }

    /**
     * @return number of contained classifiers
     */
    public int size() {
        return classifiers.size();
    }

    /**
     * @return Number of micro classifiers in this classifier set
     */    
    public int getNumerositySum() {
        return numerositySum;
    }

    /**
     * sanity check
     * @throws java.lang.Exception
     */
    public void checkNumerositySum() throws Exception {
        int num_sum = 0;
        for(Classifier c : classifiers) {
            num_sum += c.getNumerosity();
        }
        if(num_sum != numerositySum) {
            throw new Exception("Actual numerosity sum: " + num_sum + " != Saved numerosity sum: " + numerositySum + " |||| "  + this.toString());
        }        
    }

    /**
     * Changes the numerosity sum
     * @param n The difference
     */
    public void changeNumerositySum(int n) {
        numerositySum += n;
    }    
    
    private double getAveragePrediction() throws Exception {
        double prediction = 0.0;
        for (Classifier c : getClassifiers()) {
            prediction += c.getPrediction();
        }
        prediction /= (double)getNumerositySum();
        return prediction;
    }

    public double getAverageFitness() throws Exception {
        double avg_fitness = 0.0;
        for (Classifier c : getClassifiers()) {
            avg_fitness += c.getFitness();
        }
        avg_fitness /= (double)getNumerositySum();
        return avg_fitness;
    }

    private double getAverageTimestamp() {
        double timestamp = 0.0;
        for (Classifier c : getClassifiers()) {
            timestamp += (double) c.getGaTimestamp();
        }
        timestamp /= (double)getNumerositySum();
        return timestamp;
    }    
    
    @Override
    public String toString() {
        String output = new String();
        output += "   > Averages:\n";
        try{
        output += "   > Pre: " + getAveragePrediction() + " Fit: " + getAverageFitness() + " Tss: " + getAverageTimestamp() + " Num: " + getNumerositySum() + " Size: " + size() + "\n";

        Collections.sort(classifiers, new Comparator() {

            public int compare(Object a, Object b) {
                try{
                double diff = ((Classifier) a).getFitness() - ((Classifier) b).getFitness();
                if (diff < 0) {
                    return 1;
                } else if (diff > 0) {
                    return -1;
                } else {
                    return 0;
                }
                }catch(Exception e){}
                return 0;
            }
        });  
        for (Classifier c : getClassifiers()) {
            //if(c.getFitness() == 0.01) {
            //    continue;
            //}
            output += "     - " + c.toString() + "\n";
        }
        }catch(Exception e) {}
        return output;
    }
    
}