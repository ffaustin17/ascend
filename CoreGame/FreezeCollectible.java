package CoreGame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;

public class FreezeCollectible extends Collectible{
    private static final String FREEZE_COLLECTABLE_IMAGE_PATH = "./assets/collectable.png";


    public FreezeCollectible(int x, int y){

        super(x,y);
        this.type = "freeze";

            try {
                collectibleImage = ImageIO.read(new File(FREEZE_COLLECTABLE_IMAGE_PATH));
            } catch (IOException e) {
                System.out.println("Could not read the freeze collectible image file.");
                e.printStackTrace();
            }
    }

    @Override
    public void applyEffects(Player player) {
        player.setXSpeed(player.getXSpeed()- 3);
    }

    @Override
    public void applyEffects(ArrayList<Player> players) {
        for(Player player: players){
            applyEffects(player);
        }
    }

    public BufferedImage getImage(){
        return this.collectibleImage;
    }
}
