package agent;

import java.util.ArrayList;
import java.awt.Point;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class LCS_Engine {

    private ArrayList<Agent> agentList;

    public LCS_Engine() throws Exception {
        Agent.goalAgent = new GoalAgent(new Point(Configuration.getMaxX() / 2, Configuration.getMaxY() / 2));
        Agent.grid = new Grid();

        agentList = new ArrayList<Agent>();

        for (int i = 0; i < Configuration.getMaxAgents(); i++) {
            Point p;
            do {
                p = new Point(Misc.nextInt(Configuration.getMaxX()), Misc.nextInt(Configuration.getMaxY()));
            } while (!Agent.grid.isFreeField(p));
            agentList.add(new Agent(p));
        }
    }

    private void loadAIClassifiers(String file_name) {
        for (Agent a : agentList) {
            a.classifierSet.loadClassifiersFromFile(file_name);
        }
    }

    private void moveGoalAgent() throws Exception {
        Agent.goalAgent.moveRandomly();
        Agent.goalAgent.moveRandomly();
    }

    private void evoluteAgents(long gaTimestep) throws Exception {
        for (int i = 0; i < agentList.size(); i++) {
            agentList.get(i).evolutionaryAlgorithm(gaTimestep);
        }
    }

    // "Fenster" über die letzten X classifier fahren lassen?
    private void calculateReward() {
        for (int i = 0; i < agentList.size(); i++) {
            agentList.get(i).calculateReward();
        }
    }

    private void calculateNextMove(boolean do_explore, long gaTimestep) throws Exception {
        for (int i = 0; i < agentList.size(); i++) {
            agentList.get(i).calculateNextMove(do_explore, gaTimestep);
        }
    }

    /*
     * Parameter list:
     * Number of agents
     * grid size
     * 
     * */
    /************************** Multi-step Experiments *******************************/
    /**
     * Executes one multi step experiment and monitors the performance.
     *
     * @see #doOneMultiStepProblemExplore
     * @see #doOneMultiStepProblemExploit
     * @see #writePerformance
     */
    public void doOneMultiStepExperiment() {
        boolean do_explore = false;
        int currentTimestep = 0;

        // number of problems for the same population
        for (int i = 0; i < Configuration.getNumberOfProblems(); i++) {
            // switch parameter for exploiting and exploring for each problem
            // TODO Literatur!?
            do_explore = !do_explore;
            
            currentTimestep = doOneMultiStepProblem(do_explore, currentTimestep);
        }
        
        Agent.grid.checkGoalAgentInSight();
        // also print final step
        printStep(currentTimestep);
    }


    private int doOneMultiStepProblem(boolean do_explore, int stepCounter) {

        // number of steps a problem should last
        for (int currentTimestep = stepCounter; currentTimestep < Configuration.getNumberOfSteps() + stepCounter; currentTimestep++) {

            // update the quality of the run
            Agent.grid.checkGoalAgentInSight();

            printStep(currentTimestep);

            try {
                calculateNextMove(do_explore, currentTimestep);
            } catch (Exception e) {
                Log.errorLog("Problem calculating next move: ", e);
            }

            try {
                calculateReward();
            } catch (Exception e) {
                Log.errorLog("Problem calculating reward:", e);
            }

            try {
                moveGoalAgent();
            } catch (Exception e) {
                Log.errorLog("Problem moving goal agent randomly: ", e);
            }

            if (do_explore) {
                try {
                    evoluteAgents(currentTimestep);
                } catch (Exception e) {
                    Log.errorLog("Problem evoluting agents: ", e);
                }
            }
        }

        return stepCounter + Configuration.getNumberOfSteps();
    }
    
    
    /**
     * Log the current time step
     * @param currentTimestep
     */
    private void printStep(long currentTimestep) {
        Log.log("# -------------------------\n");
        Log.log("iteration " + currentTimestep + "\n");
        Log.log("# -------------------------\n");
        try {
            Log.log("# input\n");
            Log.log(Agent.grid.getInputStrings() + "\n");
            Log.log("# grid\n");
            Log.log(Agent.grid.getGridString() + "\n");
        } catch(Exception e) {
            Log.errorLog("Error creating input string for log file: ", e);
        }
        
        Log.log("# agents\n");
        Log.log(Agent.grid.getAgentStrings() + "\n");
    }    
}
