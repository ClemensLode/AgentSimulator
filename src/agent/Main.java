package agent;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Main {
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        if(args.length == 0) {
            Log.errorLog("No configuration file provided!");
            return;
        }
        Configuration.initialize(args[0]);
        double avg = 0.0;
        
        // number of experiments with the same configuration
        for(int experiment_nr = 0; experiment_nr < Configuration.getNumberOfExperiments(); experiment_nr++) {
            Log.initialize(true);
            
            Log.log("# Experiment Nr. " + (experiment_nr+1) + "\n");
            System.out.println("Experiment Nr."+(experiment_nr+1));
            
            try {
                // Reset population before each experiment
                LCS_Engine engine = new LCS_Engine();
                engine.doOneMultiStepExperiment();
            } catch(Exception e) {
                Log.errorLog("Error initializing agents: ", e);
            }
            Log.log("# Quality of run: " + Agent.grid.getTimeGoalAgentObserved() + " / " + Agent.grid.getTotalTimeGoalAgentObserved() + " (" + Agent.grid.getPercentageGoalAgentObserved() + ")");
            System.out.println("# Quality of run: " + Agent.grid.getTimeGoalAgentObserved() + " / " + Agent.grid.getTotalTimeGoalAgentObserved() + " (" + Agent.grid.getPercentageGoalAgentObserved() + ")");
            avg += Agent.grid.getPercentageGoalAgentObserved();
            System.out.println(avg / (double)(experiment_nr+1));
        }        
        
        Log.finalise();
    }      
}
