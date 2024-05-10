import java.awt.Graphics;
import java.awt.image.BufferedImage;

public abstract class ObjectAttack extends GameObject{
    double timerTick = 0, timerCheck = 0;

    public ObjectAttack(GameObjectHandler handler, double x, double y, double speed, BufferedImage collisionImage, double seconds) {
        //if seconds is zero, it assumes that timer will not be used.
        super(handler, x, y, speed, GameObjectID.Attack, collisionImage);
        this.timerCheck = seconds * handler.GAME_HERTZ;
    }

    public abstract void tick();
    
    public abstract void render(Graphics g);

    public void timerDespawnCheck(){
        if(timerCheck != 0){ //if seconds is not 0
            timerTick++;
            if(timerTick>=timerCheck){
                handler.removeFromGame(this);            
                return;
            }
        } 
    }

}
