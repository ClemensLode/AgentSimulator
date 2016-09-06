package agent;

import Misc.Point;
import lcs.Action;
import Misc.Misc;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileOutputStream;
import java.io.OutputStream;
import agents.BaseAgent;

import gif.*;


/**
 * Basic grid functionality
 *
 * @author Clemens Lode, clemens at lode.de, University Karlsruhe (TH)
 */
public class BaseGrid {
    public static long invalidActions = 0;
    public static long goalInvalidActions = 0;
    public static int goalJumps = 0;
    protected Field [][] grid;

    private Gif89Encoder gifenc;
    private OutputStream gifOS;

    protected BaseGrid() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        grid = new Field[max_x][max_y];
        for (int i = 0; i < max_x; i++) {
            for (int j = 0; j < max_y; j++) {
                grid[i][j] = new Field();
            }
        }
    }

    /**
     * resets the grid
     */
    protected void clear() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        for (int i = 0; i < max_x; i++) {
            for (int j = 0; j < max_y; j++) {
                grid[i][j].clear();
            }
        }
    }


    /**
     * Move an agent on the grid, marking the former position with 'EMPTY' and
     * the new position with its ID.
     * @param a The agent in question
     * @param action The action the agent wants to take
     * @return true if the movement was succesful, false if the movement was
     * invalid or blocked
     * @throws java.lang.Exception if the action id was out of bounds
     */
    public boolean moveAgent(BaseAgent a, final int action) throws Exception {
        if(action == Action.DO_JUMP) {
            return randomJumpAgent(a);
        }

        if(action < 0 || action >= Action.MAX_DIRECTIONS) {
            throw new Exception("Grid.moveAgent(): Action " + action + " of Agent at " + a.getX() + "/" + a.getY() + " out of range.");
        }

        if (isDirectionInvalid(a.getPosition(), action)) {
            if((!a.isGoalAgent()) && isDirectionNonGoalInvalid(a.getPosition(), action)) {
                invalidActions++;
            }
            if((a.isGoalAgent()) && isDirectionNonGoalInvalid(a.getPosition(), action)) {
                goalInvalidActions++;
            }
            return false;
        }

        int x = Geometry.correctX[128 + a.getX() + Action.dx[action]];
        int y = Geometry.correctY[128 + a.getY() + Action.dy[action]];
        if(grid[x][y].isOccupied()) {
            return false;
        }

        return putAgentTo(a, new Point(x, y));
    }


    /**
     * @return A random empty field in the grid
     */
    protected Point getFreeField() {
        int x;
        int y;
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        do {
            x = Misc.nextInt(max_x);
            y = Misc.nextInt(max_y);
        } while (grid[x][y].isOccupied());
        return new Point(x, y);
    }

    /**
     * @param p1 Top-left edge
     * @param p2 Lower-right edge
     * @return A random free field p with p1 <= p < p2
     */
    protected Point getFreeField(final Point p1, final Point p2) {
        int x;
        int y;
        do {
            x = Misc.nextInt(p2.x - p1.x) + p1.x;
            y = Misc.nextInt(p2.y - p1.y) + p1.y;
        } while (grid[x][y].isOccupied());
        return new Point(x, y);
    }

    /**
     * @param p The point in question
     * @return Numer of obstacles near p
     */
    protected int isObstacleNear(Point p) {
        int count = 0;
        for(int x = -1; x <= 1; x++) {
            for(int y = -1; y <= 1; y++) {
                if(x == 0 && y == 0) {
                    continue;
                }
                int new_x = Geometry.correctX[128 + p.x + x];
                int new_y = Geometry.correctY[128 + p.y + y];
                if(grid[new_x][new_y].isOccupied())
                {
                    count++;
                }
            }
        }
        return count;
    }

    protected Point getNeighborField(final Point p, final int d) {
        int x = Geometry.correctX[128 + p.x + Action.dx[d]];
        int y = Geometry.correctY[128 + p.y + Action.dy[d]];
        return new Point(x, y);
    }

    /**
     * Tests if the agent can move in the direction or if it is blocked
     * @param position Position of agent in question
     * @param direction desired movement direction
     * @return true if movement is invalid, false if it is valid and the agent
     * can move in that direction
     */
    protected boolean isDirectionInvalid(final Point position, final int direction) {
        int x = Geometry.correctX[128 + position.x + Action.dx[direction]];
        int y = Geometry.correctY[128 + position.y + Action.dy[direction]];
        return grid[x][y].isOccupied();
    }

    /**
     * @param position The position in question
     * @param direction The direction in question
     * @return True if the field in question is not occupied by a goal agent
     */
    protected boolean isDirectionNonGoalInvalid(final Point position, final int direction) {
        int x = Geometry.correctX[128 + position.x + Action.dx[direction]];
        int y = Geometry.correctY[128 + position.y + Action.dy[direction]];
        return (!grid[x][y].isOccupiedByGoal());
    }

    /**
     *
     * INVALID: introduced knowledge to the LCS Agent which it normally doesn't have (distance)
     * Improves the effectivity by up to 1%
     *
     * @param position The position in question
     * @return A boolean array where the invalid actions are marked (true)
     * @throws java.lang.Exception
     */
    /*protected boolean[] getInvalidActions(final Point position) throws Exception {
        boolean[] invalid_actions = new boolean[Action.MAX_DIRECTIONS];
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            invalid_actions[i] = isDirectionInvalid(position, i);
        }
        return invalid_actions;
    }*/

    /**
     * @param p The position of the agent in question
     * @return An array of directions in which the agent can move
     */
    public ArrayList<Integer> getAvailableDirections(final Point p) {
        ArrayList<Integer> list = new ArrayList<Integer>(Action.MAX_DIRECTIONS);
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            if(!isDirectionInvalid(p, i)) {
                list.add(new Integer(i));
            }
        }
        return list;
    }

    /**
     * @return An array of directions in which an agent can move
     */
    public ArrayList<Integer> getAllDirections() {
        ArrayList<Integer> list = new ArrayList<Integer>(Action.MAX_DIRECTIONS);
        for(int i = 0; i < Action.MAX_DIRECTIONS; i++) {
            list.add(new Integer(i));
        }
        return list;
    }


    public void removeExceptThisDirection(int direction, ArrayList<Integer> available_directions) {
        if(available_directions.contains(new Integer(direction))) {
            available_directions.clear();
            available_directions.add(new Integer(direction));
        } else {
            available_directions.clear();
        }
    }

    public ArrayList<Integer> getSideDirections(int direction) {
        ArrayList<Integer> list = new ArrayList<Integer>(Action.MAX_DIRECTIONS);
        switch(direction) {
            case 0:list.add(3);list.add(1);break;
            case 1:list.add(0);list.add(2);break;
            case 2:list.add(1);list.add(3);break;
            case 3:list.add(0);list.add(2);break;
        }
        return list;
    }



    /**
     * Puts the agent to a specific point
     * @param a the agent
     * @param p the target point
     * @return true if the target was empty and the move succesful
     */
    private boolean putAgentTo(BaseAgent a, final Point p) {
        if (grid[p.x][p.y].isEmpty()) {
            grid[a.getX()][a.getY()].setContent(Field.EMPTY);
            grid[p.x][p.y].setContent(a.getID());
            a.setPosition(p);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Jump to a random coordinate on the grid
     * @param a The agent we want to jump
     * @return true if the jump was succesful
     */
    protected boolean randomJumpAgent(BaseAgent a) {
        Point p;
        if(Configuration.getGoalAgentMovementType() == Configuration.TOTAL_RANDOM_MOVEMENT) {
            do {
                p = new Point(Misc.nextInt(Configuration.getMaxX()), Misc.nextInt(Configuration.getMaxY()));
            } while(grid[p.x][p.y].isOccupied());
        } else {
            p = getFreeFieldNear(a.getPosition());
        }
        return putAgentTo(a, p);
    }

    /**
     * @param p The base point
     * @return A random empty neighbor of the bas epoint, null if all neighbors are occupied
     */
    protected Point getFreeFieldNear(Point p) {
        if(!grid[p.x][p.y].isOccupied()) {
            return new Point(p.x, p.y);
        }
        for(int j = 3; j < Configuration.getHalfMaxX();j++) {
            for(int i = 0; i < 9; i++) {
                int x = Geometry.correctX[128 + Misc.nextInt(1+2*j) - j + p.x];
                int y = Geometry.correctY[128 + Misc.nextInt(1+2*j) - j + p.y];
                if(!grid[x][y].isOccupied()) {
                    return new Point(x, y);
                }
            }
        }
        return null;
    }



    /**
     * @return Darstellung des momentanen Gridzustands
     */
    private BufferedImage getImage() {
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();
        int zoom = 4;
        int cell_states = 6;
        int[] cellColor_R = new int[cell_states];
        int[] cellColor_G = new int[cell_states];
        int[] cellColor_B = new int[cell_states];
        // black, red, green, white, gray
        switch (cell_states-1) {
            // Sichtweite
            // TODO
            case 5:
                cellColor_R[5] = 64;
                cellColor_G[5] = 64;
                cellColor_B[5] = 128;
            // Rewardreichweite
            case 4:
                cellColor_R[4] = 96;
                cellColor_G[4] = 96;
                cellColor_B[4] = 192;
            // Agent
            case 3:
                cellColor_R[3] = 255;
                cellColor_G[3] = 255;
                cellColor_B[3] = 255;
            // Zielobjekt
            case 2:
                cellColor_R[2] = 96;
                cellColor_G[2] = 255;
                cellColor_B[2] = 96;
            // Hinderniss
            case 1:
                cellColor_R[1] = 255;
                cellColor_G[1] = 96;
                cellColor_B[1] = 96;
            // Frei
            case 0:
                cellColor_R[0] = 0;
                cellColor_G[0] = 0;
                cellColor_B[0] = 0;
                break;
        }

        int[] output = new int[3 * max_x * max_y * zoom * zoom];

        for(int y = 0; y < max_y; y++) {
            int index = 3 * y * max_x * zoom * zoom;
            for(int x = 0; x < max_x; x++) {
                int value = grid[x][y].getContent();
                if(value == Field.EMPTY) {
                    if(grid[x][y].isRewardedForAgents()) {
                        value = 4;
                    } else if(grid[x][y].isSeenByAgents()) {
                        value = 5;
                    } else {
                        value = 0;
                    }
                } else
                if(value > Field.GOAL_AGENT_ID) {
                    value = Field.GOAL_AGENT_ID+1;
                }

                int new_value_R = cellColor_R[value];
                int new_value_G = cellColor_G[value];
                int new_value_B = cellColor_B[value];
                for (int i = 0; i < zoom; i++) {
                    for (int j = 0; j < zoom; j++) {
                        int index2 = 3 * zoom * x + index + i * 3 * zoom * max_x + 3 * j;
                        output[index2] = new_value_R;
                        output[1 + index2] = new_value_G;
                        output[2 + index2] = new_value_B;
                    }
                }
            }
        }

        BufferedImage image = new BufferedImage(max_x * zoom, max_y * zoom, BufferedImage.TYPE_3BYTE_BGR);
        WritableRaster wr = image.getRaster();
        wr.setPixels(0, 0, max_x * zoom, max_y * zoom, output);

        return image;
    }

    protected void startGIF(int experiment_nr) throws Exception {
        gifOS = new FileOutputStream(Misc.getShortFileName("grid", experiment_nr) + ".gif");
        gifenc = new Gif89Encoder();
    }

    protected void finishGIF() throws Exception {
        gifenc.setLoopCount(0);
        gifenc.setUniformDelay(1);
        gifenc.encode(gifOS);
        
        gifOS.close();
    }

    protected void addFrameToGIF() throws Exception {
        gifenc.addFrame(getImage());
    }

    /**
     * @return A formatted string that represents the grid
     */
    public String getGridString() {
        String grid_string = new String();
        int max_x = Configuration.getMaxX();
        int max_y = Configuration.getMaxY();

        for (int i = 0; i < max_x; i++) {
            for (int j = 0; j < max_y; j++) {
                grid_string += grid[j][i].toString();
            }
            grid_string += "\n";
        }
        grid_string += "\n";
        return grid_string;
    }
}
