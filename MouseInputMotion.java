import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MouseInputMotion extends MouseMotionAdapter {
    GameObjectHandler handler;
    public MouseInputMotion(GameObjectHandler handler){
        this.handler = handler;
    }

    public void mouseMoved(MouseEvent e){
        handler.mouseX = e.getX();
        handler.mouseY = e.getY();
    }
    
}
