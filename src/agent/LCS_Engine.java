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
        Agent.grid = new Grid();
        Agent.resetID();
        Agent.goalAgent = new Agent(new Point(Configuration.getMaxX() / 2, Configuration.getMaxY() / 2));

        agentList = new ArrayList<Agent>();

        for (int i = 0; i < Configuration.getMaxAgents(); i++) {
            Point p;
            do {
                p = new Point(Misc.nextInt(Configuration.getMaxX()), Misc.nextInt(Configuration.getMaxY()));
            } while (!Agent.grid.isFreeField(p));
            agentList.add(new Agent(p));
        }
    }
/*
    private void loadAIClassifiers(String file_name) {
        for (Agent a : agentList) {
            a.classifierSet.loadClassifiersFromFile(file_name);
        }
    }*/

    private void moveGoalAgent() throws Exception {
        Agent.goalAgent.moveRandomly();
        Agent.goalAgent.moveRandomly();
    }

    private void evoluteAgents(long gaTimestep) throws Exception {
        for(Agent a : agentList) {
            a.evolutionaryAlgorithm(gaTimestep);
        }
    }

    // "Fenster" über die letzten X classifier fahren lassen?
    private void calculateIndividualRewards() {
        for(Agent a : agentList) {
            a.calculateReward();
        }
    }
    
    private void calculateAgents(boolean do_explore, long gaTimestep) {
        for(Agent a : agentList) {
            try {
                a.calculateNextMove(do_explore, gaTimestep);
            } catch (Exception e) {
                Log.errorLog("Problem calculating next move: ", e);
            }

            a.printHeader();
            a.printMatching();
            
            try {
                a.doNextMove();            
            } catch (Exception e) {
                Log.errorLog("Problem executing next move: ", e);
            }
            
            a.printAction();
            
            try {
                calculateIndividualRewards();
            } catch (Exception e) {
                Log.errorLog("Problem calculating reward:", e);
            }
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
            
            Log.log("# Problem Nr. " + (i+1) + "\n");
            System.out.println("Problem Nr."+(i+1));
            
            do_explore = !do_explore;
            
            currentTimestep = doOneMultiStepProblem(do_explore, currentTimestep);
        }
        
        Agent.grid.checkGoalAgentInSight();
        // also print final step TODO
    }



    private int doOneMultiStepProblem(boolean do_explore, int stepCounter) {

        // number of steps a problem should last
        for (int currentTimestep = stepCounter; currentTimestep < Configuration.getNumberOfSteps() + stepCounter; currentTimestep++) {
            printHeader(currentTimestep);
            // update the quality of the run
            Agent.grid.checkGoalAgentInSight();

            calculateAgents(do_explore, currentTimestep);
            
            // move the goal agent after we moved the agents and calculated the reward
            // easier for the agents? TODO
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
    
    
    private void printHeader(long currentTimestep) {
        Log.log("# -------------------------\n");
        Log.log("iteration " + currentTimestep + "\n");
        Log.log("# -------------------------\n\n");
    }
    
 
}
