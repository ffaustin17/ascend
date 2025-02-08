package CoreGame;


import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TileMap {

    private int[][] mapMatrix;       // Matrix to map out specific tile IDs
    private Tile[] tiles;            // Array to store each tile from the tileset
    private int tileWidth = 16;
    private int tileHeight = 16;
    private int mapWidth;            // Width in terms of tiles
    private int mapHeight;
    private int GOAL_TILE_ID = 10000;// Height in terms of tiles

    public TileMap(String tilesetPath, int[][] mapMatrix) throws Exception {
        //System.out.println("Tilemap constructor");

        this.mapMatrix = mapMatrix;
        this.mapWidth = mapMatrix[0].length;
        //System.out.println("Map width: " + mapWidth);
        this.mapHeight = mapMatrix.length;
        //System.out.println("Map Height: " + mapHeight);

        // Load tiles from tileset
        BufferedImage tileset = ImageIO.read(new File(tilesetPath));
        //System.out.println("Read the tileset for the map background.");
        int numTiles = tileset.getWidth() / tileWidth;
        //System.out.println("Tileset width: " + tileset.getWidth());
        //System.out.println("Num of tiles (tiles are 16x16): " + numTiles);
        tiles = new Tile[numTiles];

        for (int i = 0; i < numTiles; i++) {
            BufferedImage tileImage = tileset.getSubimage(i * tileWidth, 0, tileWidth, tileHeight);
            tiles[i] = new Tile(tileImage, i);
        }
    }
    public boolean checkGoalReached(Player player) {
        int playerTileX = player.getXPos() / tileWidth;
        int playerTileY = player.getYPos() / tileHeight;

        // Check if the player's current tile position matches a goal tile
        if (playerTileX >= 0 && playerTileX < mapWidth && playerTileY >= 0 && playerTileY < mapHeight) {
            return mapMatrix[playerTileY][playerTileX] == GOAL_TILE_ID;
        }
        return false;
    }
    // Render method to draw the map
    public void render(Graphics g) {
        for (int row = 0; row < mapMatrix.length; row++) {
            for (int col = 0; col < mapMatrix[row].length; col++) {
                int tileId = mapMatrix[row][col];
                Tile tile = getTile(tileId);
                if (tile != null) {
                    BufferedImage tileImage = tile.getScaledImage(1.0); // assuming no scaling
                    g.drawImage(tileImage, col * tileWidth, row * tileHeight, null);
                }
            }
        }
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public Tile getTile(int id) {
        if (id >= 0 && id < tiles.length) {
            return tiles[id];
        }
        return null;
    }

    public int[][] getMapMatrix() {
        return mapMatrix;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }
}
