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
    public List<Point> grid;
    public int cost = 0;

    Node(Point position, int c, PacCell[][] grid)
    {
        // add initial starting point and cost to get there
        path = new ArrayList<Point>();
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
        System.out.println("SIZEEE: " + size);
        for(int i = 0; i < size-1; i++)
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

public class PacSimEdit implements PacAction
{
    private List<Point> path;
    private int simTime;
    private static boolean plan = true;
    public static List<Point> targets;

    public PacSimEdit(String fname)
    {
        PacSim sim = new PacSim(fname);
        sim.init(this);
    }

    public static void main(String [] args)
    {
        new PacSimEdit(args[0]);
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

        int size = food.size();
        int [][] costTable = new int[size][size];

        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
            {
                
                Point x = food.get(i);
                Point y = food.get(j);
                List<Point> p = BFSPath.getPath(grid, x, y);   
                int len = p.size();
                
                costTable[i][j] = len;
            }
        }

        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
                System.out.format("%4d", costTable[i][j]);
            System.out.println();
        }

       // printPopulation(costTable, grid, food, pc);

        System.out.println();

    /* ************************************ */
        Point nearest = PacUtils.nearestFood(pc, grid);
        List<Point> newPath = BFSPath.getPath(grid, pc, nearest);
        int cost = newPath.size();
        HashMap<Point, Integer> p = new HashMap<>();
        HashSet<Point> dirty = new HashSet<>();
        p.put(nearest, cost);
        int total = 0;
     //   System.out.println("nearest: " + p);

        for(int i = 0; i < food.size()-1; i++)
        {
            Point current = food.get(i);
            nearest = PacUtils.nearestFood(current, grid);
            System.out.println("current nearest: " + nearest);
            newPath = BFSPath.getPath(grid, current, nearest);
            cost = newPath.size();
            if(p.containsKey(current))
            {
                // move on
                System.out.println("CONTAINS: " + current);
            }
            else
            {
                total += cost;
                dirty.add(current);
                p.put(nearest, cost);
                System.out.println("current: " + current);
                System.out.println("nearest: " + p);
                System.out.println();
                System.out.println("Hash Set: " + dirty);
            }
            
        }
        System.out.println("TOTAL: " + total);
    /* *********************************************** */
    }

    private static void printPopulation(ArrayList<Node> arr)
    {
        for(int i = 0; i < 10; i++)
        {
            Node n = arr.get(i);
            ArrayList<Point> p = n.getPath();
            System.out.print(i + " : " + " cost=" + n.getCost());
            boolean flag = true;
            for(Point z : p)
            {
                int x = (int)z.getX();
                int y = (int)z.getY();
                if(flag)
                {
                    System.out.print("               [(" + x + "," + y +") ," + "c" + "] ");
                    flag = false;
                }
                System.out.print("[(" + x + "," + y  +") ," + "c" +"] ");
            }
            System.out.println();            
        }
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
            for(Point m : arr)
            {
                nearestPellets.add(m);
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

        printCostTable(food, pacman, grid);
        printFoodArray(food);

        ArrayList<Node> costTable = new ArrayList<Node>(size);

        // Initialize the cost table
        for(int row = 0; row < size; row++)
        {
            // Calculate the new postion of the possible path
            Point position = food.get(row);

            // Calculate the cost to the each initial food
            List<Point> initialCost = BFSPath.getPath(grid, pacman, position);
            int cost = initialCost.size();   
            
            // Add the new cost and postion of the possible path to the list of options
            costTable.add(new Node(position, cost, PacUtils.cloneGrid(grid)));
        }

        // Fill out cost table
        for(int i = 0; i < size; i++)
        {
            Node n = costTable.get(i);
            System.out.println("Population at step: " + i);
            int numNodes = 0;
            while(n.getGrid().size() > 0)
            {
                Point loc = n.getLocation();  
                ArrayList<Point> nearestPellets = nearFood(loc, grid ,n.getGrid());

               // printPopulation(costTable);

                for(int j = 1; j < nearestPellets.size(); j++)
                {
                    Point newLoc = nearestPellets.remove(j);

                    // Copy grid and path elements to new instances
                    List<Point> newGrid = new ArrayList<Point>();
                    ArrayList<Point> newPath = new ArrayList<Point>();

                    for(Point k: n.getGrid())
                    {
                        newGrid.add(k);
                    }
                    for(Point k: n.getPath())
                    {
                        newPath.add(k);
                    }
                    
                    Node temp = new Node (newPath, n.getCost(), newGrid);

                    // Calculate the new cos
                    List<Point> newCostPath = BFSPath.getPath(grid, loc, newLoc);
                    int newCost = newCostPath.size();
                    temp.setCost(newCost);
                    temp.addLocation(newLoc);
                    //temp.print();
                    costTable.add(temp);
                }
                Point newLoc = nearestPellets.remove(0);
                // Calculate the new cost
                List<Point> temp = BFSPath.getPath(grid, loc, newLoc);

                int newCost = temp.size();
                n.setCost(newCost);
                n.addLocation(newLoc);                                     
            }
            
            System.out.println();
        }

        System.out.println();
        System.out.println();

        // Find lowest cost

        for(int i = 0; i < costTable.size(); i++)
        {
            if(costTable.get(i).size > 2)
                costTable.get(i).print();
        }



        int cost = costTable.get(0).getCost();
        List<Point> optimalPath = costTable.get(0).getPath();
        
        for(int i = 1; i < food.size(); i++)
        {
            if(costTable.get(i).getCost() < cost)
            {
                cost = costTable.get(i).getCost();
                optimalPath = costTable.get(i).getPath();
            }
        }

        System.out.println("OPTIMAL PATH: " + optimalPath);
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