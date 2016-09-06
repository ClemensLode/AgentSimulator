package agent;

import java.util.ArrayList;
import java.text.NumberFormat;

/**
 *
 * @author Clemens Lode, 1151459, University Karlsruhe (TH)
 */
public class Field {

/**
 * IDs of the cells of the grid
 * IDs above GOAL_AGENT_ID denote the agent id
 */
    public static final int EMPTY = 0;
    public static final int OBSTACLE = 1;
    public static final int GOAL_AGENT_ID = 2;
    private static NumberFormat nf = NumberFormat.getInstance(); // Get Instance of NumberFormat

    public static void init() {
        nf.setMinimumIntegerDigits(3);  // The minimum Digits required is 3
        nf.setMaximumIntegerDigits(3); // The maximum Digits required is 3
    }
    private int content = EMPTY;
    private ArrayList<Integer> seen_by;
    private ArrayList<Integer> reward_for;

    /**
     * @return the content
     */
    public int getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(int content) {
        this.content = content;
    }

    public boolean isEmpty() {
        return content == EMPTY;
    }

    public boolean isOccupied() {
        return content != EMPTY;
    }

    public Field() {
        content = EMPTY;
        seen_by = new ArrayList<Integer>();
        reward_for = new ArrayList<Integer>();
    }

    public void clear() {
        content = EMPTY;
        seen_by.clear();
        reward_for.clear();
    }

    public void clearSight() {
        seen_by.clear();
        reward_for.clear();
    }


    public boolean isSeen() {
        return !seen_by.isEmpty();
    }
    
    public boolean isSeenByAgents() {
        return seen_by.size() > 1 || (seen_by.size() == 1 && seen_by.get(0) != Field.GOAL_AGENT_ID);
    }

    public boolean isSeenBy(int id) {
        return seen_by.contains(id);
    }

    public void addSeen(int id) {
        if(seen_by.contains(id)) {
            return;
        }
        seen_by.add(id);
    }

    public boolean isRewarded() {
        return !reward_for.isEmpty();
    }

    public boolean isRewardedForAgents() {
        return reward_for.size() > 1 || (reward_for.size() == 1 && reward_for.get(0) != Field.GOAL_AGENT_ID);
    }

    public boolean isRewardFor(int id) {
        return reward_for.contains(id);
    }

    public void addRewarded(int id) {
        if(reward_for.contains(id)) {
            return;
        }
        reward_for.add(id);
    }

    @Override
    public String toString() {
        String t = new String();
        if (content == EMPTY) {
            if (isRewardedForAgents()) {
                t += "  $ ";
            } else if(isSeenByAgents()) {
                t += "  ^ ";
            } else {
                t += "  . ";
            }
        } else if (content == OBSTACLE) {
            t += "  # ";
        } else if (content == GOAL_AGENT_ID) {
            t += " [X]";
        } else {
            t += " " + (nf.format(content));
        }
        return t;
    }
}
