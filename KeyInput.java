
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyInput extends KeyAdapter{
    GameObjectHandler handler;    
    //Game game;

    //public KeyInput(GameObjectHandler handler, Game game){
    public KeyInput(GameObjectHandler handler){
        this.handler = handler;
        //this.game = game;
    }

    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        switch(key){
            case  KeyEvent.VK_W :
            case  KeyEvent.VK_UP :
                this.handler.inputUp = true;
                break;
            case KeyEvent.VK_S : 
            case KeyEvent.VK_DOWN :
                this.handler.inputDown = true;       
                break; 
            case KeyEvent.VK_A :
            case KeyEvent.VK_LEFT :
                this.handler.inputLeft = true;
                break;
            case KeyEvent.VK_D :
            case KeyEvent.VK_RIGHT : 
                this.handler.inputRight = true;       
                break;  

            case KeyEvent.VK_Q :          
                if(handler.player.equipped == ObjectPlayerWeaponID.Pistol)
                    handler.player.equipped = ObjectPlayerWeaponID.Bolo;
                else if(handler.player.equipped == ObjectPlayerWeaponID.Bolo)
                    handler.player.equipped = ObjectPlayerWeaponID.Pistol;
                break;
                
            case KeyEvent.VK_F1 : 
                handler.debugText = !handler.debugText;
                break;
            case KeyEvent.VK_F2 : 
                handler.debugNaiveUnsortedRender = !handler.debugNaiveUnsortedRender;
                break;
            case KeyEvent.VK_F3 : 
                handler.debugCollisionBoxOnly = !handler.debugCollisionBoxOnly;
                handler.debugNoTextureMode = false;
                break;
            case KeyEvent.VK_F4 : 
                handler.debugCollisionBoxOnly = false;
                handler.debugNoTextureMode = !handler.debugNoTextureMode;
                break;
            case KeyEvent.VK_F5 : 
                handler.debugShowPathFinding = !handler.debugShowPathFinding;
                break;
            case KeyEvent.VK_F6 : 
                handler.debugShowVisualContact = !handler.debugShowVisualContact;
                break;

            case KeyEvent.VK_F7 : 
                handler.killAllEnemies = !handler.killAllEnemies;
                break;       
            case KeyEvent.VK_F8 : 
                handler.isRestart = !handler.isRestart;
                break;   

                /*
            case  KeyEvent.VK_ESCAPE :
                game.isPaused = !game.isPaused;
                //hard pause
                break;

                
            case  KeyEvent.VK_Q :
                if(game.GAME_HERTZ - 10 > 0){
                    game.GAME_HERTZ -= 10;
                    System.out.println(game.GAME_HERTZ );
                }
                break;
            case  KeyEvent.VK_E :                
                game.GAME_HERTZ += 10;
                System.out.println(game.GAME_HERTZ );
                break;
                */
        }
        /*
        if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP) 
            this.handler.inputUp = true;
        if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            this.handler.inputDown = true;        
        if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) 
            this.handler.inputLeft = true;
        if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) 
            this.handler.inputRight = true;       
        */
        
    }

    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();        
        switch(key){
            case  KeyEvent.VK_W :
            case  KeyEvent.VK_UP :
                this.handler.inputUp = false;
                break;
            case KeyEvent.VK_S : 
            case KeyEvent.VK_DOWN :
                this.handler.inputDown = false;       
                break; 
            case KeyEvent.VK_A :
            case KeyEvent.VK_LEFT :
                this.handler.inputLeft = false;
                break;
            case KeyEvent.VK_D :
            case KeyEvent.VK_RIGHT : 
                this.handler.inputRight = false;       
                break;            
        }
        /*
        if(key == KeyEvent.VK_W || key == KeyEvent.VK_UP) 
            this.handler.inputUp = false;
        if(key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            this.handler.inputDown = false;
        if(key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            this.handler.inputLeft = false;
        if(key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            this.handler.inputRight = false;
        */
    }

}
