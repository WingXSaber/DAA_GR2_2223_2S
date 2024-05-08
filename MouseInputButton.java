import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseInputButton extends MouseAdapter{
    GameObjectHandler handler;
    public MouseInputButton(GameObjectHandler handler){
        this.handler = handler;
    }

    public void mousePressed(MouseEvent e){
        if(e.getButton() == 1) //1 = left_click
            handler.inputAttack1 = true;
    }
    
    public void mouseReleased(MouseEvent e){
        if(e.getButton() == 1) //1 = left_click
            handler.inputAttack1 = false;
    }

}
