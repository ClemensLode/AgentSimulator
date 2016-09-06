package agent;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Action extends GeneticData {
    public static final int ACTION_SIZE = 1;
    public static final int MAX_DIRECTIONS = 4;
    public static final int MAX_ACTIONS = MAX_DIRECTIONS + 1; //2;
    
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;
    public static final int NO_DIRECTION = 4;
    // public static final int RANDOM_DIRECTION = 5;    
    
    private static String[] actionString = {"North", "East", "South", "West", "No direction"};//, "Random"};
    public static String[] shortDirectionString = {"N","E","S","W","-"};
    
    // direction starts at 0 degrees (vertical)
    public static final int[] dx = {0, 1, 0, -1, 0};
    public static final int[] dy = {-1, 0, 1, 0, 0};    

    
    
    public Action() {
        super(ACTION_SIZE);
    }
    
    public Action(int action) {
        super(ACTION_SIZE);
        data[0] = action;
    }
    
    public Action(int[] data) {
        super(data);
    }    

    @Override
    public Action clone() {
        Action new_action = new Action(data);
        return new_action;
    }
    
    public void randomize() {
        data[0] = Misc.nextInt(MAX_ACTIONS);
    }
    
    /**
     * Mutates the action of the classifier.
     *
     * @see XCSConstants#pM
     * @param numberOfActions The number of actions/classifications possible in the environment.
     */
    public boolean mutateAction()
    {
	boolean changed=false;
    
	if(Misc.nextDouble()<Configuration.getMutationProbability()){
	    int act=0;
	    do{
		act=(int)(Misc.nextDouble() * MAX_ACTIONS);
	    }while(act==data[0]);
	    data[0]=act;
	    changed=true;
	}
	return changed;
    }
    
    public int getDirection() {
        return data[0];
    }
    
   @Override
    public String toString() {
        String output = new String();
        for(int i = 0; i < data.length; i++) {
            output += "" + Action.shortDirectionString[data[i]];//actionString[data[i]];
        }        
        return output;
    }

}
