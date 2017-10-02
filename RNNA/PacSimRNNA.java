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
        // add another position
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
        System.out.println("Cost of this path is: " + cost);
        System.out.println("Positions of the path are: ");
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
        System.out.println("\nMaze : " + args[0] + "\n");
    }

    @Override
    public void init() 
    {
        simTime = 0;
        path = new ArrayList();
    }

    private ArrayList<Point> nearFood(Point p, List<Point> arr)
    {
        ArrayList<Point> equidistantPellets = new ArrayList<Point>();
        int size = arr.size();
        Point newLoc = arr.get(0);
        int cost = PacUtils.manhattanDistance(p, newLoc);

        for(int i = 1; i < size; i++)
        {
            int newCost = PacUtils.manhattanDistance(p, arr.get(i));
            System.out.println("New cost is: " + newCost + " Regular cost: " + cost);
            if(newCost <= cost)
            {
                cost = newCost;
                Point x = arr.get(i);
                equidistantPellets.add(x);
                System.out.println("Cost of added pellet: " + cost);
            }
        }
        System.out.println("SIZE OF ARRAY " + arr.size() + " ARRAY: " +arr.toString());
        
        if(size <= 2)
        {
            for(int i = 0; i < size; i++)
            {   
                equidistantPellets.add(arr.get(i));
            }
        }
        

        return equidistantPellets;
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
        
        for(int i = 0; i < size; i++)
        {
            Node n = costTable.get(i);

            while(n.getGrid().size() > 0)
            {
                Point loc = n.getLocation();        
                // the bug is in here    
                System.out.println("GRID SIZE: " + n.getGrid().size());    
                ArrayList<Point> equidistantPellets = nearFood(loc, n.getGrid());
                System.out.println();
                
                
                //  if two or more options, create new nodes for them
                for(int j = 1; j < equidistantPellets.size(); j++)
                {
                    Point newLoc = equidistantPellets.remove(j);
                    Node temp = new Node (n.getPath(),n.getCost(),n.getGrid());
                    // Calculate the new cost with the manhattan distance
                    int newCost = PacUtils.manhattanDistance(loc,newLoc);
                    temp.setCost(newCost);
                    temp.addLocation(newLoc);
                    costTable.add(temp);
                    size++;
                } 
                
                Point newLoc = equidistantPellets.remove(0);
                // Calculate the new cost with the manhattan distance
                int newCost = PacUtils.manhattanDistance(loc,newLoc);
                n.setCost(newCost);
                n.addLocation(newLoc);                          
            }
        }

        int cost = costTable.get(0).getCost();
        ArrayList<Point> optimalPath = new ArrayList<Point>();
        for(int i = 1; i < costTable.size(); i++)
        {
            if(costTable.get(i).getCost() < cost)
            {
                cost = costTable.get(i).getCost();
                optimalPath = costTable.get(i).getPath();
            }
        }

        List<Point> op = new ArrayList<Point>();
        for(Point p : optimalPath)
        {
            op.add(p);
        }
        
        return op;
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
        }

        if( path.isEmpty() ) 
        {
            Point tgt = targets.remove(0);
            path = BFSPath.getPath(grid, pc.getLoc(), tgt);
            
            System.out.println("Pac-Man currently at: [ " + pc.getLoc().x
                  + ", " + pc.getLoc().y + " ]");
            System.out.println("Setting new target  : [ " + tgt.x
                  + ", " + tgt.y + " ]");
         }
         
         // take the next step on the current path
         
         Point next = path.remove( 0 );
         PacFace face = PacUtils.direction( pc.getLoc(), next );
         System.out.printf( "%5d : From [ %2d, %2d ] go %s%n", 
               ++simTime, pc.getLoc().x, pc.getLoc().y, face );

        // If pacman eats them by accident, we must dequeue them

         return face;
     }
}