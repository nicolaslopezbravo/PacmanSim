
package jumpforjoy;

import java.awt.Point;
import java.util.List;
import pacsim.AbstractPlayer;
import pacsim.BFSPath;
import pacsim.FoodCell;
import pacsim.HouseCell;
import pacsim.MorphCell;
import pacsim.MorphFoodCell;
import pacsim.MorphMode;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacTeam;
import pacsim.PacUtils;
import pacsim.PowerCell;
import pacsim.WallCell;

/**
 *
 * @author glinosd
 */
public class Player extends AbstractPlayer {
      
   public Player() {
      System.out.println("team1 Player constructor...");
   }

   @Override
   public void init() {
      System.out.println( morphTeam + " " + morphID + " team1 Player init..." );
   }
   
   @Override
   public PacFace action(Object state) {
      
      PacCell[][] grid = ( PacCell[][] ) state;      
      PacFace[] faces = PacUtils.randomFaces();
      Point p = morph.getLoc();

      // in ghost mode, eat an opposing pacmen in an adjacent cell, if possible
      if( morph.getMode() == MorphMode.GHOST ) {         
         for( PacFace face : faces ) {
            PacCell pc = PacUtils.neighbor( face, p, grid );            
            if( pc instanceof MorphCell &&
                ((MorphCell) pc).getTeam() != morph.getTeam() &&
                ((MorphCell) pc).getMode() == MorphMode.PACMAN )
            {
               return face;
            }
         }
         
         // if there is an opposing Pacman, go after it
         PacFace face = hunt( grid, p );
         if( face != null ) {
            return face;
         }
            
         // otherwise, go for the nearest food or a random cell
         return advance( grid, p, faces );
      }
      
      // in pacman mode, eat adjacent food, if possible
      else {
         for( PacFace face : faces ) {
            PacCell pc = PacUtils.neighbor( face, p, grid );
            if( pc instanceof MorphFoodCell &&
                ((MorphFoodCell) pc).getTeam() != morph.getTeam()  )
            {
               return face;
            }
         }
         
         // avoid opposing ghosts if they are too close
         PacFace face = avoid( grid, p, faces );
         if( face != null ) {
            return face;
         }
         
         // otherwise, go for the nearest food or a random cell
         return advance( grid, p, faces );
      }
   }
   
   private PacFace hunt( PacCell[][] grid, Point p ) {
      PacTeam opponent = PacUtils.opposingTeam( morphTeam );
      List<Point> tgts = PacUtils.findMorphs(grid, opponent );
      if( !tgts.isEmpty() ) {
         for( Point tgt : tgts ) {
            if( tgt != null && ((MorphCell) grid[ tgt.x ][ tgt.y ]).
                  getMode() == MorphMode.PACMAN ) 
            {
               List<Point> tmp = BFSPath.getPath(grid, p, tgt );
               Point next = tmp.remove( 0 );
               if( next != null && open( grid, next ) ) {
                  PacFace face = PacUtils.direction( p, next );
                  if( face != null ) {
                     return face;
                  }
               }
            }
         }
      }
      return null;
   }
   
   private PacFace avoid( PacCell[][] grid, Point p, PacFace[] faces ) {
      PacTeam opponent = PacUtils.opposingTeam( morphTeam );
      List<Point> tgts = PacUtils.findMorphs(grid, opponent );
      if( !tgts.isEmpty() ) {
         for( Point tgt : tgts ) {
            if( tgt != null && ((MorphCell) grid[ tgt.x ][ tgt.y ]).
                  getMode() == MorphMode.GHOST ) 
            {
               List<Point> tmp = BFSPath.getPath(grid, p, tgt );
               if( tmp.size() <= 2 ) {
                  Point next = tmp.remove( 0 );
                  PacFace fx = PacUtils.direction( p, next );
                  for( PacFace face : faces ) {
                     if( face != fx ) {
                        PacCell pc = PacUtils.neighbor( face, p, grid );
                        if( open( grid, pc.getLoc() ) ) {
                           return face;
                        }
                     }
                  }
               }
            }
         }
      }
      return null;
   }
   
   private PacFace advance( PacCell[][] grid, Point p, PacFace[] faces ) {
      PacTeam opponent = PacUtils.opposingTeam( morphTeam );
      Point target = PacUtils.nearestFood( grid, p, opponent );
      if( target != null ) {
         List<Point> tmp = BFSPath.getPath(grid, p, target );
         Point next = tmp.remove( 0 );
         if( next != null && open( grid, next ) ) {
            PacFace face = PacUtils.direction( p, next );
            return face;
         }
      }
      return randomOpen( faces, grid );
   }
   
   private boolean open( PacCell[][] grid, Point p ) {
      if( grid[ p.x ][ p.y ] instanceof FoodCell ) return true;
      if( grid[ p.x ][ p.y ] instanceof PowerCell ) return true;
      if( grid[ p.x ][ p.y ] instanceof MorphCell ) return false;
      if( grid[ p.x ][ p.y ] instanceof WallCell ) return false;
      return true;      
   }
   
   private PacFace randomOpen( PacFace[] faces, PacCell[][] grid ) {         
      for( PacFace face : faces ) {      
         Point p = morph.getLoc();
         PacCell pc = PacUtils.neighbor( face, p, grid );
         
         if( !(pc instanceof WallCell) && 
             !(pc instanceof MorphCell) &&               
             ( morph.getMode() == MorphMode.GHOST || !(pc instanceof HouseCell) ) )  
         {
            return face;
         }      
      }
      return null;
   }
}
