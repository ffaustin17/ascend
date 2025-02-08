package CoreGame;

import ClientSide.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class TwoPlayerTesting extends JPanel implements ActionListener {

    // Constants defining game settings
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -15;
    private static final int GOAL_SIZE = 30;

    // Character properties
    private int characterWidth = 32;// player specific
    private int characterHeight = 32; //player specific
    private int x = 0;//FRAME_WIDTH / 2 - characterWidth / 2;  player specific
    private int y = 504;//MAP_HEIGHT - characterHeight; player specific

    private boolean gameWon = false;//gameController specific


    private int cameraY;

    // Game elements
    private ArrayList<Platform> platforms;

    private Rectangle goal;

    // Character and background images
    private BufferedImage currCharSprite; // player specific
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


    //spikes container
    private ArrayList<Obstacle> spikes;

    //tower walls info
    private int leftTowerXPos;
    private int rightTowerXPos;
    private int towerWallStart;

    //used to display the info about the platform the player is on when applicable
    private Platform platformPlayerIsOn;

    private ArrayList<Collectible> collectibles;

    HashMap<Integer, Player> otherPlayers;
    private GameClient client;

    public TwoPlayerTesting(GameMap tilemap, GameClient client) {

        //String x = "aaaaa";

        spikes = new ArrayList<Obstacle>();
        collectibles = new ArrayList<Collectible>();

        this.tileMap = tilemap.getTileMap();
        this.client = client;
        otherPlayers = new HashMap();

        //players.add(new Player())
        myPlayer = new Player(1234);
        myPlayer.setYPos(1824);

        myPlayer.setCharacterWidth((int)(myPlayer.getCharacterWidth() * scaleFactor));
        myPlayer.setCharacterHeight((int)(myPlayer.getCharacterHeight() * scaleFactor));
        characterWidth = (int)(characterWidth * scaleFactor);
        characterHeight = (int)(characterHeight * scaleFactor);

        int tileWidth = (int)(tileMap.getTileWidth() * scaleFactor);

        int tileHeight = (int)(tileMap.getTileHeight() * scaleFactor);

        this.panelWidth = tileMap.getMapWidth() * tileWidth;

        this.mapHeight = tileMap.getMapHeight() * tileHeight;


        panelHeight = 26 * tileHeight;

        this.cameraY = mapHeight - panelHeight;

        setPreferredSize(new Dimension(panelWidth, panelHeight));

        tileMap.setTileWidth(tileWidth);
        tileMap.setTileHeight(tileHeight);

        leftTowerXPos = 5 * tileMap.getTileWidth() ;
        rightTowerXPos = (tileMap.getMapWidth() - 5) * tileMap.getTileWidth();
        towerWallStart = (tileMap.getMapHeight()-9)* tileMap.getTileHeight();


        // Load character and background images
        try {
            currCharSprite = ImageIO.read(new File("./assets/bear_idle.png"));
            myPlayer.updateCurrentPlayerSprite(currCharSprite);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize platforms and goal
        platforms = new ArrayList<Platform>();
        //generatePlatformsAndTraps(900, 216, 1848, 80 , 66, 15, platforms );

//        goal = new Rectangle( platforms.getLast().getXPos() + 15, platforms.getLast().getYPos() - 30, GOAL_SIZE, GOAL_SIZE);

        // Start timer for game updates
        Timer timer = new Timer(20, this);
        timer.start();

        lastTimestamp = System.currentTimeMillis();
        accumulatedTime = 0;

        // Set up key listeners for character movement
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(!myPlayer.isStaggered()) {
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

                    try {
                        client.sendToServer(myPlayer.toString());
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    repaint();
                }//redraw the frame every time an input is made.
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

                try {
                    client.sendToServer(myPlayer.toString());
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                repaint();
            }
        });


        System.out.println("Panel width: " + getPanelWidth());
        System.out.println("Character height: " + getPlayer().getCharacterHeight());
        System.out.println("Scale Factor: " + getScaleFactor());
        System.out.println("Tile width: " + tileMap.getTileWidth());
        System.out.println("Map width: " + tileMap.getMapWidth());

        setFocusable(true);

    }

    @Override
    //a lot of stuff happening here lol. This should be implemented in GameController
    public void actionPerformed(ActionEvent e) {
        if (gameWon) return;

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastTimestamp;
        accumulatedTime += elapsedTime;
        spikeAppearanceTime += elapsedTime;



        lastTimestamp = currentTime;

        // Update character animation if moving
        if (myPlayer.isMoving())
        {
            try
            {
                //currCharSprite = ImageIO.read(getClass().getResource("/assets/bear_walk.png"));
                //myPlayer.updateCurrentPlayerSprite(currCharSprite.getSubimage(32 * animationCounter, 0, 32, 32));
                myPlayer.updateCurrentPlayerSprite(ImageIO.read(new File("./assets/bear_walk.png")));
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
                //currCharSprite = ImageIO.read(getClass().getResource("/assets/bear_idle.png"));
                myPlayer.updateCurrentPlayerSprite(ImageIO.read(new File("./assets/bear_idle.png")));
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.isMoving()/*isMoving*/) {
                try {
                    //currCharSprite = ImageIO.read(getClass().getResource("/bear_walk.png"));
                    //currCharSprite = currCharSprite.getSubimage(32 * animationCounter, 0, 32, 32);
                    otherPlayer.updateCurrentPlayerSprite(ImageIO.read(new File("./assets/bear_walk.png")));
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
                    //currCharSprite = ImageIO.read(getClass().getResource("/bear_idle.png"));
                    otherPlayer.updateCurrentPlayerSprite(ImageIO.read(new File("./assets/bear_idle.png")));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        /*
        //handle appearance and disappearance of spikes
        if(spikeAppearanceTime >= Obstacle.timeBeforeDisappearing){
            Obstacle.draw = !Obstacle.draw;
            spikeAppearanceTime = 0;
        }

         */



        // Handle horizontal movement
        if (myPlayer.isMovingLeft()) myPlayer.setXPos(myPlayer.getXPos() - myPlayer.getXSpeed());
        if (myPlayer.isMovingRight()) myPlayer.setXPos(myPlayer.getXPos() + myPlayer.getXSpeed());

        for (Player otherPlayer : otherPlayers.values()) {
            //System.out.println("Attempting to move other player:");
            //System.out.println("Other player is moving left: " + otherPlayer.isMovingLeft());
            //System.out.println("Other player is moving right: " + otherPlayer.isMovingRight());
            //System.out.println("Other player is moving at all: " + otherPlayer.isMoving());
            //System.out.println("Other player current positions: " + otherPlayer.getPos());

            if (otherPlayer.isMovingLeft()/*movingLeft*/) otherPlayer.setXPos(otherPlayer.getXPos() - otherPlayer.getXSpeed());//x -= MOVE_SPEED;
            if (otherPlayer.isMovingRight()/*movingRight*/) otherPlayer.setXPos(otherPlayer.getXPos() + otherPlayer.getXSpeed());//x += MOVE_SPEED;

            //System.out.println("Other player new positions after moving: " + otherPlayer.getPos());
        }


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

        // Apply gravity for jumping/falling
        if (myPlayer.isInTheAir())
        {
            myPlayer.setYSpeed(myPlayer.getYSpeed() + GRAVITY); //gravity should be outside of the player's control I think. So maybe it's specific to the panel controller
            myPlayer.setYPos(myPlayer.getYPos() + myPlayer.getYSpeed());
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.isInTheAir()) {
                otherPlayer.setYSpeed(otherPlayer.getYSpeed() + GRAVITY); //gravity should be outside of the player's control I think. So maybe it's specific to the panel controller
                otherPlayer.setYPos(otherPlayer.getYPos() + otherPlayer.getYSpeed());
            }

            //otherPlayer.setYSpeed(otherPlayer.getYSpeed() + GRAVITY); //gravity should be outside of the player's control I think. So maybe it's specific to the panel controller
            //otherPlayer.setYPos(otherPlayer.getYPos() + otherPlayer.getYSpeed());
        }

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
                staggerTime += elapsedTime;
                if(otherPlayer.isInTheAir())
                {
                    if (otherPlayer.isFacingLeft())
                    {
                        otherPlayer.setXPos(otherPlayer.getXPos() + otherPlayer.getXSpeed());
                    }
                    else
                    {
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

        // Check collision with platforms
        for (Platform platform : platforms) {
            if (myPlayer.getYPos() + myPlayer.getCharacterHeight() >= platform.getYPos() &&
                    myPlayer.getYPos() + myPlayer.getCharacterHeight() <= platform.getYPos() + myPlayer.getYSpeed() &&
                    myPlayer.getXPos() + myPlayer.getCharacterWidth() >= platform.getXPos() &&
                    myPlayer.getXPos()  <= platform.getXPos() + platform.getWidth()
            	/*y + characterHeight >= platform.getYPos() &&
                //y + characterHeight <= platform.getYPos() + yVelocity &&
                //x + characterWidth > platform.getXPos() &&
                //x < platform.getXPos() + platform.getWidth()*/) {

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

        }

        for (Platform platform : platforms) {
            for (Player otherPlayer : otherPlayers.values()) {
                if (otherPlayer.getYPos() + otherPlayer.getCharacterHeight() >= platform.getYPos() &&
                        otherPlayer.getYPos() + otherPlayer.getCharacterHeight() <= platform.getYPos() + otherPlayer.getYSpeed() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= platform.getXPos() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() <= platform.getXPos() + platform.getWidth()
                    	/*y + characterHeight >= platform.getYPos() &&
                        //y + characterHeight <= platform.getYPos() + yVelocity &&
                        //x + characterWidth > platform.getXPos() &&
                        //x < platform.getXPos() + platform.getWidth()*/) {

                    otherPlayer.setYPos(platform.getYPos() - otherPlayer.getCharacterHeight());
                    //y = platform.getYPos() - characterHeight;

                    otherPlayer.setYSpeed(0);
                    otherPlayer.setInAir(false);
                    otherPlayer.setOnPlatform(true);
                    break;
                    //yVelocity = 0;
                    //inAir = false;
                    //onPlatform = true;
                    //break;
                }
                else {
                    otherPlayer.setOnPlatform(false);
                    //onPlatform = false; //to change this logic at some point
                }
            }
        }


        //check collision with collectibles and see if player is staggered

        /*
        for (Collectible collectible : collectibles)
        {
            if (myPlayer.getYPos() + (myPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                    myPlayer.getYPos() + (myPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                    myPlayer.getXPos() + myPlayer.getCharacterWidth() >= collectible.getXPos() &&
                    myPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth() )
            {

                collectible.applyEffects(myPlayer);
                collectibles.remove(collectible);
                break;
            }

            for (Player otherPlayer : otherPlayers.values()){
                if (otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                        otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= collectible.getXPos() &&
                        otherPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth() )
                {

                    //collectible.applyEffects(otherPlayer);
                    collectibles.remove(collectible);
                    break;
                }
            }


        }

         */

        Iterator<Collectible> iterator = collectibles.iterator();
        while (iterator.hasNext()) {
            Collectible collectible = iterator.next();
            if (myPlayer.getYPos() + (myPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                    myPlayer.getYPos() + (myPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                    myPlayer.getXPos() + myPlayer.getCharacterWidth() >= collectible.getXPos() &&
                    myPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth()) {

                collectible.applyEffects(myPlayer);
                iterator.remove(); // Safe removal using the iterator
                break;
            }

            for (Player otherPlayer : otherPlayers.values()) {
                if (otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                        otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= collectible.getXPos() &&
                        otherPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth()) {

                    // collectible.applyEffects(otherPlayer);
                    iterator.remove(); // Safe removal using the iterator
                    break;
                }
            }
        }

        /*
        for (Collectible collectible : collectibles)
        {
            for (Player otherPlayer : otherPlayers.values()){
                if (otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) >= collectible.getYPos() &&
                        otherPlayer.getYPos() + (otherPlayer.getCharacterHeight()/2) <= collectible.getYPos() + collectible.getHeight() &&
                        otherPlayer.getXPos() + otherPlayer.getCharacterWidth() >= collectible.getXPos() &&
                        otherPlayer.getXPos() <= collectible.getXPos() + collectible.getWidth() )
                {

                    //collectible.applyEffects(otherPlayer);
                    collectibles.remove(collectible);
                    break;
                }
            }
        }

         */

        // Check if player is in the air
        if (!myPlayer.isOnPlatform() && y < mapHeight - myPlayer.getCharacterHeight()) {
            myPlayer.setInAir(true);
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (!otherPlayer.isOnPlatform()/*onPlatform*/ && y < /*MAP_HEIGHT*/mapHeight - otherPlayer.getCharacterHeight()/*characterHeight*/) {
                otherPlayer.setInAir(true);
                //inAir = true;
            }
        }

        // makes player stay on top of the floor of the map
        if (myPlayer.getYPos() >= (tileMap.getMapHeight()-5)* tileMap.getTileHeight())//y >= 504 /*- characterHeight*/) {
        {
            myPlayer.setYPos((tileMap.getMapHeight()-5)* tileMap.getTileHeight());
            myPlayer.setYSpeed(0);
            myPlayer.setInAir(false);
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.getYPos() >= (tileMap.getMapHeight()-5)* tileMap.getTileHeight())//y >= 504 /*- characterHeight*/) {
            {
                otherPlayer.setYPos((tileMap.getMapHeight()-5)* tileMap.getTileHeight());
                otherPlayer.setYSpeed(0);
                otherPlayer.setInAir(false);

                //y = 504 /*- characterHeight*/;
                //yVelocity = 0;
                //inAir = false;
            }
        }

        // Adjust camera position to follow player
        if (myPlayer.getYPos()  < cameraY + panelHeight / 3)
        {
            cameraY = myPlayer.getYPos() - panelHeight / 3;
        }
        else if (myPlayer.getYPos() > cameraY + panelHeight - myPlayer.getCharacterHeight())
        {
            cameraY = myPlayer.getYPos()  - panelHeight + myPlayer.getCharacterHeight();
        }

        //to ensure that the camera is always within the bounds of the map
        cameraY = Math.max(0, Math.min(cameraY, mapHeight - panelHeight));

        /*
        //Check if player reached the goal
        if (new Rectangle(myPlayer.getXPos(), myPlayer.getYPos(), myPlayer.getCharacterWidth(), myPlayer.getCharacterHeight()).intersects(goal))
        {
            gameWon = true;
        }

         */


        //repaint after all the checks have been made
        repaint();

    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //makes sure that we display the part starting from the origin of the camera (0, cameraY)
        //and bounded by the dimesnsions of the panel
        g2d.translate(0, -cameraY);

        if (!gameWon) {

            renderMap(g2d, cameraY);

            // Draw background, platforms, goal, and player character
            for (Platform platform : platforms) {
                g2d.drawImage(Platform.getPlatformImage(), platform.getXPos(), platform.getYPos(), platform.getWidth(), platform.getHeight(), null);
            }

            //draw spikes
            if(Obstacle.draw)
            {
                for (Obstacle spike : spikes) {
                    spike.render(g2d);
                }
            }

            //draw collectibles
            for(Collectible collectible : collectibles){
                collectible.render(g2d);
            }

            //draw goal
            //g2d.setColor(Color.GREEN);
            //g2d.fillOval(goal.x, goal.y, GOAL_SIZE, GOAL_SIZE);

            //flip the player's sprite appropriately based on which direction the avatar is facing
            if (myPlayer.isFacingLeft()) {
                g2d.drawImage(myPlayer.getCurrentPlayerSprite(), myPlayer.getXPos(), myPlayer.getYPos(), myPlayer.getCharacterWidth(), myPlayer.getCharacterHeight(), null);
            } else {
                g2d.drawImage(myPlayer.getCurrentPlayerSprite(), myPlayer.getXPos() + myPlayer.getCharacterWidth(), myPlayer.getYPos(), -myPlayer.getCharacterWidth(), myPlayer.getCharacterHeight(), null);
            }

            for (Player otherPlayer : otherPlayers.values()) {
                //System.out.println("Drawing player " + otherPlayer.getId() + " at " + otherPlayer.getPos());
                if (otherPlayer.isFacingLeft()) {
                    //System.out.println("Drawing other player at : " + otherPlayer.getPos());
                    //System.out.println("Drawing other player, current sprite is " + otherPlayer.getCurrentPlayerSprite());
                    g2d.drawImage(otherPlayer.getCurrentPlayerSprite(), otherPlayer.getXPos()/*x*/, otherPlayer.getYPos()/*y*/, otherPlayer.getCharacterWidth(), otherPlayer.getCharacterHeight(), null);
                } else {
                    //System.out.println("Drawing other player at : " + otherPlayer.getPos());
                    //System.out.println("Drawing other player, current sprite is " + otherPlayer.getCurrentPlayerSprite());
                    g2d.drawImage(otherPlayer.getCurrentPlayerSprite(), otherPlayer.getXPos() + otherPlayer.getCharacterWidth(), otherPlayer.getYPos(), -otherPlayer.getCharacterWidth(), otherPlayer.getCharacterHeight(), null);
                }
            }



            //for debugging purposes
            displayDebugInfo(g2d);

        } else {
            // Display win message
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString("You Win!", panelWidth / 2 - 50, cameraY + panelHeight / 2);
        }
    }

    public void displayDebugInfo(Graphics2D g2d){
        //display the character position and other information for debugging purposes.
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Avatar Position:", 10, cameraY+10);
        g2d.drawString("( " + myPlayer.getXPos() + " , "+ myPlayer.getYPos() + " )", 10, cameraY+25);
        g2d.drawString("In Air: " + myPlayer.isInTheAir(), 10, cameraY + 40);
        g2d.drawString("On Platform: " + myPlayer.isOnPlatform(), 10, cameraY + 55);
        if(myPlayer.isFacingLeft()) {
            g2d.drawString("Facing: Left" , 10, cameraY + 70);
        }
        else
        {
            g2d.drawString("Facing: Right", 10, cameraY + 70);
        }

        //g2d.drawString("Current Sprite: " + myPlayer.getCurrentPlayerSprite(), 10, cameraY + 85);

        if(myPlayer.isOnPlatform()) {
            //g2d.drawString("Platform Dimensions:", 10, 85);

            //g2d.drawString("( " + platformPlayerIsOn.getXPos()+ ", " + platformPlayerIsOn.getYPos() +  " )", 10, cameraY + 85);
            g2d.drawString("( " + platformPlayerIsOn.getWidth()+ ", " + platformPlayerIsOn.getHeight() +  " )", 10, cameraY + 100);
        }

        g2d.drawString("CameraY: " + cameraY, 10, cameraY + 115);
        g2d.drawString("Tile Height: " + tileMap.getTileHeight(), 10, cameraY + 130);

        if(myPlayer.isStaggered()){
            g2d.drawString("STAGGERED!!!", 10, cameraY + 175);
        }




        for (Player otherPlayer : otherPlayers.values()){
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Avatar Position:", 10, cameraY+200);
            g2d.drawString("( " + otherPlayer.getXPos() + " , "+ otherPlayer.getYPos() + " )", 10, cameraY+215);
            g2d.drawString("In Air: " + otherPlayer.isInTheAir(), 10, cameraY + 230);
            g2d.drawString("On Platform: " + otherPlayer.isOnPlatform(), 10, cameraY + 245);
            if(otherPlayer.isFacingLeft()) {
                g2d.drawString("Facing: Left" , 10, cameraY + 260);
            }
            else
            {
                g2d.drawString("Facing: Right", 10, cameraY + 260);
            }

            //g2d.drawString("Current Sprite: " + otherPlayer.getCurrentPlayerSprite(), 10, cameraY + 290);

            if(otherPlayer.isOnPlatform()) {
                //g2d.drawString("Platform Dimensions:", 10, 85);

                //g2d.drawString("( " + platformPlayerIsOn.getWidth()+ ", " + platformPlayerIsOn.getHeight() +  " )", 10, cameraY + 305);
            }

            g2d.drawString("CameraY: " + cameraY, 10, cameraY + 320);
            g2d.drawString("Tile Height: " + tileMap.getTileHeight(), 10, cameraY + 335);

            if(otherPlayer.isStaggered()){
                g2d.drawString("STAGGERED!!!", 10, cameraY + 350);
            }

        }



    }

    //should be incorporated on the server side. Should only be called once to get the platform position
    public void generatePlatformsAndTraps(int mapWidth, int platformXStartPos, int playerStartingYPos, int baseJumpVal, int platformWidth, int platformHeight, ArrayList<Platform> platformsContainer) {
        Platform lastPlatform = new Platform(panelWidth/2, myPlayer.getCharacterHeight(), scaleFactor);

        int rowPos = playerStartingYPos - baseJumpVal;

        int firstTowerWallXPos = 4 * tileMap.getTileWidth();
        int secondTowerWallXPos = (tileMap.getMapWidth() - 4) * tileMap.getTileWidth();

        int towerWidth = secondTowerWallXPos - firstTowerWallXPos;

        int horizontalGap = 120;
        int currPlatformXPos = platformXStartPos;

        int numPlatformsPerRow = towerWidth / (platformWidth + horizontalGap);

        boolean startDecider = true;

        Random random = new Random();
        int spikePlacement = 0;

        int spikeDecider = 0;

        boolean hasSpikes = false;

        int collectibleDecider = 2;

        boolean collectibleOnTop = false;

        while(rowPos - platformHeight > lastPlatform.getYPos()) {
            for(int i = 0; i < numPlatformsPerRow; i++) {
                platformsContainer.add(new Platform(currPlatformXPos, rowPos, scaleFactor));
                spikeDecider = random.nextInt(4);
                //spikeDecider = 4;

                if(spikeDecider == 0){
                    hasSpikes = true;
                }
                else{
                    hasSpikes = false;
                    collectibleDecider = random.nextInt(8);
                }

                if(!hasSpikes && collectibleDecider == 0){
                    collectibles.add(new BoostCollectible(currPlatformXPos + 15, rowPos - 32));
                }

                if(hasSpikes){
                    spikePlacement = random.nextInt(3);
                    //spikePlacement = 1;
                    spikes.add(new Obstacle(currPlatformXPos + (spikePlacement * 24), rowPos-24, "spikes", scaleFactor));
                }
                else{

                }

                currPlatformXPos += (platformWidth +horizontalGap);
            }

            rowPos -= (myPlayer.getCharacterHeight()+baseJumpVal-25);
            startDecider = !startDecider;

            if(startDecider) {
                currPlatformXPos = platformXStartPos;
                numPlatformsPerRow += 1;
            }
            else {
                currPlatformXPos = platformXStartPos + platformWidth + 30;
                numPlatformsPerRow--;
            }

            collectibleDecider = -12;

        }

        System.out.println("Client generated collectibles: " + collectibles);
        System.out.println("Client generated platforms: " + platforms);
        System.out.println("Client generated spikes: " + spikes);

        platforms.add(lastPlatform);
    }





    public void renderMap(Graphics2D g2d, int cameraY){

        int[][] mapMatrix = this.tileMap.getMapMatrix();

        int rowToStartDrawingFrom = (cameraY / tileMap.getTileHeight()) - 1;
        if(rowToStartDrawingFrom < 0){rowToStartDrawingFrom = 0;}

        int rowToStopDrawingFrom = rowToStartDrawingFrom + (panelHeight / tileMap.getTileHeight()) + 4;
        if(rowToStopDrawingFrom > tileMap.getMapHeight()){rowToStopDrawingFrom = tileMap.getMapHeight();}


        for (int row = rowToStartDrawingFrom; row < rowToStopDrawingFrom; row++) {
            for (int col = 0; col < mapMatrix[row].length; col++) {
                int tileId = mapMatrix[row][col];
                Tile tile = tileMap.getTile(tileId);
                if (tile != null) {
                    g2d.drawImage(tile.getScaledImage(scaleFactor), col * tileMap.getTileWidth(), row * tileMap.getTileHeight(), null);
                }
            }
        }

    }

    public Player getPlayer() {
        //System.out.println("Returning player, current sprite is: " + myPlayer.getCurrentPlayerSprite());
        return myPlayer;
    }

    public HashMap<Integer, Player> getOtherPlayers(){
        return otherPlayers;
    }


    public void addNewPlayer(Player newPlayer) {
        System.out.println("In the addnewplayer method, new player added");

        /*
        try {
            BufferedImage tempSprite = ImageIO.read(getClass().getResource("/bear_idle.png"));
            newPlayer.updateCurrentPlayerSprite(tempSprite);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         */

        otherPlayers.put(newPlayer.getId(), newPlayer);


        //System.out.println("New player sprite: " + otherPlayers.get(newPlayer.getId()).getCurrentPlayerSprite());
        //otherPlayers.get(newPlayer.getId()).getCurrentPlayerSprite();
    }


    public void updateOtherPlayer(Player newPlayer) {


        for (Player otherPlayer : otherPlayers.values()) {
            if(otherPlayer.getId() == newPlayer.getId()) {
                //System.out.println("Player " + otherPlayer.getId() + " moved");

                //otherPlayer.setXPos(newPlayer.getXPos());
                //otherPlayer.setYPos(newPlayer.getYPos());

                otherPlayer.setMoving(newPlayer.isMoving());
                otherPlayer.setFacingLeft(newPlayer.isFacingLeft());
                otherPlayer.setMovingLeft(newPlayer.isMovingLeft());
                otherPlayer.setMovingRight(newPlayer.isMovingRight());
                otherPlayer.setYSpeed(newPlayer.getYSpeed());
                otherPlayer.setInAir(newPlayer.isInTheAir());
                otherPlayer.setOnPlatform(newPlayer.isOnPlatform());

                otherPlayer.setXPos(newPlayer.getXPos());
                otherPlayer.setYPos(newPlayer.getYPos());

                //otherPlayer.setPlayerDimensions(32,32);

                //System.out.println("Other player presed or released a key");
                //System.out.println("Other player is moving: " + otherPlayer.isMoving());


            }
        }

        repaint();
    }



    /*
    public void updateOtherPlayer(String playerString) {
    	String[] fields = playerString.split(",");
        int id = -1;

        // Parse the ID first
        for (String field : fields) {
            String[] keyValue = field.split("=");
            if (keyValue[0].trim().equals("PlayerId")) {
                id = Integer.parseInt(keyValue[1].trim());
                break;
            }
        }

        Player player = otherPlayers.get(id);
        if (player == null) {
            System.out.println("Player with ID " + id + " not found.");
            return;
        }

        // Update other fields
        for (String field : fields) {
            String[] keyValue = field.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "isMoving":
                    player.setMoving(Boolean.parseBoolean(value));
                    break;
                case "isFacingLeft":
                    player.setFacingLeft(Boolean.parseBoolean(value));
                    break;
                case "isMovingLeft":
                    player.setMovingLeft(Boolean.parseBoolean(value));
                    break;
                case "isMovingRight":
                    player.setMovingRight(Boolean.parseBoolean(value));
                    break;
                case "ySpeed":
                    player.setYSpeed(Integer.parseInt(value));
                    break;
                case "isInTheAir":
                    player.setInAir(Boolean.parseBoolean(value));
                    break;
                case "isOnPlatform":
                    player.setOnPlatform(Boolean.parseBoolean(value));
                    break;
                case "xPos":
                    player.setXPos(Integer.parseInt(value));
                    break;
                case "yPos":
                    player.setYPos(Integer.parseInt(value));
                    break;
            }
        }
    }
    */


    public void updatePositions(HashMap<Integer, Player> positions) {
        System.out.println("Updating other players");
        System.out.println(otherPlayers);
        //this.otherPlayers = positions;
        repaint();
    }

    public static Player fromString(String playerString) {
        String[] fields = playerString.split(",");
        int id = -1;

        // Parse the ID first
        for (String field : fields) {
            String[] keyValue = field.split("=");
            if (keyValue[0].trim().equals("id")) {
                id = Integer.parseInt(keyValue[1].trim());
                break;
            }
        }

        if (id == -1) {
            throw new IllegalArgumentException("Invalid player data: ID not found");
        }

        Player player = new Player(1); // Default avatarId or adjust as needed
        player.setId(id);

        // Set other fields
        for (String field : fields) {
            String[] keyValue = field.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "isMoving":
                    player.setMoving(Boolean.parseBoolean(value));
                    break;
                case "isFacingLeft":
                    player.setFacingLeft(Boolean.parseBoolean(value));
                    break;
                case "isMovingLeft":
                    player.setMovingLeft(Boolean.parseBoolean(value));
                    break;
                case "isMovingRight":
                    player.setMovingRight(Boolean.parseBoolean(value));
                    break;
                case "ySpeed":
                    player.setYSpeed(Integer.parseInt(value));
                    break;
                case "isInTheAir":
                    player.setInAir(Boolean.parseBoolean(value));
                    break;
                case "isOnPlatform":
                    player.setOnPlatform(Boolean.parseBoolean(value));
                    break;
                case "xPos":
                    player.setXPos(Integer.parseInt(value));
                    break;
                case "yPos":
                    player.setYPos(Integer.parseInt(value));
                    break;
            }
        }

        return player;
    }


    public static void main(String[] args) throws Exception {
        int[][] mapMatrix = {
                {0, 0, 0, 0, 0, 23, 15, 20, 20, 20, 20, 17, 20, 20, 20, 19, 15, 20, 20, 20, 15, 18, 20, 16, 17, 20, 20, 19, 18, 20, 20, 16, 15, 20, 15, 20, 20, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 15, 16, 20, 20, 19, 20, 20, 20, 20, 18, 18, 20, 15, 20, 20, 20, 17, 18, 15, 20, 20, 16, 17, 15, 20, 20, 15, 16, 20, 19, 16, 19, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 15, 20, 16, 20, 15, 17, 19, 20, 20, 19, 19, 20, 16, 20, 20, 17, 18, 20, 20, 20, 20, 15, 19, 18, 20, 20, 20, 15, 16, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 15, 20, 20, 20, 20, 17, 17, 18, 20, 20, 19, 18, 20, 20, 20, 19, 18, 20, 20, 20, 15, 20, 19, 15, 20, 15, 20, 20, 16, 16, 20, 17, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 19, 18, 20, 16, 20, 20, 20, 20, 19, 18, 20, 15, 20, 20, 16, 20, 20, 17, 19, 20, 20, 20, 20, 20, 20, 15, 18, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 20, 20, 20, 17, 20, 20, 20, 19, 15, 20, 20, 20, 15, 18, 20, 16, 17, 20, 20, 19, 18, 20, 20, 16, 15, 20, 15, 20, 20, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 15, 16, 20, 20, 19, 20, 20, 20, 20, 18, 18, 20, 15, 20, 20, 20, 17, 18, 15, 20, 20, 16, 17, 15, 20, 20, 15, 16, 20, 19, 16, 19, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 15, 20, 16, 20, 15, 17, 19, 20, 20, 19, 19, 20, 16, 20, 20, 17, 18, 20, 20, 20, 20, 15, 19, 18, 20, 20, 20, 15, 16, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 15, 20, 20, 20, 20, 17, 17, 18, 20, 20, 19, 18, 20, 20, 20, 19, 18, 20, 20, 20, 15, 20, 19, 15, 20, 15, 20, 20, 16, 16, 20, 17, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 19, 18, 20, 16, 20, 20, 20, 20, 19, 18, 20, 15, 20, 20, 16, 20, 20, 17, 19, 20, 20, 20, 20, 20, 20, 15, 18, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 20, 20, 20, 17, 20, 20, 20, 19, 15, 20, 20, 20, 15, 18, 20, 16, 17, 20, 20, 19, 18, 20, 20, 16, 15, 20, 15, 20, 20, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 15, 16, 20, 20, 19, 20, 20, 20, 20, 18, 18, 20, 15, 20, 20, 20, 17, 18, 15, 20, 20, 16, 17, 15, 20, 20, 15, 16, 20, 19, 16, 19, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 15, 20, 16, 20, 15, 17, 19, 20, 20, 19, 19, 20, 16, 20, 20, 17, 18, 20, 20, 20, 20, 15, 19, 18, 20, 20, 20, 15, 16, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 15, 20, 20, 20, 20, 17, 17, 18, 20, 20, 19, 18, 20, 20, 20, 19, 18, 20, 20, 20, 15, 20, 19, 15, 20, 15, 20, 20, 16, 16, 20, 17, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 19, 18, 20, 16, 20, 20, 20, 20, 19, 18, 20, 15, 20, 20, 16, 20, 20, 17, 19, 20, 20, 20, 20, 20, 20, 15, 18, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 20, 20, 20, 17, 20, 20, 20, 19, 15, 20, 20, 20, 15, 18, 20, 16, 17, 20, 20, 19, 18, 20, 20, 16, 15, 20, 15, 20, 20, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 15, 16, 20, 20, 19, 20, 20, 20, 20, 18, 18, 20, 15, 20, 20, 20, 17, 18, 15, 20, 20, 16, 17, 15, 20, 20, 15, 16, 20, 19, 16, 19, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 15, 20, 16, 20, 15, 17, 19, 20, 20, 19, 19, 20, 16, 20, 20, 17, 18, 20, 20, 20, 20, 15, 19, 18, 20, 20, 20, 15, 16, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 15, 20, 20, 20, 20, 17, 17, 18, 20, 20, 19, 18, 20, 20, 20, 19, 18, 20, 20, 20, 15, 20, 19, 15, 20, 15, 20, 20, 16, 16, 20, 17, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 19, 18, 20, 16, 20, 20, 20, 20, 19, 18, 20, 15, 20, 20, 16, 20, 20, 17, 19, 20, 20, 20, 20, 20, 20, 15, 18, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 20, 20, 20, 17, 20, 20, 20, 19, 15, 20, 20, 20, 15, 18, 20, 16, 17, 20, 20, 19, 18, 20, 20, 16, 15, 20, 15, 20, 20, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 15, 16, 20, 20, 19, 20, 20, 20, 20, 18, 18, 20, 15, 20, 20, 20, 17, 18, 15, 20, 20, 16, 17, 15, 20, 20, 15, 16, 20, 19, 16, 19, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 15, 20, 16, 20, 15, 17, 19, 20, 20, 19, 19, 20, 16, 20, 20, 17, 18, 20, 20, 20, 20, 15, 19, 18, 20, 20, 20, 15, 16, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 15, 20, 20, 20, 20, 17, 17, 18, 20, 20, 19, 18, 20, 20, 20, 19, 18, 20, 20, 20, 15, 20, 19, 15, 20, 15, 20, 20, 16, 16, 20, 17, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 20, 15, 19, 18, 20, 16, 20, 20, 20, 20, 19, 18, 20, 15, 20, 20, 16, 20, 20, 17, 19, 20, 20, 20, 20, 20, 20, 15, 18, 17, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 16, 15, 20, 20, 20, 19, 19, 17, 20, 20, 20, 18, 20, 20, 20, 20, 20, 15, 20, 20, 16, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 20, 20, 19, 16, 15, 20, 20, 16, 15, 20, 15, 15, 20, 20, 18, 20, 15, 20, 20, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 20, 20, 20, 16, 16, 17, 20, 18, 18, 20, 20, 20, 20, 19, 18, 20, 19, 16, 16, 15, 20, 20, 20, 16, 15, 17, 20, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 20, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 19, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 20, 20, 19, 16, 15, 20, 20, 16, 15, 20, 15, 15, 20, 20, 18, 20, 15, 20, 20, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 20, 20, 20, 16, 16, 17, 20, 18, 18, 20, 20, 20, 20, 19, 18, 20, 19, 16, 16, 15, 20, 20, 20, 16, 15, 17, 20, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 16, 15, 20, 20, 20, 19, 19, 17, 20, 20, 20, 18, 20, 20, 20, 20, 20, 15, 20, 20, 16, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 20, 20, 20, 20, 16, 15, 20, 20, 18, 18, 20, 15, 20, 16, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 19, 16, 20, 20, 20, 16, 17, 20, 20, 15, 18, 20, 20, 20, 20, 19, 20, 20, 19, 20, 16, 15, 20, 20, 20, 16, 20, 20, 17, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 18, 20, 20, 20, 17, 19, 19, 17, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 20, 20, 20, 20, 20, 15, 16, 20, 20, 20, 17, 20, 16, 20, 15, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 15, 20, 16, 16, 20, 20, 20, 20, 16, 15, 18, 20, 20, 19, 16, 15, 20, 20, 16, 15, 20, 15, 15, 20, 20, 18, 20, 15, 20, 20, 16, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 23, 20, 20, 16, 20, 20, 20, 16, 16, 17, 20, 18, 18, 20, 20, 20, 20, 19, 18, 20, 19, 16, 16, 15, 20, 20, 20, 16, 15, 17, 20, 20, 20, 20, 24, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 21, 19, 15, 20, 20, 17, 15, 15, 20, 16, 20, 16, 18, 18, 15, 19, 20, 16, 15, 16, 20, 20, 20, 19, 20, 18, 15, 17, 20, 20, 16, 16, 15, 18, 22, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 15, 20, 20, 20, 15, 16, 20, 20, 17, 20, 20, 17, 15, 20, 20, 16, 15, 20, 20, 20, 15, 20, 20, 16, 20, 16, 15, 20, 20, 19, 18, 20, 20, 19, 19, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 20, 16, 15, 20, 20, 17, 19, 20, 17, 20, 20, 20, 18, 20, 20, 20, 20, 20, 15, 20, 20, 16, 20, 20, 15, 16, 20, 20, 20, 17, 16, 16, 20, 15, 15, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 15, 19, 19, 16, 16, 20, 20, 20, 20, 16, 15, 18, 18, 19, 19, 16, 15, 17, 17, 16, 15, 16, 15, 15, 15, 18, 18, 16, 15, 19, 16, 16, 20, 20, 15, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 13, 14, 13, 14, 13, 14, 13, 14 ,13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 0, 0, 0, 0, 0},
                {2, 1, 2, 1, 2, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 1, 2, 1, 2, 1},
                {4, 3, 4, 3, 4, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 10, 9, 3, 4, 3, 4, 3},
                {6, 5, 6, 5, 6, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 12, 11, 5, 6, 5, 6, 5},
                {8, 7, 8, 7, 8, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 14, 13, 7, 8, 7, 8, 7}
                // Add more rows as needed
        };

        GameMap gameMap = new GameMap(1000, 800, mapMatrix);

        try {

            javax.swing.SwingUtilities.invokeLater(() -> {
                SinglePlayerTesting gamePanel = new SinglePlayerTesting(gameMap);


                //the frame info should not be there since the game panel will be accessed through the cardLayout
                JFrame frame = new JFrame("Game Panel");
                frame.setResizable(false);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.add(gamePanel);
                frame.pack();
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load the tileset.");
        }

        // Attempt to play background sound (error handling included)
        try {
            SoundPlayer audioPlayer = new SoundPlayer();
            audioPlayer.play();
        } catch (Exception ex) {
            System.out.println("Error with playing sound.");
            ex.printStackTrace();
        }
    }































    public void addPlatform(Platform platform){
        platforms.add(platform);
    }

    public void addSpike(Obstacle spike){
        spikes.add(spike);
    }

    public void addCollectible(Collectible collectible){
        System.out.println("Adding new collectible: " + collectible);
        collectibles.add(collectible);
    }



    //setters and getters
    public int getPanelWidth(){
        return panelWidth;
    }

    public double getScaleFactor(){
        return scaleFactor;
    }

    public TileMap getTileMap(){
        return tileMap;
    }

    public void setSpikes(ArrayList<Obstacle> spikes){
        this.spikes = spikes;
    }
    public ArrayList<Obstacle> getSpikes() {
        return spikes;
    }

    public void setPlatforms(ArrayList<Platform> platforms){
        this.platforms = platforms;
    }
    public ArrayList<Platform> getPlatforms() {
        return platforms;
    }

    public ArrayList<Collectible> getCollectibles(){
        return collectibles;
    }




}
