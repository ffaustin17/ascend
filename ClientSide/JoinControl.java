package ClientSide;

import CoreGame.GameController;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class JoinControl implements ActionListener {

    private JPanel container;
    private GameClient client;

    public JoinControl(JPanel container, GameClient client) {
        this.container = container;
        this.client = client;
    }

    public void actionPerformed(ActionEvent ae) {

        // get the name of the button clicked
        String command = ae.getActionCommand();

        if (command.equals("Submit")) {
//            System.out.println("ip submitted!");

            JoinData data = new JoinData(JoinPanel.getHostPassword());

           // Check the validity of the information locally first.
           if (data.getHostPassword().equals(""))
           {
               displayError("You must enter Host Password.");
                return;
           }
//            else if (!isValidIP(data.getIp())){
//                displayError("IP address is not valid.");
//                return;
//            }

            try
            {
                client.sendToServer("JOIN_REQUEST" + "#" + JoinPanel.getHostPassword());
            }
            catch (IOException e)
            {
                displayError("Error connecting to the server.");
            }

        }
        else if (command.equals("Cancel")) {
            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "4");
        }

    }

    public void displayError(String error)
    {
        JoinPanel joinPanel = (JoinPanel)container.getComponent(5);
        joinPanel.setError(error);
    }

    public static boolean isValidIP(String ip) {
        try {
            InetAddress address = InetAddress.getByName(ip);
            return ip.equals(address.getHostAddress());
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public void gameStart(){
        CardLayout cardLayout = (CardLayout)container.getLayout();
        cardLayout.show(container, "7");

        GameController.startGameTimer();
    }

}
