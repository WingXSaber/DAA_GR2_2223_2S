
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Color;

public abstract class GameObject{
    protected double x, y, sizeX, sizeY, velX, velY;
    protected GameObjectID id;
    protected BufferedImage collisionImage; 
    public GameObjectHandler handler;

    public GameObject(GameObjectHandler handler, double x, double y, 
                      GameObjectID id, double sizeX, double sizeY, BufferedImage collisionImage){
        this.handler = handler;
        this.x = x;
        this.y = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.id = id;
        //initalize collisionImage 
        if(collisionImage == null && !id.equals(GameObjectID.OutWall)){ //if no buffered image and not an outwall
            this.collisionImage = new BufferedImage(floor(sizeX), floor(sizeY), BufferedImage.TYPE_BYTE_BINARY);
            Graphics g = this.collisionImage.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, floor(sizeX), floor(sizeY));
            g.dispose();        
        }else
            this.collisionImage = collisionImage;        
        //if object is an outWall, no collisionImage needed
    }
    public abstract void tick();
    public abstract void render(Graphics g);
    public Rectangle getBounds(){
        return new Rectangle(floor(x), floor(y), floor(sizeX), floor(sizeY));
    }  
    
    public int floor (double value){
        return (int) Math.floor(value);
    }

    public boolean checkAnyCollision(){
        /*
        Here's the idea:
            the Hashtable will hold a list containing references to entities. Each keyword is a 
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
        for(int i = floor(x/handler.gameUnit)-1; i<floor((x+sizeX)/handler.gameUnit)+1; i++)
            for(int j = floor(y/handler.gameUnit)-1; j<floor((y+sizeY)/handler.gameUnit)+1; j++){
                ArrayList <GameObject> objectList = handler.objectHashMap.get(new Point(i,j));
                if(objectList != null)                    
                    for(GameObject object: objectList){                                           
                        if(!object.equals(this) && isCollidingWith(object)){
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
                            obj2pixel = object.collisionImage.getRGB(x - obj2.x , y - obj2.y);
                        //if(obj1pixel == 0xFF000000 && obj2pixel == 0xFF000000)
                        if(obj1pixel == obj2pixel) //if both are white
                            return true;    //if yes collision
                    }    
                }
            }
        }
        //if no collision
        return false;
    }

    private Rectangle getIntersectionBounds(Rectangle r1, Rectangle r2){
        Area area1 = new Area(r1),
             area2 = new Area(r2);
        area1.intersect(area2);
        return area1.getBounds();
    }

    
}
