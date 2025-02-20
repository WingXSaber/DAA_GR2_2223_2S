import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Game extends Canvas implements Runnable {
     //Initialize Global Objects and Variables here
   Window gameWindow;
   Thread thread;
   boolean isRunning = false,
           isPaused = false;
   double GAME_HERTZ = 60,     //target logic rate
          TARGET_FPS = 60;     //target display FPS
   int framecount = 0,  FPS = 0,   //used for display   
       windowWidth = 0,
       windowHeight = 0,
       mapSizeX = 0,
       mapSizeY = 0,
       gameUnit = 32;
   GameObjectHandler gameObjectHandler;
   ViewCamera camera;
   BufferedImage level = (new ImageLoader()).loadImage("res/level3.png");

   public Game() {
      System.setProperty("sun.java2d.opengl", "True");
      this.gameWindow = new Window(1024, 768, "Game", this);
      this.camera = new ViewCamera(0.0D, 0.0D, (double)this.windowWidth, (double)this.windowHeight);
      this.gameObjectHandler = new GameObjectHandler(this.gameUnit, this.GAME_HERTZ, this.camera);
      this.addKeyListener(new KeyInput(this.gameObjectHandler));
      this.addMouseListener(new MouseInputButton(this.gameObjectHandler));
      this.addMouseMotionListener(new MouseInputMotion(this.gameObjectHandler));
      this.spawnLevel(this.level);
       /*
        //Demonstration on how we create gridObstacles from map
        
        //for(int y=0; y<gameObjectHandler.gridObstacles[0].length; y++){
        //    for(int x=0; x<gameObjectHandler.gridObstacles.length; x++){
        //        if(gameObjectHandler.gridObstacles[x][y])
        //            System.out.print("W");
        //        else
        //            System.out.print(" ");
        //    }
        //    System.out.println();
        //}
        
        System.out.println();
        for(GameObject object : gameObjectHandler.allObjectList){
            if(object.id == GameObjectID.Enemy){
                System.out.println();
                
                String grid [][] = new String[gameObjectHandler.gridObstacles[0].length][gameObjectHandler.gridObstacles.length];
                for(int y=0; y<gameObjectHandler.gridObstacles[0].length; y++){
                    for(int x=0; x<gameObjectHandler.gridObstacles.length; x++){
                        if(gameObjectHandler.gridObstacles[x][y])
                            grid[x][y] = "W";
                        else
                            grid[x][y] = " ";   
                        if(x == gameObjectHandler.player.x/gameUnit && y == gameObjectHandler.player.y/gameUnit)
                            grid[x][y] = "@";   
                    }                
                }

                ArrayList <Point> path = object.generatePathTo(gameObjectHandler.player.x, gameObjectHandler.player.y);
                int a =0;
                for(Point point: path){
                    grid[point.x/gameObjectHandler.gameUnit][point.y/gameObjectHandler.gameUnit] = ""+a;                    
                    a++;
                    if(a>9)
                        a=0;
                }

                for(int y=0; y<grid[0].length; y++){
                    for(int x=0; x<grid.length; x++){
                        System.out.print(grid[x][y]);
                    }        
                    System.out.println();        
                }

            }
        }
        */

      this.start();
   }

   public void spawnLevel(BufferedImage image) {
      int width = image.getWidth();
      int height = image.getHeight();
      this.gameObjectHandler.gridObstacles = new boolean[width][height];

      for(int y = 0; y < height; ++y) {
         for(int x = 0; x < width; ++x) {
            int pixel = image.getRGB(x, y);
            int red = pixel >> 16 & 255;
            int green = pixel >> 8 & 255;
            int blue = pixel & 255;
            if (red == 255) {
               this.gameObjectHandler.addToGame(new ObjectWall(this.gameObjectHandler, (double)(x * this.gameUnit), (double)(y * this.gameUnit)));
               this.gameObjectHandler.gridObstacles[x][y] = true;
            } else if (blue == 255) {
               this.gameObjectHandler.player = new ObjectPlayer(this.gameObjectHandler, (double)(x * this.gameUnit), (double)(y * this.gameUnit));
               this.gameObjectHandler.addToGame(this.gameObjectHandler.player);
            } else if (green == 255) {
               Random r = new Random();
               int a = r.nextInt(10);
               if (a > 8) {
                  this.gameObjectHandler.addToGame(new EnemyKapre(this.gameObjectHandler, (double)(x * this.gameUnit), (double)(y * this.gameUnit)));
               } else {
                  this.gameObjectHandler.addToGame(new EnemyUndead(this.gameObjectHandler, (double)(x * this.gameUnit), (double)(y * this.gameUnit)));
               }
            }
         }
      }

      this.mapSizeX = width * this.gameUnit;
      this.mapSizeY = height * this.gameUnit;
      this.gameObjectHandler.tick();

      for(GameObject object : gameObjectHandler.allObjectList)
            //update the walls and their imageCollision based on neighbor                
            if (object.id == GameObjectID.Wall) {
               ObjectWall wallObject = (ObjectWall)object; //cast
               wallObject.updateCollisionImageWithNeighbors();
            }   
   }

   //restart game
   public void restart() {
      if (this.camera != null && this.level != null && this.gameObjectHandler != null) {
         isPaused = true;
         this.camera = new ViewCamera(0.0D, 0.0D, (double)this.windowWidth, (double)this.windowHeight);
         this.gameObjectHandler = new GameObjectHandler(this.gameUnit, this.GAME_HERTZ, this.camera);
         this.addKeyListener(new KeyInput(this.gameObjectHandler));
         this.addMouseListener(new MouseInputButton(this.gameObjectHandler));
         this.addMouseMotionListener(new MouseInputMotion(this.gameObjectHandler));
         this.spawnLevel(this.level);         
         isPaused = false;
      }
   }

   //update logic function
   public void tick() { 
      this.gameObjectHandler.tick();
      this.camera.tick(this.gameObjectHandler.player, this.windowWidth, this.windowHeight);
      if (this.gameObjectHandler.isRestart) {
         this.restart();
      }
   }

   //update grahpics function
   public void render() {
      BufferStrategy bs = this.getBufferStrategy(); //memory where graphics is placed into
      if(bs==null){
          this.createBufferStrategy(3); //use 2 or 3 only for double or triple buffering
          //these buffers are used to reduce screen artefacts.
          return;
      }  

      Graphics2D g = (Graphics2D)bs.getDrawGraphics(); //think of this as the pen
      //g.scale(.5, .5);
      g.translate(-this.camera.x, -this.camera.y); //move view towards where camera is
      
      //whichever gets rendered last, goes to the top of the view

      //No Texture mode background
      if (this.camera.getIntX() >= 0 && this.camera.getIntY() >= 0 && this.mapSizeX >= this.camera.getIntX() + this.windowWidth && this.mapSizeY >= this.camera.getIntY() + this.windowHeight) {
         if (this.gameObjectHandler.debugNoTextureMode) { 
            g.setColor(Color.LIGHT_GRAY);
         } else {
            g.setColor(new Color(106, 133, 72));
         }

         g.fillRect(this.camera.getIntX(), this.camera.getIntY(), this.windowWidth, this.windowHeight);
      } else {
         g.setColor(Color.black);
         g.fillRect(this.camera.getIntX(), this.camera.getIntY(), this.windowWidth, this.windowHeight);
         if (this.gameObjectHandler.debugNoTextureMode) {
            g.setColor(Color.LIGHT_GRAY);
         } else {
            g.setColor(new Color(106, 133, 72));
         }
         g.fillRect(this.camera.getIntX() < 0 ? 0 : this.camera.getIntX(), this.camera.getIntY() < 0 ? 0 : this.camera.getIntY(), this.camera.getIntX() + this.windowWidth > this.mapSizeX ? this.windowWidth - (this.camera.getIntX() + this.windowWidth - this.mapSizeX) : this.windowWidth, this.camera.getIntY() + this.windowHeight > this.mapSizeY ? this.windowHeight - (this.camera.getIntY() + this.windowHeight - this.mapSizeY) : this.windowHeight);
      }

      //Healt hbar
      int count = this.gameObjectHandler.render(g);
      int healthBarLength = this.gameUnit * 8;
      int healthBarHeight = this.gameUnit;
      g.setColor(Color.DARK_GRAY);
      g.fillRect(this.camera.getIntX() + 20, this.camera.getIntY() + 20, healthBarLength, healthBarHeight);
      if (this.gameObjectHandler.player.health >= 0) {
         g.setColor(Color.RED);
         g.fillRect(this.camera.getIntX() + 20 + 5, this.camera.getIntY() + 20 + 5, healthBarLength * this.gameObjectHandler.player.health / this.gameObjectHandler.player.healthMax - 10, healthBarHeight - 10);
         g.setColor(Color.WHITE);
         g.drawString("" + this.gameObjectHandler.player.health, this.camera.getIntX() + 20 + 7, this.camera.getIntY() + 20 + healthBarHeight - 10);
      }

      //Selected Weapon
      g.setColor(Color.WHITE);
      String weaponSelectedText = "";
      if (this.gameObjectHandler.player.equipped == ObjectPlayerWeaponID.Bolo) {
         weaponSelectedText = "[Q] Bolo";
      } else if (this.gameObjectHandler.player.equipped == ObjectPlayerWeaponID.Pistol) {
         weaponSelectedText = "[Q] Pistol";
      }
      g.drawString(weaponSelectedText, this.camera.getIntX() + 20, this.camera.getIntY() + 20 + healthBarHeight + 10 + 10);        


      //Debug Mode =====================================================================================================
      if (this.gameObjectHandler.debugText) {
         g.setColor(Color.white);
         int yLoc = 0;
         int fontSize = 20;
         g.drawString("FPS:" + this.FPS, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Player X : " + this.gameObjectHandler.player.x, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Player Y : " + this.gameObjectHandler.player.y, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Player velX : " + this.gameObjectHandler.player.velX, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Player velY : " + this.gameObjectHandler.player.velY, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Player Health : " + this.gameObjectHandler.player.health, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Player Max Health : " + this.gameObjectHandler.player.healthMax, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Camera X: " + this.camera.getIntX(), this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Camera Y: " + this.camera.getIntY(), this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Object Count: " + this.gameObjectHandler.allObjectCount, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Rendered Count: " + count, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Enemy Count: " + this.gameObjectHandler.enemyCount, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("Wall Count: " + this.gameObjectHandler.wallCount, this.camera.getIntX() + this.windowWidth - 150, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         yLoc += fontSize;
         g.drawString("[F2]NaiveUnsortedRender: " + this.gameObjectHandler.debugNaiveUnsortedRender, this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("[F3]CollisionBoxOnly: " + this.gameObjectHandler.debugCollisionBoxOnly, this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("[F4]NoTextureMode: " + this.gameObjectHandler.debugNoTextureMode, this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("[F5]ShowPathFinding: " + this.gameObjectHandler.debugShowPathFinding, this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("[F6]ShowVisualContact: " + this.gameObjectHandler.debugShowVisualContact, this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         yLoc += fontSize;
         g.drawString("[F7]kill all enemies ", this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);
         yLoc += fontSize;
         g.drawString("[F8]Restart Game", this.camera.getIntX() + this.windowWidth - 200, this.camera.getIntY() + yLoc);            
      }
      //Debug Mode =====================================================================================================

      //Victory/Game Over Screen ========================================================================================
      if (this.gameObjectHandler.player.health <= 0 || this.gameObjectHandler.enemyCount <= 0) {
         g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.5F));
         g.fillRect(this.camera.getIntX(), this.camera.getIntY() + this.windowHeight / 3, this.windowWidth, this.windowHeight / 3);
         int fontSize = this.windowWidth / 15;
         g.setFont(new Font(g.getFont().getFontName(), 1, fontSize));
         String text = "";
         if (this.gameObjectHandler.player.health <= 0) {
            text = "YOU DIED";
            g.setColor(Color.RED);
         } else if (this.gameObjectHandler.enemyCount <= 0) {
            text = "VICTORY ACHIEVED";
            g.setColor(Color.ORANGE);
         }
         Rectangle2D bounds = g.getFontMetrics().getStringBounds(text, g);
         int textWidth = (int)bounds.getWidth();
         int textHeight = (int)bounds.getHeight();
         g.drawString(text, this.camera.getIntX() + this.windowWidth / 2 - textWidth / 2, this.camera.getIntY() + this.windowHeight / 2 + textHeight / 4);
      }
      //Victory/Game Over Screen ========================================================================================
      g.dispose();
      bs.show();
      Toolkit.getDefaultToolkit().sync();
      
   }


   public void start(){
       isRunning = true;
       thread = new Thread(this);
       thread.start();
   }
 
 
   public void stop(){
       isRunning = false;
       try{
           thread.join();            
       }catch (InterruptedException e){
           System.out.println(e);
       }
   }

   public void run(){//run method due to Runnable, this will keep looping.
      this.requestFocus(); //Many components – even those primarily operated with the mouse, 
                           //such as buttons – can be operated with the keyboard. 
                           //For a key press to affect a component, the component must have the keyboard focus.

      //This game loop based on alleged Notch's Game Loop, modified by me

      //Calculate how many ns each frame should take for our target game hertz.
      final double TIME_BETWEEN_UPDATES = 1000000000 / GAME_HERTZ;

      //At the very most we will update the game this many times before a new render.
      //This is done so physics is more consistent at the cost of visuals
      //If worried about visual hitches more than perfect timing, set this to 1.
      final int MAX_UPDATES_BEFORE_RENDER = 2;

      //We will need the last update time.
      double lastUpdateTime = System.nanoTime();

      //Store the last time we rendered.
      double lastRenderTime = System.nanoTime();

      //If we are able to get as high as this FPS, don't render again.        
      final double TARGET_TIME_BETWEEN_RENDERS = 1000000000 / TARGET_FPS;

      //Simple way of finding FPS.
      int lastSecondTime = (int) (lastUpdateTime / 1000000000);

      while (isRunning)
      {
         double now = System.nanoTime();
         int updateCount = 0;
      
         if (!isPaused)
         {
             //Do as many game updates as we need to, potentially playing catchup.
            while( now - lastUpdateTime > TIME_BETWEEN_UPDATES && updateCount < MAX_UPDATES_BEFORE_RENDER )
            {
               tick();
               lastUpdateTime += TIME_BETWEEN_UPDATES;
               updateCount++;
            }
            //float interpolation = Math.min(1.0f, (float) ((now - lastUpdateTime) / TIME_BETWEEN_UPDATES) );
        
            render();

            framecount++;
            lastRenderTime = now;
        
            //Update the frames we got.
            int thisSecond = (int) (lastUpdateTime / 1000000000);
            if (thisSecond > lastSecondTime) {               
               //System.out.println("FPS : "+framecount+"/"+(int)TARGET_FPS);
               FPS = framecount;
               framecount = 0;               
               lastSecondTime = thisSecond;
            }
        
            //Yield until it has been at least the target time between renders. This saves the CPU from hogging.
            while ( now - lastRenderTime < TARGET_TIME_BETWEEN_RENDERS && now - lastUpdateTime < TIME_BETWEEN_UPDATES) {
              Thread.yield();            
              //This stops the app from consuming all the CPU. It makes this slightly less accurate, but is worth it.
              //We can remove this line and it will still work (better), the CPU just climbs on certain OSes.
              //FYI on some OS's this can cause pretty bad stuttering. 
              try {Thread.sleep(1);} catch(Exception e) {} 
          
              now = System.nanoTime();
            }
         }

         //Since window could be resized, we re-assign the values.
         //ideally we should just put this in a Eventlistener of the window, but this works.
         windowWidth = gameWindow.getCanvasWidth();
         windowHeight = gameWindow.getCanvasHeight();    
      }            
  }

   public static void main(String[] args) {
      new Game();
   }
}
