
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
//import java.awt.Color;

public class ObjectWallTree extends GameObject{    

    public ObjectWallTree(double x, double y, double sizeX, double sizeY, BufferedImage collisionImage){
        super(x,y, GameObjectID.Wall, sizeX, sizeY, collisionImage);        
    }

    public void tick(ArrayList <GameObject> objectList){
        
    }

    public void render(Graphics g){
        g.setColor(Color.black);
        g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
        //g.drawImage(collisionImage, floor(x), floor(y), null);
    }

}

