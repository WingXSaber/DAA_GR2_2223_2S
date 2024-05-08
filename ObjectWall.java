
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

public class ObjectWall extends GameObject{    
    
    protected BufferedImage characterSprite; 
    boolean facingLeft = false;
    
    public ObjectWall(GameObjectHandler gameObjectHandler, double x, double y){
        super(gameObjectHandler, x, y, 0, GameObjectID.Wall, new ImageLoader().loadImage("res/coll_circle_32x32.png"));                

        Random r = new Random();
        int a = r.nextInt(4);
        switch(a){
            case 0:
                characterSprite = new ImageLoader().loadImage("res/Tree Sprite.png");
                break;
            case 1:
                characterSprite = new ImageLoader().loadImage("res/Tree Sprite 2.png");
                break;
            case 2:
                characterSprite = new ImageLoader().loadImage("res/Tree Sprite 3.png");
                break;
            case 3:
                characterSprite = new ImageLoader().loadImage("res/Tree Sprite 3.png");
                break;
        }
        facingLeft = r.nextBoolean();
    }

    public void tick(){
        
    }

    public void render(Graphics g){
        if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        }else if (handler.debugNoTextureMode){
            g.setColor(Color.DARK_GRAY);
            //g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
            g.fillRect(floor(x), floor(y-sizeY), floor(sizeX), floor(sizeY*2));
        }else if(characterSprite != null){
            if(facingLeft)
                g.drawImage(characterSprite, floor(x + sizeX/2 - characterSprite.getWidth()/2), floor(y + sizeY -characterSprite.getHeight()), null);
            else
                g.drawImage(characterSprite, floor(x + sizeX/2 + characterSprite.getWidth()/2), floor(y + sizeY -characterSprite.getHeight()),
                        -characterSprite.getWidth(),characterSprite.getHeight(), null);
        }
    }

    public void updateCollisionImageWithNeighbors(){ 
        //used for ObjectWall
        //update the walls and their imageCollision based on neighbor
        //ideally set after all walls have been spawned.
        boolean isUpLeftWall    = isWallAt(x-handler.gameUnit, y-handler.gameUnit),
                isUpWall        = isWallAt(x                 , y-handler.gameUnit),
                isUpRightWall   = isWallAt(x+handler.gameUnit, y-handler.gameUnit),
                isLeftWall      = isWallAt(x-handler.gameUnit, y                 ),
                isRightWall     = isWallAt(x+handler.gameUnit, y                 ),
                isDownLeftWall  = isWallAt(x-handler.gameUnit, y+handler.gameUnit),
                isDownWall      = isWallAt(x                 , y+handler.gameUnit),
                isDownRightWall = isWallAt(x+handler.gameUnit, y+handler.gameUnit);

        Graphics g = this.collisionImage.getGraphics();
        g.setColor(Color.WHITE);
        if(isUpLeftWall)
            g.fillRect(0, 0, floor(sizeX/2), floor(sizeY/2));
        if(isUpWall)
            g.fillRect(0, 0, floor(sizeX), floor(sizeY/2));
        if(isUpRightWall)
            g.fillRect(floor(sizeX/2), 0, floor(sizeX/2), floor(sizeY/2)); 
        if(isLeftWall)
            g.fillRect(0, 0, floor(sizeX/2), floor(sizeY));
        if(isRightWall)
            g.fillRect(floor(sizeX/2), 0, floor(sizeX/2), floor(sizeY));          
        if(isDownLeftWall)
            g.fillRect(0, floor(sizeY/2), floor(sizeX/2), floor(sizeY/2));
        if(isDownWall)
            g.fillRect(0, floor(sizeY/2), floor(sizeX), floor(sizeY/2));
        if(isDownRightWall)
            g.fillRect(floor(sizeX/2), floor(sizeY/2), floor(sizeX/2), floor(sizeY/2));

        

        g.dispose();               
        
    }

    private boolean isWallAt(double i, double j){       
        ArrayList <GameObject> objectList = handler.objectHashMap.get(handler.keyFromCoordinate(i,j));
        if(objectList != null)
            for(GameObject object: objectList)
                if(object.id == GameObjectID.Wall)
                    return true;
        return false;        
    }

}

