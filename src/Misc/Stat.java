package Misc;

import lcs.ClassifierSet;
import agent.Configuration;

/**
 *
 * Class for a single statistics entry
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Stat {

    public Stat() {
        currentTimestep = 0;
        goalAgentObserved = 0;
        averageAgentDistance = 0;
        spreadAgentDistance = 0;
        averageGoalAgentDistance = 0;
        spreadGoalAgentDistance = 0;
        coveredAreaFactor = 0;
        wastedCoverage = 0;
        goalJumps = 0;
        wastedMovements = 0;
        spreadIndividualTotalPoints = 0;
        averageIndividualTotalPoints = 0;
        averagePredictionError = 0.0;
        bestLCS = null;
    }

    public Stat(
            long current_time_step,
            ClassifierSet best_lcs,
            double goal_agent_observed,
            double average_agent_distance,
            double spread_agent_distance,
            double average_goal_agent_distance,
            double spread_goal_agent_distance,
            double covered_area_factor,
            double wasted_coverage,
            double goal_jumps,
            double wasted_movements,
            double average_individual_points,
            double spread_individual_total_points,
            double average_prediction_error) throws Exception {
        currentTimestep = current_time_step;
        if (best_lcs == null) {
            bestLCS = null;
        } else if ((current_time_step % Configuration.getNumberOfSteps()) == (Configuration.getNumberOfSteps() - 1)) {
            bestLCS = best_lcs.clone_it();
        }
        goalAgentObserved = goal_agent_observed;
        averageAgentDistance = average_agent_distance;
        spreadAgentDistance = spread_agent_distance;
        averageGoalAgentDistance = average_goal_agent_distance;
        spreadGoalAgentDistance = spread_goal_agent_distance;
        coveredAreaFactor = covered_area_factor;
        wastedCoverage = wasted_coverage;
        goalJumps = goal_jumps;
        wastedMovements = wasted_movements;
        spreadIndividualTotalPoints = spread_individual_total_points;
        averageIndividualTotalPoints = average_individual_points;
        averagePredictionError = average_prediction_error;
    }

    public void add(Stat s) throws Exception {
        if (s.bestLCS != null) {
            if (bestLCS == null || s.bestLCS.getAverageFitness() > bestLCS.getAverageFitness()) {
                bestLCS = s.getBestLCS();
            }
        }
        goalAgentObserved += s.goalAgentObserved;
        averageAgentDistance += s.averageAgentDistance;
        spreadAgentDistance += s.spreadAgentDistance;
        averageGoalAgentDistance += s.averageGoalAgentDistance;
        spreadGoalAgentDistance += s.spreadGoalAgentDistance;
        coveredAreaFactor += s.coveredAreaFactor;
        wastedCoverage += s.wastedCoverage;
        goalJumps += s.getGoalJumps();
        wastedMovements += s.wastedMovements;
        spreadIndividualTotalPoints += s.spreadIndividualTotalPoints;
        averageIndividualTotalPoints += s.averageIndividualTotalPoints;
        averagePredictionError += s.averagePredictionError;
    }

    public void divide(double d) {
        goalAgentObserved /= d;
        averageAgentDistance /= d;
        spreadAgentDistance /= d;
        averageGoalAgentDistance /= d;
        spreadGoalAgentDistance /= d;
        coveredAreaFactor /= d;
        wastedCoverage /= d;
        goalJumps /= d;
        wastedMovements /= d;
        spreadIndividualTotalPoints /= d;
        averageIndividualTotalPoints /= d;
        averagePredictionError /= d;
    }

    public Stat clone_it() throws Exception {
        return new Stat(
                currentTimestep,
                bestLCS,
                goalAgentObserved,
                averageAgentDistance,
                spreadAgentDistance,
                averageGoalAgentDistance,
                spreadGoalAgentDistance,
                coveredAreaFactor,
                wastedCoverage,
                getGoalJumps(),getWastedMovements(),
                averageIndividualTotalPoints,
                spreadIndividualTotalPoints,
                averagePredictionError);
    }

    private ClassifierSet bestLCS;
    private long currentTimestep;
    private double goalAgentObserved;
    /**
     * spread of individual total points
     */
    private double spreadIndividualTotalPoints;
    /**
     * average of all individual total points
     */
    private double averageIndividualTotalPoints;
    /**
     * spread of the distances to each other
     */
    private double spreadAgentDistance;
    private double spreadGoalAgentDistance;
    /**
     * average distance between all agents
     */
    private double averageAgentDistance;
    /**
     * average distance between all agents and the goal
     */
    private double averageGoalAgentDistance;
    /**
     * currently covered area / optimal coverable area
     */
    private double coveredAreaFactor;

    /**
     * Amount of field that are covered by more than one agents at the same time
     */
    private double wastedCoverage;

    /**
     * Numer of goal jumps 
     */
    private double goalJumps;

    /**
     * Percentage of movements that failed
     */
    private double wastedMovements;

    /**
     * average prediction error (all agents)
     */
    private double averagePredictionError;

    public double getSpreadIndividualTotalPoints() {
        return spreadIndividualTotalPoints;
    }

    public double getAverageIndividualTotalPoints() {
        return averageIndividualTotalPoints;
    }

    public double getSpreadAgentDistance() {
        return spreadAgentDistance;
    }

    public double getAverageAgentDistance() {
        return averageAgentDistance;
    }

    public double getAverageGoalAgentDistance() {
        return averageGoalAgentDistance;
    }

    public double getCoveredAreaFactor() {
        return coveredAreaFactor;
    }

    public double getSpreadGoalAgentDistance() {
        return spreadGoalAgentDistance;
    }

    public double getGoalAgentObserved() {
        return goalAgentObserved;
    }

    public long getCurrentTimestep() {
        return currentTimestep;
    }

    public ClassifierSet getBestLCS() {
        return bestLCS;
    }

    /**
     * @return the wastedCoverage
     */
    public double getWastedCoverage() {
        return wastedCoverage;
    }

    /**
     * @return the averagePredictionError
     */
    public double getAveragePredictionError() {
        return averagePredictionError;
    }

    /**
     * @return the wastedMovements
     */
    public double getWastedMovements() {
        return wastedMovements;
    }

    /**
     * @return the goalJumps
     */
    public double getGoalJumps() {
        return goalJumps;
    }
};
