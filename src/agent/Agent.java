/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//Speicherstellen in Aktion

package agent;
import java.awt.Point;
import java.util.Stack;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Agent {
    

    
// previous actions    
    private Stack<Classifier> actionStack;
// previous rewards
    private Stack<Double> rewardStack;
    
    public double totalPoints = 0;

    
//    TODO welchen Classifier wähle ich bei BLANKS?
//    Ich wähle aus allen verfügbaren Classifieren den mit der höchsten Fitness!

        
    public static final double REWARD = 1.0;
    public static final int GOAL_AGENT_ID = 1;

    
    private static int global_id = 1;
    public static Grid grid = new Grid();
    public static Agent goalAgent;

    
// current state    
    private Point p;
    private int id;
//    private int[] speicherstelle;

// classifiers    
    protected ClassifierSet classifierSet = new ClassifierSet();
    
    private ClassifierSet lastMatchSet = new ClassifierSet();
    private ClassifierSet lastActionSet = new ClassifierSet();
    
//    TODO welchen Classifier wähle ich bei BLANKS?
//    Ich wähle aus allen verfügbaren Classifieren den mit der höchsten Fitness!
    
    public Agent(Point p) throws Exception {
        this.p = p;
        id = global_id;
        global_id++;
        
        actionStack = new Stack<Classifier>();
        rewardStack = new Stack<Double>();        

        grid.addAgent(this);
    }
    
    public double getReward() {
        return (grid.getAbsoluteTorusDistance(goalAgent.getPosition(), p) <= Configuration.getRewardDistance())?REWARD:0.0;
    }
    
    public final Point getPosition() {
        return p;
    }
    
    public void setPosition(Point p) {
        this.p = p;
    }
    
    public int getX() {
        return p.x;
    }
    
    public int getY() {
        return p.y;
    }
    
    public int getID() {
        return id;
    }
    
    public void moveRandomly() throws Exception {
        int rand_direction = Misc.nextInt(Grid.MAX_DIRECTIONS+1);
        grid.moveAgent(this, rand_direction);
    }
    
    public String getInputString() throws Exception {
        return Classifier.getInputString(grid, id, p);
    }
    
/* for LCS */
    public void geneticAlgorithm() {
        classifierSet.panmicticGeneticAlgorithm(Configuration.getCrossoverProbability(), Configuration.getCrossoverMutationProbability());
    }
    public void evolutionaryAlgorithm() throws Exception {
        classifierSet.evolutionaryAlgorithm(Configuration.getElitistSelection(), Configuration.getEvolutionaryMutationProbability());
    }


    
    
    public void calculateNextMove(long gaTimestep) throws Exception {
        lastMatchSet.clear();
        Classifier classifier = classifierSet.chooseClassifier(lastMatchSet, grid, id, p, gaTimestep);
        lastActionSet.clear();
        lastActionSet.addClassifier(classifier);
        
        if(actionStack.size() >= Configuration.getMaxStackSize()) {
            actionStack.pop();
        }
        actionStack.push(classifier);
        // TODO Zuordnung zu Classifierset oder so... muss ja vom Stack wieder auf das tatsächliche Objekt zurückgeführt werden
        Action action = classifier.getAction();
        int direction = action.getDirection();
        
        grid.moveAgent(this, direction);
    }
    
    public void calculateReward() {
        double reward = getReward();
        totalPoints += reward;
        
        if(rewardStack.size() >= Configuration.getMaxStackSize()) {
            rewardStack.pop();
        }
        rewardStack.push(reward);
        
        double total_reward = 0.0;
        for(int i = 0; i < rewardStack.size(); i++) {
            total_reward += rewardStack.get(i);
        }
        total_reward /= (double)(rewardStack.size());
        
        // 3 Möglichkeiten:
        // Zielagent springt völlig zufällig beliebige Distanzen, Simulation falls Zielagent 
        for(int i = 0; i < actionStack.size(); i++) {
            actionStack.get(i).updateClassifier(total_reward, Configuration.getRewardUpdateFactor());
        }
    }       
    
    @Override
    public String toString() {
        String output = new String();
        output += "Population\n";
        output += classifierSet.toString();
        
        output += "MatchSet\n";
        output += lastMatchSet.toString();
                
        output += "ActionSet\n";
        output += lastActionSet.toString();
        
        return output;
    }    
    
}
