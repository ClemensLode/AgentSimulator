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

    public static boolean isIsTorus() {
        return isTorus;
    }

    public static double getCrossoverMutationProbability() {
        return crossoverMutationProbability;
    }

    public static int getGaSteps() {
        return gaSteps;
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

    public static int getTotalSteps() {
        return totalSteps;
    }
    
    private static int totalSteps = 500;
    private static int gaSteps = 20;
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
    
    static void initialize(String file_name) {
        // load old settings if file exists
        File my_file = new File(file_name);
        if (my_file.exists()) {

            try {
                BufferedReader p = new BufferedReader(new FileReader(my_file.getAbsoluteFile()));
                totalSteps = Integer.valueOf(p.readLine());
                gaSteps = Integer.valueOf(p.readLine());
                
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
                
                p.close();
            } catch (IOException e) {
                Log.errorLog("IO Exception: Error " + e + " reading from file " + my_file.getAbsoluteFile());
            } catch (NumberFormatException e) {
                Log.errorLog("NumberFormatException: Error " + e + " reading from file " + my_file.getAbsoluteFile());
            } catch (Exception e) {
                Log.errorLog("Exception: Error " + e);
            }
        }
    }
    
}
