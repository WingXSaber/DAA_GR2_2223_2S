
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter{
    GameObjectHandler handler;    

    public KeyInput(GameObjectHandler handler){
        this.handler = handler;
    }

    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP) 
            this.handler.up = true;
        if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            this.handler.down = true;        
        if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) 
            this.handler.left = true;
        if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) 
            this.handler.right = true;
    }

    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();        
        if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP) 
            this.handler.up = false;
        if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            this.handler.down = false;
        if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            this.handler.left = false;
        if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            this.handler.right = false;
    }

}
