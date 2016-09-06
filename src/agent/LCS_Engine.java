package agent;

import agents.*;
import lcs.Action;
import Misc.Log;
import Misc.Misc;
import lcs.ClassifierSet;
import java.util.ArrayList;

/**
 *
 * This class provides routines to initialize the engine, run the experiments
 * and log the results and statistics
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class LCS_Engine {

    private ArrayList<BaseAgent> agentList;

    /**
     * Initializes a new LCS engine
     * resets the id, the goal agent and creates a new random agent list
     * @param experiment_nr Number of experiment, import for initializing the random seed
     * @throws java.lang.Exception if there was an error registering the agents
     */
    public LCS_Engine(int experiment_nr) throws Exception {
        Misc.initSeed(Configuration.getRandomSeed() + experiment_nr * Configuration.getNumberOfProblems());
        BaseAgent.grid = new Grid();
        BaseAgent.resetGlobalID();

        final int max_classifiers = Configuration.getMaxPopSize()+Action.MAX_ACTIONS+1;

        if(Configuration.getGoalAgentMovementType() == Configuration.LCS_MOVEMENT) {
            BaseAgent.goalAgent = new LCS_Goal_Agent(max_classifiers);
        } else {
            BaseAgent.goalAgent = new Random_Agent(Configuration.getGoalAgentMovementType(), true);
        }

        agentList = new ArrayList<BaseAgent>(Configuration.getMaxAgents());

        for (int i = 0; i < Configuration.getMaxAgents(); i++) {
            switch (Configuration.getAgentType()) {
                case Configuration.RANDOMIZED_MOVEMENT_AGENT_TYPE:agentList.add(new Random_Agent(Configuration.RANDOM_MOVEMENT, false));break;
                case Configuration.SIMPLE_AI_AGENT_TYPE:agentList.add(new AI_Agent());break;
                case Configuration.INTELLIGENT_AI_AGENT_TYPE:agentList.add(new Good_AI_Agent());break;
                case Configuration.NEW_LCS_AGENT_TYPE:agentList.add(new New_LCS_Agent(max_classifiers));break;
                case Configuration.OLD_LCS_AGENT_TYPE:agentList.add(new LCS_Agent(max_classifiers));break;
                case Configuration.MULTISTEP_LCS_AGENT_TYPE:agentList.add(new Multistep_LCS_Agent(max_classifiers));break;
                case Configuration.SINGLE_LCS_AGENT_TYPE:agentList.add(new Single_LCS_Agent());break;
            }
        }
        if(Configuration.getAgentType() == Configuration.SINGLE_LCS_AGENT_TYPE) {
            Single_LCS_Agent.initSingleLCSAgent(max_classifiers);
        }
    }

    /**
     * Executes a number of problems
     * @param experiment_nr Number of experiment, important for initializing the random seed
     * @throws java.lang.Exception if there was an error creating the gif file or calculating the problem
     */
    public void doOneMultiStepExperiment(int experiment_nr) throws Exception {
        int currentTimestep = 0;

        if(Configuration.isGifOutput()) {
            BaseAgent.grid.startGIF(experiment_nr);
        }
        // number of problems for the same population
        for (int i = 0; i < Configuration.getNumberOfProblems(); i++) {

            Log.log("# Problem Nr. " + (i + 1));

            /**
             * creates a new grid and deploys agents and goal at random positions
             */
            BaseAgent.grid.resetState();

            /**
             * Unterschied zu XCS: Läuft weiter...~
             */
            currentTimestep = doOneMultiStepProblem(currentTimestep);
            Misc.initSeed(Configuration.getRandomSeed() + experiment_nr * Configuration.getNumberOfProblems() + 1 + i);
        }
        if(Configuration.isGifOutput()) {
            BaseAgent.grid.finishGIF();
        }
    }

    /**
     * Executes a number of steps on the grid
     * @param do_explore whether to use the evolutionary algorithm
     * @param stepCounter current time step
     * @return the time step after the execution
     */
    private int doOneMultiStepProblem(int stepCounter) throws Exception {
        // number of steps a problem should last
        int steps_next_problem = Configuration.getNumberOfSteps() + stepCounter;
        System.out.println(stepCounter);
        for (int currentTimestep = stepCounter; currentTimestep < steps_next_problem; currentTimestep++) {
            printHeader(currentTimestep);

            if(Configuration.isGifOutput()) {
                BaseAgent.grid.addFrameToGIF();
            }

            calculateAgents(currentTimestep);
            BaseAgent.grid.updateSight();

            // calculate the reward of all agents
            rewardAgents(currentTimestep);

            // update the quality of the run
            BaseAgent.grid.updateStatistics(currentTimestep, findBestAgent());
            if(Log.isDoLog()) {
                BaseAgent.grid.printAgents();
            }
        }

        return steps_next_problem;
    }

    private ClassifierSet findBestAgent() {
        ClassifierSet best = null;
        switch(Configuration.getAgentType()) {
            case Configuration.RANDOMIZED_MOVEMENT_AGENT_TYPE:
            case Configuration.SIMPLE_AI_AGENT_TYPE:
            case Configuration.INTELLIGENT_AI_AGENT_TYPE:
                if(Configuration.getGoalAgentMovementType() == Configuration.LCS_MOVEMENT) {
                    return ((Base_LCS_Agent)(BaseAgent.goalAgent)).getClassifierSet();
                }
                return null;
            case Configuration.SINGLE_LCS_AGENT_TYPE:
                return Single_LCS_Agent.classifierSet;
        }
        double best_fit = 0.0;
        for(BaseAgent a : agentList) {
            double t = ((Base_LCS_Agent)a).getFitnessNumerosity();
            if(best == null || t > best_fit) {
                best_fit = t;
                best = ((Base_LCS_Agent)a).getClassifierSet();
            }
        }
        return best;
    }

    /**
     * Calculate the matchings and the action set of each agent and execute the
     * movement
     * @param gaTimestep current time step
     */
    private void calculateAgents(final long gaTimestep) throws Exception {
        BaseAgent.mark = false;

        int goal_speed = Configuration.getGoalAgentMovementSpeed();
        ArrayList<BaseAgent> random_list = new ArrayList<BaseAgent>(agentList.size() + goal_speed);

        random_list.addAll(agentList);
        for(int i = 0; i < goal_speed; i++) {
            random_list.add(BaseAgent.goalAgent);
        }

        for(BaseAgent a : random_list) {
            a.aquireNewSensorData();
            a.calculateNextMove(gaTimestep);
        }

        int[] array = Misc.getRandomArray(random_list.size());

        for(int i = 0; i < array.length; i++) {
            BaseAgent a = random_list.get(array[i]);
            try {
                a.doNextMove();

                if(a.isGoalAgent()) {
                    a.aquireNewSensorData();
                    a.calculateNextMove(0);
                }

            } catch (Exception e) {
                Log.errorLog("Problem executing next move: ", e);
            }
        }
    }

    /**
     * Rewards all agents
     * @throws java.lang.Exception if there was an error moving the agent
     */
    private void rewardAgents(final long gaTimestep) throws Exception {
        for(BaseAgent a : agentList) {
            a.calculateReward(gaTimestep);
        }
        BaseAgent.goalAgent.calculateReward(gaTimestep);
    }

    /**
     * Prints the header of the log file
     * @param currentTimestep Current time step
     */
    private void printHeader(long currentTimestep) throws Exception {
        if(!Log.isDoLog()) {
            return;
        }

        Log.log("# -------------------------");
        Log.log("iteration " + currentTimestep);
        Log.log("# -------------------------\n");
        Log.log("# grid");
        Log.log(LCS_Agent.grid.getGridString());
    }
}
