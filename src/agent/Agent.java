/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//Speicherstellen in Aktion

package agent;
import java.awt.Point;
import java.util.LinkedList;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Agent {
    
    
// previous actions    
    private LinkedList<ClassifierSet> actionSet;
// previous rewards
    private boolean lastReward = false;
    
    private double totalPoints = 0;

    
//    TODO welchen Classifier wähle ich bei BLANKS?
//    Ich wähle aus allen verfügbaren Classifieren den mit der höchsten Fitness!

        
    public static final int GOAL_AGENT_ID = 1;

    
    private static int global_id = 1;
    public static Grid grid;
    public static Agent goalAgent;
    
    
    // TODO: Log timestamp of last GA run
    // Evolute agent with a certain probability depending on lastGATimeStamp whenever an event happens
    // private int lastGATimeStamp = 0;

    
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
        
        actionSet = new LinkedList<ClassifierSet>();

        grid.addAgent(this);
    }
    
    public boolean isGoalAgentInSight() {
        return grid.getAbsoluteTorusDistance(goalAgent.getPosition(), p) <= Configuration.getRewardDistance();
    }
    
    
    public boolean getReward() {
        return isGoalAgentInSight();
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
    
    public void evolutionaryAlgorithm(long gaTimeStamp) throws Exception {

        if (Configuration.isDoEvolutionaryAlgorithm()) { 
            // only copy, mutate and replace
            classifierSet.evolutionaryAlgorithm();
        } else {
            // parent selection, one-point crossing over (with obstacles later: two point crossing over)
            classifierSet.panmicticGeneticAlgorithm();
        }
    }

    public void calculateNextMove(boolean do_explore, long gaTimestep) throws Exception {
        // lastMatchSet: temporary save for log output
        lastMatchSet.clear();
        Action action = classifierSet.chooseAction(do_explore, lastMatchSet, id, p, gaTimestep);
        // TODO Zuordnung zu Classifierset oder so... muss ja vom Stack wieder auf das tatsächliche Objekt zurückgeführt werden ??
        int direction = action.getDirection();

        grid.moveAgent(this, direction);

        
        // get all classifiers from the matching Set with the same action as the action winner
        lastActionSet = new ClassifierSet(lastMatchSet, direction);  
        lastActionSet.updateSet();
        
        
        /*       
		prevActionSet.updateSet(predictionArray.getBestValue(), prevReward);
                prevActionSet.runGA(stepCounter+steps, prevState, env.getNrActions());        
        */
                
        // max steps in the past, ideally getMaxStackSize is infinite
        if(actionSet.size() >= Configuration.getMaxStackSize()) {
            actionSet.removeFirst();
        }
        actionSet.addLast(lastActionSet);
    }
    
    private double calculatePositiveReward(int step, int size) {
        return ((double)(1+step)) / ((double)size);
    }
    
    private double calculateNegativeReward(int step, int size) {
        return ((double)(size - step - 1)) / ((double)size);
    }    
    /*
     * Parameter möglicher Modelle:
     * max actionStack size
     * event driven (bei Rewardänderung wird actionStack gelöscht)
     * Art der Verteilung des Rewards auf den actionStack (absteigend, absteigend quadratisch, ...)
     * */
    
    public void calculateReward() {
        boolean reward = getReward();
        if(reward) {
            totalPoints ++;
        }        
        // TODO später: Nur begrenzte Zahl vergangener Aktionen belohnen!
        // z.B. wenn actionsize überläuft
        // Mit Literatur vergleichen!
        if(Configuration.isEventDriven() && reward != lastReward) {
            if(reward) {
                for(int i = 0; i < actionSet.size(); i++) {
                    double corrected_reward = calculatePositiveReward(i, actionSet.size());
                    actionSet.get(i).updateReward(corrected_reward);
                }
            } else {
                for(int i = 0; i < actionSet.size(); i++) {
                    double corrected_reward = calculateNegativeReward(i, actionSet.size());
                    actionSet.get(i).updateReward(corrected_reward);
                }
            }
            // clear action set
            // TODO: maybe save action Set..., merge with history and timestamp it~
            actionSet.clear();
            lastReward = reward;
        }
            
            
    }       
    
    @Override
    public String toString() {
        String output = new String();
        output += " - Population:\n";
        output += classifierSet.toString();
        
        output += " - MatchSet:\n";
        output += lastMatchSet.toString();
                
        output += " - ActionSet:\n";
        output += lastActionSet.toString();
        
        return output;
    }

    public double getTotalPoints() {
        return totalPoints;
    }
    
}
