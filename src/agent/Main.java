/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;
import java.awt.Point;
import java.util.ArrayList;
/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Main {
    
    private static ArrayList<Agent> agentList;
    
    private static void initAgents() throws Exception {
        Agent.goalAgent = new GoalAgent(new Point(Configuration.getMaxX()/2, Configuration.getMaxY()/2));
        
        agentList = new ArrayList<Agent>();

        for(int i = 0; i < Configuration.getMaxAgents(); i++) {
            Point p;
            do {
                p = new Point(Misc.nextInt(Configuration.getMaxX()), Misc.nextInt(Configuration.getMaxY()));
            } while(!Agent.grid.isFreeField(p));
            agentList.add(new Agent(p));
        }
    }
    
    private static void loadAIClassifiers(String file_name) {
        for(Agent a : agentList) {
            a.classifierSet.loadClassifiersFromFile(file_name);
        }
    }
    
    private static void moveGoalAgent() throws Exception {
        Agent.goalAgent.moveRandomly();
        Agent.goalAgent.moveRandomly();
    }
    
    private static void evoluteAgents() throws Exception {
        if(Configuration.isDoEvolutionaryAlgorithm()) {
            for(int i = 0; i < agentList.size(); i++) {
                agentList.get(i).evolutionaryAlgorithm();
            }
        } else {
            for(int i = 0; i < agentList.size(); i++) {
                agentList.get(i).geneticAlgorithm();
            }
        }
    }

    
    // "Fenster" über die letzten X classifier fahren lassen?
    private static void calculateReward() {
        for(int i = 0; i < agentList.size(); i++) {
            agentList.get(i).calculateReward();
        }
    }
    
    private static boolean goalAgentInSight() {
        for(Agent a : agentList) {
            if(a.getReward() != 0.0) {
                return true;
            }
        }
        return false;
    }
    
    
    private static void calculateNextMove(long gaTimestep) throws Exception {
        for(int i = 0; i < agentList.size(); i++) {
            agentList.get(i).calculateNextMove(gaTimestep);
        }
    }    
    
    private static void printHeader(long currentTimestep) {
        Log.log("iteration " + currentTimestep + "\n");
        try {
            Log.log("input " + agentList.get(0).getInputString() + "\n");
        } catch(Exception e) {
            Log.errorLog("Error creating input string for log file: " + e);
        }        
    }
    
    private static void printAgents() {
        Log.log(agentList.get(0).toString() + "\n");
    }
    
    private static void printGrid(long currentTimestep) {
        Log.gridLog("iteration " + currentTimestep + "\n");
        try {
            Log.gridLog("input " + agentList.get(0).getInputString() + "\n");
        } catch(Exception e) {
            Log.errorLog("Error creating input string for log file: " + e);
        }
        Agent.grid.printGrid();                        
    }
    
    /*
     * Parameter list:
     * Number of agents
     * grid size
     * 
     * */
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Log.initialize(false);
        
        Configuration.initialize("default.txt");
        
        boolean ai_run = false;
        long currentTimestep = 0;
        int total_rounds_observed = 0;
        
        try {
            initAgents();
        } catch(Exception e) {
            Log.errorLog("Error initializing agents: " + e);
            e.printStackTrace();
        }
        
        if(ai_run) {
            loadAIClassifiers("AI_Classifiers4.AGT");
        }
        
        for(int i = 0; i < Configuration.getTotalSteps(); i++) {

            for(int j = 0; j < Configuration.getGaSteps(); j++) {
                currentTimestep++;
                
                calculateReward();

                if(goalAgentInSight()) {
                    total_rounds_observed++;
                }
                
                printHeader(currentTimestep);

                try {
                    calculateNextMove(currentTimestep);
                } catch(Exception e) {
                    Log.errorLog("Problem calculating next move: " + e);
                    e.printStackTrace();
                }
                    // GA Step alle X Schritte (Fenstergröße? Latency?)
               /* try {
                    moveGoalAgent();
                } catch(Exception e) {
                    Log.errorLog("Problem moving goal agent randomly: " + e);
                    e.printStackTrace();
                }*/
                
                if(agentList.size() > 0) {
                    printAgents();
                }
                printGrid(currentTimestep);                
            }
            
            if(!ai_run) 
            {
                try {
                    evoluteAgents();
                } catch(Exception e) {
                    Log.errorLog("Problem evolute agents: " + e);
                    e.printStackTrace();
                }
            }
        }
        
        System.out.println("TOTAL: " + total_rounds_observed);
        
        Log.finalise();
    }
}
