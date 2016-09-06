/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Iterator;

import java.io.*;
/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class ClassifierSet {


    private ArrayList<Classifier> classifiers;
    
    public void addClassifier(Classifier c) { classifiers.add(c); }    
    public void removeClassifier(Classifier c) { classifiers.remove(c); }    
    public Iterator<Classifier> getSetIterator() { return classifiers.iterator(); }    
    public boolean isEmpty() { return classifiers.isEmpty(); }    
    public int size() { return classifiers.size(); }    
    
    public void clear() {classifiers.clear();}
    
    public ClassifierSet() {
        classifiers = new ArrayList<Classifier>();
    }
    
    public Classifier chooseClassifier(ClassifierSet matchSet, final Grid grid, final int id, final Point p, final long gaTimestep) throws Exception {
        for(int i = 0; i < classifiers.size(); i++) {
            if(classifiers.get(i).isMatched(grid, id, p)) {
                matchSet.addClassifier(classifiers.get(i));
            }
        }
        
        if(matchSet.isEmpty()) {
            // choose random classifier from all classifiers  TODO, evtl auch neuen Classifier kreieren oder alten verallgemeinern...
            Classifier c = Classifier.createCoveringClassifier(grid, Configuration.getCoveringWildcardProbability(), id, p, gaTimestep);
            matchSet.addClassifier(c);
            addClassifier(c);
        }
        
        // choose random classifier from classifierArray randomly by fitness
        return matchSet.chooseRandom();
    }
    
    public Classifier chooseRandom() {
        int random_fitness = Misc.nextInt(1 + (int)(getSumFitness()));
        double fitness = 0.0;
        for(Classifier c : classifiers) {
            fitness += c.getFitness();
            if(fitness >= random_fitness) {
                return c;
            }
        }
        return null;
    }
    
    public void loadClassifiersFromFile(String file_name) {
        // load old settings if file exists
        File my_file = new File(file_name);
        if (my_file.exists()) {
            int n = 0;
            try {
                BufferedReader p = new BufferedReader(new FileReader(my_file.getAbsoluteFile()));
                
                String line = null;
                while((line = p.readLine()) != null) {
                    if(line.isEmpty() || line.charAt(0) == '$') {
                        n++;
                        continue;
                    }
                    String[] data_string = line.split("[, .]+");
                    if(data_string.length != Action.ACTION_SIZE + Condition.CONDITION_SIZE) { // TODO p, err?
                        p.close();
                        throw new NumberFormatException("loadClassifiersFromFile(): Number of entries do not match action size + condition size.");
                    }
                    int condition_data[] = new int[Condition.CONDITION_SIZE];
                    int action_data[] = new int[Action.ACTION_SIZE];
                    
                    int i = 0;
                    for(String s:data_string) {
                        if(i < Condition.CONDITION_SIZE) {
                            if(s.charAt(0) == '#') {
                                condition_data[i] = Condition.WILDCARD;
                            } else {
                                condition_data[i] = Integer.valueOf(s);
                            }
                        } else {
                            action_data[i - Condition.CONDITION_SIZE] = Integer.valueOf(s);
                        }
                        i++;
                    }
                    Classifier c = new Classifier(condition_data, action_data, 0);
                    
                    this.addClassifier(c);
                    
                    n++;
                }
                p.close();
            } catch (IOException e) {
                System.out.println("loadClassifiersFromFile(): IO Exception: Error " + e + " reading from file " + my_file.getAbsoluteFile());
            } catch (NumberFormatException e) {
                System.out.println("loadClassifiersFromFile(): NumberFormatException: Error " + e + " (line " + n + ") reading from file " + my_file.getAbsoluteFile());
            }
        }        
    }
    
    // action selection - exploit
    public Classifier getHighestAveragePayoffClassifier() {
        Classifier maxClassifier = null;
        double maxPayoff = 0.0;        
    	for (Classifier c : classifiers){
    		if(c.getPredictedPayoff() > maxPayoff) {
    			maxPayoff = c.getPredictedPayoff();
    			maxClassifier = c;
    		}
        }        
        return maxClassifier;
    }    
    
        // roulette selection for GA
    public double getSumFitness() {
        double sum_fitness = 0.0;
        for (Classifier c : classifiers){
            sum_fitness += c.getFitness();
        }
        return sum_fitness;
    }
    
    // roulette wheel selection for deletion
    public double getSumNicheSize() {
        double sumAvgNicheSize = 0.0;
        for (Classifier c : classifiers){
            sumAvgNicheSize += c.getAvgNicheSize();
        }
        return sumAvgNicheSize;
    }    
    
    public int checkDegreeOfRelationship(final ClassifierSet other) {
        int degree = 0;
        ArrayList<Classifier> a = new ArrayList<Classifier>(classifiers);
        ArrayList<Classifier> b = new ArrayList<Classifier>(other.classifiers);

        if(a.size() > b.size()) {
            for(Classifier c : b) {
                Classifier d = findMostRelatedClassifier(a, c);
                degree += d.getDegreeOfRelationship(c);
                b.remove(c);
                a.remove(d);                
            }
        } else {
            for(Classifier c : a) {
                Classifier d = findMostRelatedClassifier(b, c);
                degree += d.getDegreeOfRelationship(c);
                b.remove(d);
                a.remove(c);
            }            
        }
        return degree;
    }
    
    public Classifier getRouletteSelectedClassifier() {     
        double roulettePoint = getSumFitness() * Misc.nextDouble();
        double currentRoulettePoint = 0.0;       
        Classifier classifier;
        for (Iterator<Classifier> i = getSetIterator(); i.hasNext();) {
            classifier = i.next();
            currentRoulettePoint = currentRoulettePoint + classifier.getFitness();
            if(roulettePoint < currentRoulettePoint) {
                return classifier;
            }
        }
        // uh oh bad roulette wheel
        return null;
    }  

    public ArrayList<Classifier> getRouletteSelectedClassifiers(int n) {     
        ArrayList<Classifier> chosen_classifiers = new ArrayList<Classifier>();
        ClassifierSet temp = new ClassifierSet();
        temp.classifiers.addAll(classifiers);

        for(int j = 0; j < n; j++) {
            double roulettePoint = getSumFitness() * Misc.nextDouble();
            double currentRoulettePoint = 0.0;       
            Classifier classifier;
            for (Iterator<Classifier> i = getSetIterator(); i.hasNext();) {
                classifier = i.next();
                currentRoulettePoint = currentRoulettePoint + classifier.getFitness();
                if(roulettePoint < currentRoulettePoint) {
                    chosen_classifiers.add(classifier);
                    temp.removeClassifier(classifier);
                    break;
                }
            }
        }
        return chosen_classifiers;
    }  
    
    public Classifier getInverseRouletteSelectedClassifier() {     
        double roulettePoint = getSumNicheSize() * Misc.nextDouble();
        double currentRoulettePoint = 0.0;       
        Classifier classifier;
        for (Iterator<Classifier> i = getSetIterator(); i.hasNext();) {
            classifier = i.next();
            currentRoulettePoint = currentRoulettePoint + classifier.getFitness();
            if(roulettePoint < currentRoulettePoint) {
                return classifier;
            }
        }
        return null;
    }
    
    
    public ArrayList<Classifier> getInverseRouletteSelectedClassifiers(int n, ArrayList<Classifier> already_chosen) {     
        ArrayList<Classifier> chosen_classifiers = new ArrayList<Classifier>();
        ClassifierSet temp = new ClassifierSet();
        temp.classifiers.addAll(classifiers);
        temp.classifiers.removeAll(already_chosen);

        for(int j = 0; j < n; j++) {
            double roulettePoint = getSumNicheSize() * Misc.nextDouble();
            double currentRoulettePoint = 0.0;       
            Classifier classifier;
            for (Iterator<Classifier> i = temp.getSetIterator(); i.hasNext();) {
                classifier = i.next();
                currentRoulettePoint = currentRoulettePoint + classifier.getFitness();
                if(roulettePoint < currentRoulettePoint) {
                    chosen_classifiers.add(classifier);
                    temp.removeClassifier(classifier);
                    break;
                }
            }
        }
        return chosen_classifiers;
    }      
    
    public void evolutionaryAlgorithm(double elite, double mu) throws Exception {
        int n = (int)(this.size() * elite);
        ArrayList<Classifier> selected_classifiers = getRouletteSelectedClassifiers(n);
        if(selected_classifiers.size() * 2 > classifiers.size()) {
            throw new Exception("ClassifierSet.evolutionaryAlgorithm() : elite parameter out of range (" + elite + ").");
        }
        ArrayList<Classifier> replaced_classifiers = getInverseRouletteSelectedClassifiers(n, selected_classifiers);
        if(replaced_classifiers.size() * 2 > classifiers.size()) {
            throw new Exception("ClassifierSet.evolutionaryAlgorithm() : elite parameter out of range (" + elite + ").");
        }        
        if(replaced_classifiers.size() != selected_classifiers.size()) {
            throw new Exception("ClassifierSet.evolutionaryAlgorithm() : number of selected and number of replaced classifiers do not match (" + selected_classifiers.size() + "!=" + replaced_classifiers.size() + ").");
        }
        Iterator<Classifier> i = replaced_classifiers.listIterator();
        for(Classifier c : selected_classifiers) {
            Classifier j = i.next();
            j.copyAndMutate(c, mu);
        }
    }
    
    /**
     * 
     * @param chi crossover probability
     * @param mu mutation probability
     */
    public void panmicticGeneticAlgorithm(double chi, double mu) {
        Classifier mom = getRouletteSelectedClassifier();
        Classifier dad = getRouletteSelectedClassifier();
        Log.log("------------ GENETIC ALGORITHM");
        Log.log("mom = " + mom);
        Log.log("dad = " + dad);
        
        // clone the classifiers      
        Classifier childA = mom.clone();
        Classifier childB = dad.clone();        
      
        // crossover chi
        if(Misc.nextDouble() < chi) {
            Log.log("Performing crossover...");
            Classifier.crossoverClassifiers(childA, childB);
        }       
        Log.log("Before mutation...");
        Log.log("childA = " + childA);
        Log.log("childB = " + childB);
        
        // mutation mu          
        childA.mutate(mu);
        childB.mutate(mu);
        
        Log.log("After mutation...");
        Log.log("childA = " + childA);
        Log.log("childB = " + childB);
        
        // remove weakest two from population first?
        Classifier r1 = getInverseRouletteSelectedClassifier();
        removeClassifier(r1);
        Classifier r2 = getInverseRouletteSelectedClassifier();        
        removeClassifier(r2);
        
        Log.log("r1 = " + r1);
        Log.log("r2 = " + r2);
        
        // add children to population
        addClassifier(childA);
        addClassifier(childB);
    }    
        
    
    public static Classifier findMostRelatedClassifier(ArrayList<Classifier> list, final Classifier c) {
        int min_degree = -1;
        Classifier e = null;
        for(Classifier d : list) {
            int degree = d.getDegreeOfRelationship(c);
            if(min_degree == -1 || degree < min_degree) {
                min_degree = degree;
                e = d;
            }
        }
        return e;
    }
    
    private double getAveragePrediction() {
        double prediction = 0.0;
        for(Classifier c : classifiers) {
            prediction += c.getPredictedPayoff();
        }
        prediction /= (double)size();
        return prediction;
    }
    
    private double getAverageFitness() {
        double fitness = 0.0;
        for(Classifier c : classifiers) {
            fitness += c.getFitness();
        }
        fitness /= (double)size();
        return fitness;
    }
    
    private double getAverageTimestamp() {
        double timestamp = 0.0;
        for(Classifier c : classifiers) {
            timestamp += (double)c.getGaTimestamp();
        }
        timestamp /= (double)size();
        return timestamp;
    }    
    
    @Override
    public String toString() {
        String output = new String();
        output += "Averages:\n";
        output += "Pre: " + getAveragePrediction() + " Fit: " + getAverageFitness() + " Tss: " + getAverageTimestamp() + " Num: " + size() + "\n";

        for (Classifier c : classifiers) {
            output += c.toString() + "\n";
        }
        return output;
    }

}
