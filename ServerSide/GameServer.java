package ServerSide;

import CoreGame.*;
import ClientSide.*;

import ocsf.server.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

public class GameServer extends AbstractServer {
    //private TileMap tileMap; // Map data
    HashMap<Integer, Player> players;

    private boolean spikesVisible = false;
    private static final int SPIKE_TOGGLE_INTERVAL = 2000; // Time in milliseconds
    private Timer spikeTimer;

    private ArrayList<Collectible> collectibles;
    private ArrayList<Platform> platforms;
    private ArrayList<Obstacle> spikes;

    private String hostSessionPassword;
    private HashMap<Long, String> hostInformation;

    private Database database;

    //------------constructor---------------------------------------
    public GameServer(int port) {
        super(port);
        setTimeout(1000);
        startSpikeTimer();

        players = new HashMap();
        collectibles = new ArrayList<>();
        platforms = new ArrayList<>();
        spikes = new ArrayList<>();

        hostSessionPassword = "";

        generatePlatformsAndTraps(900, 216, 1848, 80, 66, 15, platforms);

    }


    //setter for the database
    public void setDatabase(Database database) {
        this.database = database;
    }


    @Override
    protected void clientConnected(ConnectionToClient client) {
        System.out.println("A new client has connected");
        System.out.println("New Client ID: " + client.getId());

        //System.out.println(client);
        //System.out.println(client.getId());

        client.getInfo("");
        System.out.println("Sending Map objects to client...");

        String platformString;
        for (Platform platform : platforms) {
            platformString = platform.toString();
            try {
                client.sendToClient(platformString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("\tAll platforms sent...");

        String spikeString;
        for (Obstacle spike : spikes) {
            spikeString = spike.toString();
            try {
                client.sendToClient(spikeString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("\tAll spikes sent...");
        String collectibleString;
        for (Collectible collectible : collectibles) {
            collectibleString = collectible.toString();
            try {
                //System.out.println("Sending collectible to client: " + collectibleString);
                client.sendToClient(collectibleString);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("\tAll collectibles sent.");

        System.out.println("Successfully sent initializing data to client.");
        //send a message to the client to let it know that we are done sending the initial setup information
        try {
            client.sendToClient("Initialization done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        System.out.println("client disconnected");
        sendToAllClients("Player disconnected");
    }

    public void serverStarted() {
        System.out.println("Server started on port " + this.getPort());
    }

    private void sendUpdateToClients() {
        synchronized (players) {
            sendToAllClients(players);
        }
    }


    @Override
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) {

        //if (msg instanceof JoinData) {
            //JoinData data = (JoinData) msg;
            //System.out.println("User joined with IP: " + data.getHostPassword());

            //sendToAllClients("START GAME");
        //}

        if (msg.equals("I'm done")){
            sendToAllClients("Player disconnected");
            System.out.println("Client " + client.getId() + " disconnected.");
        }

        if (msg instanceof String) {

            //System.out.println("got a string");
            String message = (String) msg;

            if (message.equals("HOST_REQUEST")) {
                try {
                    client.sendToClient(respondToHostRequest());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (message.startsWith("JOIN_REQUEST")) {
                System.out.println("Got a join request");

                String[] fields = message.split("#");
                String response = respondToJoinRequest(fields[1]);

                if (response.startsWith("GAME_START")) {
                    String suffix;

                    for (Thread clientThread : getClientConnections()) {
                        ConnectionToClient tempClient = (ConnectionToClient) clientThread;

                        // Skip the sender
                        if (tempClient != client) {
                            suffix = "_JOIN";

                            try {
                                tempClient.sendToClient(response + suffix);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } // Send the message to other clients
                        } else {
                            suffix = "_HOST";
                            try {
                                tempClient.sendToClient(response + suffix);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }
                } else {
                    try {
                        client.sendToClient(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (message.startsWith("PlayerId")) {
                Player tempPlayer = fromString(message);
                //System.out.println("Temp player created from string: " + tempPlayer);
                if (players.containsKey(tempPlayer.getId())) {
                    //System.out.println("Client " + client.getId() + " moved");
                    //System.out.println("Client " + client.getId() + " new postions: " + tempPlayer.getPos());
                    for (Thread clientThread : getClientConnections()) {
                        ConnectionToClient tempClient = (ConnectionToClient) clientThread;

                        // Skip the sender
                        if (tempClient != client) {
                            try {
                                tempClient.sendToClient((String) msg);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } // Send the message to other clients
                        }

                    }
                } else {
                    Player newPlayer = fromString(message);
                    players.put(newPlayer.getId(), newPlayer);
                    System.out.println("New player added: " + newPlayer);
                    //System.out.println("Number of players: " + players.size());
                    System.out.println("Player iD: " + newPlayer.getId());
                    //System.out.println("Player x pos: " + newPlayer.getXPos());
                    //System.out.println("Player y pos: " + newPlayer.getYPos());
                    //System.out.println("Current sprite of new player: " + newPlayer.getCurrentPlayerSprite());


                    //For sending the new player info about all of the existing players
                    for (Player existingPlayer : players.values()) {
                        if (existingPlayer.getId() != newPlayer.getId()) {
                            try {
                                client.sendToClient(message);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                    }


                    //For sending all of the other players info on the new player
                    for (Thread clientThread : getClientConnections()) {
                        ConnectionToClient tempClient = (ConnectionToClient) clientThread;

                        // Skip the sender
                        if (tempClient != client) {
                            try {
                                tempClient.sendToClient(message);
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } // Send the message to other clients
                        }

                    }
                }
            }

            try {

                if (message.startsWith("LoginData")) {

                    String[] fields = message.split("#");
                    String password = fields[1];
                    String username = fields[2];

                    boolean loginSuccess = database.verifyLogin(username, password);
                    if (loginSuccess) {
                        client.sendToClient("LoginSuccessful");
                        System.out.println("Client " + client.getId() + " successfully logged in as " + username + "\n");
                    } else {
                        String error = "Invalid username or password";
                        client.sendToClient(error);
                        System.out.println("Client " + client.getId() + " failed to log in\n");
                    }
                }


                if (message.startsWith("CreateAccountData")){

                    String[] fields = message.split("#");
                    String password = fields[1];
                    String username = fields[2];


                    boolean accountCreated = database.createAccount(username, password);
                    if (accountCreated) {
                        client.sendToClient("CreateAccountSuccessful");
                        System.out.println("Client " + client.getId() + " created a new account as " + username + "\n");
                    } else {
                        String error = "Username has already been selected";
                        client.sendToClient(error);
                        System.out.println("Client " + client.getId() + " failed to create a new account\n");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if(msg.equals("message")) {
            System.out.println("Client " + client.getId() + " moved");
        }
    }




    @Override
    protected void serverStopped() {
        System.out.println("Server Stopped");
    }

    public void listeningException(Throwable exception) {
        System.out.println("Listening Exception Occurred: ");
        System.out.println(exception.getMessage() + "\n");

    }

    @Override
    protected void serverClosed() {
        System.out.println("The server has been completely shut down.");
    }

    public static Player fromString(String playerString) {
        String[] fields = playerString.split(",");
        int id = -1, xPos = 0, yPos = 0, xSpeed = 0, ySpeed = 0, avatarId = 1, characterHeight = 32, characterWidth = 32;
        boolean inAir = false, onPlatform = false, isMoving = false, facingLeft = false, movingLeft = false, movingRight = false, staggered = false;
        String avatarType = "", animationFilePath = "", PLATFORM_IMAGE_PATH = "/bear_idle.png";

        // Parse fields
        for (String field : fields) {
            String[] keyValue = field.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            switch (key) {
                case "PlayerId": id = Integer.parseInt(value); break;
                case "xPos": xPos = Integer.parseInt(value); break;
                case "yPos": yPos = Integer.parseInt(value); break;
                case "xSpeed": xSpeed = Integer.parseInt(value); break;
                case "ySpeed": ySpeed = Integer.parseInt(value); break;
                case "avatarId": avatarId = Integer.parseInt(value); break;
                case "characterHeight": characterHeight = Integer.parseInt(value); break;
                case "characterWidth": characterWidth = Integer.parseInt(value); break;
                case "inAir": inAir = Boolean.parseBoolean(value); break;
                case "onPlatform": onPlatform = Boolean.parseBoolean(value); break;
                case "isMoving": isMoving = Boolean.parseBoolean(value); break;
                case "facingLeft": facingLeft = Boolean.parseBoolean(value); break;
                case "movingLeft": movingLeft = Boolean.parseBoolean(value); break;
                case "movingRight": movingRight = Boolean.parseBoolean(value); break;
                case "isStaggered": staggered = Boolean.parseBoolean(value); break;
                // case "avatarType": avatarType = value; break;
                // case "animationFilePath": animationFilePath = value; break;
                // case "PLATFORM_IMAGE_PATH": PLATFORM_IMAGE_PATH = value; break;
            }
        }

        // Create a new Player with the parsed data
        Player player = new Player(avatarId);
        player.setId(id);
        player.setPos(xPos, yPos);
        player.setXSpeed(xSpeed);
        player.setYSpeed(ySpeed);
        player.setPlayerDimensions(characterHeight, characterWidth);
        player.setInAir(inAir);
        player.setOnPlatform(onPlatform);
        player.setMoving(isMoving);
        player.setFacingLeft(facingLeft);
        player.setMovingLeft(movingLeft);
        player.setMovingRight(movingRight);
        player.setStaggered(staggered);

        return player;
    }

    private void startSpikeTimer() {
        spikeTimer = new Timer(true); // Daemon thread
        spikeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                spikesVisible = !spikesVisible;
                broadcastSpikeState();
            }
        }, 0, SPIKE_TOGGLE_INTERVAL);
    }

    private void broadcastSpikeState() {
        String spikeStateMessage = "SPIKE_STATE:" + spikesVisible;
        // Broadcast this message to all connected clients
        for (Thread clientThread : getClientConnections()) {
            ConnectionToClient tempClient = (ConnectionToClient) clientThread;

            try {
                tempClient.sendToClient(spikeStateMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }






    public void generatePlatformsAndTraps(int mapWidth, int platformXStartPos, int playerStartingYPos, int baseJumpVal, int platformWidth, int platformHeight, ArrayList<Platform> platformsContainer) {
        Platform lastPlatform = new Platform(1080/2, 48, 1.5);

        int rowPos = playerStartingYPos - baseJumpVal;

        int firstTowerWallXPos = 4 * 24;
        int secondTowerWallXPos = (45 - 4) * 24;

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
        int collectibleType = 0;

        boolean collectibleOnTop = false;

        while(rowPos - platformHeight > lastPlatform.getYPos()) {
            for(int i = 0; i < numPlatformsPerRow; i++) {
                platformsContainer.add(new Platform(currPlatformXPos, rowPos, 1.5));
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
                    collectibleType = random.nextInt(10);
                    if(collectibleType % 2 == 0) {
                        //System.out.println("boost collectible");
                        collectibles.add(new BoostCollectible(currPlatformXPos + 15, rowPos - 32));
                    }
                    else{
                        //System.out.println("freeze collectible");
                        collectibles.add(new FreezeCollectible(currPlatformXPos + 15, rowPos - 32));
                    }
                }

                if(hasSpikes){
                    spikePlacement = random.nextInt(3);
                    spikes.add(new Obstacle(currPlatformXPos + (spikePlacement * 24), rowPos-24, "spikes", 1.5));
                }
                else{

                }

                currPlatformXPos += (platformWidth +horizontalGap);
            }

            rowPos -= (48+baseJumpVal-25);
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

        platforms.add(lastPlatform);

        System.out.println("Generated collectibles...");
        System.out.println("Generated platforms...");
        System.out.println("Generated spikes...");
    }


    //this method is called whenever a HOST_REQUEST is sent from the client.
    //this method determines whether to grant host permission to this client
    //by checking if there is already a hostSessionPassword stored in the server,
    //which would mean that there is already a host. In that case, the request will be
    //denied. Otherwise, the request will be granted. Note that the response is returned in the form of a String
    private String respondToHostRequest(){
        String response;
        final String DELIMITER = "#";

        if(hostSessionPassword.isEmpty()){
            //grant permission

            //generate session password
            generateSessionPassword();

            //return the granted permission to the requesting client
            response = "HOST_PERMISSION_GRANTED" + DELIMITER + hostSessionPassword;
        }
        else{ //there is already a client currently hosting
            response = "HOST_PERMISSION_DENIED" + DELIMITER + "There is already a host. Please try joining instead or try again later.";
            //return the denied permission to the requesting client
        }

        return response;
    }

    private void generateSessionPassword(){
        final String PREFIX = "UCA";
        final String DELIMITER = "-";

        Random random = new Random();

        int middle_section = random.nextInt(1000);
        int final_section = random.nextInt(1000);

        StringBuilder middle = new StringBuilder(Integer.toString(middle_section));
        StringBuilder last = new StringBuilder(Integer.toString(final_section));

        while(middle.length() < 3 && last.length() < 3) {
            if (middle.length() < 3) {
                middle.insert(0, "0");
            }

            if(last.length() < 3){
                last.insert(0, "0");
            }
        }

        hostSessionPassword = (PREFIX + DELIMITER + middle + DELIMITER + last);

    }

    private String respondToJoinRequest(String providedPassword){
        String response;
        if(hostSessionPassword.isEmpty()){
            response = "JOINT_PERMISSION_DENIED" + "#" + "There are currently no hosts for the session. Please try hosting instead or try again later.";
        }
        else{
            if(providedPassword.equals(hostSessionPassword)){
                response = "GAME_START";
            }
            else{
                response = "JOIN_PERMISSION_DENIED" + "#" + "Invalid Password";
            }
        }

        return response;
    }

    private String respondToCreateAccountData(String Password, String Username){
        return null;
    }

        //---------------------main--------------------------------------------------
    public static void printServerBanner()
    {

     String firstLine    = "     _    ____   ____ _____ _   _ ____  ";
     String secondLine   = "    / \\  / ___| / ___| ____| \\ | |  _ \\ ";
     String thirdLine    = "   / _ \\ \\___ \\| |   |  _| |  \\| | | | |";
     String fourthLine   = "  / ___ \\ ___) | |___| |___| |\\  | |_| |";
     String fifthLine    = " /_/   \\_\\____/ \\____|_____|_| \\_|____/ ";
     String sixthLine    = "";
     String subTitleLine = "             Server Program";

     String startOfLines = "\t\t\t";

     ArrayList<String> banner = new ArrayList<>(List.of(firstLine, secondLine, thirdLine, fourthLine, fifthLine, sixthLine, subTitleLine));


     for(int i = 0; i < 7; i++){
         System.out.println(banner.get(i));
     }

     System.out.println();
     System.out.println();

    }

    public static void main(String[] args) {
        int port = 12345; // Change port as needed

        printServerBanner();

        GameServer server = new GameServer(port);


        //create the database instance
        Database database = new Database();
        server.setDatabase(database);



        try {
            server.listen(); // Start the server
            //System.out.println("Server is running on port " + port);
        } catch (IOException e) {
            System.out.println("Failed to start the server.");
            e.printStackTrace();
        }


        
    }
}
