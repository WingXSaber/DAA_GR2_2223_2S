
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.awt.Color;

public abstract class GameObject{
    protected double x, y, sizeX, sizeY, velX, velY, speed;
    protected GameObjectID id;
    protected BufferedImage collisionImage; 
    public GameObjectHandler handler;
    public GameObjectState state = GameObjectState.idle;

    double  radiansToDegrees = 180 / Math.PI,
            degreesToRadians = Math.PI / 180;

    public GameObject(GameObjectHandler handler, double x, double y, 
                      GameObjectID id, double sizeX, double sizeY, BufferedImage collisionImage){
        this.handler = handler;
        this.x = x;
        this.y = y;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.id = id;
        //initalize collisionImage 
        if(collisionImage == null && !id.equals(GameObjectID.OutWall)){ //if no buffered image and not an outwall
            this.collisionImage = new BufferedImage(floor(sizeX), floor(sizeY), BufferedImage.TYPE_BYTE_BINARY);
            Graphics g = this.collisionImage.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, floor(sizeX), floor(sizeY));
            g.dispose();        
        }else
            this.collisionImage = collisionImage;        
        //if object is an outWall, no collisionImage needed
    }

    public void updateCollisionImageWithNeighbors(){ 
        //used for ObjectWall
        //update the walls and their imageCollision based on neighbor
        //ideally set after all walls have been spawned.
        if(id == GameObjectID.Wall){
            boolean isUpLeftWall    = isWallAt(x-handler.gameUnit, y-handler.gameUnit),
                    isUpWall        = isWallAt(x                 , y-handler.gameUnit),
                    isUpRightWall   = isWallAt(x+handler.gameUnit, y-handler.gameUnit),
                    isLeftWall      = isWallAt(x-handler.gameUnit, y                 ),
                    isRightWall     = isWallAt(x+handler.gameUnit, y                 ),
                    isDownLeftWall  = isWallAt(x-handler.gameUnit, y+handler.gameUnit),
                    isDownWall      = isWallAt(x                 , y+handler.gameUnit),
                    isDownRightWall = isWallAt(x+handler.gameUnit, y+handler.gameUnit);

            Graphics g = this.collisionImage.getGraphics();
            g.setColor(Color.WHITE);
            if(isUpLeftWall)
                g.fillRect(0, 0, floor(sizeX/2), floor(sizeY/2));
            if(isUpWall)
                g.fillRect(0, 0, floor(sizeX), floor(sizeY/2));
            if(isUpRightWall)
                g.fillRect(floor(sizeX/2), 0, floor(sizeX/2), floor(sizeY/2)); 
            if(isLeftWall)
                g.fillRect(0, 0, floor(sizeX/2), floor(sizeY));
            if(isRightWall)
                g.fillRect(floor(sizeX/2), 0, floor(sizeX/2), floor(sizeY));          
            if(isDownLeftWall)
                g.fillRect(0, floor(sizeY/2), floor(sizeX/2), floor(sizeY/2));
            if(isDownWall)
                g.fillRect(0, floor(sizeY/2), floor(sizeX), floor(sizeY/2));
            if(isDownRightWall)
                g.fillRect(floor(sizeX/2), floor(sizeY/2), floor(sizeX/2), floor(sizeY/2));

            g.dispose();               
        }
    }

    public boolean isWallAt(double i, double j){       
        ArrayList <GameObject> objectList = handler.objectHashMap.get(handler.keyFromCoordinate(i,j));
        if(objectList != null)
            for(GameObject object: objectList)
                if(object.id == GameObjectID.Wall)
                    return true;
        return false;        
    }

    public abstract void tick();
    public abstract void render(Graphics g);
    public Rectangle getBounds(){
        return new Rectangle(floor(x), floor(y), floor(sizeX), floor(sizeY));
    }  
    
    public int floor (double value){
        return (int) Math.floor(value);
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

    public boolean checkAnyCollision(){          
        //checks for any collision with any objects
        for(int i = floor(x/handler.gameUnit)-1; i<floor((x+sizeX)/handler.gameUnit)+1; i++)
            for(int j = floor(y/handler.gameUnit)-1; j<floor((y+sizeY)/handler.gameUnit)+1; j++){
                ArrayList <GameObject> objectList = handler.objectHashMap.get(new Point(i,j));
                if(objectList != null)                    
                    for(GameObject object: objectList){                                           
                        if(!object.equals(this) && isCollidingWith(object)){
                            return true;
                        }
                    }
            }
        
        //if no collision
        return false;
    }
    
    public boolean isCollidingWith(GameObject object){
        //check boundaries between this and other object first
        //if there is collision within bounding boxes/rectangles
        Rectangle obj1 = getBounds(),
                  obj2 = object.getBounds();
        if(obj1.intersects(obj2)){
            Rectangle bounds =  getIntersectionBounds(obj1, obj2);
            if(!bounds.isEmpty()){
                // Check all the pixels in the collisionBounds to determine
                // if there are any non-alpha pixel collisions.
                for(int x = bounds.x; x < bounds.x+bounds.getWidth(); x++){
                    for(int y = bounds.y; y < bounds.y+bounds.getHeight(); y++){
                        //check if that specific pixel has both white pixel
                        //getRGB returns hex value
                        //0xFF000000 is white                        
                        int obj1pixel = collisionImage.getRGB(x - obj1.x , y - obj1.y),
                            obj2pixel = object.collisionImage.getRGB(x - obj2.x , y - obj2.y),
                            red1 = (obj1pixel >> 16) & 0xff,
                            green1 = (obj1pixel >> 8) & 0xff,
                            blue1 = (obj1pixel) & 0xff,
                            red2 = (obj2pixel >> 16) & 0xff,
                            green2 = (obj2pixel >> 8) & 0xff,
                            blue2 = (obj2pixel) & 0xff;

                        //this is not smart and I dislike this.
                        if(red1 == 255 && green1 == 255 && blue1 == 255 &&
                           red2 == 255 && green2 == 255 && blue2 == 255)
                        //if(obj1pixel == obj2pixel) //if both are white
                            return true;    //if yes collision
                    }    
                }
            }
        }
        //if no collision
        return false;
    }

    private Rectangle getIntersectionBounds(Rectangle r1, Rectangle r2){
        Area area1 = new Area(r1),
             area2 = new Area(r2);
        area1.intersect(area2);
        return area1.getBounds();
    }

    public double getDegrees(double x1, double y1, double x2, double y2){
        return Math.atan2(y2-y1,x2-x1) * radiansToDegrees;
    }



    //A* Pathfinding ================================================================================================
    public ArrayList <Point> generatePathTo(double tx, double ty){
        Point origin = handler.keyFromCoordinate(x, y),
              target = handler.keyFromCoordinate(tx, ty);
        
        final class NodePoint{  
            //this will serve as the single point with a value cost, stored in the minHeap

            NodePoint parentNode;   //used for backtracing the path when goal is found.
            public Point location;  //the coordinate of this node
            public double f = 0,
                          g = 0,   
                          h = 0;            
            
            public NodePoint(NodePoint parentNode, Point location, Point target){
                this.parentNode = parentNode;
                this.location = location;
                
                if(parentNode != null) //if this node is not an origin.            
                    this.g = parentNode.g + manhattanDistance(parentNode.location, location);
                //g is the distance "cost" from origin to this current node.
                //get the parent's distance cost, then add the distance cost of this node
                // from the parent via manhattan distance
                //also known as Dijkstra's Algorithm
                
                h = 2 * manhattanDistance(location, target);     
                //h is the "heuristic", the distance from this current node to the 
                // target via manhattan distance
                //also known as  Greedy Search                
                //With a weight, multiply the effect of the Greedy Search to take more risks. It just works.
                //This multiplication is called Weighted A*

                f = g + h;
                //f is the total
                //The A* Algorithm is a combination  Dijkstra's Algorithm & Greedy Search.
            }

            private double manhattanDistance(Point point1, Point point2){
                return Math.abs(point1.x - point2.x) + Math.abs(point1.y - point2.y);
            }    

            //we override the equals because we just need to use the Node's location as comparison
            //regular equals would compare everything, not just the location.
            @Override
            public boolean equals(Object obj){  //we override the equals() for contains()
                if(obj instanceof NodePoint){  //if obj is an NodePoint instance
                    NodePoint node = (NodePoint) obj;
                    return this.location.equals(node.location);
                }                    
                return false;                
            }
               
            /*
              "You must override hashCode() in every class that overrides equals().
              Failure to do so will result in a violation of the general contract 
              for Object.hashCode(), which will prevent your class from functioning 
              properly in conjunction with all hash-based collections, including 
              HashMap, HashSet, and Hashtable."

                -from Effective Java, by Joshua Bloch
            */
            @Override
            public int hashCode() {
                return this.location.hashCode();
            }
            
        }   
        
        PriorityQueue<NodePoint> openHeap = new PriorityQueue<NodePoint>(new Comparator<NodePoint>() {
            @Override
            public int compare(NodePoint ent1, NodePoint ent2) { //used when calling sort(). by smallest at the root
                double answer = ent1.f - ent2.f;
                if(answer > 0) return 1;        //if greater
                else if (answer < 0) return  -1; //if less
                else return 0;                  //if equal
            }
        });        
        openHeap.add(new NodePoint(null, origin, target));        
        //OpenHeap that contains the nodes/points to be evaluated.
        //At the start, has the origin point.
        //in common A* implementations, they use openlist. This is the equivalent
        //we use a minHeap to priortize getting the lowest f-cost.

        HashMap <Point, NodePoint> openListRef = new HashMap<>();
        openListRef.put(origin, new NodePoint(null, origin, target));
        //A second "OpenList" that uses a HashMap for O(1) speed.
        //Because we also need to check the openList, but
        //minHeaps don't have O(1) element checking.
        //Everytime we add/remove elements in openHeap, we update this too.

        HashMap <Point, NodePoint> closedList = new HashMap<>();
        //The Closedlist that uses a HashMap for O(1) speed.
        //Everytime a node is inspected in the OpenHeap/OpenList
        //We add them here to mark them as "searched"

        //System.out.println(origin+" "+target);      
        
        //Main A* Loop ----------------------------------------------------------------------------------------------
        while(!openHeap.isEmpty() ){            

            NodePoint currentNode = openHeap.poll();  //Pop and return the head, maintaining the heap.  
            
            if(openListRef.containsKey(currentNode.location))
                if(openListRef.get(currentNode.location).f < currentNode.f ){
                    currentNode = openListRef.get(currentNode.location);
                }
            openListRef.remove(currentNode.location);
            
            closedList.put(currentNode.location, currentNode); //Copy the currentNode to the closedList since it has been checked                                

            if(currentNode.location.equals(target) || origin == target){ //if target is found or target and origin are the same -------
                //System.out.println("size "+openHeap.size());
                ArrayList <Point> path = new ArrayList<>();                
                
                NodePoint node = currentNode.parentNode;
                
                while(node.parentNode != null){
                    //mulitply each coordinate by gameUnit
                    Point realLocation = new Point((node.location.x * handler.gameUnit),
                                                   (node.location.y * handler.gameUnit));
                    //path.add(node.location);
                    path.add(realLocation);
                    node = node.parentNode;
                }
                Collections.reverse(path);                
                
                //System.out.println("Path found!");
                return path;
                
            }else{  //Generate children successors of current node in eight directions -----------------------------
              /* Children Nodes in 8 directions
                ---------------------------------
                | x-1, y-1 |  x, y-1 | x+1, y-1 |
                ---------------------------------
                | x-1, y   |  x, y   | x+1, y   |
                ---------------------------------                
                | x-1, y+1 |  x, y+1 | x+1, y+1 |
                --------------------------------- */
                /*
                //Not considering if gameObject is larger than one tile
                for(int i=-1; i<2; i++)
                    for(int j=-1; j<2; j++){
                        //for k
                        //for l
                        Point newLocation = new Point(currentNode.location.x+i, currentNode.location.y+j);
                        if( !closedList.containsKey(newLocation)
                            &&
                            (newLocation.x >-1 && newLocation.x < handler.gridObstacles.length)
                            &&
                            (newLocation.y >-1 && newLocation.y < handler.gridObstacles[0].length)){

                            if(!handler.gridObstacles[newLocation.x][newLocation.y]){ //if no obstacle in that location
                                if( (Math.abs(i) == Math.abs(j)) && Math.abs(i)>0 && Math.abs(j)!=0 ){   //if node is a corner
                                    if(!handler.gridObstacles[currentNode.location.x][newLocation.y]
                                        &&
                                        !handler.gridObstacles[newLocation.x][currentNode.location.y]) //check adjacent of corner

                                       openHeap.add(new NodePoint(currentNode, newLocation, target));
                                }else
                                    openHeap.add(new NodePoint(currentNode, newLocation, target));
                            }
                        }
                    } 

                */
                //Considering if gameObject is larger than one tile
                for(int i=-1; i<2; i++)
                    for(int j=-1; j<2; j++){
                        boolean hasNoObstacle = true;
                        Point newLocation = new Point(currentNode.location.x+i, currentNode.location.y+j);
                        
                        if(!closedList.containsKey(newLocation) &&
                           !openListRef.containsKey(newLocation) &&
                            (newLocation.x >-1 && newLocation.x < handler.gridObstacles.length) &&
                            (newLocation.y >-1 && newLocation.y < handler.gridObstacles[0].length))
                        { //if node is not in closed list, open list and within grid                            
                            subLoops:
                            for(int k = 0; k<(int) Math.floor(sizeX/handler.gameUnit); k++)
                                for(int l = 0; l<(int) Math.floor(sizeY/handler.gameUnit); l++)                             
                                    if( (newLocation.x+k >-1 && newLocation.x+k < handler.gridObstacles.length) &&
                                        (newLocation.y+l >-1 && newLocation.y+l < handler.gridObstacles[0].length))
                                    { //if node is not in closed list and within grid     
                                        if(handler.gridObstacles[newLocation.x+k][newLocation.y+l]){ //if there is obstacle in that location
                                            hasNoObstacle = false;
                                            break subLoops;
                                        }
                                    }  
                                    
                            if( (Math.abs(i) == Math.abs(j)) && Math.abs(i)>0 && Math.abs(j)>0 && hasNoObstacle){   //if node is a corner                          
                                boolean adjacentX = false,
                                        adjacentY = false;    

                                subLoops://check horizontal adjacent node of corner
                                for(int k = 0; k<(int) Math.floor(sizeX/handler.gameUnit); k++)
                                    for(int l = 0; l<(int) Math.floor(sizeY/handler.gameUnit); l++)
                                        if(handler.gridObstacles[currentNode.location.x+k][newLocation.y+l] &&
                                        (currentNode.location.x+k >-1 && currentNode.location.x+k< handler.gridObstacles.length) &&
                                        (newLocation.y+l >-1 && newLocation.y+l < handler.gridObstacles[0].length) )
                                        {//if there is obstacle in that location  and within grid  
                                           adjacentX = true;
                                           Point adjacentLocation = new Point(currentNode.location.x+k , newLocation.y+l);
                                           closedList.put(adjacentLocation, new NodePoint(currentNode, adjacentLocation, target));
                                           break subLoops;
                                        } 

                                subLoops://check Vertical adjacent node of corner
                                for(int k = 0; k<(int) Math.floor(sizeX/handler.gameUnit); k++)
                                    for(int l = 0; l<(int) Math.floor(sizeY/handler.gameUnit); l++)
                                        if( handler.gridObstacles[newLocation.x+k][currentNode.location.y+l] &&
                                            (newLocation.x+k >-1 && newLocation.x+k < handler.gridObstacles.length) &&
                                            (currentNode.location.y+l >-1 && currentNode.location.y+l < handler.gridObstacles[0].length) )
                                        {//if there is obstacle in that location  and within grid  
                                           adjacentY = true;
                                           Point adjacentLocation = new Point(newLocation.x+k, currentNode.location.y+l);
                                           closedList.put(adjacentLocation, new NodePoint(currentNode, adjacentLocation, target));
                                           break subLoops;
                                        }       

                                if(adjacentX || adjacentY)
                                    hasNoObstacle = false;                                  

                            }

                            if(hasNoObstacle){
                                /*//If node exists in openList, replace it.
                                //Using this gives a more accurate result, 
                                //but at a high cost as it rechecks openlist again and again
                                //Decided to stop using this. "Fake it til you make it"
                                NodePoint newNode = new NodePoint(currentNode, newLocation, target);
                                if(openList.containsKey(newLocation)){
                                    if(newNode.f < openList.get(newLocation).f){
                                        openList.remove(newLocation);
                                        openList.put(newLocation, newNode);
                                    }
                                }
                                openHeap.add(newNode);   
                                */                       
                                NodePoint newNode = new NodePoint(currentNode, newLocation, target);
                                openHeap.add(newNode);         
                                openListRef.put(newLocation, newNode);
                            }
                        }                        
                    }                

            } 
        }//Main A* Loop --------------------------------------------------------------------

        //if openHeap is empty and hasn't returned any path, it means that path was not found.
        //System.out.println("Path not found");
        return null;
    }
    //A* Pathfinding ================================================================================================
    
    public boolean isNear(double tx, double ty){
        double deltaX = Math.abs(tx - this.x),
               deltaY = Math.abs(ty - this.y);
        //return deltaX <= handler.gameUnit/2 && deltaY <= handler.gameUnit/2;
        return deltaX <= handler.gameUnit/8 && deltaY <= handler.gameUnit/8;
    }

    public boolean checkVisualContact(GameObject object){ //uses DDA (Digital Differential Analyzer) Algorithm
        Point origin = handler.keyFromCoordinate(x+(sizeX/2), y+(sizeY/2)),
              target = handler.keyFromCoordinate(object.x+(object.sizeX/2), object.y+(object.sizeY/2));
        float run =  target.x - origin.x,
			  rise = target.y - origin.y,
	   		  steps = Math.abs(run) > Math.abs(rise) ? Math.abs(run) : Math.abs(rise),
    		  xStep = run / steps,
			  yStep = rise / steps,
			  x = origin.x,
			  y = origin.y;
              
		for(int i=0; i<=(float)steps; i++){
			if(handler.gridObstacles[(int)Math.floor(x)][(int)Math.floor(y)]) //if there is a wall
                return false;
			x+=xStep;
			y+=yStep;
		}
        return true;
    }
}
