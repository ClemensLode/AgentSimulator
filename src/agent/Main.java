package agent;

import Misc.Log;
import Misc.Statistics;
import Misc.Misc;

/**
 * Main class
 * Takes one parameter, the name of the configuration file
 * Runs the simulation according to the parameters of the configuration file
 * Saves an output in the output_<TIME> directory
 * 
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
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
            Misc.initNewOutputDirectory(date[i]);
            try {
                Configuration.initialize(args[i]);
                Configuration.copyConfigFile(args[i]);
            } catch(Exception e) {
                Log.errorLog("Error initializing configuration file: ", e);
            }
            Statistics.initialize();
            System.out.println("Running test with configuration " + args[i] + "...");
            Misc.initPlotFile();
        // number of experiments with the same configuration
            long time = System.currentTimeMillis();
            BaseGrid.invalidActions = 0;
            BaseGrid.goalJumps = 0;
            for (int experiment_nr = 1; experiment_nr <= Configuration.getNumberOfExperiments(); experiment_nr++) {
                Log.initialize(false);
            
                Configuration.printConfiguration();
                Log.log("# Experiment Nr. " + experiment_nr);
                System.out.println("Experiment Nr. " + experiment_nr);
                
                try {
                // Reset population before each experiment
                    LCS_Engine engine = new LCS_Engine(experiment_nr);
//                    System.out.println("initialized");
                    engine.doOneMultiStepExperiment(experiment_nr);
//                    System.out.println("done");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.errorLog("Error initializing agents: ", e);
                }
                Log.finalise();
                //Statistics.printStatistics(false);
                Misc.nextExperiment();
                Statistics.nextExperiment();
            }
            System.out.println((System.currentTimeMillis() - time) + "ms");
            Statistics.printAverageStatistics();     
            Misc.appendPlotFile();
            Misc.resetExperimentCounter();
        }
        
    }
}
