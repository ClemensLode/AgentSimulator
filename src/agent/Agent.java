/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//Speicherstellen in Aktion

package agent;
import java.awt.Point;
import java.util.LinkedList;
import java.text.NumberFormat;

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
    
    Action calculatedAction;
    
    
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
    
    public static void resetID() {
        global_id = GOAL_AGENT_ID;
    }
    
    public boolean isGoalAgent() {
        return id == GOAL_AGENT_ID;
    }
    
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
    
    /**
     * Tries to insert two new children into the population
     * They are generated by using crossing over and mutation
     * The two parents are taken from the current population
     * @param current_state
     * @param gaTimeStamp
     * @throws java.lang.Exception
     */
    public void evolutionaryAlgorithm(long gaTimeStamp) throws Exception {
        
        

    //    if (Configuration.isDoEvolutionaryAlgorithm()) { 
            // only copy, mutate and replace
            classifierSet.evolutionaryAlgorithm(getCurrentState(), gaTimeStamp);
      //  } else {
            // parent selection, one-point crossing over (with obstacles later: two point crossing over)
      //      classifierSet.panmicticGeneticAlgorithm();
        //}
    }
    
    private Condition getCurrentState() {
        return new Condition(id, p);
    }

    public void calculateNextMove(boolean do_explore, long gaTimestep) throws Exception {
        // lastMatchSet: temporary save for log output
        lastMatchSet = new ClassifierSet(getCurrentState(), classifierSet, gaTimestep);
        lastMatchSet.removeInvalidActions(this);
        
        calculatedAction = classifierSet.chooseAction(do_explore, lastMatchSet);
        // TODO Zuordnung zu Classifierset oder so... muss ja vom Stack wieder auf das tatsächliche Objekt zurückgeführt werden ??

        lastActionSet = new ClassifierSet(lastMatchSet, calculatedAction.getDirection());
        lastActionSet.updateSet();
                
        // max steps in the past, ideally getMaxStackSize is infinite
        if(actionSet.size() >= Configuration.getMaxStackSize()) {
            actionSet.removeFirst();
        }
        actionSet.addLast(lastActionSet);        
    }
        // get all classifiers from the matching Set with the same action as the action winner
    
    public void doNextMove() throws Exception {
        grid.moveAgent(this, calculatedAction.getDirection());
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
    
    /**
     * is called in each step
     */
    public void calculateReward() {
        boolean reward = getReward();
        
        // goal agent is in sight?
        if(reward) {
            totalPoints ++;
        }        
        // TODO später: Nur begrenzte Zahl vergangener Aktionen belohnen!
        // z.B. wenn actionsize überläuft
        // Mit Literatur vergleichen!
        
//            double reward = env.executeAction( actionWinner );
		// prevActionSet.confirmClassifiersInSet(); => REWARD!
		// prevActionSet.updateSet(predictionArray.getBestValue(), prevReward);
                // prevActionSet.runGA(stepCounter+steps, prevState, env.getNrActions());
                

        // event?
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
    
    public void printMatching() {
        try {
            Log.log("# grid\n");
            Log.log(Agent.grid.getGridString() + "\n");
            Log.log("# input\n");
            Log.log(getInputString() + "\n\n");
            Log.log("# classifiers\n");
            Log.log(" - Population:\n");
            Log.log(classifierSet.toString() + "\n");
            Log.log(" - MatchSet:\n");
            Log.log(lastMatchSet.toString() + "\n\n");
        } catch(Exception e) {
            Log.errorLog("Error creating input string for log file: ", e);
        }        
    }
    
    public void printHeader() {
        Log.log("# AGENT\n");
        NumberFormat nf=NumberFormat.getInstance(); // Get Instance of NumberFormat
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3
        
        String sb="ID " + (nf.format((long)getID()));
        Log.log(sb + "\n\n");
    }
    
    public void printAction() {
        Log.log("# action set\n");
        Log.log(" - ActionSet:\n");
        Log.log(lastActionSet.toString() + "\n\n");
    }    

    
    public double getTotalPoints() {
        return totalPoints;
    }
    
}
