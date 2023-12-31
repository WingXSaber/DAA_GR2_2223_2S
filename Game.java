
import java.awt.Canvas;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Color;

//import java.util.ArrayList;
//import java.awt.Point;



public class Game extends Canvas implements Runnable{

    //Initialize Global Objects and Variables here
    Window gameWindow;
    Thread thread;
    boolean isRunning = false,
            isPaused = false;
    double GAME_HERTZ = 60,     //target logic rate
           TARGET_FPS = 60;     //target display FPS
    int framecount = 0, FPS = 0,    //used for display
        windowWidth = 0,    
        windowHeight = 0,
        gameUnit = 32;
    
    GameObjectHandler gameObjectHandler;
    ViewCamera camera;
     
    public Game(){//Constructor method
        gameWindow = new Window(1024, 768, "Game", this);
        gameObjectHandler = new GameObjectHandler(gameUnit, GAME_HERTZ);
        this.addKeyListener(new KeyInput(gameObjectHandler));
        

        ImageLoader loader = new ImageLoader();
        BufferedImage level = loader.loadImage("res/level0.png");
        
        spawnLevel(level);

        camera = new ViewCamera(0, 0, windowWidth, windowHeight);
        
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

        start();
    }
    
    public void spawnLevel(BufferedImage image){
        int width = image.getWidth(),
            height = image.getHeight();
        gameObjectHandler.gridObstacles = new boolean [width] [height];

        ImageLoader loader = new ImageLoader();
        BufferedImage collisionImage; 
        
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                //get pixel color value
                int pixel = image.getRGB(x,y),
                    red = (pixel >> 16) & 0xff,
                    green = (pixel >> 8) & 0xff,
                    blue = (pixel) & 0xff;
                if(red == 255){
                    collisionImage = loader.loadImage("res/coll_circle_32x32.png");
                    gameObjectHandler.addToGame(new ObjectWallTree(gameObjectHandler,
                                                             x*gameUnit, 
                                                             y*gameUnit, 
                                                             collisionImage.getWidth(),
                                                             collisionImage.getHeight(),                                                             
                                                             collisionImage));
                    gameObjectHandler.gridObstacles[x][y] = true;
                }else if(blue == 255){
                    collisionImage = loader.loadImage("res/coll_circle_32x32.png");
                    gameObjectHandler.player = new ObjectPlayer(gameObjectHandler,
                                              x*gameUnit,
                                              y*gameUnit, 
                                              collisionImage.getWidth(),
                                              collisionImage.getHeight(),
                                              collisionImage);
                    gameObjectHandler.addToGame(gameObjectHandler.player);
                }else if(green == 255){
                    collisionImage = loader.loadImage("res/coll_circle_32x32.png");
                    gameObjectHandler.addToGame(new ObjectEnemy(gameObjectHandler,
                                              x*gameUnit,
                                              y*gameUnit, 
                                              collisionImage.getWidth(),
                                              collisionImage.getHeight(),
                                              5,
                                              collisionImage));                          
                }
            
            }
        }

        for(GameObject object : gameObjectHandler.allObjectList)
            //update the walls and their imageCollision based on neighbor                
            object.updateCollisionImageWithNeighbors();        
    }



    public void tick(){
        //System.out.println("Frame:"+framecount);
        gameObjectHandler.tick();
        camera.tick(gameObjectHandler.player, windowWidth, windowHeight);
    }

    public void render(){
        BufferStrategy bs = this.getBufferStrategy(); //memory where graphics is placed into
        if(bs==null){
            this.createBufferStrategy(3); //use 2 or 3 only for double or triple buffering
            //these buffers are used to reduce screen artefacts.
            return;
        }  

        Graphics2D g = (Graphics2D) bs.getDrawGraphics(); //think of this as the pen
        //g.scale(.5, .5);

        g.translate(-camera.x,-camera.y);  //move view towards where camera is
        
        //whichever gets rendered last, goes to the top of the view. =========
        g.setColor(Color.DARK_GRAY);
        g.fillRect(camera.getIntX(), camera.getIntY(), windowWidth, windowHeight);     //background color    
        int count = gameObjectHandler.render(g, camera);    //draw objects
        if(gameObjectHandler.debugText){
            g.setColor(Color.white);
            g.drawString("FPS:"+FPS, camera.getIntX()+windowWidth-150, camera.getIntY()+20);
            g.drawString("Player X : "+gameObjectHandler.player.x, camera.getIntX()+windowWidth-150, camera.getIntY()+40);
            g.drawString("Player Y : "+gameObjectHandler.player.y, camera.getIntX()+windowWidth-150, camera.getIntY()+60);
            g.drawString("Object Count: "+gameObjectHandler.allObjectCount, camera.getIntX()+windowWidth-150, camera.getIntY()+80);
            g.drawString("Camera X: "+ camera.getIntX(), camera.getIntX()+windowWidth-150, camera.getIntY()+100);
            g.drawString("Camera Y: "+ camera.getIntY(), camera.getIntX()+windowWidth-150, camera.getIntY()+120);
            g.drawString("Rendered Count: "+ count, camera.getIntX()+windowWidth-150, camera.getIntY()+140); 
        }
        //=====================================================================

        g.dispose();  //to safely dispose for memory purposes, this is preferable than letting Java's garbage collector do it automatically.                
        bs.show();  //display the current frame we just drew.
        
        Toolkit.getDefaultToolkit().sync();  //this smooths out animations on some systems, particularly Linux+GNU systems
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
    
    public static void main(String args[]){ 
        new Game();
    }
}