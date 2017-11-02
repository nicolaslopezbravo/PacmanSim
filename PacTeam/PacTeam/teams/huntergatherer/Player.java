package huntergatherer;
import java.io.*;
import java.awt.*;
import java.util.List;
import pacsim.*;

/**
 *  Nicolas Lopez
 *  Alexandra Aguirre
 *  Pacman Team Tournament
 * 
 *  11/1/2017
 */
public class Player extends AbstractPlayer 
{     

   public Player() 
   {
      System.out.println("Player constructor...");
   }

   private int strat;

   @Override
   public void init() 
   {
      System.out.println( morphTeam + " " + morphID + " team Player init...");
      strat = 0;
   }
   
   @Override
   public PacFace action(Object state) 
   {  
      PacCell[][] grid = ( PacCell[][] ) state;      
      PacFace[] faces = PacUtils.randomFaces();
      Point p = morph.getLoc();
      boolean hunter = (this.getID() == 1) ? true : false;
      boolean gatherer = !hunter;

      if(hunter)
      {
            if(morph.getMode() == MorphMode.GHOST && strat >= 4)
            {
                // if there is an opposing Pacman, go after it
                PacFace face = hunt(grid, p);
                if(face != null) return face;
                strat = 0;

            }else
            {
                // go protect the food
                Point target = PacUtils.nearestFood(grid, p, morphTeam);

                List<Point> path = BFSPath.getPath(grid, p, target);
                Point next = path.remove(0);
                if(next != null && open(grid, next)) 
                {
                   PacFace face = PacUtils.direction(p, next);
                   if(face != null) return face;
                }
                strat++; // have it go on for a while to pretevent thrashing
            }
      }
      if(gatherer)
      {
          // avoid opposing ghosts if they are too close
          PacFace face = avoid(grid, p, faces);
          if(face != null) return face;
          for(PacFace f : faces) 
          {
            PacCell pc = PacUtils.neighbor(f, p, grid); // if the food is edible
            if(pc instanceof MorphFoodCell && ((MorphFoodCell)pc).getTeam() != morph.getTeam()) 
            {
                return face;
            }
          }
      }
      // otherwise, go for the nearest food or a random cell
      return advance(grid, p, faces);
   }
   
   private PacFace hunt(PacCell[][] grid, Point p) 
   {
      PacTeam opponent = PacUtils.opposingTeam(morphTeam);
      List<Point> tgts = PacUtils.findMorphs(grid, opponent);
      if(!tgts.isEmpty()) 
      {
         for(Point tgt : tgts) 
         {
            if(tgt != null && ((MorphCell)grid[tgt.x][tgt.y]).getMode() == MorphMode.PACMAN) 
            {
               List<Point> path = BFSPath.getPath(grid, p, tgt);
               Point next = path.remove(0);
               if(next != null && open(grid, next)) 
               {
                  PacFace face = PacUtils.direction(p, next);
                  if(face != null) return face;
               }
            }
         }
      }
      return null;
   }
   
   private PacFace avoid(PacCell[][] grid, Point p, PacFace[] faces) 
   {
      PacTeam opponent = PacUtils.opposingTeam(morphTeam);
      List<Point> tgts = PacUtils.findMorphs(grid, opponent);

      if(!tgts.isEmpty()) 
      {
         for(Point tgt : tgts)
         {
            if(tgt != null && ((MorphCell) grid[tgt.x][tgt.y]).getMode() == MorphMode.GHOST) 
            {
               List<Point> path = BFSPath.getPath(grid, p, tgt);
               if(path.size() <= 2) 
               {
                  Point next = path.remove(0);
                  PacFace fx = PacUtils.direction(p, next);
                  for(PacFace face : faces) 
                  {
                     if(face != fx) 
                     {
                        PacCell pc = PacUtils.neighbor(face, p, grid);
                        if(open( grid, pc.getLoc())) return face;
                     }
                  }
               }
            }
         }
      }
      return null;
   }
   
   private PacFace advance(PacCell[][] grid, Point p, PacFace[] faces) 
   {
      PacTeam opponent = PacUtils.opposingTeam(morphTeam);
      Point target = PacUtils.nearestFood(grid, p, opponent);

      if(target != null) 
      {
         List<Point> path = BFSPath.getPath(grid, p, target);
         Point next = path.remove(0);
         if(next != null && open(grid, next)) 
         {
            PacFace face = PacUtils.direction(p, next);
            return face;
         }
      }
      return randomOpen(faces, grid);
   }
   
   private boolean open(PacCell[][] grid, Point p) 
   {
      if(grid[p.x][p.y] instanceof MorphCell) return false;
      if(grid[p.x][p.y] instanceof WallCell) return false;
      return true;      
   }
   
   private PacFace randomOpen(PacFace[] faces, PacCell[][] grid) 
   {         
      for(PacFace face : faces) 
      {      
         Point p = morph.getLoc();
         PacCell pc = PacUtils.neighbor(face, p, grid);
         
         if(!(pc instanceof WallCell) && !(pc instanceof MorphCell) && (morph.getMode() == MorphMode.GHOST 
            || !(pc instanceof HouseCell)))  
         {
            return face;
         }      
      }
      return null;
   }
}
