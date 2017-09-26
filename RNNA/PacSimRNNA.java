import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import pacsim.*;

/*
CAP 4630 - AI
Program 1 - PacSimRNNA

Alexandra Aguirre
Nicolas Lopez

9-5-17
*/
class Node
{
    private int size;
    public ArrayList<Point> path = new ArrayList<Point>();
    public ArrayList<Point> grid;
    public int cost = 0;

    Node(Point position, int c, PacCell[][] grid)
    {
        // add initial starting point and cost to get there
        path.add(position);
        this.grid = PacUtils.findFood(grid);
        setCost(c);
        size = 1;
    }

    Node(ArrayList<Point> path, int cost)
    {
        this.path = path;
        this.cost = cost;
    }

    public void addLocation(Point position)
    {
        // add another position
        path.add(position);
        size++;
    }

    public Point getLocation()
    {
        //get the last location added
        return path.get(size-1);
    }

    public ArrayList<Point> getPath()
    {
        //get the entire path
        return path;
    }

    public void setCost(int c)
    {
        // set the cost for the current path
        cost += c;
    }

    public int getCost()
    {
        // get the cost for the current path
        return cost;
    }

    public void print()
    {
        System.out.println("Cost of this path is: " + cost);
        System.out.println("Positions of the path are: ");
        for(int i = 0; i < size; i++)
        {
            System.out.print(path.get(i) + " ");
        }
        System.out.println();
    }

    public void removePoint(Point f)
    {
        if(grid.contains(f))
        {
            grid.remove(f);
        }
    }

    public ArrayList<Point> getGrid()
    {
        return this.grid;
    }  

    @Override
    public Node clone()
    {
        return new Node(path,cost);
    }      
}

public class PacSimRNNA implements PacAction
{
    private List<Point> path;
    private int simTime;
    private static boolean plan = true;

    public PacSimRNNA(String fname)
    {
        PacSim sim = new PacSim(fname);
        sim.init(this);
    }

    public static void main(String [] args)
    {
        new PacSimRNNA(args[0]);
        System.out.println("\nMaze : " + args[0] + "\n");
    }

    @Override
    public void init() 
    {
        simTime = 0;
        path = new ArrayList();
    }

     public void PacPlanner(PacCell [][] grid, PacmanCell pc)
     {
        List<Point> food = PacUtils.findFood(grid);
            
        // The cost of pacman to getting to the food (initial start state) will be determined and set as the initial
        // value of the cost table. The cost table will hold only one row, continuously updating the value of each node
        // holding the total cost and the path taken as values.

        int numFood = PacUtils.numFood(grid);

        ArrayList<Node> costTable = new ArrayList<Node>(numFood);

        // Initialize the cost table
        Point pacman = pc.getLoc();
        for(int row = 0; row < numFood; row++)
        {
            // Calculate the cost to the each initial food
            int cost = PacUtils.manhattanDistance(pacman, food.get(row));
            // Calculate the new postion of the possible path
            Point position = food.get(row);
            // Add the new cost and postion of the possible path to the list of options
            costTable.add(new Node(position, cost, PacUtils.cloneGrid(grid)));
        }
        int size = numFood;
        for(int i = 0; i < numFood; i++)
        {
            Node n = costTable.get(i);
            ArrayList<Point> gr = n.getGrid();
            while(gr.size() > size);
            {
                // Debugging
                System.out.println("Number of food remaining " + PacUtils.numFood(gr));
                Point loc = n.getLocation();
                // Transform gr to PacCell[][]
                Point newLoc = PacUtils.nearestFood(loc,gr);
                // Prevent thrashing
                gr.remove(newLoc);
                // test to find multiple foods at same distance if so, clone and add to cost table, also increase size by 1
                int newCost = PacUtils.manhattanDistance(loc,newLoc);
                n.setCost(newCost);
                n.addLocation(newLoc);
                n.setGrid(gr);
            }
        }
     }

     @Override
     public PacFace action(Object state) 
     {
  
        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman(grid);
        
        // make sure Pac-Man is in this game
        if(pc == null) return null;
        
        if(plan)
        {
            PacPlanner(grid, pc);
            plan = false;
        }

        // if current path completed (or just starting out),
        // select a the nearest food using the city-block 
        // measure and generate a path to that target
        if(path.isEmpty()) 
        {
            Point tgt = PacUtils.nearestFood(pc.getLoc(), grid);

            path = BFSPath.getPath(grid, pc.getLoc(), tgt);

           
            System.out.println("Pac-Man currently at: [ " + pc.getLoc().x
                 + ", " + pc.getLoc().y + " ]");

            System.out.println("Setting new target  : [ " + tgt.x
                 + ", " + tgt.y + " ]");
        }
        
        // take the next step on the current path
        
        Point next = path.remove(0);

        PacFace face = PacUtils.direction(pc.getLoc(), next);

        System.out.printf("%5d : From [%2d, %2d] go %s%n", 
              ++simTime, pc.getLoc().x, pc.getLoc().y, face);

        return face;
     }
}