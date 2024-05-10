import java.awt.Graphics;
import java.util.ArrayList;
//import java.awt.Color;

public class AttackPlayerBullet extends ObjectAttack{

    GameObject object;
    double knockbackX, knockbackY;
    ArrayList <GameObject> pastCollidedEnemies = new ArrayList<>();

    public AttackPlayerBullet(GameObject object, double x, double y, double duration, double angle) {
        super(object.handler, x, y, 5, new ImageLoader().loadImage("res/coll_circle_8x8.png"), duration);        
        
        this.object = object;
        this.x -= collisionImage.getWidth() /2;
        this.y -= collisionImage.getHeight()/2;
        this.velX = Math.cos(degreesToRadians(angle)) * speed;
        this.velY = Math.sin(degreesToRadians(angle)) * speed;

        double knockbackSpeed = 0;
        this.knockbackX = Math.cos(degreesToRadians(angle)) * knockbackSpeed;
        this.knockbackY = Math.sin(degreesToRadians(angle)) * knockbackSpeed;

        applyVelocity();
    }

    public void tick(){     
        //timerDespawnCheck();        
        
        
//have a list of all colided, if already colided, no need to recollide

        if(isAnyCollision()){
            ArrayList <GameObject> collidedEnemies = getCollidedEnemies();
            for(GameObject object: collidedEnemies){
                if(!pastCollidedEnemies.contains(object)){       
                    //effects of attack here:      
                    object.state = ObjectState.stunned;
                    object.health -=20;

                    //mark gameObject has hit
                    pastCollidedEnemies.add(object);
                }
                //continous effects of attack here:
                object.velX = knockbackX;
                object.velY = knockbackY;
               
            }
            handler.removeFromGame(this);
        }else{
        
             
        handler.removeFromHashMap(this);     
        x+= velX;
        y+= velY;      
           
            //y = object.y+object.sizeY/2 + offSetY - collisionImage.getWidth() /2;  
            //x = object.x+object.sizeX/2 + offSetX - collisionImage.getHeight()/2;
          
        
             handler.addToHashMap(this);    
        }    
    }

    public void render(Graphics g) {
        //if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        //}else if (handler.debugNoTextureMode){
        //    g.setColor(Color.GREEN);            
        //    g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
        //}
    }

    
}
