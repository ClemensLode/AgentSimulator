/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public abstract class GeneticData {
    int[] data;
    public abstract void randomize();
    public abstract void mutate(double mutation_probability);
    
    public GeneticData(int size) {
        data = new int[size];
        randomize();        
    }
    
    public GeneticData(int[] data) {
        this.data = new int[data.length];
        for(int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }
    
    public int getDegreeOfRelationship(GeneticData a) {
        int degree = 0;
        for(int i = 0; i < data.length; i++) {
            degree += (data[i] - a.data[i]) * (data[i] - a.data[i]);
        }
        return degree;
    }
    
    public void setData(int[] data) {
        for(int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }
    
    public int getLength() {
        return data.length;
    }
    
    public void copyTo(int[] dest, int index) {
        for(int i = 0; i < data.length; i++) {
            dest[i + index] = data[i];
        }
    }
    
    @Override
    public String toString() {
        String output = new String();
        for(int i = 0; i < data.length; i++) {
            output += "" + data[i];
        }        
        return output;
    }   
    
}
