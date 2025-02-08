package ClientSide;

import CoreGame.GamePanel;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class InitialControl implements ActionListener{

    // Fields
    private JPanel container;
    private GameClient client;

    // Constructor
    public InitialControl(JPanel container, GameClient client) {
        this.container = container;
        this.client = client;
    }

    // Methods
    public void actionPerformed(ActionEvent ae){
        // get name of the button clicked
        String command = ae.getActionCommand();

        // the login button takes the user to the login panel
        if (command.equals("Login")){
            LoginPanel loginPanel = (LoginPanel)container.getComponent(1);
            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "2");
        }
        // the create button takes the user to the create account panel
        else if (command.equals("Create")){
            CreateAccountPanel createAccountPanel = (CreateAccountPanel)container.getComponent(2);
            CardLayout cardLayout = (CardLayout)container.getLayout();
            cardLayout.show(container, "3");
        }
//        else if (command.equals("test")){
//            HostOrJoinGamePanel hostOrJoinGamePanel = (HostOrJoinGamePanel)container.getComponent(3);
//            CardLayout cardLayout = (CardLayout)container.getLayout();
//            cardLayout.show(container, "4");
//        }
    }

    /*
    public void handleLoginAction() {
        // Logic to handle the login action
        System.out.println("Handling login action.");
        // Implement further login action handling as needed
    }

    public void handleCreateAccountAction() {
        // Logic to handle the create account action
        System.out.println("Handling create account action.");
        // Implement further create account action handling as needed
    }
     */
}
