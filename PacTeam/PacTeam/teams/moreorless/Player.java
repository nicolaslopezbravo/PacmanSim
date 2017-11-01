
package moreorless;

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
   
   private Point target = null;
   private BFSPath bfs = null;
   
   public Player() {
      System.out.println("team2 Player constructor...");
   }
   
   @Override
   public void init() {
      
      target = null;
      bfs = null;
      System.out.println( morphTeam + " " + morphID + " team2 Player init..." );
   }
   
   @Override
   public PacFace action(Object state) {
      
      //System.out.println( morphTeam + " " + morphID + " team2 Player action..." );
      
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
               target = null;
               bfs = null;
               return face;
            }
         }
                  
         // otherwise, morph #1 advances towards nearest food, if possible,
         // or if not, then chooses a random open cell
         
         if( morph.getPlayer().getID() == 1 ) {
            return advance( grid, p, faces );
         }
         
         // but morph #2 pursues nearest opposing morph, even if cross into
         // enemy territory; if none, then chooses a random open cell
         
         else {
            return hunt( grid, p, faces );
         }
      }
      
      // in pacman mode, eat adjacent food; if none, then advance towards
      // nearest food, if possible; otherwise, choose a random open cell
      
      else {
         for( PacFace face : faces ) {
            PacCell pc = PacUtils.neighbor( face, p, grid );
            if( pc instanceof MorphFoodCell &&
                ((MorphFoodCell) pc).getTeam() != morph.getTeam()  )
            {
               target = null;
               bfs = null;
               return face;
            }
         }
         
         return advance( grid, p, faces );
      }
   }
   
   private PacFace hunt( PacCell[][] grid, Point p, PacFace[] faces ) {
      
      Point next = null;
      if( target == null ) {
         PacTeam opponent = PacUtils.opposingTeam( morphTeam );
         target = PacUtils.nearestMorph( grid, p, opponent );
         if( target != null ) {
            List<Point> path = BFSPath.getPath(grid, p, target );
            if( path != null && path.size() > 0 ) {
               next = path.remove( 0 );
            }
         }
      }

      if( next != null && open( grid, next ) ) {
         PacFace face = PacUtils.direction( p, next );
         target = null;
         bfs = null;            
         return face;
      }
         
      target = null;
      bfs = null;
      return randomOpen( faces, grid );
   }
   
   private PacFace advance( PacCell[][] grid, Point p, PacFace[] faces ) {
      
      Point next = null;
      if( target == null ) {
         PacTeam opponent = PacUtils.opposingTeam( morphTeam );
         target = PacUtils.nearestFood( grid, p, opponent );
         if( target != null ) {
            List<Point> path = BFSPath.getPath(grid, p, target );
            if( path != null && path.size() > 0 ) {
               next = path.remove( 0 );
            }
         }
      }

      if( next != null && open( grid, next ) ) {
         PacFace face = PacUtils.direction( p, next );
         target = null;
         bfs = null;            
         return face;
      }
         
      target = null;
      bfs = null;
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
