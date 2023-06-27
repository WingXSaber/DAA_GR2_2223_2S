import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.awt.Graphics;
import java.awt.Point;

public class GameObjectHandler {

    ArrayList <GameObject> allObjectList = new ArrayList<>();
    HashMap <Point,  ArrayList <GameObject>> objectHashMap = new HashMap <>();//Abandoned HashTable in favor of HashMap. Better performance
    ObjectPlayer player;

    int gameUnit, allObjectCount = 0, GAME_HERTZ;
    boolean gridObstacles [] [],
            up = false, down = false, left = false, right = false,
            debugText = true, debugCollisionBoxOnly = false, debugNoTextureMode = true,
            debugShowPathFinding = true, debugShowVisualContact = true;
            

    public GameObjectHandler(int gameUnit, double GAME_HERTZ){
        this.gameUnit = gameUnit;
        this.GAME_HERTZ = (int) GAME_HERTZ;
    }

    public void tick(){        
        for(GameObject object : allObjectList)
            object.tick();                     
    }   

    public int render(Graphics g, ViewCamera camera){
        /*
        //this the naive
        for(GameObject object : allObjectList){
            object.render(g);
        }
        */
        ArrayList <GameObject> renderList = new ArrayList<>();
        for(int i = floor(camera.x/gameUnit)-1; i<floor((camera.x+camera.sizeX)/gameUnit)+1; i++){            
            for(int j = floor(camera.y/gameUnit)-1; j<floor((camera.y+camera.sizeY)/gameUnit)+4; j++){               
                ArrayList <GameObject> objectList = objectHashMap.get(new Point(i, j));
                if(objectList != null){
                    for(GameObject object: objectList){
                        renderList.add(object);                           
                    }
                }
            }         
        }

        //This is the Quicksort by Y, ascending ====================
        renderList.sort(new Comparator<GameObject>() { 
            @Override
            public int compare(GameObject ob1, GameObject ob2){
                double answer = ob1.y - ob2.y;
                if(answer > 0) return 1;        //if greater
                else if (answer < 0) return -1; //if less
                else return 0;                  //if equal
            }
        });
        //=========================================================

        for(int i=0; i<renderList.size(); i++){
            renderList.get(i).render(g);
        }
        return renderList.size();
    }    
    
    public void addToGame(GameObject gameObject){
        if(!allObjectList.contains(gameObject))
            allObjectList.add(gameObject);
        addToHashMap(gameObject);
        allObjectCount++;
    }

    public void removeFromGame(GameObject gameObject){
        if(allObjectList.contains(gameObject))
            allObjectList.remove(gameObject);
        removeFromHashMap(gameObject);
        allObjectCount--;
    }  

    /*
        Here's the idea:
            the Hashmap will hold a list containing references to entities. Each keyword is a 
        tuple/an ordered pair representing i,j repectively, a 'cell' that will hold an area 
            A cell will cover an area with respect to its index/keyword. 
        IE. Diameter is 10
            [1,1] will cover i: 10-19 , j: 10-19 
            [2,3] will cover i: 20-29 , j: 30-39            

        Visual diagram:
        ---------------------------------
        | i-1, j-1 |  i, j-1 | i+1, j-1 |
        ---------------------------------
        | i-1, j   |  i, j   | i+1, j   |
        ---------------------------------                
        | i-1, j+1 |  i, j+1 | i+1, j+1 |
        ---------------------------------
    
            The collision will check it's neighboring 'cells' (+1/+0/-1 i, +1/+0/-1 j), 
        NW, N, NE, W, center, E, SW, S, and SE.        
            If Entity moves, and out of the cell, put it in the proper cell. If selected cell
		does not eiist, create it by calling a function here. This class will store the matrix.   
            i+1 is usually the sizeX.
            j+1 is usually the sizeY.
			
			However, there might be entities that are larger than one cell. That's why we 
		check the size and base the iteration there.        
		Visual diagram: (example, an entiiy who's 2x1 cells big)
        --------------------------------------------
        | i-1, j-1 |  i, j-1 |          | i+1, j-1 |
        --------------------------------------------
        | i-1, j   |  i, j   |          | i+1, j   |
        --------------------------------------------                
        | i-1, j+1 |  i, j+1 |          | i+1, j+1 |
        --------------------------------------------

        Visual diagram: (example, an entiiy who's 2x2 cells big)
        --------------------------------------------
        | i-1, j-1 |  i, j-1 |          | i+1, j-1 |
        --------------------------------------------
        | i-1, j   |  i, j   |          | i+1, j   |
        --------------------------------------------                
        |          |         |          |          |
        --------------------------------------------                
        | i-1, j+1 |  i, j+1 |          | i+1, j+1 |
        --------------------------------------------
    */    
    
    //This is the Hashing and Collision Method(s) and A* Methods ============
    public Point keyFromCoordinate(double x, double y){   //double parameter
        int pX = (int) floor(x / this.gameUnit),
            pY = (int) floor(y / this.gameUnit);
        return new Point(pX,pY);
    }

    public Point keyFromCoordinate(int x, int y){         //int parameter
        int pX = floor(x / this.gameUnit),
            pY = floor(y / this.gameUnit);
        return new Point(pX,pY);
    }

    public void addToHashMap(GameObject gameObject){
        Point key =  keyFromCoordinate(gameObject.x ,gameObject.y);
        if(objectHashMap.containsKey(key)){   //if key already exists
            objectHashMap.get(key).add(gameObject);
        }else{                                 //if key doesn't exist, create a new one.
            ArrayList <GameObject> objectList = new ArrayList<>();
            objectList.add(gameObject);
            objectHashMap.put(key, objectList);
        }
    }

    public void removeFromHashMap(GameObject gameObject){
        Point key =  keyFromCoordinate(gameObject.x ,gameObject.y);
        if(objectHashMap.containsKey(key)){ //if key already exists
            if(objectHashMap.get(key).contains(gameObject)){ //if object already exists in list of key
                objectHashMap.get(key).remove(gameObject);
                if(objectHashMap.get(key).isEmpty()){ //if the cell contains no more objects, delete.
                    objectHashMap.remove(key);
                }
            }
        }
    }   
    //======================================================================


    public int floor (double value){
        return (int) Math.floor(value);
    }
}
