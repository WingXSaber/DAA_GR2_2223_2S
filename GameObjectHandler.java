import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.awt.Graphics;
import java.awt.Point;

public class GameObjectHandler {

    ArrayList <GameObject> allObjectList = new ArrayList<>();

    //Abandoned HashTable in favor of HashMap. Better performance
    HashMap <Point,  ArrayList <GameObject>> objectHashMap = new HashMap <>();

    int gameUnit, allObjectCount = 0;

    public GameObjectHandler(int gameUnit){
        this.gameUnit = gameUnit;
    }

    public void tick(){
        //Collection<ArrayList<GameObject>> allObjectsCollection = objectHashTable.values();
        /*
        ArrayList<ArrayList<GameObject>> allObjectsCollection = new ArrayList<>(objectHashTable.values());
        for(ArrayList<GameObject> eachList : allObjectsCollection){
            for(GameObject object: eachList){
                object.tick();                
            }
        } */  
        /*           
        ArrayList<ArrayList<GameObject>> allObjectsCollection = new ArrayList<>(objectHashTable.values());
        for(ArrayList<GameObject> eachList : allObjectsCollection){
            for(Iterator<GameObject> entryIterator = eachList.iterator(); entryIterator.hasNext();){                
                entryIterator.next().tick();
            }
        }*/
        /*
        ArrayList<ArrayList<GameObject>> allObjectsCollection = new ArrayList<>(objectHashTable.values());
        for(ArrayList<GameObject> eachList : allObjectsCollection){
            Iterator <GameObject> itr = eachList.iterator();
            while(itr.hasNext()){
                GameObject object = itr.next();                
                object.tick();                
            }
        }
        */
        /*
        ArrayList<ArrayList<GameObject>> allObjectsList = new ArrayList<>(objectHashTable.values());
        Iterator <ArrayList<GameObject>> allObjectsListIterator = allObjectsList.iterator();
        while(allObjectsListIterator.hasNext()){            
            Iterator <GameObject> itr = allObjectsListIterator.next().iterator();
            while(itr.hasNext()){
                GameObject object = itr.next();       
                if(object.id != GameObjectID.Wall){
                    System.out.println(object.id);
                    remove(object);         
                }
                object.tick();              
                if(object.id != GameObjectID.Wall)
                    add(object);           
            }
        }
        */
        for(GameObject object : allObjectList){
            if(object.id != GameObjectID.Wall)
               removeFromHashTable(object);
            
            object.tick();             

            if(object.id != GameObjectID.Wall)
                addToHashTable(object);          
        }
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
            for(int j = floor(camera.y/gameUnit)-1; j<floor((camera.y+camera.sizeY)/gameUnit)+1; j++){               
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
        addToHashTable(gameObject);
        allObjectCount++;
    }

    public void removeFromGame(GameObject gameObject){
        if(allObjectList.contains(gameObject))
            allObjectList.remove(gameObject);
        removeFromHashTable(gameObject);
        allObjectCount--;
    }  
    
    //This is the Hashing and Collision Function(s) ============
    public Point keyFromCoordinate(double x, double y){   //double parameter
        int pX = (int) floor(x / this.gameUnit),
            pY = (int) floor(y / this.gameUnit);
        return new Point(pX,pY);
    }

    public Point keyFromCoordinate(int x, int y){         //int parameter
        int pX = (int) floor(x / this.gameUnit),
            pY = (int) floor(y / this.gameUnit);
        return new Point(pX,pY);
    }

    public void addToHashTable(GameObject gameObject){
        Point key =  keyFromCoordinate(gameObject.x ,gameObject.y);
        if(objectHashMap.containsKey(key)){   //if key already exists
            objectHashMap.get(key).add(gameObject);
        }else{                                 //if key doesn't exist, create a new one.
            ArrayList <GameObject> objectList = new ArrayList<>();
            objectList.add(gameObject);
            objectHashMap.put(key, objectList);
        }
    }

    public void removeFromHashTable(GameObject gameObject){
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
    //=========================================================


    public int floor (double value){
        return (int) Math.floor(value);
    }
}
