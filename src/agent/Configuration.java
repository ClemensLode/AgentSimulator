/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Configuration {

    public static int getMaxClassifiers() {
        return maxClassifiers;
    }

    public static boolean isEventDriven() {
        return eventDriven;
    }

    public static int getNumberOfExperiments() {
        return numberOfExperiments;
    }

    public static int getNumberOfProblems() {
        return numberOfProblems;
    }    
    
    public static int getNumberOfSteps() {
        return numberOfSteps;
    }
    
    public static boolean isDoGASubsumption() {
        return doGASubsumption;
    }

    public static double getMutationProbability() {
        return mutationProbability;
    }

    public static double getThetaGA() {
        return thetaGA;
    }

    public static double getAlpha() {
        return alpha;
    }

    public static double getNu() {
        return nu;
    }

    public static boolean isActionSetSubsumption() {
        return actionSetSubsumption;
    }

    public static double getGamma() {
        return gamma;
    }

    public static double getDelta() {
        return delta;
    }

    public static double getDeltaDel() {
        return deltaDel;
    }

    public static int getMaxPopSize() {
        return maxPopSize;
    }

    public static double getPredictionInitialization() {
        return predictionInitialization;
    }

    public static double getPredictionErrorInitialization() {
        return predictionErrorInitialization;
    }

    public static double getFitnessInitialization() {
        return fitnessInitialization;
    }

    public static double getBeta() {
        return beta;
    }

    public static double getThetaSubsumer() {
        return thetaSubsumer;
    }

    public static double getEpsilon0() {
        return epsilon0;
    }

    public static boolean isIsTorus() {
        return isTorus;
    }

    public static double getCrossoverMutationProbability() {
        return crossoverMutationProbability;
    }

    public static int getMaxX() {
        return maxX;
    }

    public static int getMaxY() {
        return maxY;
    }

    public static double getRewardDistance() {
        return rewardDistance;
    }

    public static double getSightRange() {
        return sightRange;
    }

    public static int getMaxAgents() {
        return maxAgents;
    }

    public static int getMaxStackSize() {
        return maxStackSize;
    }

    public static double getCoveringWildcardProbability() {
        return coveringWildcardProbability;
    }

    public static double getCrossoverProbability() {
        return crossoverProbability;
    }

    public static double getElitistSelection() {
        return elitistSelection;
    }

    public static double getEvolutionaryMutationProbability() {
        return evolutionaryMutationProbability;
    }

    public static double getRewardUpdateFactor() {
        return rewardUpdateFactor;
    }

    public static boolean isDoEvolutionaryAlgorithm() {
        return doEvolutionaryAlgorithm;
    }

    
    private static boolean eventDriven = true;

    /**
     * The number of experiments that are calculated for each configuration
     */
    private static int numberOfExperiments = 10;

    /**
     * The number of problems that a single population of agents should be tested on
     */
    private static int numberOfProblems = 100;
    
    /**
     * The maximal number of steps executed in one trial in a multi-step problem.
     */    
    private static int numberOfSteps = 500;
    
    /**
     * The maximal number of (micro-)classifiers of one rule set of an agent
     * If the maximum number is reached when a classifier was inserted determine which micro-classifier to delete
     * A deletion can be a removal of a whole classifier with numerosity = 1 or the decrease of the numberosity of a classifier by 1
     * 
     */
    private static int maxClassifiers = 100;
    
    private static int maxX = 40;
    private static int maxY = 40;
    private static double rewardDistance = 4.0;
    private static double sightRange = 5.0;
    private static int maxAgents = 2;
    private static int maxStackSize = 32;
    private static double coveringWildcardProbability = 0.1;
    private static double crossoverProbability = 0.1;
    private static double crossoverMutationProbability = 0.05;
    private static double elitistSelection = 0.2;
    private static double evolutionaryMutationProbability = 0.2;
    private static double rewardUpdateFactor = 0.1;
    private static boolean doEvolutionaryAlgorithm = true;
    private static boolean isTorus = true;
    
    private static double thetaSubsumer = 0.1;
    private static double epsilon0 = 0.05;
    private static double beta = 0.1;
    
    private static double predictionInitialization = 0.1;
    private static double predictionErrorInitialization = 0.1;
    private static double fitnessInitialization = 0.5;
    
    private static int maxPopSize = 32;
    
    private static double delta = 0.1;
    private static double deltaDel = 0.1;
    private static double gamma = 0.1;
    
    private static boolean actionSetSubsumption = false;
    
    private static double alpha = 0.1;
    private static double nu = 0.1;
    
    /**
     * The threshold for the GA application in an action set.
     */
    private static double thetaGA = 25.0;
    
    private static double predictionErrorReduction = 0.1;
    private static double fitnessReduction = 0.1;
    private static double mutationProbability = 0.1;
    
    private static boolean doGASubsumption = true;
    
    static void initialize(String file_name) {
        // load old settings if file exists
        File my_file = new File(file_name);
        if (my_file.exists()) {

            try {
                BufferedReader p = new BufferedReader(new FileReader(my_file.getAbsoluteFile()));
                
                numberOfExperiments = Integer.valueOf(p.readLine());
                numberOfProblems = Integer.valueOf(p.readLine());
                numberOfSteps = Integer.valueOf(p.readLine());
                
                maxClassifiers = Integer.valueOf(p.readLine());                
                
                eventDriven = Boolean.valueOf(p.readLine());
                
                maxX = Integer.valueOf(p.readLine());
                maxY = Integer.valueOf(p.readLine());
                isTorus = Boolean.valueOf(p.readLine());
                rewardDistance = Double.valueOf(p.readLine());
                sightRange = Double.valueOf(p.readLine());
    
                maxAgents = Integer.valueOf(p.readLine());
    
    // number of steps for multi step problem
                maxStackSize = Integer.valueOf(p.readLine());
    
                coveringWildcardProbability = Double.valueOf(p.readLine());
                crossoverProbability = Double.valueOf(p.readLine());
                crossoverMutationProbability = Double.valueOf(p.readLine());
                elitistSelection = Double.valueOf(p.readLine());
                evolutionaryMutationProbability = Double.valueOf(p.readLine());
                rewardUpdateFactor = Double.valueOf(p.readLine());
                doEvolutionaryAlgorithm = Boolean.valueOf(p.readLine());
                
                thetaSubsumer = Double.valueOf(p.readLine());
                epsilon0 = Double.valueOf(p.readLine());
                beta = Double.valueOf(p.readLine());
                
                predictionInitialization = Double.valueOf(p.readLine());
                predictionErrorInitialization = Double.valueOf(p.readLine());
                fitnessInitialization = Double.valueOf(p.readLine());
                
                maxPopSize = Integer.valueOf(p.readLine());
                
                delta = Double.valueOf(p.readLine());
                deltaDel = Double.valueOf(p.readLine());
                gamma = Double.valueOf(p.readLine());
                
                actionSetSubsumption = Boolean.valueOf(p.readLine());
                
                alpha = Double.valueOf(p.readLine());
                nu = Double.valueOf(p.readLine());
                thetaGA = Double.valueOf(p.readLine());
                
                predictionErrorReduction = Double.valueOf(p.readLine());
                fitnessReduction = Double.valueOf(p.readLine());
                mutationProbability = Double.valueOf(p.readLine());
                
                doGASubsumption = Boolean.valueOf(p.readLine());
                
                p.close();
            } catch (IOException e) {
                Log.errorLog("IO Exception: Error reading from file " + my_file.getAbsoluteFile(), e);
            } catch (NumberFormatException e) {
                Log.errorLog("NumberFormatException: Error reading from file " + my_file.getAbsoluteFile(), e);
            } catch (Exception e) {
                Log.errorLog("Exception: Error ", e);
            }
        }
    }

    public static double getPredictionErrorReduction() {
        return predictionErrorReduction;
    }

    public static double getFitnessReduction() {
        return fitnessReduction;
    }
    
}
