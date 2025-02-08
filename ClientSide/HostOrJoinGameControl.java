package ClientSide;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Random;

public class HostOrJoinGameControl implements ActionListener {

    private JPanel container;
    private GameClient client;
    private String sessionUniquePassword;

    public HostOrJoinGameControl(JPanel container, GameClient client) {
        this.container = container;
        this.client = client;
    }

    public void actionPerformed(ActionEvent ae) {

        // get the name of the button clicked
        String command = ae.getActionCommand();

        if (command.equals("Logout")) {
            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "1");
        }
        else if (command.equals("Host")) {

            //share this session id to the server so that the server knows that someone is hosting a game
            try {
                client.sendToServer("HOST_REQUEST");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //wait for response from server either accepting this session id and granting permission to host
            //or refusing the host request because the session already has a host.
        }
        else if (command.equals("Join")) {
            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "6");
        }

    }


    public void hostRequestGranted(String hostUniquePassword){
        HostPanel hostPanel = (HostPanel)container.getComponent(4);
        hostPanel.setUniqueSessionPassword(hostUniquePassword);

        CardLayout cardLayout = (CardLayout)container.getLayout();
        cardLayout.show(container, "5");
    }

    public void hostRequestDenied(String deny_message){
        HostOrJoinGamePanel hostOrJoinGamePanel = (HostOrJoinGamePanel)container.getComponent(3);
        hostOrJoinGamePanel.setError(deny_message);
    }

}