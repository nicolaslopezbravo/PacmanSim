import java.awt.Point;
import java.util.*;
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
    public List<Point> grid;
    public int cost = 0;

    Node(Point position, int c, PacCell[][] grid)
    {
        // add initial starting point and cost to get there
        path.add(position);
        this.grid = PacUtils.findFood(grid);
        this.grid.remove(position);
        setCost(c);
        size = 1;
    }

    Node(ArrayList<Point> path, int cost, List<Point>grid)
    {
        this.path = path;
        this.cost = cost;
        this.grid = grid;
        size = 1;
    }

    public void addLocation(Point position)
    {
        // add another position & remove it from the grid
        path.add(position);
        grid.remove(position);
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
        for(int i = 0; i < size; i++)
        {
            System.out.print(path.get(i) + " ");
        }
        System.out.println();
    }

    public List<Point> getGrid()
    {
        return this.grid;
    }     
}

public class PacSimRNNA implements PacAction
{
    private List<Point> path;
    private int simTime;
    private static boolean plan = true;
    public static List<Point> targets;

    public PacSimRNNA(String fname)
    {
        PacSim sim = new PacSim(fname);
        sim.init(this);
    }

    public static void main(String [] args)
    {
        new PacSimRNNA(args[0]);
        printTitle();
        System.out.println("\nMaze : " + args[0] + "\n");
    }

    @Override
    public void init() 
    {
        simTime = 0;
        path = new ArrayList();
    }

    private void printFoodArray(List<Point> food)
    {
        System.out.println("Food Array:\n");

        int len = food.size();
        
        for(int i = 0; i < len; i++)
        {
            // update the X and Y coordiantes for each new food pellet
            int x = (int)(food.get(i)).getX();
            int y = (int)(food.get(i)).getY();

            System.out.println(i + " : (" + x + "," + y + ")");
        }
        System.out.println();
    }

    private static void printTitle()
    {
        System.out.println();
        System.out.println();
        System.out.println("TSP using Repetitive Nearest Neighbor Algorithm by Alexandra Aguirre & Nicolas Lopez");
    }

    private static void printCostTable(ArrayList<Node> costTable)
    {   /*
        for(int i = 0; i < costTable.size(); i++)
        {
            Node n = costTable.get(i);
            n.print();
        }   */
        System.out.println("Cost Table: ");
        System.out.println();
        System.out.println();
    }

    private static void printPopulation(int i)
    {
        System.out.println("Population at step: " + i);
    }

    private void printTime()
    {
        System.out.println("Time to generate plan: " + simTime +" msec");
        System.out.println();
        System.out.println();
    }

    private ArrayList<Point> nearFood(Point p, List<Point> arr)
    {
        ArrayList<Point> nearestPellets = new ArrayList<Point>();
        Point newLoc = arr.get(0);
        int cost = PacUtils.manhattanDistance(p, newLoc);

        for(int i = 1; i < arr.size(); i++)
        {
            int newCost = PacUtils.manhattanDistance(p, arr.get(i));
            if(newCost <= cost)
            {
                cost = newCost;
                Point x = arr.get(i);
                nearestPellets.add(x);
            }
        }        
        if(nearestPellets.isEmpty())
        {   
            for(Point m : arr)
            {
                nearestPellets.add(m);
            }   
        }
        return nearestPellets;
    }

     public List<Point> PacPlanner(PacCell [][] grid, PacmanCell pc)
     {
        List<Point> food = PacUtils.findFood(grid);

        int size = PacUtils.numFood(grid);

        ArrayList<Node> costTable = new ArrayList<Node>(size);

        // Initialize the cost table
        Point pacman = pc.getLoc();
        for(int row = 0; row < size; row++)
        {
            // Calculate the new postion of the possible path
            Point position = food.get(row);
            // Calculate the cost to the each initial food
            int cost = PacUtils.manhattanDistance(pacman, position);            
            // Add the new cost and postion of the possible path to the list of options
            costTable.add(new Node(position, cost, PacUtils.cloneGrid(grid)));
        }

        // Fill out cost table
        for(int i = 0; i < size; i++)
        {
            Node n = costTable.get(i);

            while(n.getGrid().size() > 0)
            {
                Point loc = n.getLocation();  
                ArrayList<Point> nearestPellets = nearFood(loc, n.getGrid());
                //  if two or more options, create new nodes for them
                for(int j = 1; j < nearestPellets.size(); j++)
                {
                    Point newLoc = nearestPellets.remove(j);

                    // Copy grid and path elements to new instances
                    List<Point> newGrid = new ArrayList<Point>();
                    Collections.copy(n.getGrid(), newGrid);
                    ArrayList<Point> newPath = new ArrayList<Point>();
                    Collections.copy(n.getPath(), newPath);

                    Node temp = new Node (newPath, n.getCost(), newGrid);

                    // Calculate the new cost with the manhattan distance
                    int newCost = PacUtils.manhattanDistance(loc, newLoc);
                    temp.setCost(newCost);
                    temp.addLocation(newLoc);
                    costTable.add(temp);
                    size++;
                }
                Point newLoc = nearestPellets.remove(0);
                // Calculate the new cost with the manhattan distance
                int newCost = PacUtils.manhattanDistance(loc, newLoc);
                n.setCost(newCost);
                n.addLocation(newLoc);                                     
            }
        }

        printCostTable(costTable);
        printFoodArray(food);        
        printPopulation(0);
        System.out.println();
        System.out.println();

        // Find lowest cost
        int cost = costTable.get(0).getCost();
        List<Point> optimalPath = costTable.get(0).getPath();
        for(int i = 1; i < costTable.size(); i++)
        {
            if(costTable.get(i).getCost() < cost)
            {
                cost = costTable.get(i).getCost();
                optimalPath = costTable.get(i).getPath();
            }
        }
        return optimalPath;
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
            targets = PacPlanner(grid, pc);
            plan = false;
            printTime();
            System.out.println("Solution moves: ");
            System.out.println();
            System.out.println();
        }

        if(path.isEmpty()) 
        {
            Point tgt = targets.remove(0);
            path = BFSPath.getPath(grid, pc.getLoc(), tgt);
        }
         
         // take the next step on the current path
        
         Point next = path.remove(0);

         PacFace face = PacUtils.direction( pc.getLoc(), next );

         System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
               ++simTime, pc.getLoc().x, pc.getLoc().y, face );

         return face;
     }
}