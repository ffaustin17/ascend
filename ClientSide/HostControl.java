package ClientSide;

import CoreGame.GameController;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class HostControl implements ActionListener {

    private JPanel container;
    private GameClient client;

    public HostControl(JPanel container, GameClient client) {
        this.container = container;
        this.client = client;
    }

    public void actionPerformed(ActionEvent ae) {

        // get the name of the button clicked
        String command = ae.getActionCommand();

        if (command.equals("Cancel")) {
            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "4");
        }

    }

    public void gameStart(){
        CardLayout cardLayout = (CardLayout)container.getLayout();
        cardLayout.show(container, "7");

        GameController.startGameTimer();
    }

}
