
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class GameObject{
    protected double x, y, sizeX, sizeY, velX, velY, speed, centerX, centerY;
    private double sizeXHalf, sizeYHalf;
    protected int health = 0, healthMax = 0;
    protected GameObjectID id;
    protected BufferedImage collisionImage; 
    protected GameObjectHandler handler;
    protected ObjectState state = ObjectState.idle;

    public GameObject(GameObjectHandler handler, double x, double y, double speed,
                      GameObjectID id, BufferedImage collisionImage){
        this.handler = handler;
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.sizeX = collisionImage.getWidth();
        this.sizeY = collisionImage.getHeight();
        this.id = id;
        this.collisionImage = collisionImage;            
        this.sizeXHalf = sizeX/2;
        this.sizeYHalf = sizeY/2;
        this.centerX = x+sizeXHalf;
        this.centerY = y+sizeYHalf;
    }
    
    public BufferedImage loadImage(String path){        
        return new ImageLoader().loadImage(path);
    }

    public abstract void tick();
    
    public abstract void render(Graphics g);

    public Rectangle getBounds(){
        return new Rectangle(floor(x), floor(y), floor(sizeX), floor(sizeY));
    }  
    
    public int floor (double value){
        return (int) Math.floor(value);
    }
    public double abs (double value){
        return (double) Math.abs(value);
    }
    public float abs (float value){
        return (float) Math.abs(value);
    }

    public void applyVelocity(){
        if(velX!=0){ //update x position
            handler.removeFromHashMap(this);      
            x += velX;
            if(isAnyCollision()){
                //Slide in x velocity
                boolean sideCollide = true;
                if( sideCollide){ //slide to the up
                    y-=1;
                    if(isAnyCollision()){
                        y-=1;
                        if(isAnyCollision()){
                            y+=2;                              
                            sideCollide = true;
                        }else   
                            sideCollide = false;
                    }else   
                         sideCollide = false;
                }
                if(sideCollide){ //slide to the down
                    y+=1;
                    if(isAnyCollision()){
                        y+=1;
                        if(isAnyCollision()){
                            y-=2;                    
                            sideCollide = true;
                        }else   
                            sideCollide = false;
                    }else   
                         sideCollide = false;
                }
                
                if(sideCollide){ //cha-cha no slide in x, go back
                    x-=velX;
                    velX=0;   
                }
            }
            handler.addToHashMap(this);
        }/*else{
            if(x% 1 != 0) //if there is lingering floating value, floor it.
                x = floor(x);
        }*/

       if(velY!=0){//update y position
           handler.removeFromHashMap(this);      
           y += velY;                
           if(isAnyCollision()){
               //Slide in x velocity                    
               boolean sideCollide = true;
               if( sideCollide){
                   x-=1;
                   if( sideCollide){
                       x-=1;
                       if(isAnyCollision()){ //slide to the left
                           x+=2;                              
                           sideCollide = true;
                       }else   
                           sideCollide = false;
                   }else   
                        sideCollide = false;
               }
               if(sideCollide){
                   x+=1;
                   if(isAnyCollision()){ //slide to the right
                       x+=1;                 
                       if(isAnyCollision()){
                           x-=2;                    
                           sideCollide = true;
                       }else   
                           sideCollide = false;
                   }else   
                        sideCollide = false;
               }
               
               if(sideCollide){ //cha-cha no slide in y, go back
                   y-=velY;
                   velY=0;   
               }
           }
           handler.addToHashMap(this);
        }/*else{ 
            if(y% 1 != 0) //if there is lingering floating value, floor it.
                y = floor(y);
        }*/

        this.centerX = x+sizeXHalf;
        this.centerY = y+sizeYHalf;
   }

    public void applyFriction(){
        //reduce velocity / apply friction
        applyFrictionX();
        applyFrictionY();
    }

    public void applyFrictionX(){
        //reduce X velocity / apply friction
        if(velX>0)    
            if(velX<1)  //if floating value but near zero.
                velX = 0;
            else
                velX--;
        else if(velX<0)  
            if(velX>1)  //if floating value but near zero.
                velX = 0;
            else              
                velX++; 
    }

    public void applyFrictionY(){
        //reduce Y velocity / apply friction
        if(velY>0)
            if(velY<1)  //if floating value but near zero.
                velY = 0;
            else
                velY--;
        else if(velY<0)
            if(velY>1)  //if floating value but near zero.
                velY = 0;
            else
                velY++;
    }

    /*
        Here's the idea:
            the Hashmap will hold a list containing references to entities. Each keyword is a 
        tuple/an ordered pair representing i,j repectively, a 'cell' that will hold an area 
            A cell will cover an area with respect to its index/keyword. 
        IE. Diameter is 10
            [1,1] will cover i: 10-19 , j: 10-19 
            [2,3] will cover i: 20-29 , j: 30-39            

        Visual diagram:
        ---------------------------------
        | i-1, j-1 |  i, j-1 | i+1, j-1 |
        ---------------------------------
        | i-1, j   |  i, j   | i+1, j   |
        ---------------------------------                
        | i-1, j+1 |  i, j+1 | i+1, j+1 |
        ---------------------------------
    
            The collision will check it's neighboring 'cells' (+1/+0/-1 i, +1/+0/-1 j), 
        NW, N, NE, W, center, E, SW, S, and SE.        
            If Entity moves, and out of the cell, put it in the proper cell. If selected cell
		does not eiist, create it by calling a function here. This class will store the matrix.   
            i+1 is usually the sizeX.
            j+1 is usually the sizeY.
			
			However, there might be entities that are larger than one cell. That's why we 
		check the size and base the iteration there.        
		Visual diagram: (example, an entiiy who's 2x1 cells big)
        --------------------------------------------
        | i-1, j-1 |  i, j-1 |          | i+1, j-1 |
        --------------------------------------------
        | i-1, j   |  i, j   |          | i+1, j   |
        --------------------------------------------                
        | i-1, j+1 |  i, j+1 |          | i+1, j+1 |
        --------------------------------------------

        Visual diagram: (example, an entiiy who's 2x2 cells big)
        --------------------------------------------
        | i-1, j-1 |  i, j-1 |          | i+1, j-1 |
        --------------------------------------------
        | i-1, j   |  i, j   |          | i+1, j   |
        --------------------------------------------                
        |          |         |          |          |
        --------------------------------------------                
        | i-1, j+1 |  i, j+1 |          | i+1, j+1 |
        --------------------------------------------

        */  

    public boolean isAnyCollision(){          
        //checks for any collision with any objects
        for(int i = floor(x/handler.gameUnit)-1 ; i<floor((x+sizeX)/handler.gameUnit)+1; i++)
            for(int j = floor(y/handler.gameUnit)-1; j<floor((y+sizeY)/handler.gameUnit)+1; j++){
                ArrayList <GameObject> objectList = handler.objectHashMap.get(new Point(i,j));
                if(objectList != null)                    
                    for(GameObject object: objectList){                                           
                        if(object.id != GameObjectID.Attack && !object.equals(this) && isCollidingWith(object)){
                            return true;
                        }
                    }
            }
        
        //if no collision
        return false;
    }

    public boolean isAnyEnemyCollidableAt(double tx, double ty){          
        //checks for any collision with any objects at a certain location
        for(int i = floor(tx/handler.gameUnit)-1 ; i<floor((tx+sizeX)/handler.gameUnit)+1; i++)
            for(int j = floor(ty/handler.gameUnit)-1; j<floor((ty+sizeY)/handler.gameUnit)+1; j++){
                ArrayList <GameObject> objectList = handler.objectHashMap.get(new Point(i,j));
                if(objectList != null)                    
                    for(GameObject object: objectList){                                           
                        if(object.id == GameObjectID.Enemy && !object.equals(this) && isCollidingWithAt(object, tx, ty)){
                            return true;
                        }
                    }
            }
        
        //if no collision
        return false;
    }
    
    public boolean isCollidingWith(GameObject object){
        //check boundaries between this and other object first
        //if there is collision within bounding boxes/rectangles
        Rectangle obj1 = getBounds(),
                  obj2 = object.getBounds();
        if(obj1.intersects(obj2)){
            Rectangle bounds =  getIntersectionBounds(obj1, obj2);
            if(!bounds.isEmpty()){
                // Check all the pixels in the collisionBounds to determine
                // if there are any non-alpha pixel collisions.
                for(int x = bounds.x; x < bounds.x+bounds.getWidth(); x++){
                    for(int y = bounds.y; y < bounds.y+bounds.getHeight(); y++){
                        //check if that specific pixel has both white pixel
                        //getRGB returns hex value
                        //0xFF000000 is white                        
                        int obj1pixel = collisionImage.getRGB(x - obj1.x , y - obj1.y),
                            obj2pixel = object.collisionImage.getRGB(x - obj2.x , y - obj2.y),
                            red1 = (obj1pixel >> 16) & 0xff,
                            green1 = (obj1pixel >> 8) & 0xff,
                            blue1 = (obj1pixel) & 0xff,
                            red2 = (obj2pixel >> 16) & 0xff,
                            green2 = (obj2pixel >> 8) & 0xff,
                            blue2 = (obj2pixel) & 0xff;

                        //this is not smart and I dislike this.
                        if(red1 == 255 && green1 == 255 && blue1 == 255 &&
                           red2 == 255 && green2 == 255 && blue2 == 255)
                        //if(obj1pixel == obj2pixel) //if both are white
                            return true;    //if yes collision
                    }    
                }
            }
        }
        //if no collision
        return false;
    }

    public boolean isCollidingWithAt(GameObject object, double tx, double ty){
        //check boundaries between this (at a certain location) and other object first
        //if there is collision within bounding boxes/rectangles
        Rectangle obj1 = getBounds(),
                  obj2 = object.getBounds();
        obj1.x = (int) tx;
        obj1.y = (int) ty;
        if(obj1.intersects(obj2)){
            return true;
            /*
            //pixel perfect collision
            Rectangle bounds =  getIntersectionBounds(obj1, obj2);
            if(!bounds.isEmpty()){
                // Check all the pixels in the collisionBounds to determine
                // if there are any non-alpha pixel collisions.
                for(int x = bounds.x; x < bounds.x+bounds.getWidth(); x++){
                    for(int y = bounds.y; y < bounds.y+bounds.getHeight(); y++){
                        //check if that specific pixel has both white pixel
                        //getRGB returns hex value
                        //0xFF000000 is white                        
                        int obj1pixel = collisionImage.getRGB(x - obj1.x , y - obj1.y),
                            obj2pixel = object.collisionImage.getRGB(x - obj2.x , y - obj2.y),
                            red1 = (obj1pixel >> 16) & 0xff,
                            green1 = (obj1pixel >> 8) & 0xff,
                            blue1 = (obj1pixel) & 0xff,
                            red2 = (obj2pixel >> 16) & 0xff,
                            green2 = (obj2pixel >> 8) & 0xff,
                            blue2 = (obj2pixel) & 0xff;

                        //this is not smart and I dislike this.
                        if(red1 == 255 && green1 == 255 && blue1 == 255 &&
                           red2 == 255 && green2 == 255 && blue2 == 255)
                        //if(obj1pixel == obj2pixel) //if both are white
                            return true;    //if yes collision
                    }    
                }
            }*/
        }
        //if no collision
        return false;
    }

    public ArrayList <GameObject> getCollidedEnemies(){          
        //checks for all collisions with objects and returns a list of them
        ArrayList <GameObject> collidedEnemies = new ArrayList<>();
        for(int i = floor(x/handler.gameUnit)-1 ; i<floor((x+sizeX)/handler.gameUnit)+1; i++)
            for(int j = floor(y/handler.gameUnit)-1; j<floor((y+sizeY)/handler.gameUnit)+1; j++){
                ArrayList <GameObject> objectList = handler.objectHashMap.get(new Point(i,j));
                if(objectList != null)                    
                    for(GameObject object: objectList){                                           
                        if(object.id == GameObjectID.Enemy && !object.equals(this) && isCollidingWith(object)){
                            collidedEnemies.add(object);
                        }
                    }
            }
        return collidedEnemies;
    }
    

    private Rectangle getIntersectionBounds(Rectangle r1, Rectangle r2){
        Area area1 = new Area(r1),
             area2 = new Area(r2);
        area1.intersect(area2);
        return area1.getBounds();
    }

    public double radiansToDegrees (double value){
         return value * (180 / Math.PI);
    }
    public double degreesToRadians  (double value){
            return value * (Math.PI / 180);
    }

    public double getDegrees(double x1, double y1, double x2, double y2){
        return radiansToDegrees(Math.atan2(y2-y1,x2-x1)) ;
    }

    public boolean isNearViaOrigin(double tx, double ty, double distance){
        double deltaX = abs(tx - this.x),
               deltaY = abs(ty - this.y);        
        //return deltaX <= handler.gameUnit/2 && deltaY <= handler.gameUnit/2;
        return deltaX <= distance && deltaY <= distance;
    }

    public boolean isNearViaCenter(double tx, double ty, double distance){        
        double deltaX = abs(tx - this.centerX),
               deltaY = abs(ty - this.centerY);        
        return deltaX <= distance && deltaY <= distance;
    }


    /*
    //single line from center
    public boolean checkVisualContact(GameObject object){ //uses DDA (Digital Differential Analyzer) Algorithm
        Point origin = handler.keyFromCoordinate(x+(sizeX/2), y+(sizeY/2)),
              target = handler.keyFromCoordinate(object.x+(object.sizeX/2), object.y+(object.sizeY/2));
        float run =  target.x - origin.x,
			  rise = target.y - origin.y,
	   		  steps = abs(run) > abs(rise) ? abs(run) : abs(rise),
    		  xStep = run / steps,
			  yStep = rise / steps,
			  x = origin.x,
			  y = origin.y;
              
		for(int i=0; i<=(float)steps; i++){
			if(handler.gridObstacles[(int)floor(x)][(int)floor(y)]) //if there is a wall
                return false;
			x+=xStep;
			y+=yStep;
		}
        return true;
    }
    */

    //using four points    
    public boolean checkVisualContact(GameObject object){ //uses DDA (Digital Differential Analyzer) Algorithm
        Point origin [] = {handler.keyFromCoordinate(x, y),
                           handler.keyFromCoordinate(x, y+sizeY-1),
                           handler.keyFromCoordinate(x+sizeX-1, y),
                           handler.keyFromCoordinate(x+sizeX-1, y+sizeY-1)},
              target [] = {handler.keyFromCoordinate(object.x, object.y),
                           handler.keyFromCoordinate(object.x, object.y+object.sizeY-1),
                           handler.keyFromCoordinate(object.x+object.sizeX-1, object.y),
                           handler.keyFromCoordinate(object.x+object.sizeX-1, object.y+object.sizeY-1)};
        for(int a=0; a<4; a++){
            float run =  target[a].x - origin[a].x,
		    	  rise = target[a].y - origin[a].y,
	   	    	  steps = abs(run) > abs(rise) ? abs(run) : abs(rise),
    	    	  xStep = run / steps,
		    	  yStep = rise / steps,
		    	  x = origin[a].x,
		    	  y = origin[a].y;
            
		    for(int i=0; i<=(float)steps; i++){
                try{		    	
                    if(handler.gridObstacles[(int)floor(x)][(int)floor(y)])   //if there is a wall                      //  || handler.objectHashMap.get(new Point((int)floor(x),(int)floor(y))) != null)
                        return false;
                }catch(ArrayIndexOutOfBoundsException ex){}
		    	x+=xStep;
		    	y+=yStep;
		    }            
        }
        return true;
    }    
}
