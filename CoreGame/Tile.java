package CoreGame;

import java.awt.Image;
import java.awt.image.BufferedImage;

public class Tile {

	private BufferedImage originalImage;
	private BufferedImage scaledImage;
	private int id;

	public Tile(BufferedImage image, int id) {
		this.originalImage = image;
		this.id = id;
	}

	public BufferedImage getScaledImage(double scaleFactor) {
        if (scaledImage == null || scaleFactor != 1.0) {
            int newWidth = (int) (originalImage.getWidth() * scaleFactor);
            int newHeight = (int) (originalImage.getHeight() * scaleFactor);
            Image tmp = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            scaledImage.getGraphics().drawImage(tmp, 0, 0, null);
        }
        return scaledImage;
    }

	public int getId() {
        return id;
    }

}
