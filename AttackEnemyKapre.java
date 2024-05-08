import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
//import java.awt.Color;

public class AttackEnemyKapre extends ObjectAttack{

    GameObject object;
    double knockbackX, knockbackY;
    ArrayList <GameObject> pastCollidedEnemies = new ArrayList<>();
    boolean hasCollidedWithPlayer = false; 

    public AttackEnemyKapre(GameObject object, double x, double y, double duration, double angle) {
        super(object.handler, x, y, 0, new ImageLoader().loadImage("res/coll_circle_72x72.png"), duration);        
        
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

        double knockbackSpeed = 12;
        this.knockbackX = Math.cos(degreesToRadians(angle)) * knockbackSpeed;
        this.knockbackY = Math.sin(degreesToRadians(angle)) * knockbackSpeed;

        applyVelocity();
    }

    public void tick(){     
        timerDespawnCheck();
        
        if(!hasCollidedWithPlayer){
            if(isCollidingWith(handler.player)){
                //effects of attack here:
                handler.player.health-=10;
                handler.player.velX = knockbackX;
                handler.player.velY = knockbackY;

                //mark gameObject has hit
                hasCollidedWithPlayer = true;
            }
            //continous effects of attack here:            
            
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
