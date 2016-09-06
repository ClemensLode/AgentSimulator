/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package agent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Log {

    private static BufferedWriter out;
    private static BufferedWriter grid_out;
    private static BufferedWriter error_out;
    
    private static boolean doLog;
    private static boolean errorLogInitialized = false;

    public static void initialize(boolean do_log) {
        doLog = do_log;
        
        if(!doLog) {
            return;
        }
        
        try {
            String filename = Misc.getFileName("agent") + ".log";
            out = new BufferedWriter(new FileWriter(filename));
            String grid_filename = Misc.getFileName("grid") + ".txt";
            grid_out = new BufferedWriter(new FileWriter(grid_filename));
        } catch (IOException e) {
            errorLog("initialize failed: " + e);
            System.err.print(e);
            e.printStackTrace();
        }
    }

    public static void log(String s) {
        if(!doLog) {
            return;
        }
        try {
            out.write(s);
        } catch (IOException e) {
            System.err.print(e);
            e.printStackTrace();
        }
    }
    public static void errorLog(String s) {
        if(!errorLogInitialized) {
            try {
                String error_filename = Misc.getFileName("error") + ".txt";
                error_out = new BufferedWriter(new FileWriter(error_filename));
                errorLogInitialized = true;
            } catch (IOException e) {
                System.err.print(e);
                e.printStackTrace();
                return;
            }
        }
        try {
            error_out.write(s);
        } catch (IOException e) {
            System.err.print(e);
            e.printStackTrace();
        }
    }  
    public static void gridLog(String s) {
        if(!doLog) {
            return;
        }
        try {
            grid_out.write(s);
        } catch (IOException e) {
            System.err.print(e);
            e.printStackTrace();
        }        
    }

    public static void finalise() {
        if(errorLogInitialized) {
            try {
                error_out.flush();
                error_out.close();
            } catch (IOException e) {
                System.err.print(e);
                e.printStackTrace();
            }
        }
        
        if(!doLog) {
            return;
        }
        
        try {
            out.flush();
            out.close();
            grid_out.flush();
            grid_out.close();            
        } catch (IOException e) {
            errorLog("finalise failed: " + e);
            System.err.print(e);
            e.printStackTrace();
        }
    }

    public static void flush() {
        if(errorLogInitialized) {
            try {
                error_out.flush();
            } catch(IOException e) {
                System.err.print(e);
                e.printStackTrace();
            }
        }
        
        if(!doLog) {
            return;
        }
        
        try {
            out.flush();
            grid_out.flush();
        } catch (IOException e) {
            errorLog("flush failed: " + e);
            System.err.print(e);
            e.printStackTrace();
        }
    }
}
