/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.awt.Point;

// matchings:

// Näher als alpha entfernt
// Etwa alpha entfernt
// Weiter als alpha entfernt

// 

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */


// Wahrnehmung: 

// 4 Konenförmige Sichten  > < v /\
// Entfernung nähester Agent (Manhattan Distanzmaß)
// Entfernung Zielagent
// Speicher

// Aktion
// Speicher int
// Bewegung 1-4

// 18/10/08: Added experience, numerosity


public class Classifier {
    
  
    private double predictionError;
    // predicted payoff
    private double prediction;  
    
    // number of equal classifiers that were added after this classifier + 1
    private int numerosity;
    
    // number of times this classifier was updated
    private int experience;
    
    private long gaTimestamp;
    
    private double fitness;
    
    private Action action;
    private Condition condition;
    

    /**
     * The action set size estimate of the classifier.
     */
    private double actionSetSize;    
    /*
    public Classifier(long gaTimestamp) {
        resetAll();
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(int[] condition, long gaTimestamp) throws Exception {
        reset();
        
        setCondition(condition);
        action = new Action();
        setGaTimestamp(gaTimestamp);
    }*/
    
    public Classifier(Condition condition, double setSize, long gaTimestamp) throws Exception {
        classifierSetVariables(setSize, gaTimestamp);
                
        this.condition = condition.clone();
    }
    
    public Classifier(Condition condition, Action action, double setSize, long gaTimestamp) {
        classifierSetVariables(setSize, gaTimestamp);

        this.condition = condition.clone();
        this.action = action.clone();
    }
    
    /*
    public Classifier(int[] condition, int[] action, long gaTimestamp) {
        reset();
        
        setCondition(condition);
        setAction(action);
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(int[] condition, int[] action, double predictedPayoff, double error, double avgNicheSize, long gaTimestamp) {
        setCondition(condition);
        setAction(action);
        
        resetStats();
        
        setPrediction(predictedPayoff);
        setPredictionError(error);
        setAvgNicheSize(avgNicheSize);
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(Condition condition, Action action, double predictedPayoff, double error, double avgNicheSize, long gaTimestamp) {
        this.condition = condition;
        this.action = action;
        
        resetStats();
        
        setPrediction(predictedPayoff);
        setPredictionError(error);
        setAvgNicheSize(avgNicheSize);        
        setGaTimestamp(gaTimestamp);
    }*/
    
    /**
     * Constructs an identical XClassifier.
     * However, the experience of the copy is set to 0 and the numerosity is set to 1 since this is indeed 
     * a new individual in a population.
     *
     * @param clOld The to be copied classifier.
     */
    public Classifier(Classifier clOld)
    {
	condition=new Condition(clOld.condition);
	action=clOld.action;	
	this.prediction=clOld.prediction;
	this.predictionError=clOld.predictionError;
	// Here we should divide the fitness by the numerosity to get a accurate value for the new one!
	this.fitness=clOld.fitness/clOld.numerosity;
	this.numerosity=1;
	this.experience=0;
	this.actionSetSize=clOld.actionSetSize;
	this.gaTimestamp=clOld.gaTimestamp;
    }    
    
  
    /** 
     * Sets the initial variables of a new classifier.
     *
     * @see XCSConstants#predictionIni
     * @see XCSConstants#predictionErrorIni
     * @see XCSConstants#fitnessIni
     * @param setSize The size of the set the classifier is created in.
     * @param time The actual number of instances the XCS learned from so far.
     */
    private void classifierSetVariables(double setSize, long time)
    {		
        action = new Action();
        condition = new Condition();
        
	this.setPrediction(Configuration.getPredictionInitialization());
	this.setPredictionError(Configuration.getPredictionErrorInitialization());
	this.setFitness(Configuration.getFitnessInitialization());
    
	this.numerosity=1;
	this.experience=0;
	this.actionSetSize=setSize;
	this.gaTimestamp=time;
    }    
    

    /**
     * Increases the Experience of the classifier by one.
     */
    public void increaseExperience()
    {
	experience++;
    }
    

    /**
     * Updates the action set size.
     *
     * @see XCSConstants#beta
     * @param numeriositySum The number of micro-classifiers in the population
     */
    public double updateActionSetSize(double numerositySum)
    {
	if(experience < 1./Configuration.getBeta()){
	    actionSetSize= (actionSetSize * (double)(experience-1)+ numerositySum) / (double)experience;
	}else{
	    actionSetSize+= Configuration.getBeta() * (numerositySum - actionSetSize);
	}
	return actionSetSize*numerosity;
    }    
    
    @Override
    public Classifier clone() {
        return new Classifier(this);
    }      
    
    public static Classifier createCoveringClassifier(final Grid grid, final double wildcard_probability, final int id, final Point position, final double setSize, final long gaTimestamp) throws Exception {
        double[] direction_agent_distance = grid.getDirectionAgentDistances(position, id);
        int direction_goal_agent = grid.getAgentDirectionRange(position, Agent.goalAgent.getPosition());
        
        return (new Classifier(Condition.createCoveringCondition(direction_agent_distance, direction_goal_agent, wildcard_probability), setSize, gaTimestamp));
    }      
    
    public int[] createGeneticString() {
        int[] genetic_string = new int[condition.getLength() + action.getLength()];
        condition.copyTo(genetic_string, 0);
        action.copyTo(genetic_string, condition.getLength());
        return genetic_string;
    }
    
    public void setGeneticData(int[] data) {
        int[] new_condition = new int[condition.getLength()];
        int[] new_action = new int[action.getLength()];
        
        for(int i = 0; i < condition.getLength(); i++) {
            new_condition[i] = data[i];
        }
        for(int i = 0; i < action.getLength(); i++) {
            new_action[i] = data[i + condition.getLength()];
        }
        setCondition(new_condition);
        setAction(new_action);
    }
    
    public static void crossOverGeneticData(Classifier childA, Classifier childB) {
        // combine condition and action parts of classifier to form strings
        int[] childAStr = childA.createGeneticString();
        int[] childBStr = childB.createGeneticString();
        
        // assume childAStr.length() == childBStr.length()
        int crossoverIndex = 1 + Misc.nextInt(childAStr.length - 2);
        
        // do the crossover
        int[] newChildAStr = new int[childAStr.length];
        int[] newChildBStr = new int[childBStr.length];
        
        for(int i = 0; i < crossoverIndex; i++)  {
            newChildAStr[i] = childAStr[i];
            newChildBStr[i] = childBStr[i];
        }
        for(int i = crossoverIndex; i < childAStr.length; i++) {
            newChildAStr[i] = childBStr[i];
            newChildBStr[i] = childAStr[i];
        }
        
        childA.setGeneticData(newChildAStr);
        childB.setGeneticData(newChildBStr);
    }
        
    /*
    public static void crossoverClassifiers(Classifier childA, Classifier childB) {
        // do some crossover
        double avgChildPredictedPayoff = (childA.getPrediction() + childB.getPrediction()) * 0.5;
        double avgChildError = (childA.getPredictionError() + childB.getPredictionError()) * 0.5;
        double avgChildNicheSize = (childA.getAvgNicheSize() + childB.getAvgNicheSize()) * 0.5;

        // set the strengths to the mean of the strengths prior to crossover
        childA.setPrediction(avgChildPredictedPayoff);
        childB.setPrediction(avgChildPredictedPayoff);
        
        childA.setPredictionError(avgChildError);
        childB.setPredictionError(avgChildError);
        
        childA.setAvgNicheSize(avgChildNicheSize);
        childB.setAvgNicheSize(avgChildNicheSize);
        
        crossOverGeneticData(childA, childB);
    }    */
    
    public boolean isMatched(final Condition condition) {
        return this.condition.equals(condition);
    }
     
    public boolean isMatched(final Grid grid, final int id, final Point position) {
        double[] direction_agent_distance = grid.getDirectionAgentDistances(position, id);
        int direction_goal_agent = grid.getAgentDirectionRange(position, Agent.goalAgent.getPosition());
                
        return(condition.isMatched(direction_agent_distance, direction_goal_agent));
    }
    
    public int getDegreeOfRelationship(final Classifier a) {
        int degree = 0;
        degree += condition.getDegreeOfRelationship(a.getCondition());
        degree += action.getDegreeOfRelationship(a.getAction());

        return degree;
    }
    
    public boolean isMoreGeneral(final Classifier a) { 
        return condition.isMoreGeneral(a.getCondition());
    }
    
    public boolean isPossibleSubsumer() {
	if(experience > Configuration.getThetaSubsumer() && getPredictionError() < (double)Configuration.getEpsilon0()) {
	    return true;
        }
	return false;        
    }
    
    public boolean subsumes(Classifier c) {
        if(action.equals(c.getAction())) {
            if(isPossibleSubsumer()) {
                if(isMoreGeneral(c)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean equals(Classifier c) {
        if(condition.equals(c.getCondition())) {
            if(action.equals(c.getAction())) {
                return true;                
            }
        }
        return false;
    }
    
    /**
     * Returns the vote for deletion of the classifier.
     *
     * @see XCSConstants#delta
     * @see XCSConstants#theta_del
     * @param meanFitness The mean fitness in the population.
     */
    public double getDelProp(double mean_fitness)
    {
	if(fitness/numerosity >= Configuration.getDelta()*mean_fitness || experience < Configuration.getDeltaDel()) {
	    return actionSetSize*numerosity;
        }
	return actionSetSize*numerosity*mean_fitness / ( fitness/numerosity);
    }    
    
    public final Action getAction() {
        return action;
    }
    public final Condition getCondition() {
        return condition;
    }
    
    public double getFitness() {
        return fitness;
//        return 1.0 / (predictionError + 1.0);    
    }

    /**
     * Returns the accuracy of the classifier.
     * The accuracy is determined from the prediction error of the classifier using Wilson's 
     * power function as published in 'Get Real! XCS with continuous-valued inputs' (1999)
     *
     * @see XCSConstants#epsilon_0
     * @see XCSConstants#alpha
     * @see XCSConstants#nu
     */
    public double getAccuracy()
    {
	double accuracy;

	if(getPredictionError() <= Configuration.getEpsilon0()){
	    accuracy = 1.;
	}else{
	    accuracy = Configuration.getAlpha() * Math.pow( getPredictionError() / Configuration.getEpsilon0() , -Configuration.getNu());
	}
	return accuracy;
    }    
    
    public double getPrediction() {return prediction;}
    public void setPrediction(double predictedPayoff) {this.prediction = predictedPayoff;}
    public double getPredictionError() {return predictionError;}
    public void setPredictionError(double error) {this.predictionError = error;}
    public long getGaTimestamp() {return gaTimestamp;}
    public void setGaTimestamp(long gaTimestamp) {this.gaTimestamp = gaTimestamp;}
    
    private void setCondition(int[] condition) {
        this.condition.setData(condition);
    }
    
    private void setAction(int[] action) {
        this.action.setData(action);
    }
 
    
    public void updateClassifier(double payoff) {
        setPredictionError(getPredictionError() + Configuration.getRewardUpdateFactor() * (Math.abs(payoff - prediction) - getPredictionError()));
        setPrediction(prediction + Configuration.getRewardUpdateFactor() * (payoff - prediction));
        experience++;
    }   
    
    /**
     * Updates the fitness of the classifier according to the relative accuracy.
     *
     * @see XCSConstants#beta
     * @param accSum The sum of all the accuracies in the action set
     * @param accuracy The accuracy of the classifier.
     */
    public double updateFitness(double accSum, double accuracy)
    {
	setFitness(fitness + Configuration.getBeta() * ((accuracy * getNumerosity()) / accSum - fitness));	
	return fitness;//fitness already considers numerosity
    }    
    
    /**
     * Updates the prediction error of the classifier according to P.
     *
     * @see XCSConstants#beta
     * @param P The actual Q-payoff value (actual reward + max of predicted reward in the following situation).
     */
    public double updatePreError(double P)
    {
	if( (double)experience < 1./Configuration.getBeta()){
	    setPredictionError((getPredictionError() * ((double) experience - 1.) + Math.abs(P - prediction)) / (double) experience);
	}else{
	    setPredictionError(getPredictionError() + Configuration.getBeta() * (Math.abs(P - prediction) - getPredictionError()));
	}
	return getPredictionError()*getNumerosity();
    }    
    
    /**
     * Updates the prediction of the classifier according to P.
     *
     * @see XCSConstants#beta
     * @param P The actual Q-payoff value (actual reward + max of predicted reward in the following situation).
     */
    public double updatePrediction(double P)
    {
	if( (double)experience < 1./Configuration.getBeta()){
	    setPrediction((prediction * ((double) experience - 1.) + P) / (double) experience);
	}else{
	    setPrediction(prediction + Configuration.getBeta() * (P - prediction));
	}
	return prediction*numerosity;
    }
 
    /**
     * Applies a niche mutation to the classifier. 
     * This method calls mutateCondition(state) and mutateAction(numberOfActions) and returns 
     * if at least one bit or the action was mutated.
     *
     * @param state The current situation/problem instance
     * @param numberOfActions The maximal number of actions possible in the environment.
     */
    public boolean applyMutation(Condition state, int numberOfActions)
    {
	boolean changed=condition.mutateCondition(state);
	if(action.mutateAction(numberOfActions)) {
	    changed=true;
        }
	return changed;			
    }    


    
    @Override
    public String toString() {
        String output = new String();
        output += condition.toString();

        output += "-";
        output += action.toString();

        output += " " + Misc.round(getPrediction(), 0.01);
        output += " " + Misc.round(getPredictionError(), 0.01);
        output += " " + Misc.round(getFitness(), 0.01);
        output += " " + getGaTimestamp();
        
        return output;
    }
    
    public static String getInputString(final Grid grid, final int id, final Point position) throws Exception {
        double[] direction_agent_distance = grid.getDirectionAgentDistances(position, id);
        int direction_goal_agent = grid.getAgentDirectionRange(position, Agent.goalAgent.getPosition());

        return Condition.getInputString(direction_agent_distance, direction_goal_agent);
    }

    public int getNumerosity() {
        return numerosity;
    }
    
    /**
     * Adds to the numerosity of the classifier.
     *
     * @param num The added numerosity (can be negative!).
     */
    public void addNumerosity(int num)
    {
	numerosity+=num;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
    
                
}
