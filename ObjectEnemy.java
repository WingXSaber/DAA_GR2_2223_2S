import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.PriorityQueue;

public abstract class ObjectEnemy extends GameObject{    
    private ArrayList<Point> path =  new ArrayList<>();
    private int timerPathTick = new Random().nextInt((int)handler.GAME_HERTZ),
                timerPathCheck = 2 * (int) handler.GAME_HERTZ,
                timerPathDelay = new Random().nextInt(7)+3,
                timerStunTick = 0,
                timerStunCheck = floor(.5 *  handler.GAME_HERTZ);
    private double pastX, pastY,
                   degreesTarget;
    boolean seePlayer = false, facingLeft = false;


    protected BufferedImage characterSprite; 

    public ObjectEnemy(GameObjectHandler gameObjectHandler, double x, double y, double speed, BufferedImage collisionImage){
        super(gameObjectHandler, x, y, speed, GameObjectID.Enemy, collisionImage);               
        this.pastX = x;
        this.pastY = y;     
    }


    public void tick(){          
        updateEnemyAction();
        applyVelocity();
        if(state != ObjectState.walking)
            applyFriction();
        if(velX < 0)
            facingLeft = true;
        else if(velX > 0)    
            facingLeft = false;
    }


  

    public void updateEnemyAction(){
        if(health <= 0){
            handler.removeFromGame(this);
            return;
        }

        attack();

        //generate path
        if(timerPathTick>=timerPathCheck){  //pathfinding timed check
            timerPathTick=0;
            //if( (pastX == x && pastY == y) || (path.isEmpty()) ){               
                ArrayList<Point> tempPath;
                tempPath = generatePathTo(handler.player.x+(handler.player.sizeX/2), handler.player.y+(handler.player.sizeY/2));     
                if(!tempPath.isEmpty()){
                    path = tempPath;
                    state = ObjectState.walking;
                }else if(!path.isEmpty())
                    path.clear();   //clear path
            //}           
        }

        switch(state){
            case attacking:
                timerPathTick = timerPathCheck - timerPathDelay;
                break;

            case walking:                
                seePlayer = checkVisualContact(handler.player);
               
                if(!seePlayer){ //not see player         
                    timerPathTick++;
                    if(!path.isEmpty()){      
                        if( pastX == x && pastY == y){ // wiggle function
                            Random r = new Random();
                            velX = (r.nextBoolean()? 1:-1) * r.nextInt((int)this.sizeX/8);
                            velY = (r.nextBoolean()? 1:-1) * r.nextInt((int)this.sizeY/8);
                        }else{               
                            degreesTarget = getDegrees(this.x, this.y, path.get(0).x,path.get(0).y); //target path
                            this.velX = Math.cos(degreesToRadians(degreesTarget))*this.speed; 
                            this.velY = Math.sin(degreesToRadians(degreesTarget))*this.speed;  
                            //if(isNearViaOrigin(path.get(0).x,path.get(0).y, handler.gameUnit/8)){  //is near the current point
                            if(isNearViaOrigin(path.get(0).x,path.get(0).y, (this.sizeX+this.sizeY)*.125)){  //is near the current point
                                path.remove(0);     //pop the point from path
                            } 
                        }
                    }else{              //not see player but there is no path.                        
                        velX = 0;
                        velY = 0;
                    }       
                }else{//see player
                    if(!path.isEmpty())
                        path.clear();   //clear path
                    if( pastX == x && pastY == y){ // wiggle function
                        Random r = new Random();
                        velX = (r.nextBoolean()? 1:-1) * r.nextInt((int)this.sizeX/8);
                        velY = (r.nextBoolean()? 1:-1) * r.nextInt((int)this.sizeY/8);
                    }else{    
                        degreesTarget = getDegrees(this.x, this.y, handler.player.x, handler.player.y); //target player
                        this.velX = Math.cos(degreesToRadians(degreesTarget))*this.speed; 
                        this.velY = Math.sin(degreesToRadians(degreesTarget))*this.speed;                    
                    }
                    //timerPathTick = timerPathCheck-10;
                    timerPathTick = timerPathCheck - timerPathDelay;
                }                                                
                //if(velX==0 && velY==0)   //update state       
                //    state = ObjectState.idle;
                break;

            case stunned:
                timerStunTick++;
                if(timerStunTick>=timerStunCheck){
                    timerStunTick=0;
                    state = ObjectState.idle;
                    timerPathTick = timerPathCheck;
                }                
                
                break;

            case idle:                                         
                timerPathTick++;       
                break;

            default:
               break;
        }

        //update past velocity
        pastX = x;
        pastY = y;
    }

    public abstract void attack();
   
    public void render(Graphics g){        

        if(handler.debugShowPathFinding && !path.isEmpty() ){
            //g.setColor(Color.YELLOW);
            g.setColor(new Color(1f, 1f, 0f, .5f)); //YELLOW
            for(Point cell: path){
                g.fillRect(cell.x, cell.y, floor(sizeX), floor(sizeY));    
                //g.setColor(Color.GRAY);
                g.setColor(new Color(0f, 0f, 0f, .1f));
            }
            //g.setColor(Color.orange);
            g.setColor(new Color(1f, .5f, 0f, .4f)); //ORANGE
            g.fillRect(path.get(path.size()-1).x, path.get(path.size()-1).y, floor(sizeX), floor(sizeY));
        }

        if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        }else if (handler.debugNoTextureMode){
            g.setColor(Color.RED);
            //g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
            g.fillRect(floor(x), floor(y-sizeY), floor(sizeX), floor(sizeY*2));
        }else if(characterSprite != null){
            if(facingLeft)
                g.drawImage(characterSprite, floor(x + sizeX/2 - characterSprite.getWidth()/2), floor(y + sizeY -characterSprite.getHeight()), null);
            else
                g.drawImage(characterSprite, floor(x + sizeX/2 + characterSprite.getWidth()/2), floor(y + sizeY -characterSprite.getHeight()),
                            -characterSprite.getWidth(),characterSprite.getHeight(), null);
        }
    
        if(seePlayer && handler.debugShowVisualContact){
            g.setColor(Color.green);
            //g.drawLine((int) (x+sizeX/2), (int) (y+sizeY/2), 
            //           (int) (handler.player.x+handler.player.sizeX/2), 
            //           (int) (handler.player.y+handler.player.sizeY/2));
            g.drawLine((int) (x), (int) (y), 
                       (int) (handler.player.x), (int) (handler.player.y));

            g.setColor(Color.blue);
            g.drawLine((int) (x+sizeX), (int) (y), 
                       (int) (handler.player.x+handler.player.sizeX-1), 
                       (int) (handler.player.y));
            g.setColor(Color.magenta);
            g.drawLine((int) (x), (int) (y+sizeY), 
                       (int) (handler.player.x), 
                       (int) (handler.player.y+handler.player.sizeY-1));
            g.setColor(Color.ORANGE);
            g.drawLine((int) (x+sizeX), (int) (y+sizeY), 
                       (int) (handler.player.x+handler.player.sizeX-1), 
                       (int) (handler.player.y+handler.player.sizeY-1));                       
                                
        }

    }



    //A* Pathfinding ================================================================================================

    //This could be improved by including the objects as obstacles, rather than walls only.
    public ArrayList <Point> generatePathTo(double tx, double ty){
        //returns null if path not found

        Point origin = handler.keyFromCoordinate(x, y),
              target = handler.keyFromCoordinate(tx, ty);
        
        final class NodePoint{  
            //this will serve as the single point with a value cost, stored in the minHeap

            NodePoint parentNode = null;   //used for backtracing the path when goal is found.
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
        while(!openHeap.isEmpty() && closedList.size() < 5000){            

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
                
                while(!Objects.isNull( node) && !Objects.isNull( node.parentNode)){
                    //mulitply each coordinate by gameUnit
                    Point realLocation = new Point((node.location.x * handler.gameUnit),
                                                   (node.location.y * handler.gameUnit));
                    //path.add(node.location);
                    path.add(realLocation);
                    node = node.parentNode;
                }                
                if(!path.isEmpty())
                    path.remove(path.size()-1);
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
                There is a topic called Jump Point Search. You could make some research about it. Start checking this and            &&
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
                try{
                    starLoops:
                    for(int i=-1; i<2; i++){
                        for(int j=-1; j<2; j++){
                            boolean hasNoObstacle = true;
                            Point newLocation = new Point(currentNode.location.x+i, currentNode.location.y+j);

                            if(!closedList.containsKey(newLocation) &&
                               !openListRef.containsKey(newLocation) &&
                                (newLocation.x >-1 && newLocation.x < handler.gridObstacles.length) &&
                                (newLocation.y >-1 && newLocation.y < handler.gridObstacles[0].length))
                            { //if node is not in closed list, open list and within grid                            
                                subLoops:
                                for(int k = 0; k<(int) Math.round(sizeX/handler.gameUnit); k++)
                                    for(int l = 0; l<(int) Math.round(sizeY/handler.gameUnit); l++)                             
                                        if( (newLocation.x+k >-1 && newLocation.x+k < handler.gridObstacles.length) &&
                                            (newLocation.y+l >-1 && newLocation.y+l < handler.gridObstacles[0].length)
                                        ){ //if node is within grid  
                                            if(new Point(newLocation.x+k,newLocation.y+l).equals(target)){ // if the node/set of grid is where target at, cancel generation
                                                NodePoint newNode = new NodePoint(currentNode, newLocation, target);
                                                openHeap.add(newNode);         
                                                openListRef.put(newLocation, newNode);
                                                break starLoops;
                                            }else if(handler.gridObstacles[newLocation.x+k][newLocation.y+l]
                                                     ||
                                                     isAnyEnemyCollidableAt((newLocation.x+k)*handler.gameUnit, (newLocation.y+l)*handler.gameUnit)
                                            ){ //if there is obstacle in that location
                                                hasNoObstacle = false;
                                                break subLoops;
                                            }
                                        }  

                                if( (Math.abs(i) == Math.abs(j)) && Math.abs(i)>0 && Math.abs(j)>0 && hasNoObstacle){   //if node is a corner                          
                                    boolean adjacentX = false,
                                            adjacentY = false;    

                                    subLoops://check horizontal adjacent node of corner
                                    for(int k = 0; k<(int) Math.round(sizeX/handler.gameUnit); k++)
                                        for(int l = 0; l<(int) Math.round(sizeY/handler.gameUnit)+1; l++)
                                            if( handler.gridObstacles[currentNode.location.x+k][newLocation.y+l] &&
                                                (currentNode.location.x+k >-1 && currentNode.location.x+k< handler.gridObstacles.length) &&
                                                (newLocation.y+l >-1 && newLocation.y+l < handler.gridObstacles[0].length) 
                                                ||
                                                isAnyEnemyCollidableAt((newLocation.x+k)*handler.gameUnit, (newLocation.y+l)*handler.gameUnit)
                                            ){//if there is obstacle in that location  and within grid  
                                               adjacentX = true;
                                               Point adjacentLocation = new Point(currentNode.location.x+k , newLocation.y+l);
                                               closedList.put(adjacentLocation, new NodePoint(currentNode, adjacentLocation, target));
                                               break subLoops;
                                            } 

                                    subLoops://check Vertical adjacent node of corner
                                    for(int k = 0; k<(int) Math.round(sizeX/handler.gameUnit); k++)
                                        for(int l = 0; l<(int) Math.round(sizeY/handler.gameUnit); l++)
                                            if( handler.gridObstacles[newLocation.x+k][currentNode.location.y+l] &&
                                                (newLocation.x+k >-1 && newLocation.x+k < handler.gridObstacles.length) &&
                                                (currentNode.location.y+l >-1 && currentNode.location.y+l < handler.gridObstacles[0].length)
                                                ||
                                                isAnyEnemyCollidableAt((newLocation.x+k)*handler.gameUnit, (newLocation.y+l)*handler.gameUnit)
                                            ){//if there is obstacle in that location  and within grid  
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
                }catch(ArrayIndexOutOfBoundsException ex){}
            } 
        }//Main A* Loop --------------------------------------------------------------------

        //if openHeap is empty and hasn't returned any path, it means that path was not found.
        //System.out.println("Path not found");
        return  new ArrayList<>();
    }
    //A* Pathfinding ================================================================================================
    
 
}
