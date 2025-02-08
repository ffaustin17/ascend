package CoreGame;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;

public class Player {
    //--player character fields--
    public int playerId = 1;
    private int xPos;
    private int yPos;
    private int xSpeed = 5;
    private int ySpeed;
    private final int avatarId;  //used to access the player's character avatar and animations
    private BufferedImage currentPlayerImage;  //corresponds to the image of the player displayed in one frame
    private int characterHeight = 32;
    private int characterWidth = 32;

    //--flags to determine status of the player character--
    private boolean inAir = false;
    private boolean onPlatform = false;
    private boolean isMoving = false;
    private boolean facingLeft = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean isBoosted = false;
    private boolean isFrozen = false;
    private boolean staggered = false;
    private boolean goalReached = false;

    //--for retrieving the files relevant to the character's rendering--
    private String avatarType = "";
    private final String[] ANIMATION_FILE_NAMES = {"../assets/bear_idle.png", "../assets/bear_walk.png", "../assets/bear_jump.png"};
    private String animationFilePath;

    public String PLATFORM_IMAGE_PATH = "assets/platform.png";

    //--Constants--
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = -15;

    //------------------------Constructor-------------------------------------------------------------
    public Player(int avatarId) {
        Random random = new Random();

        // Generate a random integer
        int randomInt = random.nextInt(2000);
        this.setId(randomInt);

        this.avatarId = avatarId;

        switch(avatarId) {
            case 1:
                avatarType = "bear";
                break;
            case 2:
                avatarType = "witch";
                break;
            default:
                avatarType = "bear";
                break;
        }

        animationFilePath = avatarType + "/" + ANIMATION_FILE_NAMES[0];

        try {
            currentPlayerImage = ImageIO.read(new File("./assets/bear_idle.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //------------------------Movement Methods-------------------------------------------------------------
    public void moveLeft() {
        facingLeft = true;
        movingLeft = true;
        xPos -= xSpeed;
    }

    public void moveRight() {
        facingLeft = false;
        movingRight = true;
        xPos += xSpeed;
    }

    public void jump(int i) {
        if (!inAir) {  // Only jump if not already in the air
            ySpeed = JUMP_STRENGTH;
            inAir = true;
            onPlatform = false;
        }
    }

    public void applyGravity() {
        if (inAir) {
            ySpeed += GRAVITY; // Increase downward speed due to gravity
            yPos += ySpeed;
        }
    }

    public void updatePosition() {
        if (movingLeft) moveLeft();
        if (movingRight) moveRight();
        applyGravity();
    }

    //------------------------Collision Handling-------------------------------------------------------------
    public void handlePlatformCollision(Platform platform) {
        if (yPos + characterHeight >= platform.getYPos() &&
                yPos + characterHeight <= platform.getYPos() + ySpeed &&
                xPos + characterWidth > platform.getXPos() &&
                xPos < platform.getXPos() + platform.getWidth()) {

            yPos = platform.getYPos() - characterHeight;  // Adjust position to be on top of platform
            ySpeed = 0;
            inAir = false;
            onPlatform = true;
        } else {
            onPlatform = false;
        }
    }

    public void preventFallingThroughMap(int mapHeight) {
        if (yPos + characterHeight >= mapHeight) {
            yPos = mapHeight - characterHeight;
            ySpeed = 0;
            inAir = false;
        }
    }

    //------------------------Setters-------------------------------------------------------------
    public void setPos(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void setXPos(int newXPos) {
        this.xPos = newXPos;
    }

    public void setYPos(int newYPos) {
        this.yPos = newYPos;
    }

    public void setXSpeed(int xSpeed) {
        this.xSpeed = xSpeed;
    }

    public void setYSpeed(int ySpeed) {
        this.ySpeed = ySpeed;
    }

    public void updateCurrentPlayerSprite(BufferedImage newPlayerImage) {
        this.currentPlayerImage = newPlayerImage;
    }

    public void setPlayerDimensions(int playerHeight, int playerWidth) {
        this.characterWidth = playerHeight;
        this.characterHeight = playerWidth;
    }

    public void setStaggered(boolean status){
        this.staggered = status;
    }
    public void setCharacterWidth(int newCharacterWidth) {
        this.characterWidth = newCharacterWidth;
    }

    public void setCharacterHeight(int newCharacterHeight) {
        this.characterHeight = newCharacterHeight;
    }

    public void setMovingLeft(boolean status) {
        this.movingLeft = status;
    }

    public void setMovingRight(boolean status) {
        this.movingRight = status;
    }

    public void setFacingLeft(boolean status) {
        this.facingLeft = status;
    }

    public void setInAir(boolean status) {
        this.inAir = status;
    }

    public void setOnPlatform(boolean status) {
        this.onPlatform = status;
    }

    public void setMoving(boolean status) {
        this.isMoving = status;
    }

    public void setBoosted(boolean status){
        this.isBoosted = status;
    }

    public void setFrozen(boolean status){
        this.isFrozen = status;
    }

    public void setId(int id) {
        this.playerId = id;
    }

    public void setGoalReached(boolean status){
        this.goalReached = status;
    }

    //------------------------Getters-------------------------------------------------------------
    public int getXPos() {
        return this.xPos;
    }

    public int getYPos() {
        return this.yPos;
    }

    public Point getPos() {
        return new Point(xPos, yPos);
    }

    public int getXSpeed() {
        return this.xSpeed;
    }

    public int getYSpeed() {
        return this.ySpeed;
    }

    public int getAvatarId() {
        return this.avatarId;
    }

    public boolean isStaggered(){return this.staggered;}

    public BufferedImage getCurrentPlayerSprite() {
        return this.currentPlayerImage;
    }

    public Dimension getPlayerDimensions() {
        return new Dimension(this.characterWidth, this.characterHeight);
    }

    public int getCharacterWidth() {
        return this.characterWidth;
    }

    public int getCharacterHeight() {
        return this.characterHeight;
    }

    public int getId() {
        return playerId;
    }

    public boolean isMoving() {
        return this.movingLeft || this.movingRight;
    }

    public boolean isFacingLeft() {
        return this.facingLeft;
    }

    public boolean isInTheAir() {
        return this.inAir;
    }

    public boolean isOnPlatform() {
        return this.onPlatform;
    }

    public boolean isMovingLeft() {
        return this.movingLeft;
    }

    public boolean isMovingRight() {return this.movingRight;}

    public boolean isBoosted(){
        return this.isBoosted;
    }

    public boolean isFrozen(){
        return this.isFrozen;
    }

    public boolean hasReachedGoal(){
        return this.goalReached;
    }

    //------------------------Interaction with Server-------------------------------------------------------------

    //this method should only be used by the Player class corresponding to the client
    public String getPacketForServer(){
        String packetType = "Player";
        String delimiter = "#";

        //String currentViewAssetFolder
        //String currentAnimation

        return packetType + delimiter + this.xPos + delimiter + this.yPos /* + currentViewAssetFolder + delimiter + currentAnimation*/;
    }

    //this method should only be used on a client's "other players" class.
    public void receivePacketFromServerAndUpdate(String packetFromServer){
        String[] packetElements = packetFromServer.split("#");

        this.xPos = Integer.parseInt(packetElements[1]);
        this.yPos = Integer.parseInt(packetElements[2]);

        //more parsing as needed.


    }

    @Override
    public String toString() {
        return "PlayerId=" + playerId +
                ",xPos=" + xPos +
                ",yPos=" + yPos +
                ",xSpeed=" + xSpeed +
                ",ySpeed=" + ySpeed +
                ",avatarId=" + avatarId +
                ",characterHeight=" + characterHeight +
                ",characterWidth=" + characterWidth +
                ",inAir=" + inAir +
                ",onPlatform=" + onPlatform +
                ",isMoving=" + isMoving +
                ",facingLeft=" + facingLeft +
                ",movingLeft=" + movingLeft +
                ",movingRight=" + movingRight +
                ",avatarType=" + avatarType +
                ",animationFilePath=" + animationFilePath +
                ",PLATFORM_IMAGE_PATH=" + PLATFORM_IMAGE_PATH +
                ",isStaggered=" + staggered;
    }
}
