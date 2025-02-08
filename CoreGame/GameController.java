package CoreGame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ClientSide.EndPanel;
import ClientSide.GameClient;

import javax.imageio.ImageIO;
import javax.swing.*;

//all the logic of the game is handled here
public class GameController implements ActionListener, KeyListener {
    private static Timer gameLoopTimer;
    private GameClient client;
    private JPanel container;
    private GamePanel gamePanel;


    private boolean gameWon = false;
    private int cameraY;


    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -15;

    private int y = 504;

    //used to display the info about the platform the player is on when applicable
    private Platform platformPlayerIsOn;
    // Game elements
    private ArrayList<Platform> platforms;
    private int animationCounter = 0; //player specific
    private final int NUM_MOVE_ANIMATIONS = 4;
    private long lastTimestamp;
    private long accumulatedTime;
    private long spikeAppearanceTime;
    private long staggerTime;

    //added from Markie's implementation

    private TileMap tileMap;
    private double scaleFactor = 1.5;
    private int panelWidth;
    private int panelHeight;

    private int mapHeight;

    //private ArrayList<Player> players;
    private Player myPlayer;
    private HashMap<Integer, Player> otherPlayers;

    //spikes container
    private ArrayList<Obstacle> spikes;

    //tower walls info
    private int leftTowerXPos;
    private int rightTowerXPos;
    private int towerWallStart;

    private ArrayList<Collectible> collectibles;

    private long currentTime;
    private long elapsedTime;

    private long readyTime = 0;
    private long goTime;
    private long goalAnimationTime;

    private boolean gameStarted = false;

    private Platform platfromPlayerIsOn;

    private Goal goal;

    private long freezeTime;
    private long boostTime;


    //--------constructor---------------------------------------------
    public GameController(JPanel container, GameClient client){
        System.out.println("Game Controller is constructed.");
        this.client = client;
        this.container = container;

        this.gameLoopTimer = new Timer(20,this);

        myPlayer = new Player(1234);
        myPlayer.setYPos(1824);

        myPlayer.setCharacterWidth((int)(myPlayer.getCharacterWidth() * scaleFactor));
        myPlayer.setCharacterHeight((int)(myPlayer.getCharacterHeight() * scaleFactor));

        this.platforms = new ArrayList<Platform>();
        this.spikes = new ArrayList<Obstacle>();
        this.collectibles = new ArrayList<Collectible>();
        this.otherPlayers = new HashMap();
    }

    //------game loop------------------------------------------------

    //------what should typically happen before a frame is drawn-----------------
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameWon) {
//            this.gamePanel.setGameWon(gameWon);
//            this.gamePanel.repaint();
            stopGameTimer();

            CardLayout cardLayout = (CardLayout)container.getLayout();
            EndPanel endPanel = (EndPanel)container.getComponent(7);
            endPanel.setScreen(myPlayer);
            cardLayout.show(container, "8");
        }
        else {

            currentTime = System.currentTimeMillis();
            elapsedTime = currentTime - lastTimestamp;
            accumulatedTime += elapsedTime;
            spikeAppearanceTime += elapsedTime;
            goalAnimationTime += elapsedTime;
            lastTimestamp = currentTime;
            goTime += elapsedTime;


            if (goTime >= 3000) {
                this.gamePanel.setGo(false);
            }

            if(myPlayer.isBoosted()){
                boostTime += elapsedTime;
                System.out.println("Boost time: " + boostTime);
                if(boostTime >= 5000){
                    boostTime = 0;
                    myPlayer.setBoosted(false);
                    myPlayer.setXSpeed(5);
                }
            }

            if(myPlayer.isFrozen()){
                freezeTime += elapsedTime;
                System.out.println("Freeze time: " + freezeTime);
                if(freezeTime >= 5000){
                    freezeTime = 0;
                    myPlayer.setFrozen(false);
                    myPlayer.setXSpeed(5);
                }
            }

            if (gameStarted) {
                updateCharacterAnimation();

        /*
        //handle appearance and disappearance of spikes
        if(spikeAppearanceTime >= Obstacle.timeBeforeDisappearing){
            Obstacle.draw = !Obstacle.draw;
            spikeAppearanceTime = 0;
        }

         */
                animateGoal();

                // Handle horizontal movement
                handleHorizontalMovement();

                //this part ensures that the player never moves out of bounds horizontally
                keepPlayerWithinBounds();

                //ensures that the players never go beyond the top of the map
                keepPlayersBelowTopOfMap();

                // Apply gravity for jumping/falling
                applyGravity();

                // Handle collisions
                handlePlayerCollision();

                // Check if player is in the air
                checkIfInAir();

                // makes player stay on top of the floor of the map
                keepPlayersGrounded();

                // Adjust camera position to follow player
                adjustCamera();


                updateGamePanelComponents();
            } else {
                if (this.gamePanel.getReady()) {
                    if (elapsedTime >= 1000000) {
                        readyTime = 0;
                        freezeTime = 0;
                        boostTime = 0;
                    } else {
                        readyTime += elapsedTime;
                    }

                }

                if (readyTime >= 3000) {
                    this.gamePanel.setReady(false);
                    this.gamePanel.setGo(true);
                    gameStarted = true;
                }



            }

            try {
                client.sendToServer(myPlayer.toString());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            //repaint after all the checks have been made
            gamePanel.repaint();
        }

    }


    //----------------what happens every time the player inputs command from the keyboard-----------
    @Override
    public void keyTyped(KeyEvent e) {
        //do nothing
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //System.out.println("In key pressed.");
        if(gameStarted && !myPlayer.isStaggered()) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                myPlayer.setMoving(true);
                myPlayer.setMovingLeft(true);
                myPlayer.setFacingLeft(true);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                myPlayer.setMoving(true);
                myPlayer.setMovingRight(true);
                myPlayer.setFacingLeft(false);
            } else if (e.getKeyCode() == KeyEvent.VK_SPACE && !myPlayer.isInTheAir()/*inAir*/) {
                myPlayer.setMoving(false);//?
                myPlayer.setYSpeed(JUMP_STRENGTH); // Should have the jump strength be a field in the player class itself.
                myPlayer.setInAir(true);
                myPlayer.setOnPlatform(false);
            }

            gamePanel.repaint();
        }//redraw the frame every
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            myPlayer.setMoving(false);
            myPlayer.setMovingLeft(false);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            myPlayer.setMoving(false);
            myPlayer.setMovingRight(false);
        }

        gamePanel.repaint();
    }



    //--------collision handling--------------------------------------------

    public void handlePlayerCollision(){

        //check collision with spikes and see if player is staggered
        handleCollisionWithSpikes();

        // Check collision with platforms
        handleCollisionWithPlatforms();

        //check collisions with collectibles
        handleCollisionWithCollectibles();


        handleCollisionWithOtherPlayers();

        //check collision with goal
        handleCollisionWithGoal();
    }

    public void handleCollisionWithPlatforms(){
        // Check collision with platforms for the client's player
        for (Platform platform : platforms) {
            if (myPlayer.getYPos() + myPlayer.getCharacterHeight() >= platform.getYPos() &&
                    myPlayer.getYPos() + myPlayer.getCharacterHeight() <= platform.getYPos() + myPlayer.getYSpeed() &&
                    myPlayer.getXPos() + myPlayer.getCharacterWidth() >= platform.getXPos() &&
                    myPlayer.getXPos()  <= platform.getXPos() + platform.getWidth()) {

                platformPlayerIsOn = platform;
                myPlayer.setYPos(platform.getYPos() - myPlayer.getCharacterHeight());

                myPlayer.setYSpeed(0);
                myPlayer.setInAir(false);
                myPlayer.setOnPlatform(true);
                break;
            }
            else {
                myPlayer.setOnPlatform(false);
            }

            for (Player otherPlayer: otherPlayers.values()) {
                if (otherPlayer.getYPos() + otherPlayer.getCharacterHeight() >= platform.getYPos() &&
                        otherPlayer.getYPos() + otherPlayer.getCharacterHeight() <= platform.getYPos() + otherPlayer.getYSpeed() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= platform.getXPos() &&
                        otherPlayer.getXPos()  <= platform.getXPos() + platform.getWidth()) {

                    otherPlayer.setYPos(platform.getYPos() - otherPlayer.getCharacterHeight());

                    otherPlayer.setYSpeed(0);
                    otherPlayer.setInAir(false);
                    otherPlayer.setOnPlatform(true);
                    break;
                }
                else {
                    myPlayer.setOnPlatform(false);
                }
            }

        }

    }

    public void handleCollisionWithSpikes(){
        //check collision with spikes and see if player is staggered
        if(Obstacle.draw && !myPlayer.isStaggered())
        {
            for (Obstacle spike : spikes)
            {
                if (myPlayer.getYPos() + myPlayer.getCharacterHeight() >= spike.getYPos() &&
                        myPlayer.getYPos() + myPlayer.getCharacterHeight() <= spike.getYPos() + spike.getHeight() &&
                        myPlayer.getXPos() + myPlayer.getCharacterWidth() >= spike.getXPos() &&
                        myPlayer.getXPos() <= spike.getXPos() + spike.getWidth() )
                {

                    myPlayer.setStaggered(true);
                    myPlayer.setYSpeed(JUMP_STRENGTH*3/5);
                    break;
                }
            }
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if(Obstacle.draw && !otherPlayer.isStaggered())
            {
                for (Obstacle spike : spikes)
                {
                    if (otherPlayer.getYPos() + otherPlayer.getCharacterHeight() >= spike.getYPos() &&
                            otherPlayer.getYPos() + otherPlayer.getCharacterHeight() <= spike.getYPos() + spike.getHeight() &&
                            otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= spike.getXPos() &&
                            otherPlayer.getXPos() <= spike.getXPos() + spike.getWidth() )
                    {

                        //System.out.println("other player staggered is true");
                        otherPlayer.setInAir(true);
                        otherPlayer.setStaggered(true);
                        otherPlayer.setYSpeed(JUMP_STRENGTH*3/5);
                        break;
                    }
                }
            }
        }

        //how the player is affected when they are staggered (in terms of their position)
        //(a player is also no longer able to move while they are staggered, but that is on the KeyListener)
        if(myPlayer.isStaggered())
        {
            staggerTime += elapsedTime;
            if(myPlayer.isInTheAir())
            {
                if (myPlayer.isFacingLeft())
                {
                    myPlayer.setXPos(myPlayer.getXPos() + myPlayer.getXSpeed());
                }
                else
                {
                    myPlayer.setXPos((myPlayer.getXPos()) - myPlayer.getXSpeed());
                }
            }
        }

        //player stays staggered for a specific amount of time. Here it's supposed to be 2 seconds
        if(staggerTime >= 2000){
            myPlayer.setStaggered(false);
            staggerTime = 0;
        }

        for (Player otherPlayer : otherPlayers.values()){
            if(otherPlayer.isStaggered())
            {
                //System.out.println("other player is staggered and should be moving");
                staggerTime += elapsedTime;
                if(otherPlayer.isInTheAir())
                {
                    if (otherPlayer.isFacingLeft())
                    {
                        //System.out.println("moving");
                        otherPlayer.setXPos(otherPlayer.getXPos() + otherPlayer.getXSpeed());
                    }
                    else
                    {
                        //System.out.println("moving");
                        otherPlayer.setXPos((otherPlayer.getXPos()) - otherPlayer.getXSpeed());
                    }
                }
            }

            //player stays staggered for a specific amount of time. Here it's supposed to be 2 seconds
            if(staggerTime >= 2000){
                otherPlayer.setStaggered(false);
                staggerTime = 0;
            }
        }
    }

    public void handleCollisionWithCollectibles(){
        Iterator<Collectible> iterator = collectibles.iterator();
        while (iterator.hasNext()) {
            Collectible collectible = iterator.next();
            if (myPlayer.getYPos() + (myPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                    myPlayer.getYPos() + (myPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                    myPlayer.getXPos() + myPlayer.getCharacterWidth() >= collectible.getXPos() &&
                    myPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth()) {

                if(collectible instanceof BoostCollectible) {
                    collectible.applyEffects(myPlayer);
                    myPlayer.setBoosted(true);
                }
                //collectible.applyEffects(myPlayer);
                iterator.remove(); // Safe removal using the iterator
                break;
            }

            for (Player otherPlayer : otherPlayers.values()) {
                if (otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                        otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= collectible.getXPos() &&
                        otherPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth()) {

                    // collectible.applyEffects(otherPlayer);

                    if(collectible instanceof FreezeCollectible){
                        collectible.applyEffects(myPlayer);
                        myPlayer.setFrozen(true);
                    }
                    iterator.remove(); // Safe removal using the iterator
                    break;
                }
            }
        }
    }

    public void handleCollisionWithGoal(){
        if(new Rectangle(myPlayer.getXPos(), myPlayer.getYPos(), myPlayer.getCharacterWidth(), myPlayer.getCharacterHeight()).intersects(new Rectangle(goal.getXPos(), goal.getYPos(), goal.getWidth(), goal.getHeight()) )){
            gameWon = true;
            myPlayer.setGoalReached(true);
        }

        for(Player otherPlayer: otherPlayers.values()){
            if(new Rectangle(otherPlayer.getXPos(), otherPlayer.getYPos(), otherPlayer.getCharacterWidth(), otherPlayer.getCharacterHeight()).intersects(new Rectangle(goal.getXPos(), goal.getYPos(), goal.getWidth(), goal.getHeight()) )){
                gameWon = true;
                break;
            }
        }
    }

    public void handleCollisionWithOtherPlayers(){

    }

    //---------Boundary Checking----------------------------------------------------
    public void keepPlayerWithinBounds(){
        //this part ensures that the player never moves out of bounds horizontally
        myPlayer.setXPos(Math.max(0, Math.min(myPlayer.getXPos(), this.panelWidth - myPlayer.getCharacterWidth())));

        for (Player otherPlayer : otherPlayers.values()) {
            otherPlayer.setXPos(Math.max(0, Math.min(otherPlayer.getXPos(), this.panelWidth - otherPlayer.getCharacterWidth())));

        }

        //ensure that the player avatar is bounded by the castle walls
        if(myPlayer.getYPos() < towerWallStart && myPlayer.getXPos() > (leftTowerXPos-10) && myPlayer.getXPos() < rightTowerXPos - 10)
        {
            myPlayer.setXPos(Math.max(leftTowerXPos, Math.min(myPlayer.getXPos(), rightTowerXPos - myPlayer.getCharacterWidth())));
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if(otherPlayer.getYPos() < towerWallStart && otherPlayer.getXPos() > (leftTowerXPos-10) && otherPlayer.getXPos() < rightTowerXPos - 10)
            {
                otherPlayer.setXPos(Math.max(leftTowerXPos, Math.min(otherPlayer.getXPos(), rightTowerXPos - otherPlayer.getCharacterWidth())));
            }
        }
    }

    public void updateCharacterAnimation(){
        // Update character animation if moving
        if (myPlayer.isMoving())
        {
            try
            {
                myPlayer.updateCurrentPlayerSprite(ImageIO.read(getClass().getResource("/assets/bear_walk.png")));
                myPlayer.updateCurrentPlayerSprite(myPlayer.getCurrentPlayerSprite().getSubimage(32 * animationCounter, 0, 32, 32));
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }

            // Update animation every 70ms
            if (accumulatedTime >= 70)
            {
                animationCounter++;
                accumulatedTime = 0;
            }

            if (animationCounter >= NUM_MOVE_ANIMATIONS) animationCounter = 0;
        }
        else
        {
            // Reset to idle sprite when not moving
            try
            {
                myPlayer.updateCurrentPlayerSprite(ImageIO.read(getClass().getResource("/assets/bear_idle.png")));
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.isMoving()) {
                try {
                    otherPlayer.updateCurrentPlayerSprite(ImageIO.read(getClass().getResource("/assets/bear_walk.png")));
                    otherPlayer.updateCurrentPlayerSprite(otherPlayer.getCurrentPlayerSprite().getSubimage(32 * animationCounter, 0, 32, 32));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                // Update animation every 70ms
                if (accumulatedTime >= 70) {
                    animationCounter++;
                    accumulatedTime = 0;
                }
                if (animationCounter >= NUM_MOVE_ANIMATIONS) animationCounter = 0;
            } else {
                // Reset to idle sprite when not moving
                try {
                    otherPlayer.updateCurrentPlayerSprite(ImageIO.read(getClass().getResource("/assets/bear_idle.png")));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    // Adjust camera position to follow player
    public void adjustCamera(){
        if (myPlayer.getYPos()  < cameraY + panelHeight / 3)
        {
            cameraY = myPlayer.getYPos() - (panelHeight / 3) ;
        }
        else if (myPlayer.getYPos() > cameraY + panelHeight - myPlayer.getCharacterHeight())
        {
            cameraY = myPlayer.getYPos()  - panelHeight + myPlayer.getCharacterHeight() ;
        }

        //to ensure that the camera is always within the bounds of the map
        cameraY = Math.max(0, Math.min(cameraY, mapHeight - panelHeight));
    }

    public void keepPlayersGrounded(){
        if (myPlayer.getYPos() >= (tileMap.getMapHeight()-5)* tileMap.getTileHeight())
        {
            myPlayer.setYPos((tileMap.getMapHeight()-5)* tileMap.getTileHeight());
            myPlayer.setYSpeed(0);
            myPlayer.setInAir(false);
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.getYPos() >= (tileMap.getMapHeight()-5)* tileMap.getTileHeight())
            {
                otherPlayer.setYPos((tileMap.getMapHeight()-5)* tileMap.getTileHeight());
                otherPlayer.setYSpeed(0);
                otherPlayer.setInAir(false);
            }
        }
    }

    public void keepPlayersBelowTopOfMap(){
        if (myPlayer.getYPos() <= 0)
        {
            myPlayer.setYPos(0);
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.getYPos() <= 0)
            {
                otherPlayer.setYPos(0);
            }
        }
    }

    public void animateGoal(){
        // Update animation every 70ms
        if (goalAnimationTime >= 200)
        {
            goal.animate();
            goalAnimationTime = 0;
        }
    }

    public void handleHorizontalMovement(){
        // Handle horizontal movement
        if (myPlayer.isMovingLeft()) myPlayer.setXPos(myPlayer.getXPos() - myPlayer.getXSpeed());
        if (myPlayer.isMovingRight()) myPlayer.setXPos(myPlayer.getXPos() + myPlayer.getXSpeed());

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.isMovingLeft()) otherPlayer.setXPos(otherPlayer.getXPos() - otherPlayer.getXSpeed());
            if (otherPlayer.isMovingRight()) otherPlayer.setXPos(otherPlayer.getXPos() + otherPlayer.getXSpeed());
        }
    }

    public void applyGravity(){
        if (myPlayer.isInTheAir())
        {
            myPlayer.setYSpeed(myPlayer.getYSpeed() + GRAVITY);
            myPlayer.setYPos(myPlayer.getYPos() + myPlayer.getYSpeed());
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.isInTheAir()) {
                otherPlayer.setYSpeed(otherPlayer.getYSpeed() + GRAVITY); //gravity should be outside of the player's control I think. So maybe it's specific to the panel controller
                otherPlayer.setYPos(otherPlayer.getYPos() + otherPlayer.getYSpeed());
            }
        }
    }

    public void checkIfInAir(){
        // Check if player is in the air
        if (!myPlayer.isOnPlatform() && y < mapHeight - myPlayer.getCharacterHeight()) {
            myPlayer.setInAir(true);
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (!otherPlayer.isOnPlatform() && y < mapHeight - otherPlayer.getCharacterHeight()) {
                otherPlayer.setInAir(true);
            }
        }
    }

    //----------panel switching----------------------------------------------------
    public void switchToResultsPanel(){
        CardLayout cardLayout = (CardLayout)container.getLayout();
        cardLayout.show(container, "8");
    }


    public void updateGamePanelComponents(){
        gamePanel.setPlayer(this.myPlayer);
        gamePanel.setCollectibles(this.collectibles);
        gamePanel.setCameraY(this.cameraY);
        gamePanel.setPlatforms(this.platforms);
        gamePanel.setSpikes(this.spikes);
        gamePanel.setOtherPlayers(this.otherPlayers);
    }

    //this function is called from the client once the client has successfully opened its connection with the server.
    //this function should only be called once.
    public void setUp(GameMap gameMap){
        this.tileMap = gameMap.getTileMap();

        int tileWidth = (int)(tileMap.getTileWidth() * scaleFactor);

        int tileHeight = (int)(tileMap.getTileHeight() * scaleFactor);

        this.panelWidth = tileMap.getMapWidth() * tileWidth;

        this.mapHeight = tileMap.getMapHeight() * tileHeight;


        this.panelHeight = 26 * tileHeight;

        this.cameraY = mapHeight - panelHeight;

        this.tileMap.setTileWidth(tileWidth);
        this.tileMap.setTileHeight(tileHeight);

        this.leftTowerXPos = 5 * tileMap.getTileWidth() ;
        this.rightTowerXPos = (tileMap.getMapWidth() - 5) * tileMap.getTileWidth();
        this.towerWallStart = (tileMap.getMapHeight()-9)* tileMap.getTileHeight();

        this.gamePanel.setTileMap(tileMap);
        goal = new Goal( platforms.getLast().getXPos() + 15, platforms.getLast().getYPos() - 32);
        updateGamePanelComponents();
        this.gamePanel.setGoal(goal);
        this.gamePanel.setReady(true);
    }

    public void addPlatform(Platform platform){
        //System.out.println("Received new platform");
        platforms.add(platform);
    }

    public void addSpike(Obstacle spike){
        //System.out.println("Received new spike");
        spikes.add(spike);
    }

    public void addCollectible(Collectible collectible){
        //System.out.println("Received new collectible");
        collectibles.add(collectible);
    }

    public TileMap getTileMap(){
        return tileMap;
    }

    public int getPanelWidth(){
        return this.panelWidth;
    }

    public int getPanelHeight(){
        return this.panelHeight;
    }

    public void setGamePanel(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }

    public static void startGameTimer(){
        gameLoopTimer.start();
    }

    public static void stopGameTimer(){
        gameLoopTimer.stop();
    }

    public Player getPlayer(){
        return this.myPlayer;
    }


    public void addNewPlayer(Player newPlayer) {
        //System.out.println("In the addnewplayer method, new player added");

        otherPlayers.put(newPlayer.getId(), newPlayer);

    }

    public void removePlayer(){

        //System.out.println("Other player was removed");
        for(Player otherPlayer: otherPlayers.values()){
            otherPlayers.remove(otherPlayer.getId());
        }

        myPlayer.setGoalReached(true);
        stopGameTimer();
        CardLayout cardLayout = (CardLayout)container.getLayout();
        EndPanel endPanel = (EndPanel)container.getComponent(7);
        endPanel.setScreen(myPlayer);
        cardLayout.show(container, "8");
        
        //revert this state back to false so that the player may start a new game after logging out
        myPlayer.setGoalReached(false);

        this.gamePanel.setOtherPlayers(otherPlayers);

        //System.out.println("Other player object: " + otherPlayers);
    }

    public void updateOtherPlayer(Player newPlayer) {


        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.getId() == newPlayer.getId()) {


                otherPlayer.setMoving(newPlayer.isMoving());
                otherPlayer.setFacingLeft(newPlayer.isFacingLeft());
                otherPlayer.setMovingLeft(newPlayer.isMovingLeft());
                otherPlayer.setMovingRight(newPlayer.isMovingRight());
                otherPlayer.setYSpeed(newPlayer.getYSpeed());
                otherPlayer.setInAir(newPlayer.isInTheAir());
                otherPlayer.setOnPlatform(newPlayer.isOnPlatform());
                otherPlayer.setStaggered(newPlayer.isStaggered());

                otherPlayer.setXPos(newPlayer.getXPos());
                otherPlayer.setYPos(newPlayer.getYPos());


            }
        }
    }

    public HashMap<Integer, Player> getOtherPlayers(){
        return otherPlayers;
    }
}
