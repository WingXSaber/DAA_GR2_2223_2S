public class ViewCamera {
    protected double zoom = 1,
                     x, y, sizeX, sizeY;

    public ViewCamera(double x, double y, double sizeX, double sizeY){
        this.x = x;
        this.y = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    public void tick(GameObject object, int gameWindowWidth, int gameWindowHeight){
        //object here is the player, which we lock on to.
        double objectSizeX = object.sizeX,
               objectSizeY = object.sizeY;                   
        
        x = ((object.x+objectSizeX/2) - gameWindowWidth / 2);
        y = ((object.y+objectSizeY/2) - gameWindowHeight / 2);   
        
        sizeX = gameWindowWidth;
        sizeY = gameWindowHeight;
    }
    public int getIntX(){
        return (int) Math.floor(x);
    }
    public int getIntY(){
        return (int) Math.floor(y);
    }
}
