package CoreGame;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

public class Goal {
    private int xPos, yPos;// Position of the platform
    private final int minYPos;
    private final int maxYPos;
    private int width = 32, height = 32; // Dimensions of the platform
    private int animationDirection = 2;

    private static final String GOAL_IMAGE_PATH = "./assets/crown.png";
    private static BufferedImage goalImage = null;
    private static boolean imageRead = false;

    public Goal(int xPos, int yPos) {
        if (!imageRead) {
            try {
                BufferedImage tmpImage = ImageIO.read(new File(GOAL_IMAGE_PATH));
                //System.out.println("read crown fine");
                goalImage = tmpImage;
                imageRead = true;
            } catch (IOException e) {
                System.out.println("Could not read the goal image file.");
                e.printStackTrace();
            }
        }

        this.minYPos = yPos;
        this.maxYPos = yPos - height/4;

        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void setWidth(int width){
        this.width = width;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getXPos() {
        return this.xPos;
    }

    public int getYPos() {
        return this.yPos;
    }

    public static BufferedImage getGoalImage() {
        return goalImage;
    }

    public void animate(){

        if(this.yPos == this.maxYPos || this.yPos == this.minYPos){
            animationDirection *= -1;
        }

        yPos += animationDirection;

    }
}
