package com.clawsoftware.agentsimulator.agent;

import com.clawsoftware.agentsimulator.Misc.Log;
import com.clawsoftware.agentsimulator.Misc.Statistics;
import com.clawsoftware.agentsimulator.Misc.Misc;

/**
 * Main class
 * Takes one parameter, the name of the configuration file
 * Runs the simulation according to the parameters of the configuration file
 * Saves an output in the output_<TIME> directory
 * 
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class Main {

    /**
     * @param args The first argument denotes the name configuration file
     */
    public static void main(String[] args) {
        String[] date = new String[args.length];
        for(int i = 0; i < args.length; i++) {
            String configuration_file_name = args[i];
            date[i] = configuration_file_name.substring(7, configuration_file_name.length()-4);
        }
        Field.init();

        for(int i = 0; i < args.length; i++) {
            System.out.println("Initializing " + args[i]);
            Misc.initNewOutputDirectory(date[i]);
            try {
                Configuration.initialize(args[i]);
                Configuration.copyConfigFile(args[i]);
            } catch(Exception e) {
                Log.errorLog("Error initializing configuration file: ", e);
            }
            Statistics.initialize();
            System.out.println("Running test...");
            Misc.initPlotFile();
        // number of experiments with the same configuration
            long time = System.currentTimeMillis();
            BaseGrid.invalidActions = 0;
            BaseGrid.goalInvalidActions = 0;
            BaseGrid.goalJumps = 0;
            double[] average_cover_actions = new double[Configuration.getNumberOfProblems()];
            for(int t = 0; t < Configuration.getNumberOfProblems(); t++) {
                average_cover_actions[t] = 0.0;
            }

            for (int experiment_nr = 1; experiment_nr <= Configuration.getNumberOfExperiments(); experiment_nr++) {
                Log.initialize(false);
                Log.log("# Experiment Nr. " + experiment_nr);
                System.out.println("Experiment Nr. " + experiment_nr);


                try {
                // Reset population before each experiment
                    LCS_Engine engine = new LCS_Engine(experiment_nr);
//                    System.out.println("initialized");
                    engine.doOneMultiStepExperiment(average_cover_actions, experiment_nr);
//                    System.out.println("done");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.errorLog("Error initializing agents: ", e);
                }

                Log.finalise();
                Misc.nextExperiment();
                Statistics.nextExperiment();
            }
            for(int t = 0; t < Configuration.getNumberOfProblems(); t++) {
                average_cover_actions[t] /= ((double)(Configuration.getMaxAgents() * Configuration.getNumberOfExperiments()));
                System.out.println(average_cover_actions[t]);
            }

            System.out.println((System.currentTimeMillis() - time) + "ms");
            System.out.println("Invalid goal actions: " + 100.0 * (double)BaseGrid.goalInvalidActions / (double)(Configuration.getTotalTimeSteps() * Configuration.getNumberOfExperiments()) + "%");
            Statistics.printAverageStatistics();     
            Misc.appendPlotFile();
            Misc.resetExperimentCounter();
        }
        
    }
}
