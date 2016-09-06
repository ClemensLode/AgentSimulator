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

    private void evoluteAgents() throws Exception {
        if (Configuration.isDoEvolutionaryAlgorithm()) {
            for (int i = 0; i < agentList.size(); i++) {
                agentList.get(i).evolutionaryAlgorithm();
            }
        } else {
            for (int i = 0; i < agentList.size(); i++) {
                agentList.get(i).geneticAlgorithm();
            }
        }
    }

    // "Fenster" über die letzten X classifier fahren lassen?
    private void calculateReward() {
        for (int i = 0; i < agentList.size(); i++) {
            agentList.get(i).calculateReward();
        }
    }

    private boolean goalAgentInSight() {
        for (Agent a : agentList) {
            if (a.getReward() != 0.0) {
                return true;
            }
        }
        return false;
    }

    private void calculateNextMove(long gaTimestep) throws Exception {
        for (int i = 0; i < agentList.size(); i++) {
            agentList.get(i).calculateNextMove(gaTimestep);
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

        for (int i = 0; i < Configuration.getTotalRuns(); i++) {
            do_explore = !do_explore;
            currentTimestep = doOneMultiStepProblem(do_explore, currentTimestep);
        }
        
        // also print final step
        printStep(currentTimestep);
    }
    
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

    private int doOneMultiStepProblem(boolean do_explore, int stepCounter) {
        for (int currentTimestep = stepCounter; currentTimestep < Configuration.getGaSteps() + stepCounter; currentTimestep++) {

            // update the quality of the run
            Agent.grid.checkGoalAgentInSight();

            printStep(currentTimestep);

            try {
                calculateNextMove(currentTimestep);
            } catch (Exception e) {
                Log.errorLog("Problem calculating next move: ", e);
            }

            try {
                calculateReward();
            } catch (Exception e) {
                Log.errorLog("Problem calculating reward:", e);
            }

            // GA Step alle X Schritte (Fenstergröße? Latency?)
            try {
                moveGoalAgent();
            } catch (Exception e) {
                Log.errorLog("Problem moving goal agent randomly: ", e);
            }
        }

        // TODO
        if (do_explore) {
            try {
                evoluteAgents();
            } catch (Exception e) {
                Log.errorLog("Problem evolute agents: ", e);
            }
        }

        return stepCounter + Configuration.getGaSteps();
    }
}
