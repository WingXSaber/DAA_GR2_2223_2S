
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter{
    Game game;    

    public KeyInput(Game game){
        this.game = game;
    }

    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP) 
            this.game.up = true;
        if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            this.game.down = true;        
        if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) 
            this.game.left = true;
        if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) 
            this.game.right = true;
    }

    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();        
        if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP) 
            this.game.up = false;
        if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            this.game.down = false;
        if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            this.game.left = false;
        if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            this.game.right = false;
    }

}
