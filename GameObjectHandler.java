import java.util.ArrayList;
import java.util.Comparator;
import java.awt.Graphics;

public class GameObjectHandler {

    ArrayList <GameObject> allObjectList = new ArrayList<>();    

    int gameUnit;
    public GameObjectHandler(int gameUnit){
        this.gameUnit = gameUnit;
    }

    public void tick(){
        for(int i=0; i<allObjectList.size(); i++){
            allObjectList.get(i).tick(allObjectList);
        }        
    }   

    public void render(Graphics g, ViewCamera camera){
        ArrayList <GameObject> renderList = new ArrayList<>();
        //this is naive data structure.
        //should apply hashing for faster render checks
        for(int i=0; i<allObjectList.size(); i++){ 
            double objX = allObjectList.get(i).x, 
                   objY = allObjectList.get(i).y, 
                   objSizeX = allObjectList.get(i).sizeX, 
                   objSizeY = allObjectList.get(i).sizeY;
            //check if object is within bounds of camera            
            if( (objX >= camera.x && objX <= camera.x +camera.sizeX) || (objX+objSizeX >= camera.x && objX+objSizeX <= camera.x +camera.sizeX)
                &&
                (objY >= camera.y && objY <= camera.y +camera.sizeY) || (objY+objSizeY >= camera.y && objY+objSizeY <= camera.y +camera.sizeY)
            )
                renderList.add(allObjectList.get(i));
        }

        //This is the Quicksort by Y, ascending
        renderList.sort(new Comparator<GameObject>() { 
            @Override
            public int compare(GameObject ob1, GameObject ob2){
                double answer = ob1.y - ob2.y;
                if(answer > 0) return 1;        //if greater
                else if (answer < 0) return -1; //if less
                else return 0;                  //if equal
            }
        });

        for(int i=0; i<renderList.size(); i++){
            renderList.get(i).render(g);
        }
    }

    public void add(GameObject gameObject){
        allObjectList.add(gameObject);
    }

    public void remove(GameObject gameObject){
        allObjectList.remove(gameObject);
    }    

}