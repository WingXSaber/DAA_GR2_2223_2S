import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class ObjectPlayer extends GameObject{      

    private double timerStrikeTick = 0,                    
                   timerStrikeBoloAttackStart = .15 * handler.GAME_HERTZ,
                   timerSTrikeBoloAttackRest = .1 * handler.GAME_HERTZ,
                   timerStrikeBoloTotalDuration = timerStrikeBoloAttackStart + .18 * handler.GAME_HERTZ + timerSTrikeBoloAttackRest,
                   angleTarget = 0;

    private boolean isAttack = false, isStrikingBolo = false, facingLeft = false;
    
    public ObjectPlayerWeaponID equipped = ObjectPlayerWeaponID.Pistol;
    
    protected BufferedImage characterSprite; 
    

    public ObjectPlayer(GameObjectHandler handler, double x, double y){
        super(handler, x, y, 5, GameObjectID.Player, new ImageLoader().loadImage("res/coll_circle_32x32.png"));                        
        this.healthMax = 100;
        this.health = this.healthMax;

        characterSprite = new ImageLoader().loadImage("res/Player Sprite.png");
    }

    public void tick(){    
        //  Movement ===============================================================
        if(!isAttack){
            //horizontal movement --------------------------------------------------
            if(handler.inputLeft && handler.inputRight || !handler.inputLeft && !handler.inputRight) {                   
                //reduce velocity / apply friction
                applyFrictionX();                    
            }else{               
                //add velocity.
                if(handler.inputLeft){
                    if(velX>-speed) //add velocity.
                        velX--; 
                    else            //if too much velocity
                        if(velX > -(speed+1) )
                            velX = -speed;                    
                        else
                            velX++;
                }else if(handler.inputRight){
                    if(velX<speed)
                        if(velX > (speed+1) )
                            velX = speed;
                        else
                            velX++;
                    else    
                        velX--;
                }
            }        
            
            //Vertical movement --------------------------------------------------
            if(handler.inputUp && handler.inputDown || !handler.inputUp && !handler.inputDown){
                //reduce velocity / apply friction
                applyFrictionY();
            }else{ 
                //add velocity.
                if(handler.inputUp){
                    if(velY>-speed) //add velocity.
                        velY--; 
                    else            //if too much velocity
                        if(velY > -(speed+1) )
                            velY = -speed;                    
                        else
                            velY++;
                }else if(handler.inputDown){
                    if(velY<speed)
                        if(velY > (speed+1) )
                            velY = speed;
                        else
                            velY++;
                    else    
                        velY--;
                }
            }
        }

        applyVelocity();        
        if(velX < 0)
            facingLeft = true;
        else if(velX > 0)
            facingLeft = false;        
        
        //  Attack ========================================================================
        if(!isAttack){
            if(handler.inputAttack1){
                isAttack = true;

                angleTarget = getDegrees(centerX, centerY, handler.mouseX+handler.camera.x, handler.mouseY+handler.camera.y);
                //movement due to attack
                //double xIntercept = Math.cos(degreesToRadians(angleTarget)),
                //       yIntercept = Math.sin(degreesToRadians(angleTarget)),
                //       strikeAttackMoveSpeed = 2;      
                //velX=xIntercept*strikeAttackMoveSpeed;
                //velY=yIntercept*strikeAttackMoveSpeed;
                velX=0;
                velY=0;

                //Since we click, we set to false. If we want continous, dont add this
                handler.inputAttack1 = false;
            }
        }
        if(isAttack){
            timerStrikeTick++;
            if(equipped == ObjectPlayerWeaponID.Bolo)
                if(!isStrikingBolo && timerStrikeTick >= timerStrikeBoloAttackStart){
                    double  seconds = ((timerStrikeBoloTotalDuration - timerStrikeBoloAttackStart) - timerSTrikeBoloAttackRest) / handler.GAME_HERTZ,                    
                            attackOffset = handler.gameUnit*1, //distance between object and attack.      
                            xIntercept = Math.cos(degreesToRadians(angleTarget)),
                            yIntercept = Math.sin(degreesToRadians(angleTarget));

                    handler.addToGame(new AttackPlayerStrikeBolo(this,
                                                        centerX+xIntercept*attackOffset, 
                                                        centerY+yIntercept*attackOffset,                                                    
                                                        seconds,
                                                        angleTarget));
                    angleTarget = 0;

                    isStrikingBolo = true;
                }
            if(timerStrikeTick >= timerStrikeBoloTotalDuration){                
                timerStrikeTick=0;
                isAttack = false;
                isStrikingBolo = false;
                x = floor(x);
                y = floor(y);
                velX = floor(velX);
                velY = floor(velY);
            }

            if(equipped == ObjectPlayerWeaponID.Pistol){
                double  seconds = 0,                    
                        attackOffset = handler.gameUnit*1, //distance between object and attack.      
                        xIntercept = Math.cos(degreesToRadians(angleTarget)),
                        yIntercept = Math.sin(degreesToRadians(angleTarget));
                handler.addToGame(new AttackPlayerBullet(this,
                                                         centerX+xIntercept*attackOffset, 
                                                         centerY+yIntercept*attackOffset,                                                    
                                                         seconds,
                                                         angleTarget));
                //angleTarget = 0;
                isAttack = false;
            }

            applyFriction();
        }
    }        


    public void render(Graphics g) {
        if(handler.debugCollisionBoxOnly){
            g.drawImage(collisionImage, floor(x), floor(y), null);
        }else if (handler.debugNoTextureMode){            
            g.setColor(Color.BLUE);
            //g.fillRect(floor(x), floor(y), floor(sizeX), floor(sizeY));
            g.fillRect(floor(x), floor(y-sizeY), floor(sizeX), floor(sizeY*2));            
        }else if(characterSprite != null){
            if(facingLeft)
                g.drawImage(characterSprite, floor(x + sizeX/2 - characterSprite.getWidth()/2), floor(y + sizeY -characterSprite.getHeight()), null);
            else
                g.drawImage(characterSprite, floor(x + sizeX/2 + characterSprite.getWidth()/2), floor(y + sizeY -characterSprite.getHeight()),
                            -characterSprite.getWidth(),characterSprite.getHeight(), null);
        }
            
    }

}
