
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ObjectWall extends GameObject{    

    public ObjectWall(GameObjectHandler gameObjectHandler, double x, double y, double sizeX, double sizeY, BufferedImage collisionImage){
        super(gameObjectHandler, x, y, GameObjectID.Wall, sizeX, sizeY, collisionImage);        
    }

    public void tick(){
        
    }

    public void render(Graphics g){
        if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        }else if (handler.debugNoTextureMode){
            g.setColor(Color.BLACK);
            //g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
            g.fillRect(floor(x), floor(y-sizeY), floor(sizeX), floor(sizeY*2));
        }
    }

}

