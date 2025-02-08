package CoreGame;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class Platform {
    private int xPos, yPos; // Position of the platform
    private int width = 48, height = 10; // Dimensions of the platform

    private static final String PLATFORM_IMAGE_PATH = "./assets/platform.png";
    private static BufferedImage platformImage = null;
    private static boolean imageRead = false;

    private boolean isDisappearing;
    private boolean hasSpikes;

    public Platform(int xPos, int yPos, double scaleFactor) {
        if (!imageRead) {
            try {
                BufferedImage tmpImage = ImageIO.read(new File(PLATFORM_IMAGE_PATH));
                //System.out.println("read platforms fine");
                platformImage = tmpImage;
                imageRead = true;
            } catch (IOException e) {
                System.out.println("Could not read the platform image file.");
                e.printStackTrace();
            }
        }

        this.xPos = xPos;
        this.yPos = yPos;
        this.width = (int) (width * scaleFactor);
        this.height = (int) (height * scaleFactor);
    }

    public void setWidth(int width){
        this.width = width;
    }

    public void setHeight(int height){
        this.height = height;
    }

    public void setIsDisappearing(boolean bool){
        this.isDisappearing = bool;
    }

    public void setHasSpikes(boolean bool){
        this.hasSpikes = bool;
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

    public static BufferedImage getPlatformImage() {
        return platformImage;
    }

    // Render method for drawing the platform
    public void render(Graphics g) {
        if (platformImage != null) {
            g.drawImage(platformImage, xPos, yPos, width, height, null);
        }
    }

    @Override
    public String toString() {
        return String.format("Platform[xPos=%d,yPos=%d,width=%d,height=%d,isDisappearing=%b,hasSpikes=%b]",
                xPos, yPos, width, height, isDisappearing, hasSpikes);
    }



}
