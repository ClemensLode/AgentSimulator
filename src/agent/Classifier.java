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


public class Classifier {
    
  
    private double error;
    // predicted payoff
    private double predictedPayoff;  
    // estimated average niche size
    private double avgNicheSize;   
    
    private long gaTimestamp;
    
    private Action action;
    private Condition condition;
    
    
    public Classifier(long gaTimestamp) {
        resetAll();
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(int[] condition, long gaTimestamp) throws Exception {
        reset();
        
        setCondition(condition);
        action = new Action();
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(Condition condition, long gaTimestamp) throws Exception {
        reset();
                
        this.condition = condition;
        action = new Action();
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(int[] condition, int[] action, long gaTimestamp) {
        reset();
        
        setCondition(condition);
        setAction(action);
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(int[] condition, int[] action, double predictedPayoff, double error, double avgNicheSize, long gaTimestamp) {
        setCondition(condition);
        setAction(action);
        
        setPredictedPayoff(predictedPayoff);
        setError(error);
        setAvgNicheSize(avgNicheSize);
        setGaTimestamp(gaTimestamp);
    }
    
    public Classifier(Condition condition, Action action, double predictedPayoff, double error, double avgNicheSize, long gaTimestamp) {
        this.condition = condition;
        this.action = action;
        
        setPredictedPayoff(predictedPayoff);
        setError(error);
        setAvgNicheSize(avgNicheSize);        
        setGaTimestamp(gaTimestamp);
    }
    
    public void reset() {
        action = new Action();
        condition = new Condition();
        error = 0.0;
        predictedPayoff = Misc.nextDouble();
        avgNicheSize = 0.1;
    }
    
    
    public void resetAll() {
        reset();
        condition = new Condition();
        action = new Action();        
    }

    @Override
    public Classifier clone() {
        Condition clonedCondition = condition.clone();
        Action clonedAction = action.clone();

        return new Classifier(clonedCondition, clonedAction, getPredictedPayoff(), getError(), getAvgNicheSize(), getGaTimestamp());
    }      
    
    public static Classifier createCoveringClassifier(final Grid grid, final double wildcard_probability, final int id, final Point position, long gaTimestamp) throws Exception {
        double[] direction_agent_distance = grid.getDirectionAgentDistances(position, id);
        int direction_goal_agent = grid.getAgentDirectionRange(position, Agent.goalAgent.getPosition());
        
        return (new Classifier(Condition.createCoveringCondition(direction_agent_distance, direction_goal_agent, wildcard_probability), gaTimestamp));
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
        
    
    public static void crossoverClassifiers(Classifier childA, Classifier childB) {
        // do some crossover
        double avgChildPredictedPayoff = (childA.getPredictedPayoff() + childB.getPredictedPayoff()) * 0.5;
        double avgChildError = (childA.getError() + childB.getError()) * 0.5;
        double avgChildNicheSize = (childA.getAvgNicheSize() + childB.getAvgNicheSize()) * 0.5;

        // set the strengths to the mean of the strengths prior to crossover
        childA.setPredictedPayoff(avgChildPredictedPayoff);
        childB.setPredictedPayoff(avgChildPredictedPayoff);
        
        childA.setError(avgChildError);
        childB.setError(avgChildError);
        
        childA.setAvgNicheSize(avgChildNicheSize);
        childB.setAvgNicheSize(avgChildNicheSize);
        
        crossOverGeneticData(childA, childB);
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
    
    public final Action getAction() {
        return action;
    }
    public final Condition getCondition() {
        return condition;
    }
    
    public double getFitness() {
        return 1.0 / (error + 1.0);    
    }
    public double getPredictedPayoff() {return predictedPayoff;}
    private void setPredictedPayoff(double predictedPayoff) {this.predictedPayoff = predictedPayoff;}
    public double getError() {return error;}
    private void setError(double error) {this.error = error;}
    public double getAvgNicheSize() {return avgNicheSize;}
    private void setAvgNicheSize(double avgNicheSize) {this.avgNicheSize = avgNicheSize;}
    public long getGaTimestamp() {return gaTimestamp;}
    private void setGaTimestamp(long gaTimestamp) {this.gaTimestamp = gaTimestamp;}
    
    private void setCondition(int[] condition) {
        this.condition.setData(condition);
    }
    
    private void setAction(int[] action) {
        this.action.setData(action);
    }
 
    
    public void updateClassifier(double payoff, double beta) {
        error = error + beta * (Math.abs(payoff - predictedPayoff) - error);
        predictedPayoff = predictedPayoff + beta * (payoff - predictedPayoff);
    }    

    public void mutate(double mutation_probability) {
        condition.mutate(mutation_probability);
        action.mutate(mutation_probability);
    }
    
    public void copyAndMutate(Classifier c, double mutation_probability) {
        reset();
        setCondition(c.condition.data);
        setAction(c.action.data);
        mutate(mutation_probability);
    }
    
    @Override
    public String toString() {
        String output = new String();
        output += condition.toString();

        output += "-";
        output += action.toString();

        output += " " + Misc.round(getPredictedPayoff(), 0.01);
        output += " " + Misc.round(getError(), 0.01);
        output += " " + Misc.round(getFitness(), 0.01);
        output += " " + getGaTimestamp();
        
        return output;
    }
    
    public static String getInputString(final Grid grid, final int id, final Point position) throws Exception {
        double[] direction_agent_distance = grid.getDirectionAgentDistances(position, id);
        int direction_goal_agent = grid.getAgentDirectionRange(position, Agent.goalAgent.getPosition());

        return Condition.getInputString(direction_agent_distance, direction_goal_agent);
    }    
    
                
}
