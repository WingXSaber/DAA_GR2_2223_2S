import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Point;

public class ObjectEnemy extends GameObject{    
    private ArrayList<Point> path;
    int tick = 0, 
        timerPathTick = 0, timerPathCheck = 1;
    double speed = 0,
           pastX, pastY,
           degreesTarget ;
    boolean seePlayer = false;

    public ObjectEnemy(GameObjectHandler gameObjectHandler, double x, double y, double sizeX, double sizeY, double speed, BufferedImage collisionImage){
        super(gameObjectHandler, x, y, GameObjectID.Enemy, sizeX, sizeY, collisionImage); 
        this.speed = speed;  
        this.pastX = x;
        this.pastY = y;     
    }

    public void tick(){
        tick++;
        if(tick == handler.GAME_HERTZ){
            tick=0;

            timerPathTick++;
            if(timerPathTick==timerPathCheck){
                timerPathTick=0;


                //if(path == null || path.isEmpty() || (pastX == x &&  pastY == y) ){       
                    
                seePlayer = checkVisualContact(handler.player);

                if( !seePlayer 
                        &&
                    (state == GameObjectState.idle || state == GameObjectState.walking && (pastX == x &&  pastY == y)) )
                {               
                    path = generatePathTo(handler.player.x, handler.player.y);     
                    state = GameObjectState.walking;
                }           
            }
        }

        if(velX!=0){            
            handler.removeFromHashMap(this);      
            x += velX;
            if(checkAnyCollision()){
                x-=velX;
                //velX=0;                
                if(velX>0)
                    if(velX>1)
                        velX--;
                    else    
                        velX=0;
                if(velX<0)
                    if(velX<-1)
                        velX++;
                    else    
                        velX=0;                     
                        
            }
            handler.addToHashMap(this);
        } 

        if(velY!=0){
            handler.removeFromHashMap(this);      
            y += velY;                
            if(checkAnyCollision()){
                y-=velY;
                //velY=0;                
                if(velY>0)
                    if(velY>1)
                        velY--;
                    else    
                        velY=0;
                if(velY<0)
                    if(velY<-1)
                        velY++;
                    else    
                        velY=0;
            }
            handler.addToHashMap(this);
        }       

        if(!seePlayer){
            if(path != null  && !path.isEmpty() && isNear(path.get(0).x,path.get(0).y) ){
                path.remove(0);
            }            
            if(path != null && !path.isEmpty()){   
                degreesTarget = getDegrees(this.x, this.y, path.get(0).x,path.get(0).y);
                this.velX = Math.cos(degreesTarget*degreesToRadians)*this.speed; 
                this.velY = Math.sin(degreesTarget*degreesToRadians)*this.speed;          
                if(pastX == x &&  pastY == y){ //wiggle
                    Random r = new Random();
                    velX = (velX * (r.nextBoolean()? 1:-1))+ ((r.nextBoolean()? 1:-1)*r.nextInt(20));
                    velY = (velY * (r.nextBoolean()? 1:-1))+ ((r.nextBoolean()? 1:-1)*r.nextInt(20));
                }   
            }else{
                velX = 0;
                velY = 0;
            }       
        }else{
            if(path != null && !path.isEmpty())
                path.clear();
            degreesTarget = getDegrees(this.x, this.y, handler.player.x, handler.player.y);
            this.velX = Math.cos(degreesTarget*degreesToRadians)*this.speed; 
            this.velY = Math.sin(degreesTarget*degreesToRadians)*this.speed;                     
            
        }

        if(velX==0 && velY==0){
            state = GameObjectState.idle;
        }else{
            state = GameObjectState.walking;
        }

        pastX = x;
        pastY = y; 

    }

    public void render(Graphics g){
        

        if(handler.debugShowPathFinding && path != null  && !path.isEmpty() ){
            g.setColor(Color.GRAY);
            for(Point cell: path){
                g.fillRect(cell.x, cell.y, floor(sizeX), floor(sizeY));    
            }
            g.setColor(Color.YELLOW);
            g.fillRect(path.get(0).x, path.get(0).y, floor(sizeX), floor(sizeY));
        }

        if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        }else if (handler.debugNoTextureMode){
            g.setColor(Color.RED);
            //g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
            g.fillRect(floor(x), floor(y-sizeY), floor(sizeX), floor(sizeY*2));
        }

        if(seePlayer && handler.debugShowVisualContact){
            g.setColor(Color.BLACK);
            g.drawLine((int) (x+sizeX/2), (int) (y+sizeY/2), 
                       (int) (handler.player.x+handler.player.sizeX/2), 
                       (int) (handler.player.y+handler.player.sizeY/2));
        }

    }
 
}
