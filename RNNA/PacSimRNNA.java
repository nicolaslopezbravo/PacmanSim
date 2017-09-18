import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import pacsim.*;

/*
CAP 4630 - AI
Program 1 - PacSimRNNA

Alexandra Aguirre
Nicolas Lopez

9-5-17
*/


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
            
        // the cost of pacman to getting to the food (initial start state) will be determined and set as the first row
        // of the cost table

        int size = PacUtils.numFood(grid);
        int [] foodArray = new int[size + 1];
        int [][] cost = new int[size + 1][size + 1];

        // Set each starting point
        Point pacman = pc.getLoc();
        for(int i = 0; i < size; i++)
        {
            cost[i][0] = PacUtils.manhattanDistance(pacman,food.get(i));
            // Debugging
            System.out.println("cost: " + cost[i][0]);
        }

        // filling out the table
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; i < size; i++)
            {

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