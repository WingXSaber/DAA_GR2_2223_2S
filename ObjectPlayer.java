import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ObjectPlayer extends GameObject{    
    Game game; //transfer this shit to gameobjecthandler.
    private double speed = 10;
    
    public ObjectPlayer(GameObjectHandler handler, double x, double y, 
                        double sizeX, double sizeY, Game game, BufferedImage collisionImage){
        super(handler, x, y, GameObjectID.Player, sizeX, sizeY, collisionImage);        
        this.game = game;      
    }

    public void tick(){        
        //handler.remove(this);
        
        if(game.left && velX>-speed)
            velX--;
        if(game.right && velX<speed)
            velX++;
        if(game.left && game.right || !game.left && !game.right)
            velX = 0;
        x += velX;
        
        //floor(sizeX/gameUnit ) = amount of cells in X??

        //this is naive, need to change later when data structure works
        /*
        thisLoop:
        for(int i=0; i<objectList.size(); i++){
            //if(!objectList.get(i).id.equals(GameObjectID.Player)){
            if(!objectList.get(i).equals(this)){
                //if(objectList.get(i).getBounds().intersects(this.getBounds())){
                if(isCollidingWith(objectList.get(i))){
                    x-=velX;
                    velX=0;
                    break thisLoop;
                }                    
            }
        }*/
        //handler.remove(this);   
        
        if(checkAnyCollision()){
            x-=velX;
            velX=0;
        }
        
        // handler.add(this);
        
               
        if(this.game.up && velY>-speed)
            velY--;
        if(game.down && velY<speed)
            velY++;
        if(game.up && game.down || !game.up && !game.down)
            this.velY = 0;        
        y += velY;

        //this is naive, need to change later when data structure works
        /*thisLoop:
        for(int i=0; i<objectList.size(); i++){
            //if(!objectList.get(i).id.equals(GameObjectID.Player)){
            if(!objectList.get(i).equals(this)){            
                //if(objectList.get(i).getBounds().intersects(this.getBounds())){
                if(isCollidingWith(objectList.get(i))){
                    y-=velY;
                    velY=0;
                    break thisLoop;
                }                    
            }
        }*/            
        
        if(checkAnyCollision()){
            y-=velY;
            velY=0;
        }
        
        
        //handler.add(this);
    }       


    public void render(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(floor(x),floor(y-sizeY), floor(sizeX), floor(sizeY*2));          
        // g.drawImage(collisionImage, floor(x),floor(y), null);
    }

    public double getKeyX(){
        return x;
    }
    public double getKeyY(){
        return y;
    }
}