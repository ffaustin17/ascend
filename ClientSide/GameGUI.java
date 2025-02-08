package ClientSide;

import CoreGame.GameController;
import CoreGame.GameMap;
import CoreGame.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class GameGUI extends JFrame{
    // Constructor
    private String serverIp;
    private final String CONFIG_FILE = "./resources/clientConfig.txt";

    public GameGUI() throws Exception {
        // set up the game client
        this.serverIp = loadServerIp();

        GameClient client = new GameClient(serverIp, 12345);

        try {
            client.openConnection();
        }catch(IOException e) {
            e.printStackTrace();
        }

        // set title and default close operation
        this.setTitle("Ascend");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create the card layout container.
        CardLayout cardLayout = new CardLayout();
        JPanel container = new JPanel(cardLayout);

        // create the controllers
        InitialControl ic = new InitialControl(container, client);
        LoginControl lc = new LoginControl(container, client);
        CreateAccountControl cac = new CreateAccountControl(container,client);
        HostOrJoinGameControl hj = new HostOrJoinGameControl(container,client);
        HostControl hc = new HostControl(container, client);
        JoinControl jc = new JoinControl(container, client);
        GameController gc = new GameController(container, client);
        EndControl ec = new EndControl(container, client);

        // set the controllers that will be communicating with the client in the client
        client.setLoginControl(lc);
        client.setCreateAccountControl(cac);
        client.setHostOrJoinControl(hj);
        client.setGameController(gc);
        client.setJoinControl(jc);
        client.setHostControl(hc);
        //cleint.setEndControl(ec);

        // open connection to server
        //the client should automatically pass the map it will have created after a successful connection to the server to the gameController

        // link the different panels/views of the app/GUI to their controllers
        JPanel view1 = new InitialPanel(ic);
        JPanel view2 = new LoginPanel(lc);
        JPanel view3 = new CreateAccountPanel(cac);
        JPanel view4 = new HostOrJoinGamePanel(hj);
        JPanel view5 = new HostPanel(hc);
        JPanel view6 = new JoinPanel(jc);
        JPanel view7 = new GamePanel(gc);
        JPanel view8 = new EndPanel(ec);

        while(true) {
            if(client.isConnectionSetUpOver()) {
                //System.out.println("set up is over");
                client.createMap();
                break;
            }
            //System.out.println("In loop");
        }

        // add the views to the card layout container.
        container.add(view1, "1");
        container.add(view2, "2");
        container.add(view3, "3");
        container.add(view4, "4");
        container.add(view5, "5");
        container.add(view6, "6");
        container.add(view7,"7");
        container.add(view8, "8");

        // show the initial view in the card layout.
        cardLayout.show(container, "1");

        // add the card layout container to the JFrame.
        this.add(container);

        // show the JFrame.
        this.setSize(1080, 624);
        //this.setResizable(false);
        this.setVisible(true);
        this.setLocationRelativeTo(null);


        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    client.sendToServer("I'm done");
                    client.closeConnection(); // Ensure the client closes the connection
                    System.out.println("Connection closed successfully.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0); // Exit the application
            }
        });
    }

    private String loadServerIp() {

        String defaultIp = "127.0.0.1"; // Default to localhost
        File configFile = new File(CONFIG_FILE); // Directly read the file

        if (!configFile.exists()) {
            System.err.println("Resource " + CONFIG_FILE + " not found. Defaulting to " + defaultIp);
            return defaultIp;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile)))) {
            String ip = reader.readLine();
            if (ip != null && isValidIp(ip.trim())) {
                return ip.trim();
            } else {
                System.err.println("Invalid or missing IP in " + CONFIG_FILE + ". Defaulting to " + defaultIp);
            }
        } catch (IOException e) {
            System.err.println("Failed to read " + CONFIG_FILE + ". Defaulting to " + defaultIp);
        }
        return defaultIp;
    }

    private boolean isValidIp(String ip) {
        // Regex pattern for validating IPv4 addresses
        String ipv4Pattern =
                "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        return Pattern.matches(ipv4Pattern, ip);
    }

    public static void main(String[] args){
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                new GameGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
