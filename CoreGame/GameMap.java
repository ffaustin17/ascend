package CoreGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameMap {

    // Fields
    private int mapHeight;
    private int mapWidth;
    private List<Platform> platforms;
    private List<Collectible> collectibles;
    private TileMap tileMap;      // TileMap instance to manage the tile-based background
    private Player player;        // Player instance for the game
    private int[][] mapMatrix;    // Matrix to represent the map layout

    // Constructor
    public GameMap(int mapHeight, int mapWidth, int[][] mapMatrix) throws Exception {
        //System.out.println("In game map constructor.");

        this.mapHeight = mapHeight;
        this.mapWidth = mapWidth;
        this.platforms = new ArrayList<>();
        this.collectibles = new ArrayList<>();
        this.mapMatrix = mapMatrix;

        // Initialize TileMap with the tileset image path and map matrix
        this.tileMap = new TileMap("./assets/tileset.png", mapMatrix);

        // Initialize Player with a unique avatarId, position can be set later
        this.player = new Player(1);  // avatarId of 1 for example
    }

    // Add a platform to the map
    public void addPlatform(Platform platform) {
        platforms.add(platform);
        System.out.println("Platform added at position: " + platform.getXPos() + ", " + platform.getYPos());
    }

    // Add a collectible to the map
    public void addCollectible(Collectible collectible) {
        collectibles.add(collectible);
        System.out.println("Collectible added at position: " + collectible.getXPos() + ", " + collectible.getYPos());
    }

    // Generate map layout with platforms and collectibles
    public void generateMap() {
        System.out.println("Generating map with dimensions: " + mapWidth + " x " + mapHeight);
        platforms.clear();
        collectibles.clear();

        Random rand = new Random();

        // Generate platforms based on random positions or other logic
        int numPlatforms = 10;  // Number of platforms (example)
        double scaleFactor = 1.5;  // Example scale factor for platform size
        for (int i = 0; i < numPlatforms; i++) {
            int x = rand.nextInt(mapWidth - 100);
            int y = rand.nextInt(mapHeight - 100);
            Platform platform = new Platform(x, y, scaleFactor);
            addPlatform(platform);
        }

        // Generate collectibles based on random positions or other logic
        int numCollectibles = 5;  // Number of collectibles (example)
        for (int i = 0; i < numCollectibles; i++) {
            int x = rand.nextInt(mapWidth - 100);
            int y = rand.nextInt(mapHeight - 100);
            Collectible collectible = new Collectible(x, y);  // Example effect
            addCollectible(collectible);
        }
    }

    // Render the entire map, including tile map, platforms, and collectibles
    public void renderMap(Graphics g) {
        // Render the tile map background
        tileMap.render(g);

        // Render each platform
        for (Platform platform : platforms) {
            platform.render(g);
        }

        // Render each collectible
        for (Collectible collectible : collectibles) {
            collectible.render(g);
        }
    }

    // Reset and regenerate the map
    public void resetMap() {
        System.out.println("Resetting map...");
        generateMap();
    }

    // Getters for map dimensions and elements
    public int getMapHeight() {
        return mapHeight;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public List<Platform> getPlatforms() {
        return platforms;
    }

    public List<Collectible> getCollectibles() {
        return collectibles;
    }

    public TileMap getTileMap() {
        return tileMap;
    }

    public Player getPlayer() {
        return player;
    }
}
