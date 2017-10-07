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
    public int cost;
    public ArrayList<Point> path;
    public List<Point> notEaten;
    private HashMap<Point,Integer> primaryCosts;

    Node(Point position, int c, PacCell[][] grid, int[][] costValues)
    {   // add initial starting point and cost to get there, first gen nodes
        path = new ArrayList<Point>();
        notEaten = new ArrayList<Point>();
        path.add(position);
        notEaten = PacUtils.findFood(grid);
        generateHashMap(costValues, notEaten);
        notEaten.remove(position);
        cost = c;
        size = 1;
    }

    Node(ArrayList<Point> path, int cost, List<Point>grid, PacCell[][] gr, int[][] costValues)
    {   
        this.path = new ArrayList<Point>();
        notEaten = grid;
        this.cost = cost;
        size = path.size();
        List<Point> newGrid = new ArrayList<Point>();
        this.path.addAll(path);
        newGrid.addAll(grid);
        generateHashMap(costValues,PacUtils.findFood(gr));
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

    private void generateHashMap(int[][] costValues, List<Point> food)
    {   // make hashmap with initial costs
        primaryCosts = new HashMap<Point,Integer>();
        for(int i = 0 ; i < food.size(); i++)
        {
            primaryCosts.put(food.get(i),costValues[0][i+1]);
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
    public static HashSet<Point> visited;

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

    private static int[][] printCostTable(List<Point> food, Point pc, PacCell[][] grid)
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
        return costTable;
    }

    private void printTime(int a)
    {
        System.out.println("Time to generate plan: " + a + " msec");
        System.out.println();
        System.out.println();
    }

    private ArrayList<Point> nearFood(Point p, List<Point> food, List<Point> arr, PacCell[][] grid)
    {
        if(arr.size() <= 0)
        {
            return null;
        } 
        ArrayList<Point> nearestPellets = new ArrayList<Point>();
        Point newLoc = arr.get(0);
        int cost = BFSPath.getPath(grid,p,newLoc).size();

        for(int i = 1; i < arr.size(); i++)
        {
            newLoc = arr.get(i);
            int newCost = BFSPath.getPath(grid,p,newLoc).size();
            if(newCost < cost)
            {
                cost = newCost;
            }
        }

        for(int i = 0; i < arr.size(); i++)
        {
            newLoc = arr.get(i);
            int pathCost = BFSPath.getPath(grid,p,newLoc).size();
            if(pathCost == cost)
            {
                nearestPellets.add(newLoc);
            }
        }
        
        return nearestPellets;
    }

     public List<Point> PacPlanner(PacCell [][] grid, PacmanCell pc)
     {
        System.out.println();
        List<Point> food = PacUtils.findFood(grid);
        int size = PacUtils.numFood(grid);
        Point pacman = pc.getLoc();
        
        int [][] costValues = printCostTable(food, pacman, grid);
        printFoodArray(food);
        int lowest = 100;
        int cost = 0;
        
        ArrayList<Node> costTable = new ArrayList<Node>(size);
        // Initialize the cost table
        for(int row = 0; row < size; row++)
        {   // Calculate the new postion of the possible path
            Point position = food.get(row);
            // Calculate the cost to the each initial food
            cost = BFSPath.getPath(grid,pacman,position).size();     
            Node n = new Node(position, cost, PacUtils.cloneGrid(grid), costValues);       
            // Add the new cost and postion of the possible path to the list of options
            costTable.add(n);
        }
        
        // Fill out cost table & find lowest cost
        cost = 0;
        List<Point> optimalPath = new ArrayList<Point>();
        int nodeIndex = 0;
        int table = food.size();
        int stepNumber = 0;
        for(int f = 0; f < food.size(); f++)
        {       
            //print out the population
            System.out.println();
            System.out.println("Population at step "+ stepNumber++ +" : ");
            System.out.println();
            table = costTable.size();
            nodeIndex = 0;
            ArrayList<Node> tempCostTable = new ArrayList<Node>();

            for(int i = 0; i < costTable.size(); i++)
            {
                Node n = costTable.get(i);
                Point loc = n.getLocation();  
                ArrayList<Point> nearestPellets = nearFood(loc, food ,n.getLeftOvers(),grid);
                if(nearestPellets != null)
                {
                    for(int j = 1; j < nearestPellets.size(); j++)
                    {
                        Point newLoc = nearestPellets.remove(j);
                        List<Point> newGrid = new ArrayList<Point>();
                        ArrayList<Point> newPath = new ArrayList<Point>();
                        newGrid = PacUtils.clonePointList(n.getLeftOvers());
                        newPath.addAll(n.getPath());
                        Node temp = new Node (newPath, n.getCost(), newGrid, PacUtils.cloneGrid(grid), costValues);
    
                        // Calculate the new cost
                        int newCost = BFSPath.getPath(grid,loc,newLoc).size();
                        temp.setCost(newCost);
                        temp.addLocation(newLoc);
                        tempCostTable.add(temp);    
                    }
                    int newCost = BFSPath.getPath(grid,loc,nearestPellets.get(0)).size();
                    n.setCost(newCost); 
                    n.addLocation(nearestPellets.get(0)); 
                }
                // print number of nodes since last step
                System.out.print(nodeIndex++);
                n.print(stepNumber);
            }
            costTable.addAll(tempCostTable);
        }

        for(int i = 0; i < costTable.size(); i++)
        {
            if(i == 0)  
            {
                cost = costTable.get(0).getCost();
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
            visited = new HashSet<Point>();
        }

        if(path.isEmpty()) 
        {
            Point tgt = targets.remove(0);
            path = BFSPath.getPath(grid, pc.getLoc(), tgt);
        }
         
         // take the next step on the current path
        
         Point next = path.remove(0);
         visited.add(next);
         PacFace face = PacUtils.direction( pc.getLoc(), next );

         System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
               ++simTime, pc.getLoc().x, pc.getLoc().y, face );

         return face;
     }
}