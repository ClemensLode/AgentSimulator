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
        spreadIndividualTotalPoints = 0;
        averageIndividualTotalPoints = 0;
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
            double average_individual_points,
            double spread_individual_total_points) {
        currentTimestep = current_time_step;
        if (best_lcs == null || (current_time_step % Configuration.getNumberOfSteps()) != (Configuration.getNumberOfSteps() - 1)) {
            bestLCS = null;
        } else {
            bestLCS = best_lcs.clone();
        }
        goalAgentObserved = goal_agent_observed;
        averageAgentDistance = average_agent_distance;
        spreadAgentDistance = spread_agent_distance;
        averageGoalAgentDistance = average_goal_agent_distance;
        spreadGoalAgentDistance = spread_goal_agent_distance;
        coveredAreaFactor = covered_area_factor;
        spreadIndividualTotalPoints = spread_individual_total_points;
        averageIndividualTotalPoints = average_individual_points;
    }

    public void add(Stat s) {
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
        spreadIndividualTotalPoints += s.spreadIndividualTotalPoints;
        averageIndividualTotalPoints += s.averageIndividualTotalPoints;
    }

    public void divide(double d) {
        goalAgentObserved /= d;
        averageAgentDistance /= d;
        spreadAgentDistance /= d;
        averageGoalAgentDistance /= d;
        spreadGoalAgentDistance /= d;
        coveredAreaFactor /= d;
        spreadIndividualTotalPoints /= d;
        averageIndividualTotalPoints /= d;
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
};
