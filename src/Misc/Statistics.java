package Misc;

import java.util.ArrayList;
import agent.Configuration;
import lcs.ClassifierSet;

/**
 * This class logs the statistics
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Statistics {

    /**
     * current experiment number
     */
    private static int experiment_count = 0;

    /**
     * This class is static only, no instantiation please
     */
    private Statistics() {
    }

    /**
     * Prepare for next experiment
     */
    public static void nextExperiment() {
        experiment_count++;
        stats.clear();
    }
    /**
     * Possible statistics:
     * Each step:
     * - statistical spread of individual total points
     * - statistical spread of agent positions
     * - statistical spread of goal agent
     * 
     * - average distance to goal
     * - average distance to each other
     * - covered area / optimal coverable area
     *
     */
    private static ArrayList<Stat> stats;
    private static ArrayList<Stat> average_stats;

    /**
     * Add one set of data
     * @param current_time_step
     * @param in_sight
     * @param average_agent_distance
     * @param spread_agent_distance
     * @param average_goal_agent_distance
     * @param spread_goal_agent_distance
     * @param covered_area_factor
     * @param average_individual_points
     * @param spread_individual_total_points
     */
    public static void addStatisticEntry(
            long current_time_step,
            ClassifierSet c_set,
            boolean in_sight,
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

        Stat t = new Stat(
                current_time_step,
                c_set,
                in_sight? 1 : 0,
                average_agent_distance,
                spread_agent_distance,
                average_goal_agent_distance,
                spread_goal_agent_distance,
                covered_area_factor,
                wasted_coverage,
                goal_jumps,
                wasted_movements,
                average_individual_points,
                spread_individual_total_points,
                average_prediction_error);
        stats.add(t);
        // first experiment?
        if(average_stats.size() < stats.size()) {
            average_stats.add(t);
        } else {
            average_stats.get(stats.size()-1).add(t);
        } 
    }

    /**
     * Re-initialize after configuration change
     */
    public static void initialize() {
        stats = new ArrayList<Stat>(1+Configuration.getTotalTimeSteps());
        average_stats = new ArrayList<Stat>(1+Configuration.getTotalTimeSteps());
    }

    /**
     * At the end of each experiment print the running average statistic in the log files
     */
    public static void printAverageStatistics() {
        
        for(Stat s : average_stats) {
            s.divide((double)experiment_count);
        }
        System.out.println("stats prepared.");

        try {

        int counter = 0;
        /*
        Log.newCustomLog(Misc.getBaseFileName("points_spread"));
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getSpreadIndividualTotalPoints() + "\n");
            counter++;
        }
        Log.closeCustomLog();

        Log.newCustomLog(Misc.getBaseFileName("points_average"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getAverageIndividualTotalPoints() + "\n");
            counter++;
        }
        Log.closeCustomLog();

        Log.newCustomLog(Misc.getBaseFileName("distance_spread"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getSpreadAgentDistance() + "\n");
            counter++;
        }
        Log.closeCustomLog();
        
        Log.newCustomLog(Misc.getBaseFileName("goal_agent_distance_spread"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getSpreadGoalAgentDistance() + "\n");
            counter++;
        }
        Log.closeCustomLog();
        
        Log.newCustomLog(Misc.getBaseFileName("distance_average"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getAverageAgentDistance() + "\n");
            counter++;
        }
        Log.closeCustomLog();
        
        Log.newCustomLog(Misc.getBaseFileName("goal_agent_distance_average"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getAverageGoalAgentDistance() + "\n");
            counter++;
        }
        Log.closeCustomLog();
        
        Log.newCustomLog(Misc.getBaseFileName("covered_area"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getCoveredAreaFactor() + "\n");
            counter++;
        }
        Log.closeCustomLog();*/

        Log.newCustomLog(Misc.getBaseFileName("goal_percentage"));
        counter = 0;
        for(Stat s : average_stats) {
            Log.customLog("" + counter + " " + s.getGoalAgentObserved()*100.0 + "\n");
            counter++;
        }
        Log.closeCustomLog();

        System.out.println("stats compiled.");


        
        {
            String entry = new String("");
            for(int i = 0; i < Configuration.getNumberOfProblems(); i++) {
                ClassifierSet t = average_stats.get(i * Configuration.getNumberOfSteps() + Configuration.getNumberOfSteps() - 1).getBestLCS();
                if(t != null) {
                    entry += "Best after " + (i * Configuration.getNumberOfSteps() + Configuration.getNumberOfSteps() - 1) + " steps\n\n";
                    entry += t.toString() + "\n\n\n";
                }
            }
            if(!entry.isEmpty()) {
                Log.newCustomLog(Misc.getBaseFileName("LCS"));
                Log.customLog(entry);
                Log.closeCustomLog();
            }
        }

        Log.newCustomLog(Misc.getBaseFileName("results"));
        Stat average_average_stat = new Stat();
        for(Stat s : average_stats) {
            average_average_stat.add(s);
        }
        average_average_stat.divide(average_stats.size());

        String entry = new String("");
        entry += average_average_stat.getSpreadIndividualTotalPoints() + "\n";
        entry += average_average_stat.getAverageIndividualTotalPoints() + "\n";
        entry += average_average_stat.getSpreadAgentDistance() + "\n";
        entry += average_average_stat.getSpreadGoalAgentDistance() + "\n";
        entry += average_average_stat.getAverageAgentDistance() + "\n";
        entry += average_average_stat.getAverageGoalAgentDistance() + "\n";
        entry += average_average_stat.getAveragePredictionError() + "\n";
        entry += average_average_stat.getCoveredAreaFactor() + "\n";
        entry += average_average_stat.getWastedCoverage()*100.0 + "\n";
        entry += average_average_stat.getGoalJumps() * 100.0 + "\n";
        entry += average_average_stat.getWastedMovements()*100.0 + "\n";
        entry += average_average_stat.getGoalAgentObserved()*100.0 + "\n";
        Log.customLog(entry);
        Log.closeCustomLog();

        System.out.println("stats compiled.");

        Log.newCustomLog(Misc.getBaseFileName("half_results"));
        average_average_stat = new Stat();
        for(Stat s : average_stats) {
            if(s.getCurrentTimestep() % Configuration.getNumberOfSteps() >= Configuration.getNumberOfSteps() / 2) {
                average_average_stat.add(s);
            }
        }
        average_average_stat.divide(average_stats.size() / 2);

        entry = new String("");
        entry += average_average_stat.getSpreadIndividualTotalPoints() + "\n";
        entry += average_average_stat.getAverageIndividualTotalPoints() + "\n";
        entry += average_average_stat.getSpreadAgentDistance() + "\n";
        entry += average_average_stat.getSpreadGoalAgentDistance() + "\n";
        entry += average_average_stat.getAverageAgentDistance() + "\n";
        entry += average_average_stat.getAverageGoalAgentDistance() + "\n";
        entry += average_average_stat.getAveragePredictionError() + "\n";
        entry += average_average_stat.getCoveredAreaFactor() + "\n";
        entry += average_average_stat.getWastedCoverage()*100.0 + "\n";
        entry += average_average_stat.getGoalJumps() * 100.0 + "\n";
        entry += average_average_stat.getWastedMovements() * 100.0 + "\n";
        entry += average_average_stat.getGoalAgentObserved()*100.0 + "\n";
        Log.customLog(entry);
        Log.closeCustomLog();


        entry = new String("");
        ArrayList<Double> entry_list = new ArrayList<Double>();
        double last = 0.0;
        for(int i = 0; i < average_stats.size(); i++) {
            average_average_stat = new Stat();
            int max = Configuration.getNumberOfSteps() - 1 > i ? i : Configuration.getNumberOfSteps() - 1;
            for(int j = 0; j < 1 + max; j++) {
                average_average_stat.add(average_stats.get(i - j));
            }
            average_average_stat.divide(max+1);
            double new_entry = average_average_stat.getGoalAgentObserved();
//            if(last < new_entry) {
                last = new_entry;
//            }
            entry_list.add(last);
        }

        Log.newCustomLog(Misc.getBaseFileName("100_goal_agent_observed"));
        
        /*entry += average_average_stat.getSpreadIndividualTotalPoints() + "\n";
        entry += average_average_stat.getAverageIndividualTotalPoints() + "\n";
        entry += average_average_stat.getSpreadAgentDistance() + "\n";
        entry += average_average_stat.getSpreadGoalAgentDistance() + "\n";
        entry += average_average_stat.getAverageAgentDistance() + "\n";
        entry += average_average_stat.getAverageGoalAgentDistance() + "\n";
        entry += average_average_stat.getCoveredAreaFactor() + "\n";*/
//        for(int i = 0; i < average_average_stat_list.size(); i++) {
//            entry += "" + i + " " + average_average_stat.getGoalAgentObserved() + "\n";
//        }
        int count = 0;
        for(double d : entry_list) {
            Log.customLog("" + count + " " + d + "\n");
            count++;
        }
        Log.closeCustomLog();



        average_stats.clear();

        } catch(Exception e) {
            Log.errorLog("Error printing statistics", e);
        }


        average_stats.clear();


        experiment_count = 0;        
        System.out.println("done printing stats.");
    }
}
