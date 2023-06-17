
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.awt.Color;

public abstract class GameObject{
    protected double x, y, sizeX, sizeY, velX, velY;
    protected GameObjectID id;
    protected BufferedImage collisionImage; 
    
    public GameObject(double x, double y, GameObjectID id, double sizeX, double sizeY, BufferedImage collisionImage){
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
    public abstract void tick(ArrayList <GameObject> objectList);
    public abstract void render(Graphics g);
    public Rectangle getBounds(){
        return new Rectangle(floor(x), floor(y), floor(sizeX), floor(sizeY));
    }  
    
    public int floor (double value){
        return (int) Math.floor(value);
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
