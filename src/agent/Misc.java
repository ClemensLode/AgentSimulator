/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;
import java.util.Random;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Misc {
    private static Random generator = new Random();
    
    public static int nextInt(int n) {
        return generator.nextInt(n);
    }
    public static double nextDouble() {
        return generator.nextDouble();
    }
    public static double round(final double d, final double increment) {
        return ((int)((d + 0.5 * increment) / increment) * increment);
    }    
    public static String getFileName(String prefix) {
        // "yyyy.MMMMM.dd.hh:mm.ss.SS"
        SimpleDateFormat fmt = new SimpleDateFormat("dd-MM-yy-HH_mm_ss_SS");
        return prefix + "-" + fmt.format(new Date());
    }    
}
