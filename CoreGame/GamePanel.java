package CoreGame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GamePanel extends JPanel {
    //-----------gamePanel components---------------------------------
    private Player myPlayer;
    private TileMap tileMap;
    private ArrayList<Platform> platforms;
    //spikes container
    private ArrayList<Obstacle> spikes;
    private ArrayList<Collectible> collectibles;
    HashMap<Integer, Player> otherPlayers;
    private Goal goal;
    private int cameraY;
    private double scaleFactor = 1.5;

    private boolean ready = false;
    private boolean go = false;

    private boolean gameWon = false;

    private JPanel container;


    //-----------constructor------------------------------------------------------------

    //we assume that the client has already received the map from the server and communicated it to the gameController
    //at the point where we call the gamePanel constructor.
    public GamePanel(GameController gameController){
        System.out.println("GamePanel is constructed");
        this.tileMap = gameController.getTileMap();
        this.setPreferredSize(new Dimension(gameController.getPanelWidth(), gameController.getPanelHeight()));

        //set up the event listeners of the panel.
        this.addKeyListener(gameController);

        this.setFocusable(true);

        gameController.setGamePanel(this);

        this.container = container;
    }

    //-------------setters to update the game components of the panel (from controller)----------------------

    public void setPlayer(Player player){
        this.myPlayer = player;
    }

    public void setPlatforms(ArrayList<Platform> platforms){
        this.platforms = platforms;
    }

    public void setCollectibles(ArrayList<Collectible> collectibles){
        this.collectibles = collectibles;
    }

    public void setSpikes(ArrayList<Obstacle> spikes){
        this.spikes = spikes;
    }

    public void setCameraY(int cameraY){
        this.cameraY = cameraY;
    }

    public void setOtherPlayers(HashMap otherPlayers){
        this.otherPlayers = otherPlayers;
    }

    //-------------drawing of the panel------------------------------------------------

    protected void paintComponent(Graphics g){
        //System.out.println("gamePanel is being drawn");
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        //makes sure that we display the part starting from the origin of the camera (0, cameraY)
        //and bounded by the dimesnsions of the panel
        g2d.translate(0, -cameraY);

        if(gameWon){

            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "4");

            //displayWinMessage(g2d);
        }
        else {
            if (this.tileMap != null) {

                renderMap(g2d);
                renderPlatforms(g2d);
                renderObstacles(g2d);
                renderCollectibles(g2d);
                renderPlayer(g2d);
                //displayDebugInfo(g2d);

                //draw goal
                renderGoal(g2d);

                if (ready) {
                    displayReady(g2d);
                }

                if (go) {
                    displayStartMessage(g2d);
                }

            } else {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                g2d.drawString("Game Panel could not load.", 500, cameraY + 300);
            }
        }
    }


    public void displayWinMessage(Graphics g2d){
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Someone won the Game!!!.", 500, cameraY + 300);
    }

    //display the character position and other information for debugging purposes.
    public void displayDebugInfo(Graphics g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Avatar Position:", 10, cameraY + 10);
        g2d.drawString("( " + myPlayer.getXPos() + " , " + myPlayer.getYPos() + " )", 10, cameraY + 25);
        g2d.drawString("In Air: " + myPlayer.isInTheAir(), 10, cameraY + 40);
        g2d.drawString("On Platform: " + myPlayer.isOnPlatform(), 10, cameraY + 55);
        if (myPlayer.isFacingLeft()) {
            g2d.drawString("Facing: Left", 10, cameraY + 70);
        } else {
            g2d.drawString("Facing: Right", 10, cameraY + 70);
        }

        g2d.drawString("Current Sprite: " + myPlayer.getCurrentPlayerSprite(), 10, cameraY + 85);

        if (myPlayer.isOnPlatform()) {
            g2d.drawString("Platform Dimensions:", 10, 85);

//            g2d.drawString("( " + platformPlayerIsOn.getXPos()+ ", " + platformPlayerIsOn.getYPos() +  " )", 10, cameraY + 85);
//            g2d.drawString("( " + platformPlayerIsOn.getWidth()+ ", " + platformPlayerIsOn.getHeight() +  " )", 10, cameraY + 100);
        }

        g2d.drawString("CameraY: " + cameraY, 10, cameraY + 115);
        g2d.drawString("Tile Height: " + tileMap.getTileHeight(), 10, cameraY + 130);

        if (myPlayer.isStaggered()) {
            g2d.drawString("STAGGERED!!!", 10, cameraY + 175);
        }


        for (Player otherPlayer : otherPlayers.values()) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString("Avatar Position:", 10, cameraY + 200);
            g2d.drawString("( " + otherPlayer.getXPos() + " , " + otherPlayer.getYPos() + " )", 10, cameraY + 215);
            g2d.drawString("In Air: " + otherPlayer.isInTheAir(), 10, cameraY + 230);
            g2d.drawString("On Platform: " + otherPlayer.isOnPlatform(), 10, cameraY + 245);
            if (otherPlayer.isFacingLeft()) {
                g2d.drawString("Facing: Left", 10, cameraY + 260);
            } else {
                g2d.drawString("Facing: Right", 10, cameraY + 260);
            }

            g2d.drawString("Current Sprite: " + otherPlayer.getCurrentPlayerSprite(), 10, cameraY + 290);

            if (otherPlayer.isOnPlatform()) {
//                g2d.drawString("Platform Dimensions:", 10, 85);
//
//                g2d.drawString("( " + platformPlayerIsOn.getWidth()+ ", " + platformPlayerIsOn.getHeight() +  " )", 10, cameraY + 305);
            }

            g2d.drawString("CameraY: " + cameraY, 10, cameraY + 320);
            g2d.drawString("Tile Height: " + tileMap.getTileHeight(), 10, cameraY + 335);

            if (otherPlayer.isStaggered()) {
                g2d.drawString("STAGGERED!!!", 10, cameraY + 350);
            }

        }
    }


    public void renderMap(Graphics g2d){
        int[][] mapMatrix = this.tileMap.getMapMatrix();

        int rowToStartDrawingFrom = (cameraY / tileMap.getTileHeight()) - 1;
        if(rowToStartDrawingFrom < 0){rowToStartDrawingFrom = 0;}

        int rowToStopDrawingFrom = rowToStartDrawingFrom + (this.getHeight() / this.tileMap.getTileHeight()) + 4;
        if(rowToStopDrawingFrom > this.tileMap.getMapHeight()){rowToStopDrawingFrom = this.tileMap.getMapHeight();}


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

    public void renderPlatforms(Graphics g2d){
        // Draw background, platforms, goal, and player character
        for (Platform platform : platforms) {
            g2d.drawImage(Platform.getPlatformImage(), platform.getXPos(), platform.getYPos(), platform.getWidth(), platform.getHeight(), null);
        }
    }

    public void renderObstacles(Graphics g2d){
        //draw spikes
        if(Obstacle.draw)
        {
            for (Obstacle spike : spikes) {
                spike.render(g2d);
            }
        }
    }

    public void renderCollectibles(Graphics g2d){
        //draw collectibles
        for(Collectible collectible : collectibles){
            g2d.drawImage(collectible.getImage(), collectible.getXPos(), collectible.getYPos(), collectible.getWidth(), collectible.getHeight(), null);
        }
    }

    public void renderGoal(Graphics g2d){
        //draw goal
        g2d.drawImage(Goal.getGoalImage(), goal.getXPos(), goal.getYPos(), goal.getWidth(), goal.getHeight(), null);

    }

    public void renderPlayer(Graphics g2d){
        //flip the player's sprite appropriately based on which direction the avatar is facing
        if (myPlayer.isFacingLeft()) {
            g2d.drawImage(myPlayer.getCurrentPlayerSprite(), myPlayer.getXPos(), myPlayer.getYPos(), myPlayer.getCharacterWidth(), myPlayer.getCharacterHeight(), null);
        } else {
            g2d.drawImage(myPlayer.getCurrentPlayerSprite(), myPlayer.getXPos() + myPlayer.getCharacterWidth(), myPlayer.getYPos(), -myPlayer.getCharacterWidth(), myPlayer.getCharacterHeight(), null);
        }

        for (Player otherPlayer : otherPlayers.values()) {
            if (otherPlayer.isFacingLeft()) {
                g2d.drawImage(otherPlayer.getCurrentPlayerSprite(), otherPlayer.getXPos()/*x*/, otherPlayer.getYPos()/*y*/, otherPlayer.getCharacterWidth(), otherPlayer.getCharacterHeight(), null);
            } else {
                g2d.drawImage(otherPlayer.getCurrentPlayerSprite(), otherPlayer.getXPos() + otherPlayer.getCharacterWidth(), otherPlayer.getYPos(), -otherPlayer.getCharacterWidth(), otherPlayer.getCharacterHeight(), null);
            }
        }
    }

    public void displayReady(Graphics g2d){
        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.drawString("READY?", 500, cameraY+300);
    }

    public void displayStartMessage(Graphics g2d){
        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.drawString("GO!!!", 500, cameraY+300);
    }

    public void setGameWon(boolean status){
        this.gameWon = status;
    }

    public void setTileMap(TileMap tileMap){
        this.tileMap = tileMap;
    }

    public void setReady(boolean status){
        this.ready = status;
    }

    public void setGo(boolean status){
        this.go = status;
    }

    public boolean getReady(){
        return this.ready;
    }

    public boolean getGo(){
        return this.go;
    }

    public void setGoal(Goal goal){
        this.goal = goal;
    }

}
