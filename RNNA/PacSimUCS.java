import java.util.List;
import java.util.ArrayList;

import pacsim.*;

/**
 * UCF - Fall 2016
 * CAP4630 Pac-Man Agent
 * @author Zachary Gill
 * @author Sayeed Tahseen
 */
public class PacSimUCS implements PacAction {
    
    private long expanded;
    private long moves;
    private PacmanCell pmCell;
    private ArrayList<PacCell> path;
    private PacPriorityQueue visited;
    private PacPriorityQueue fringe;
    
    public PacSimUCS( String fname ) {
        PacSim sim = new PacSim( fname );
        sim.init(this);
    }
    
    public static void main( String[] args ) {
        String fname = "";
        if (args.length > 0) {
            fname = args[ 0 ];
        }
        if ("".equals(fname)) {
            System.out.println("You must pass in the map file as a parameter.");
            return;
        }
        new PacSimUCS( fname );
    }
    
    @Override
    public void init() {
        expanded = 0;
        moves = 0;
        path = null;
        visited = new PacPriorityQueue();
        fringe = new PacPriorityQueue();
    }
    
    @Override
    public PacFace action( Object state ) {
        PacCell[][] grid = (PacCell[][]) state;
        pmCell = PacUtils.findPacman( grid );
        
        // make sure Pac-Man is in this game
        if (pmCell == null) {
            System.out.println("There is no PacMan on this board.");
        }
        
        //if we havent calculated the path yet
        if (path == null) {
    
            //calculate path
            calculatePath(grid);
            
            //start path
            PacCell start = path.remove(0);
            
            System.out.println("Solution moves:");
        }
        
        //get the new face direction
        PacFace newFace = getFace(path.remove(0));
        moves++;
        System.out.println(moves + ": " + newFace.toString());
        return newFace;
    }
    
    /**
     * Calculates the optimal path for PacMan.
     * @param grid The grid.
     * @return The optimal path.
     */
    private void calculatePath(PacCell[][] grid)
    {
        path = new ArrayList<PacCell>();

        //find all food cells
        ArrayList<PacCell> food = new ArrayList<PacCell>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (PacUtils.goody(i, j, grid)) {
                    food.add(grid[i][j]);
                }
            }
        }

        //create an initial plan
        ArrayList<PacCell> initPlan = new ArrayList<PacCell>();
        initPlan.add(pmCell);
        fringe.put(new PacState(pmCell, initPlan, food));

        //holds the next state off the fringe
        PacState state;
        do {
            state = fringe.poll(); //expand node
            visited.put(state); //add to closed set
            expanded++;

            if (expanded % 1000 == 0) { //print out node expansion every 1000 nodes
                System.out.println("Nodes expanded: " + expanded + " fringe size = " + fringe.getSize());
            }

            //add neighbors to fringe
            PacCell cell = state.getLastCell();
            for (PacFace dir : PacFace.values()) {
                PacCell neighbor = PacUtils.neighbor(dir, cell, grid);
                if (!(neighbor instanceof WallCell)) {
                    queueState(state, neighbor);
                }
            }

        } while (state.getFood().size() > 0);

        //print out final nodes expanded
        System.out.println();
        System.out.println("Nodes expanded: " + expanded + " fringe size = " + fringe.getSize());
        System.out.println();
    
        //now we have the optimal path
        path = state.getPath();

        //print it out
        int moveCount = 0;
        System.out.println("Solution path:");
        for (PacCell ps : path) {
            System.out.println(moveCount + ": (" + ps.getX() + ", " + ps.getY() + ")");
            moveCount++;
        }
        System.out.println();
    }

    private void queueState(PacState state, PacCell move)
    {
        //copy old food
        ArrayList<PacCell> newFood = new ArrayList<PacCell>();
        newFood.addAll(state.getFood());
        if (move instanceof FoodCell) { //if you moved onto a food cell, remove it
            newFood.remove(move);
        }

        //create new path
        ArrayList<PacCell> newPath = new ArrayList<PacCell>();
        newPath.addAll(state.getPath());
        newPath.add(move);

        //create new state
        PacState newState = new PacState(move, newPath, newFood);
        if (!visited.contains(newState)) { //if it hasnt been visited before, add it to fringe
            fringe.put(newState);
        }
    }

    /**
     * Gets the direction of the next movement.
     * @param move The move.
     * @return The direction of the move.
     */
    private PacFace getFace(PacCell move)
    {
        int xdif = pmCell.getX() - move.getX();
        int ydif = pmCell.getY() - move.getY();

        switch (xdif) {
            case -1:
                return PacFace.E;
            case 0:
                switch (ydif) {
                    case -1:
                        return PacFace.S;
                    case 0:
                        return null;
                    case 1:
                        return PacFace.N;
                    default:
                        return null;
                }
            case 1:
                return PacFace.W;
            default:
                return null;
        }
    }
    
}

/**
 * A class storing the state of the game at a certain point.
 */
class PacState {
    private int x;
    private int y;
    private ArrayList<PacCell> path;
    private ArrayList<PacCell> food;
    
    PacState(PacCell loc, ArrayList<PacCell> path, ArrayList<PacCell> food)
    {
        x = loc.getX();
        y = loc.getY();
        this.path = path;
        this.food = food;
    }
    
    /**
     * Returns the cost of the PacState path.
     * @return
     */
    public int cost()
    {
        return path.size();
    }
    
    /**
     * Returns the last cell in the path.
     * @return The last cell in the path.
     */
    public PacCell getLastCell()
    {
        return path.get(path.size() - 1);
    }
    
    /**
     * Determines if two PacStates are equal.
     * @param compare The PacState to compare.
     * @return Whether the two states are equal or not.
     */
    public boolean equals(PacState compare)
    {
        //check location
        if (compare.getX() == x && compare.getY() == y) {
            
            //check food
            if (compare.getFood().size() != food.size()) {
                return false;
            }
            int match = 0;
            for (PacCell aFood : food) {
                for (PacCell bFood : compare.getFood()) {
                    FoodCell a = (FoodCell) aFood;
                    FoodCell b = (FoodCell) bFood;
                    
                    if ((a.getX() == b.getX() && a.getY() == b.getY())) {
                        match++;
                        break;
                    }
                }
            }
            if (match != food.size()) {
                return false;
            }
            
            //they are equal, test if this is a better path
            if (compare.cost() < cost()) {
                path = compare.getPath();
            }
            return true;
        }
        return false;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public ArrayList<PacCell> getPath()
    {
        return path;
    }
    
    public ArrayList<PacCell> getFood()
    {
        return food;
    }
}

/**
 * Priority queue.
 */
class PacPriorityQueue
{
    private ArrayList<PacState> queue;

    public PacPriorityQueue()
    {
        queue = new ArrayList<PacState>();
    }

    /**
     * Put a new element in the priority queue.
     * @param state The new element.
     */
    public void put(PacState state)
    {
        if (contains(state)) {
            return;
        }
        int counter = 0;
        for (PacState s : queue) {
            if (state.cost() < s.cost()) {
                break;
            }
            counter++;
        }
        queue.add(counter, state);
    }

    /**
     * Get the first element out of the priority queue.
     * @return The first element.
     */
    public PacState poll() {
        if (queue.size() > 0) {
            return queue.remove(0);
        } else {
            return null;
        }
    }

    /**
     * Determines if a state exists in the priority queue.
     * @param state The state to look for.
     * @return Whether the state exists in the priority queue or not.
     */
    public boolean contains(PacState state)
    {
        for (PacState s : queue) {
            if (s.equals(state)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns the size of the Priority Queue.
     * @return The size of the Priority Queue.
     */
    public int getSize()
    {
        return queue.size();
    }
}