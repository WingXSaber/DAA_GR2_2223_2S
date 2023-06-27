import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ObjectPlayer extends GameObject{        
    private double speed = 10;
    
    public ObjectPlayer(GameObjectHandler handler, double x, double y, 
                        double sizeX, double sizeY, BufferedImage collisionImage){
        super(handler, x, y, GameObjectID.Player, sizeX, sizeY, collisionImage);  
    }

    public void tick(){        
        //Movement ===============================================================
        if(handler.left && handler.right || !handler.left && !handler.right) {                   
            if(velX>0)    
                if(velX<1)  //if floating value but near zero.
                    velX = 0;
                else
                    velX--;
            else if(velX<0)  
                if(velX>1)
                    velX = 0;
                else              
                    velX++;                    
        }else{
            if(handler.left && velX>-speed)
                velX--;
            else if(handler.right && velX<speed)
                velX++;
        }        

        if(velX!=0){            
            handler.removeFromHashMap(this);      
            x += velX;
            if(checkAnyCollision()){
                x-=velX;
                velX=0;
            }
            handler.addToHashMap(this);
        } 


        if(handler.up && handler.down || !handler.up && !handler.down){
             if(velY>0)
                if(velY<1)  //if floating value but near zero.
                    velY = 0;
                else
                    velY--;
            else if(velY<0)
                if(velY>1)
                    velY = 0;
                else
                    velY++;
        }else{ 
            if(this.handler.up && velY>-speed)
                velY--;
            else if(handler.down && velY<speed)
                velY++;
        }

        if(velY!=0){
            handler.removeFromHashMap(this);      
            y += velY;                
            if(checkAnyCollision()){
                y-=velY;
                velY=0;
            }
            handler.addToHashMap(this);
        }   
        //========================================================================
    }       


    public void render(Graphics g) {
        if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        }else if (handler.debugNoTextureMode){
            g.setColor(Color.WHITE);
            //g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
            g.fillRect(floor(x), floor(y-sizeY), floor(sizeX), floor(sizeY*2));
        }
    }
}