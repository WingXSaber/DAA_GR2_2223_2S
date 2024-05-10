import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
//import java.awt.Color;

public class AttackPlayerStrikeBolo extends ObjectAttack{

    GameObject object;
    double knockbackX, knockbackY;
    ArrayList <GameObject> pastCollidedEnemies = new ArrayList<>();

    public AttackPlayerStrikeBolo(GameObject object, double x, double y, double duration, double angle) {
        super(object.handler, x, y, 0, new ImageLoader().loadImage("res/coll_strikeBolo_96x96.png"), duration);        
        
        this.object = object;
        this.x -= collisionImage.getWidth() /2;
        this.y -= collisionImage.getHeight()/2;

        BufferedImage tempImage = new BufferedImage(collisionImage.getWidth(), collisionImage.getHeight(), collisionImage.getType());
        Graphics2D g =  tempImage.createGraphics();                   
        //g.setColor(Color.BLACK);
        //g.fillRect(0, 0, collisionImage.getWidth(), collisionImage.getHeight());
        g.rotate(degreesToRadians(angle), collisionImage.getWidth() /2, collisionImage.getHeight()/2);
        g.drawImage(collisionImage, null, 0, 0);
        collisionImage = tempImage;
        g.dispose();   

        double knockbackSpeed = 10;
        this.knockbackX = Math.cos(degreesToRadians(angle)) * knockbackSpeed;
        this.knockbackY = Math.sin(degreesToRadians(angle)) * knockbackSpeed;

        applyVelocity();
    }

    public void tick(){     
        timerDespawnCheck();
        
        
        //have a list of all colided, if already colided, no need to recollide
        ArrayList <GameObject> collidedEnemies = getCollidedEnemies();
        for(GameObject object: collidedEnemies){
            if(!pastCollidedEnemies.contains(object)){       
                //effects of attack here:      
                object.state = ObjectState.stunned;
                object.health -=10;
                
                //mark gameObject has hit
                pastCollidedEnemies.add(object);
            }
            //continous effects of attack here:
            object.velX = knockbackX;
            object.velY = knockbackY;
        }

        
        //handler.removeFromHashMap(this);              
        //y = object.y+object.sizeY/2 + offSetY - collisionImage.getWidth() /2;  
        //x = object.x+object.sizeX/2 + offSetX - collisionImage.getHeight()/2;
        //handler.addToHashMap(this);        
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
