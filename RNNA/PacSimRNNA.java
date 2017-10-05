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
    public int size;
    public ArrayList<Point> path;
    public List<Point> notEaten;
    public int cost;
    private HashMap<Point,Integer> primaryCosts;

    Node(Point position, int c, PacCell[][] grid, Point pacman)
    {   // add initial starting point and cost to get there, first gen nodes
        path = new ArrayList<Point>();
        notEaten = new ArrayList<Point>();
        path.add(position);
        notEaten = PacUtils.findFood(grid);
        generateHashMap(grid, pacman, PacUtils.findFood(grid));
        notEaten.remove(position);
        cost = c;
        size = 1;
    }

    Node(ArrayList<Point> path, int cost, List<Point>grid, Point pacman, PacCell[][] gr)
    {   //newly created nodes from other nodes (2nd gen nodes)
        this.path = path;
        notEaten = grid;
        this.cost = cost;
        size = path.size();

        List<Point> newGrid = new ArrayList<Point>();
        newGrid.addAll(path);
        newGrid.addAll(grid);
        generateHashMap(gr, pacman, newGrid);
    }

    public void addLocation(Point loc)
    {
        // add another position & remove it from the grid
        path.add(loc);
        notEaten.remove(loc);
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

    private void generateHashMap(PacCell[][] grid, Point pc, List<Point> food)
    {   // make hashmap with initial costs
        primaryCosts = new HashMap<Point,Integer>();
        for(int i = 0 ; i < food.size(); i++)
        {
            List<Point> costP = BFSPath.getPath(grid,pc,food.get(i));
            int cost = costP.size();
            primaryCosts.put(food.get(i),cost);
        }
    }

    public void print(int size)
    {   // Make sure it doesn't go over the total amount of food
        if(size > path.size() + notEaten.size()){size--;}

        System.out.print(" :  cost=" + cost + " : " );
        for(int i = 0; i < size; i++)
        {
            Point p = path.get(i);
            int x = (int)p.getX();
            int y = (int)p.getY();
            int c = primaryCosts.get(p);
            System.out.print("[(" + x + "," + y + "),"+ c +"] ");
        }
        System.out.println();
    }

    public List<Point> getLeftOvers()
    {
        return this.notEaten;
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

    private static void printCostTable(List<Point> food, Point pc, PacCell[][] grid)
    {   
        System.out.println("Cost Table:");
        System.out.println();

        int foodSize = food.size();
        int [][] costTable = new int[foodSize+1][foodSize+1];

        for(int i = 0; i < foodSize; i++)
        {
            for(int j = 0; j < foodSize; j++)
            {
               
                Point x = food.get(i);
                Point y = food.get(j);
                // calculate the distance from pacman to each food pelet
                // x for the ith row
                // y for the jth row
                List<Point> p = BFSPath.getPath(grid, pc, x);   
                List<Point> p2 = BFSPath.getPath(grid, pc, y);   
                // the distance is the size of the list of points to get from pacman to x (or y)
                int xLen = p.size();
                int yLen = p2.size();

                // add the distance to the cost table
                costTable[i+1][0] = xLen;
                costTable[0][j+1] = yLen;
            }
        }

        for(int i = 0; i < foodSize; i++)
        {
            for(int j = 0; j < foodSize; j++)
            {
                 // calculate the distance from each starting food pellet to every other pellet
                // x for the ith row
                // y for the jth row
                Point x = food.get(i);
                Point y = food.get(j);
                List<Point> p = BFSPath.getPath(grid, x, y);   
                // the distance is the size of the list of points to get from x to y
                int len = p.size();

                // add the distance to the cost table
                costTable[i+1][j+1] = len;
            }
        }

        // print out the entire cost table
        for(int i = 0; i < foodSize+1; i++)
        {
            for(int j = 0; j < foodSize+1; j++)
                System.out.format("%4d", costTable[i][j]);
            System.out.println();
        }
        System.out.println();
    }

    private void printTime(int a)
    {
        System.out.println("Time to generate plan: " + a +" msec");
        System.out.println();
        System.out.println();
    }

    private ArrayList<Point> nearFood(Point p, PacCell[][] grid, List<Point> arr)
    {
        ArrayList<Point> nearestPellets = new ArrayList<Point>();
        Point newLoc = arr.get(0);
        List<Point> firstCosts = BFSPath.getPath(grid, p, newLoc);
        int cost = firstCosts.size();

        for(int i = 1; i < arr.size(); i++)
        {
            List<Point> newCostPath = BFSPath.getPath(grid, p, arr.get(i));
            int newCost = newCostPath.size();

            if(newCost <= cost)
            {
                cost = newCost;
                Point x = arr.get(i);
                nearestPellets.add(x);
            }
        }        
        if(nearestPellets.isEmpty())
        {   
            nearestPellets.addAll(arr); 
        }
        return nearestPellets;
    }

     public List<Point> PacPlanner(PacCell [][] grid, PacmanCell pc)
     {
        System.out.println();
        List<Point> food = PacUtils.findFood(grid);
        int size = PacUtils.numFood(grid);
        Point pacman = pc.getLoc();

        printCostTable(food, pacman, grid);
        printFoodArray(food);

        ArrayList<Node> costTable = new ArrayList<Node>(size);

        // Initialize the cost table
        for(int row = 0; row < size; row++)
        {   // Calculate the new postion of the possible path
            Point position = food.get(row);
            // Calculate the cost to the each initial food
            List<Point> initialCost = BFSPath.getPath(grid, pacman, position);
            int cost = initialCost.size();               
            // Add the new cost and postion of the possible path to the list of options
            costTable.add(new Node(position, cost, PacUtils.cloneGrid(grid), pacman));
        }
        
        // Fill out cost table & find lowest cost
        int cost = 0;
        List<Point> optimalPath = new ArrayList<Point>();
        int nodeIndex = 0;
        int table = food.size();
        int stepNumber = 0;

        for(int i = 0; i < costTable.size(); i++)
        {
            Node n = costTable.get(i);
            
            while(n.getLeftOvers().size() > 0)
            {
                Point loc = n.getLocation();  
                ArrayList<Point> nearestPellets = nearFood(loc, grid ,n.getLeftOvers());
                
                for(int j = 1; j < nearestPellets.size(); j++)
                {
                    Point newLoc = nearestPellets.remove(j);
                    // Copy grid and path elements to new instances (2nd gen nodes)
                    List<Point> newGrid = new ArrayList<Point>();
                    ArrayList<Point> newPath = new ArrayList<Point>();
                    newGrid = PacUtils.clonePointList(n.getLeftOvers());
                    newPath.addAll(n.getPath());
                    Node temp = new Node (newPath, n.getCost(), newGrid, pacman, PacUtils.cloneGrid(grid));

                    // Calculate the new cost
                    List<Point> newCostPath = BFSPath.getPath(grid, loc, newLoc);
                    int newCost = newCostPath.size();
                    temp.setCost(newCost);
                    temp.addLocation(newLoc);
                    costTable.add(i+1,temp);    // place it in order
                }
                Point newLoc = nearestPellets.remove(0);
                List<Point> temp = BFSPath.getPath(grid, loc, newLoc);
                int newCost = temp.size();
                n.setCost(newCost);
                n.addLocation(newLoc);                                     
            }

            if(i == 0 || i == table && stepNumber < food.size())
            {   //print out the population
                System.out.println();
                System.out.println("Population at step "+ stepNumber++ +" : ");
                System.out.println();
                table = costTable.size();
                nodeIndex = 0;
            }
            // print number of nodes since last step
           // System.out.print(nodeIndex++);
           // n.print(stepNumber);

            if(i == 0)  // look for lowest path so far
            {
                cost = n.getCost();
                optimalPath = costTable.get(0).getPath();
            }
            else if(costTable.get(i).getCost() < cost)
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
        
        if(plan)    //make the plan in the first pass
        {
            long before = System.currentTimeMillis();
            targets = PacPlanner(grid, pc);
            long after = System.currentTimeMillis();
            plan = false;
            printTime((int)(after - before));
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