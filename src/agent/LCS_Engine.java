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
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
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

        final int max_classifiers = Configuration.getMaxPopSize()+Action.MAX_DIRECTIONS;

        if(Configuration.getGoalAgentMovementType() == Configuration.XCS_MOVEMENT) {
            BaseAgent.goalAgent = new SXCS_Goal_Agent(max_classifiers);
        } else {
            BaseAgent.goalAgent = new Random_Agent(Configuration.getGoalAgentMovementType(), true);
        }

        agentList = new ArrayList<BaseAgent>(Configuration.getMaxAgents());

        for (int i = 0; i < Configuration.getMaxAgents(); i++) {
            switch (Configuration.getAgentType()) {
                case Configuration.RANDOMIZED_MOVEMENT_AGENT_TYPE:agentList.add(new Random_Agent(Configuration.RANDOM_MOVEMENT, false));break;
                case Configuration.SIMPLE_AI_AGENT_TYPE:agentList.add(new EinfacheHeuristik_Agent());break;
                case Configuration.INTELLIGENT_AI_AGENT_TYPE:agentList.add(new IntelligenteHeuristik_Agent());break;
                case Configuration.DSXCS_AGENT_TYPE:agentList.add(new DSXCS_Agent(max_classifiers));break;
                case Configuration.SXCS_AGENT_TYPE:agentList.add(new SXCS_Agent(max_classifiers));break;
                case Configuration.XCS_AGENT_TYPE:agentList.add(new XCS_Agent(max_classifiers));break;
            }
        }
    }

    /**
     * Executes a number of problems
     * @param experiment_nr Number of experiment, important for initializing the random seed
     * @throws java.lang.Exception if there was an error creating the gif file or calculating the problem
     */
    public void doOneMultiStepExperiment(double[] average_cover_actions, int experiment_nr) throws Exception {
        int currentTimestep = 0;

        if(Configuration.isGifOutput()) {
            BaseAgent.grid.startGIF(experiment_nr);
        }
        // number of problems for the same population

        for (int i = 0; i < Configuration.getNumberOfProblems(); i++) {

            //Log.log("# Problem Nr. " + (i + 1));

            /**
             * creates a new grid and deploys agents and goal at random positions
             */
            BaseAgent.grid.resetState();

            Base_XCS_Agent.cover_actions = 0;
            currentTimestep = doOneMultiStepProblem(currentTimestep);
            average_cover_actions[i] += Base_XCS_Agent.cover_actions;

            Misc.initSeed(Configuration.getRandomSeed() + experiment_nr * Configuration.getNumberOfProblems() + 1 + i);
        }
        if(Configuration.isGifOutput()) {
            BaseAgent.grid.finishGIF();
        }
    }

    /**
     * Executes a number of steps on the grid
     * @param stepCounter current time step
     * @return the time step after the execution
     */
    private int doOneMultiStepProblem(int stepCounter) throws Exception {
        // number of steps a problem should last
        int steps_next_problem = Configuration.getNumberOfSteps() + stepCounter;
        //System.out.println(stepCounter);

        for (int currentTimestep = stepCounter; currentTimestep < steps_next_problem; currentTimestep++) {
            BaseAgent.grid.updateSight();
            // update the quality of the run
            BaseAgent.grid.updateStatistics(currentTimestep, findBestAgent());

            if(Log.isDoLog()) {
                printHeader(currentTimestep);
                BaseAgent.grid.printAgents();
            }

            if(Configuration.isGifOutput()) {
                BaseAgent.grid.addFrameToGIF();
            }

            calculateAgents(currentTimestep);

            if(currentTimestep > stepCounter) {
                // calculate the reward of all agents
                rewardAgents(currentTimestep);
            }

            moveAgents(currentTimestep);
        }
        BaseAgent.grid.updateSight();
        rewardAgents(steps_next_problem);

        return steps_next_problem;
    }

    /**
     * @param gaTimestep current time step
     * @throws java.lang.Exception
     */
    private void calculateAgents(long gaTimestep) throws Exception {
        for(BaseAgent a : agentList) {
            a.aquireNewSensorData();
            a.calculateNextMove(gaTimestep);
        }
        BaseAgent.goalAgent.aquireNewSensorData();
        BaseAgent.goalAgent.calculateNextMove(gaTimestep);
    }

    private ClassifierSet findBestAgent() throws Exception {
        ClassifierSet best = null;
        switch(Configuration.getAgentType()) {
            case Configuration.RANDOMIZED_MOVEMENT_AGENT_TYPE:
            case Configuration.SIMPLE_AI_AGENT_TYPE:
            case Configuration.INTELLIGENT_AI_AGENT_TYPE:
                if(Configuration.getGoalAgentMovementType() == Configuration.XCS_MOVEMENT) {
                    return ((Base_XCS_Agent)(BaseAgent.goalAgent)).getClassifierSet();
                }
                return null;
        }
        double best_fit = 0.0;
        for(BaseAgent a : agentList) {
            double t = ((Base_XCS_Agent)a).getFitnessNumerosity();
            if(best == null || t > best_fit) {
                best_fit = t;
                best = ((Base_XCS_Agent)a).getClassifierSet();
            }
        }
        return best;
    }

    /**
     * Calculate the matchings and the action set of each agent and execute the
     * movement
     */
    private void moveAgents(long gaTimestep) throws Exception {
        int goal_speed = (int)Configuration.getGoalAgentMovementSpeed();

        double prop_one_more = Configuration.getGoalAgentMovementSpeed() - ((double)goal_speed);
        if(prop_one_more > 0.0) {
            if(Misc.nextDouble() <= prop_one_more) {
                goal_speed++;
            }
        }
        ArrayList<BaseAgent> random_list = new ArrayList<BaseAgent>(agentList.size() + goal_speed);

        random_list.addAll(agentList);
        for(int i = 0; i < goal_speed; i++) {
            random_list.add(BaseAgent.goalAgent);
        }

        int[] array = Misc.getRandomArray(random_list.size());

        for(int i = 0; i < array.length; i++) {
            BaseAgent a = random_list.get(array[i]);
            try {
                a.doNextMove();
                // will there be another goal move?
                if(a.isGoalAgent() && goal_speed > 1) {
                    goal_speed--;
                    a.aquireNewSensorData();
                    a.calculateNextMove(gaTimestep);
                    a.calculateReward(gaTimestep);
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
        Log.log(SXCS_Agent.grid.getGridString());
    }
}
