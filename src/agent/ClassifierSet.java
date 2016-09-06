package agent;

import java.util.ArrayList;

import java.io.*;
/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class ClassifierSet {


    private int numerositySum = 0;

    /**
     * Each set keeps a reference to the parent set out of which it was generated. In the population itself
     * this pointer is set to zero.
     */
    private ClassifierSet parentSet = null;   
    
    private ArrayList<Classifier> classifiers = new ArrayList<Classifier>();

        
    
    public void removeClassifier(Classifier c) { classifiers.remove(c); }    
    public void removeClassifier(int index) {classifiers.remove(index); }
    public boolean isEmpty() { return classifiers.isEmpty(); }    
    public int size() { return classifiers.size(); }    
    
    public void clear() {classifiers.clear();}

    
    public ClassifierSet() {
    }
    
    /**
     * Constructs an action set out of the given match set. 
     * 
     * @param matchSet The current match set
     * @param action The chosen action for the action set.
     */
    public ClassifierSet(ClassifierSet matchSet, int action)
    {
        parentSet = matchSet;
        for(Classifier c : matchSet.classifiers) {
            if(c.getNumerosity() > 0 && c.getAction().getDirection() == action ) {
                addClassifier(c);
            }
        }
    }    
    /**
     * Constructs a match set out of the population. After the creation, it is checked if the match set covers all possible actions
     * in the environment. If one or more actions are not present, covering occurs, generating the missing action(s). If maximal 
     * population size is reached when covering, deletion occurs.
     *
     * @see XClassifier#XClassifier(double,int,String,int)
     * @see XCSConstants#maxPopSize
     * @see #deleteFromPopulation
     * @param state The current situation/problem instance.
     * @paramn pop The current population of classifiers.
     * @param time  The actual number of instances the XCS learned from so far.
     * @param numberOfActions The number of actions possible in the environment.     
     */
    
    public ClassifierSet(final Condition state, ClassifierSet parent_pop, final long gaTime)
    {
        parentSet = parent_pop;
        
        boolean[] actionCovered =  new boolean[Action.MAX_ACTIONS];
        
        for(int i=0; i<actionCovered.length; i++) {
            actionCovered[i]=false;
        }

        // If we have matching classifiers in the original population then include it here so that they are not generated twice
        for(Classifier c : parent_pop.classifiers) {
            if(c.isMatched(state)) {
                addClassifier(c);
                actionCovered[c.getAction().getDirection()] = true;
            }
        }

        //Check if each action is covered. If not -> generate covering XClassifier and delete if the population is too big
        boolean again;
        
        do{
            again=false;
            for(int i=0; i<actionCovered.length; i++){
                if(!actionCovered[i]){
                    Classifier newCl=new Classifier(state, new Action(i), numerositySum+1, gaTime);
                    
                    addClassifier(newCl);
                    // add new covering classifier to the original population
                    parent_pop.addClassifier(newCl);
                }
            }
            
            while(parent_pop.numerositySum > Configuration.getMaxPopSize()){
                
                // delete a random classifier (decrease numerosity or remove it from parent_pop)
                Classifier delete_classifier = parent_pop.deleteFromPopulation();
                
                
                // update the current match set in case a classifier was deleted out of that 
                // and redo the loop if now another action is not covered in the match set anymore.

                int pos = -1;
                
                    // deleted classifier from the set we've just created?
                    // then remove classifier also from this set and try again.
                if(delete_classifier != null && (pos = containsClassifier(delete_classifier)) != -1) {
		    numerositySum--;
                    // did the classifier have numerosity 1 before we deleted it?
		    if(delete_classifier.getNumerosity() == 0){
                        // then also remove it from this set
			removeClassifier(pos);
                        // end recheck covered actions
			if( !isActionCovered(delete_classifier.getAction().getDirection())){
			    again=true;
			    actionCovered[delete_classifier.getAction().getDirection()]=false;
			}
		    }
		}
            }
            // repeat until we have deleted a classifier that is not in the current set
        }while(again);
    }

    /**
     * remove actions that cannot be executed on the current grid (i.e. blocked spaces)
     */
    public void removeInvalidActions(Agent a) {
        ArrayList<Classifier> to_delete = new ArrayList<Classifier>();
        for(Classifier c : classifiers) {
            if(Agent.grid.isDirectionInvalid(a, c.getAction().getDirection())) {
                to_delete.add(c);
            }
        }
        classifiers.removeAll(to_delete);
    }
    
    public Action chooseAction(boolean do_explore, ClassifierSet matchSet) throws Exception {
        // determine all matching classifiers
        // create new classifiers if some or all actions were not matched

        Classifier c;
        
        // choose random classifier from classifierArray randomly by fitness
        if(do_explore) {
            c = matchSet.chooseRandom();
        } else {
            c = matchSet.chooseBest();
        }
        return c.getAction();
    }
    
    public Classifier chooseBest() {
        Classifier best_classifier = null;
        for(Classifier c : classifiers) {
            if(best_classifier == null || c.getFitness() > best_classifier.getFitness()) {
                best_classifier = c;
            }            
        }
        return best_classifier;
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
    /*
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
                                condition_data[i] = Condition.DONTCARE;
                            } else {
                                condition_data[i] = Integer.valueOf(s);
                            }
                        } else {
                            action_data[i - Condition.CONDITION_SIZE] = Integer.valueOf(s);
                        }
                        i++;
                    }
                    addClassifier(new Classifier(new Condition(condition_data), new Action(action_data), 0));
                    // numerosity??
                    
                    n++;
                }
                p.close();
            } catch (IOException e) {
                System.out.println("loadClassifiersFromFile(): IO Exception: Error " + e + " reading from file " + my_file.getAbsoluteFile());
            } catch (NumberFormatException e) {
                System.out.println("loadClassifiersFromFile(): NumberFormatException: Error " + e + " (line " + n + ") reading from file " + my_file.getAbsoluteFile());
            }
        }        
    }*/
    
    /**
     * Returns the sum of the time stamps of all classifiers in the set.
     */
    private double getTimeStampSum()
    {
        double sum=0.;
        
        for(Classifier c : classifiers) {
            sum += c.getGaTimestamp() * c.getNumerosity();
        }

        return sum;
    }

    /**
     * Returns the average of the time stamps in the set.
     */
    private double getTimeStampAverage()
    {
        return getTimeStampSum()/numerositySum;
    }
    
    /**
     * Returns the sum of the prediction values of all classifiers in the set.
     */
    private double getPredictionSum()
    {
        double sum=0.;
        
        for(Classifier c : classifiers) {
            sum += c.getPrediction() * c.getNumerosity();
        }
        return sum;
    }

    /**
     * Returns the sum of the fitnesses of all classifiers in the set.
     */
    private double getFitnessSum()
    {
        double sum=0.;
        
        for(Classifier c : classifiers) {
            sum += c.getFitness();
        }

        return sum;
    }    
    
    /**
     * Returns if the specified action is covered in this set.
     */
    private boolean isActionCovered(int action)
    {
        for(Classifier c : classifiers) {
            if(c.getAction().getDirection() == action) {
                return true;
            }
        }
        return false;
    }   

    
    /**
     * Returns the position of the classifier in the set if it is present and -1 otherwise.
     */
    private int containsClassifier(Classifier cl)
    {
        for(int i=0; i<classifiers.size(); i++) {
            if(classifiers.get(i).equals(cl)) {
                return i;
            }
        }
        return -1;
    }    
    

    

    /**
     * Looks for an identical classifier in the population.
     *
     * @param newCl The new classifier.
     * @return Returns the identical classifier if found, null otherwise.
     */
    private Classifier getIdenticalClassifier(Classifier newCl)
    {
        for(Classifier c : classifiers) {
            if(c.equals(newCl)) {
                return c;
            }
        }
        return null;
    }    
    
    // action selection - exploit
    public Classifier getHighestAveragePayoffClassifier() {
        Classifier maxClassifier = null;
        double maxPayoff = 0.0;        
    	for (Classifier c : classifiers){
    		if(c.getPrediction() > maxPayoff) {
    			maxPayoff = c.getPrediction();
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

    
    public double getDelPropSum(double mean_fitness) {
        double sum = 0.;
        
        for(Classifier c : classifiers) {
            sum += c.getDelProp(mean_fitness);
        }
        return sum;
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
    
    public Classifier getRouletteSelectedClassifier() throws Exception {     
        double roulettePoint = getSumFitness() * Misc.nextDouble();
        double currentRoulettePoint = 0.0;       
        
        for(Classifier c : classifiers) {
            currentRoulettePoint += c.getFitness();
            if(roulettePoint <= currentRoulettePoint) {
                return c;
            }
        }
 
        throw new Exception("Could not find roulette classifier");
        // uh oh bad roulette wheel
    }  
/*
    public ArrayList<Classifier> getRouletteSelectedClassifiers(int n) throws Exception {     
        ArrayList<Classifier> chosen_classifiers = new ArrayList<Classifier>();
        ClassifierSet temp = new ClassifierSet();
        temp.classifiers.addAll(classifiers);

        for(int j = 0; j < n; j++) {
            double roulettePoint = getSumFitness() * Misc.nextDouble();
            double currentRoulettePoint = 0.0;       
            Classifier classifier;
            for(Classifier c : classifiers) 
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
        if(chosen_classifiers.size() != n) {
            throw new Exception("Could not find enough roulette classifiers.");
        }
        return chosen_classifiers;
    }  */
    // TODO

    
    /**
     * Deletes one classifier in the population.
     * The classifier that will be deleted is chosen by roulette wheel selection 
     * considering the deletion vote. Returns the macro-classifier which got decreased by one micro-classifier.
     * 
     * @see XClassifier#getDelProp    
     */
    private Classifier deleteFromPopulation()
    {
        // get average fitness of classifiers
        double meanFitness = getFitnessSum() / (double)getNumerositySum();
        double sum = getDelPropSum(meanFitness);
        
        // roulette
        double choicePoint=sum*Misc.nextDouble();
        sum=0.;
        for(Classifier c : classifiers) {
            sum += c.getDelProp(meanFitness);
            if(sum > choicePoint) {
                Classifier cl = c;
                cl.addNumerosity(-1);
                numerositySum--;
                if(cl.getNumerosity() == 0) {
                    removeClassifier(c);
                }
                return cl;
            }
        }
        
        return null;
    }
    
    
    // TODO Replace?
    // Oder über numerosity? TODO!
 //   insertDiscoveredXClassifiers benutzen
   //         nicht ersetzen
            
    public void evolutionaryAlgorithm(Condition current_state, long gaTimestep) throws Exception {
        
    /**
     * The Genetic Discovery in XCS takes place here. If a GA takes place, two classifiers are selected
     * by roulette wheel selection, possibly crossed and mutated and then inserted.
     *
     * @see XCSConstants#theta_GA
     * @see #selectXClassifierRW
     * @see XClassifier#twoPointCrossover
     * @see XClassifier#applyMutation
     * @see XCSConstants#predictionErrorReduction
     * @see XCSConstants#fitnessReduction
     * @see #insertDiscoveredXClassifiers
     * @param time  The actual number of instances the XCS learned from so far.
     * @param state  The current situation/problem instance.
     * @param numberOfActions The number of actions possible in the environment.
     */ 
        // Don't do a GA if the theta_GA threshold is not reached, yet
        if(classifiers.isEmpty() || gaTimestep - getTimeStampAverage() < Configuration.getThetaGA()) {
            return;
        }        
        // setTimeStamps(gaTimestep); ?? Warum werden GA Timestamps aller auf die aktuelle Zeit gesetzt?
        // sollte nicht z.B. Experience auch eine Rolle spielen??
        
        int n = (int)(this.size() * Configuration.getElitistSelection());
        
     // Select two parent Classifiers with roulette Wheel Selection
        Classifier cl1P = getRouletteSelectedClassifier();
        Classifier cl2P = getRouletteSelectedClassifier();
    // children
        Classifier cl1 = new Classifier(cl1P);
        Classifier cl2 = new Classifier(cl2P);
    
        Classifier.crossOverClassifiers(cl1, cl2);

        cl1.applyMutation(current_state);
        cl2.applyMutation(current_state);
        
        cl1.setPrediction((cl1.getPrediction() + cl2.getPrediction())/2.);
        cl1.setPredictionError(Configuration.getPredictionErrorReduction() * (cl1.getPredictionError() + cl2.getPredictionError())/2.);
        cl1.setFitness(Configuration.getFitnessReduction() * (cl1.getFitness() + cl2.getFitness())/2.);
        cl2.setPrediction(cl1.getPrediction());
        cl2.setPredictionError(cl1.getPredictionError());
        cl2.setFitness(cl1.getFitness());
   
        insertDiscoveredClassifiers(cl1, cl2, cl1P, cl2P);
    }
    
    /**
     * 
     *//*
    public void panmicticGeneticAlgorithm() {
        Classifier mom = getRouletteSelectedClassifier();
        Classifier dad = getRouletteSelectedClassifier();

        // clone the classifiers      
        Classifier childA = mom.clone();
        Classifier childB = dad.clone();        
      
        // crossover chi
        if(Misc.nextDouble() < Configuration.getCrossoverProbability()) {
            Classifier.crossoverClassifiers(childA, childB);
        }       
        
        // mutation mu          
        childA.mutate(Configuration.getCrossoverMutationProbability());
        childB.mutate(Configuration.getCrossoverMutationProbability());
        
        // remove weakest two from population first?
        Classifier r1 = getInverseRouletteSelectedClassifier();
        removeClassifier(r1);
        Classifier r2 = getInverseRouletteSelectedClassifier();        
        removeClassifier(r2);
        
        // add children to population
        addClassifier(childA);
        addClassifier(childB);
    } */
        
    
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
            prediction += c.getPrediction();
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
    

    /**
     * Updates the numerositySum of the set and deletes all classifiers with numerosity 0.
     */
    public void confirmClassifiersInSet()
    {
        ArrayList<Classifier> to_delete = new ArrayList<Classifier>();
        for(Classifier c : classifiers) {
            if(c.getNumerosity() == 0) {
                to_delete.add(c);
            }
        }
        classifiers.removeAll(to_delete);
    }


    /**
     * Sets the time stamp of all classifiers in the set to the current time. The current time 
     * is the number of exploration steps executed so far.
     *
     * @param time The actual number of instances the XCS learned from so far.
     */
    private void setTimeStamps(long time)
    {
        for(Classifier c : classifiers) {
            c.setGaTimestamp(time);
        }
    }

    /** 
     * Adds a classifier to the set and increases the numerositySum value accordingly.
     *
     * @param classifier The to be added classifier.
     */
    public void addClassifier(Classifier classifier)
    {
        classifiers.add(classifier);
        numerositySum += classifier.getNumerosity();
    }

    

        
            /**
     * Updates all parameters in the current set (should be the action set).
     * Essentially, reinforcement Learning as well as the fitness evaluation takes place in this set.
     * Moreover, the prediction error and the action set size estimate is updated. Also, 
     * action set subsumption takes place if selected. As in the algorithmic description, the fitness is updated
     * after prediction and prediction error. However, in order to be more conservative the prediction error is 
     * updated before the prediction.
     *
     * @see XCSConstants#gamma
     * @see XClassifier#increaseExperience
     * @see XClassifier#updatePreError
     * @see XClassifier#updatePrediction
     * @see XClassifier#updateActionSetSize
     * @see #updateFitnessSet
     * @see XCSConstants#doActionSetSubsumption
     * @see #doActionSetSubsumption
     * @param maxPrediction The maximum prediction value in the successive prediction array 
     * (should be set to zero in single step environments).
     * @param reward The actual resulting reward after the execution of an action.
     */
    public void updateSet()
    {
        for(Classifier c : classifiers) {
            c.increaseExperience();
//            c.updatePreError(P);
//            c.updatePrediction(P);
            c.updateActionSetSize(numerositySum);
        }
      //  updateFitnessSet(); ?

        if(Configuration.isDoActionSetSubsumption()) {
            doActionSetSubsumption();
        }
    }
    
    public void updateReward(double reward) {
        // What is maxPrediction?
        // double P=reward + Configuration.getGamma()*maxPrediction;
        for(Classifier c : classifiers) {
            c.updatePreError(reward);
            c.updatePrediction(reward);
        }
        updateFitnessSet();
    }
    
    private double getAccuracySum() {
        double sum = 0.;
        for(Classifier c : classifiers) {
            sum += c.getAccuracy();
        }
        return sum;
    }
    
    /**
     * Special function for updating the fitnesses of the classifiers in the set.
     *
     * @see XClassifier#updateFitness
     */
    private void updateFitnessSet()
    {
        double accuracySum=getAccuracySum();
        
        
        //First, calculate the accuracies of the classifier and the accuracy sums
        //Next, update the fitnesses accordingly
        for(Classifier c : classifiers) {
            c.updateFitness(accuracySum, c.getAccuracy());
        }
    }
  
  
    /**
     * Inserts both discovered classifiers keeping the maximal size of the population and possibly doing GA subsumption.
     *
     * @see XCSConstants#doGASubsumption
     * @see #subsumeXClassifier
     * @see #addXClassifierToPopulation
     * @see XCSConstants#maxPopSize
     * @see #deleteFromPopulation
     * @param cl1 The first classifier generated by the GA.
     * @param cl2 The second classifier generated by the GA.
     * @param cl1P The first parent of the two new classifiers.
     * @param cl2P The second classifier of the two new classifiers.
     */
    
    private void insertDiscoveredClassifiers(Classifier cl1, Classifier cl2, Classifier cl1P, Classifier cl2P)
    {
        ClassifierSet pop=this;
        while(pop.parentSet!=null) {
            pop=pop.parentSet;
        }

        if(Configuration.isDoGASubsumption()){
            subsumeClassifier(cl1, cl1P, cl2P);        
            subsumeClassifier(cl2, cl1P, cl2P);        
        }else{
	    pop.addClassifierToPopulation(cl1);
            pop.addClassifierToPopulation(cl2);
        }
        
        while(pop.numerositySum > Configuration.getMaxPopSize()) {
            pop.deleteFromPopulation();
        }
    } 


  
 
    /**
     * Tries to subsume a classifier in the parents.
     * If no subsumption is possible it tries to subsume it in the current set.
     *
     * @see #subsumeXClassifier(XClassifier)
     */
    private void subsumeClassifier(Classifier cl, Classifier cl1P, Classifier cl2P)
    {
        if(cl1P!=null && cl1P.subsumes(cl)){
            increaseNumerositySum(1);
            cl1P.addNumerosity(1);
        }else if(cl2P!=null && cl2P.subsumes(cl)){
            increaseNumerositySum(1);
            cl2P.addNumerosity(1);
        }else{
            subsumeClassifier(cl); //calls second subsumeXClassifier fkt!
        }            
    }
    

    /**
     * Tries to subsume a classifier in the current set. 
     * This method is normally called in an action set. 
     * If no subsumption is possible the classifier is simply added to the population considering 
     * the possibility that there exists an identical classifier.
     *
     * @param cl The classifier that may be subsumed.
     * @see #addXClassifierToPopulation
     */
    private void subsumeClassifier(Classifier cl)
    {
        //Open up a new Vector in order to chose the subsumer candidates randomly
        ArrayList<Classifier> choices = new ArrayList<Classifier>();
        for(Classifier c : classifiers) {
            if(c.subsumes(cl)) {
                choices.add(c);
            }
        }
        if(choices.size() > 0) {
            int choice=(int)((double)Misc.nextDouble()*choices.size());
            choices.get(choice).addNumerosity(1);
            increaseNumerositySum(1);
            return;
        }
	//If no subsumer was found, add the classifier to the population
        addClassifierToPopulation(cl);
    }    

    /**
     * Executes action set subsumption. 
     * The action set subsumption looks for the most general subsumer classifier in the action set 
     * and subsumes all classifiers that are more specific than the selected one.
     *
     * @see XClassifier#isSubsumer
     * @see XClassifier#isMoreGeneral
     */
    private void doActionSetSubsumption()
    {
        ClassifierSet pop=this;
        while(pop.parentSet!=null) {
            pop=pop.parentSet;
        }
        
        Classifier subsumer=null;
        for(Classifier c : classifiers) {
            if(c.isPossibleSubsumer()) {
                if(subsumer == null || c.isMoreGeneral(subsumer)) {
                    subsumer = c;
                }
            }
        }        
        
	//If a subsumer was found, subsume all more specific classifiers in the action set
        if(subsumer!=null){
            ArrayList<Classifier> to_remove = new ArrayList<Classifier>();
            for(Classifier c : classifiers) {
                if(subsumer.isMoreGeneral(c)) {
                    int num=c.getNumerosity();
		    subsumer.addNumerosity(num);
		    c.addNumerosity((-1)*num);
		    pop.removeClassifier(c);
                    to_remove.add(c);
		}
	    }
            for(Classifier c : to_remove) {
                removeClassifier(c);
            }
	}
     }

    /**
     * Adds the classifier to the population and checks if an identical classifier exists. 
     * If an identical classifier exists, its numerosity is increased.
     *
     * @see #getIdenticalClassifier
     * @param cl The to be added classifier.
     */
    private void addClassifierToPopulation(Classifier cl)
    {
        // set pop to the actual population
        ClassifierSet pop=this;
        while(pop.parentSet!=null) {
            pop=pop.parentSet;
        }
    
        Classifier oldcl=null;
        if((oldcl=pop.getIdenticalClassifier(cl))!=null){
            oldcl.addNumerosity(1);
            increaseNumerositySum(1);
        }else{
            pop.addClassifier(cl);
        }
    }

    
    /**
     * Increases recursively all numerositySum values in the set and all parent sets.
     * This function should be called when the numerosity of a classifier in some set is increased in 
     * order to keep the numerosity sums of all sets and essentially the population up to date.
     */
    private void increaseNumerositySum(int nr)
    {
        numerositySum+=nr;
        if(parentSet!=null) {
            parentSet.increaseNumerositySum(nr);
        }
    }
    
    @Override
    public String toString() {
        String output = new String();
        output += "   > Averages:\n";
        output += "   > Pre: " + getAveragePrediction() + " Fit: " + getAverageFitness() + " Tss: " + getAverageTimestamp() + " Num: " + size() + "\n";

        for (Classifier c : classifiers) {
            output += "     - " + c.toString() + "\n";
        }
        return output;
    }

    public int getNumerositySum() {
        return numerositySum;
    }

}
