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
    private static BufferedWriter error_out;
    
    private static boolean doLog = false;
    private static boolean errorLogInitialized = false;
    private static boolean notFinalised = false;

    public static void initialize(boolean do_log) {
        doLog = do_log;
        
        if(!doLog) {
            return;
        }
        
        if(notFinalised) {
            finalise();
        }        
        notFinalised = true;
        
        try {
            String filename = Misc.getFileName("agent") + ".log";
            out = new BufferedWriter(new FileWriter(filename));
        } catch (IOException e) {
            errorLog("initialize log failed: ", e);
        }
    }

    public static void log(String s) {
        if(!doLog) {
            return;
        }
        try {
            out.write(s);
        } catch (IOException e) {
            errorLog("Error writing log: ", e);
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
    
    public static void errorLog(String s, Throwable e) {
        StringBuilder error_string = new StringBuilder();
        final String NEW_LINE = System.getProperty("line.separator");
        
        error_string.append(s);
        error_string.append(e.toString());
        error_string.append(NEW_LINE);
        for(StackTraceElement ste : e.getStackTrace()) {
            error_string.append(ste.toString());
            error_string.append(NEW_LINE);
        }
        
        errorLog(error_string.toString());
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
        } catch (IOException e) {
            errorLog("finalise failed: ", e);
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
        } catch (IOException e) {
            errorLog("flush failed: ", e);
        }
    }
}
