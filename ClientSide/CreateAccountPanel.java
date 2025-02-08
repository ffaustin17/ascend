package ClientSide;

import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

public class CreateAccountPanel extends JPanel {
    // Private data fields for the important GUI components.
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField passwordVerifyField;
    private JLabel errorLabel;

    private double scaleF = 1.5;
    private BufferedImage background;
    private JButton submit;
    private JButton cancel;

    private Color purpleD = new Color(59, 5, 58);
    private Color purpleL = new Color(152, 129, 137);

    // Getter for the text in the username field.
    public String getUsername() {
        return usernameField.getText();
    }

    // Getter for the text in the password field.
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    // Getter for the text in the second password field.
    public String getPasswordVerify() {
        return new String(passwordVerifyField.getPassword());
    }

    // Setter for the error text.
    public void setError(String error) {
        errorLabel.setText(error);
    }

    // Constructor for the create account panel.
    public CreateAccountPanel(CreateAccountControl cac) throws IOException{

        this.setPreferredSize(new Dimension(1080,624));
        this.setLayout(new GridBagLayout());


        // get original images
        BufferedImage original_background_image = ImageIO.read(new File("./assets/background_2.png"));
        BufferedImage original_submit_image = ImageIO.read(new File("./assets/submit_button.png"));
        BufferedImage original_cancel_image = ImageIO.read(new File("./assets/cancel_button.png"));

        // get re-scaled width and height
        int newWidthB = (int) (original_background_image.getWidth() * scaleF);
        int newHeightB = (int) (original_background_image.getHeight() * scaleF);
        int newWidthS = (int) (original_submit_image.getWidth() * scaleF);
        int newHeightS = (int) (original_submit_image.getHeight() * scaleF);
        int newWidthC = (int) (original_cancel_image.getWidth() * scaleF);
        int newHeightC = (int) (original_cancel_image.getHeight() * scaleF);

        // resizing
        background = new BufferedImage(newWidthB, newHeightB, BufferedImage.TYPE_INT_ARGB);
        BufferedImage submitButton = new BufferedImage(newWidthS, newHeightS, BufferedImage.TYPE_INT_ARGB);
        BufferedImage cancelButton = new BufferedImage(newWidthC, newHeightC, BufferedImage.TYPE_INT_ARGB);

        // draw BACKGROUND!!!
        Graphics2D g2b = background.createGraphics();
        g2b.drawImage(original_background_image, 0, 0, newWidthB, newHeightB, null);
        g2b.dispose();

        // draw SUBMIT
        Graphics2D g2s = submitButton.createGraphics();
        g2s.drawImage(original_submit_image, 0, 0, newWidthS, newHeightS, null);
        g2s.dispose();

        // draw CANCEL
        Graphics2D g2c = cancelButton.createGraphics();
        g2c.drawImage(original_cancel_image, 0, 0, newWidthC, newHeightC, null);
        g2c.dispose();

        // ---------------------------------------------------

        // Create buttons with resized images
        submit = createCustomButton(submitButton);
        cancel = createCustomButton(cancelButton);

        submit.setActionCommand("Submit");
        cancel.setActionCommand("Cancel");

        submit.addActionListener(cac);
        cancel.addActionListener(cac);

        this.add(submit);
        this.add(cancel);

        // ---------------------------------------------------


        // Create a panel for the labels at the top of the GUI.
        JPanel labelPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        JLabel instructionLabel = new JLabel("Enter a username and password to create an account.", JLabel.CENTER);
        JLabel instructionLabel2 = new JLabel("Your password must be at least 6 characters.", JLabel.CENTER);
        instructionLabel.setForeground(purpleD);
        instructionLabel2.setForeground(purpleD);
        labelPanel.add(errorLabel);
        labelPanel.add(instructionLabel);
        labelPanel.add(instructionLabel2);
        labelPanel.setOpaque(false);

        // Create a panel for the account information form.
        JPanel accountPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        JLabel usernameLabel = new JLabel("Username:", JLabel.RIGHT);
        usernameLabel.setForeground(purpleD);
        usernameField = new JTextField(10);
        usernameField.setBackground(purpleL);
        usernameField.setBorder(BorderFactory.createLineBorder(purpleL, 1));
        usernameField.setForeground(purpleD);
        JLabel passwordLabel = new JLabel("Password:", JLabel.RIGHT);
        passwordLabel.setForeground(purpleD);
        passwordField = new JPasswordField(10);
        passwordField.setBackground(purpleL);
        passwordField.setBorder(BorderFactory.createLineBorder(purpleL, 1));
        passwordField.setForeground(purpleD);
        JLabel passwordVerifyLabel = new JLabel("Verify Password:", JLabel.RIGHT);
        passwordVerifyLabel.setForeground(purpleD);
        passwordVerifyField = new JPasswordField(10);
        passwordVerifyField.setBackground(purpleL);
        passwordVerifyField.setBorder(BorderFactory.createLineBorder(purpleL, 1));
        passwordVerifyField.setForeground(purpleD);
        accountPanel.add(usernameLabel);
        accountPanel.add(usernameField);
        accountPanel.add(passwordLabel);
        accountPanel.add(passwordField);
        accountPanel.add(passwordVerifyLabel);
        accountPanel.add(passwordVerifyField);
        accountPanel.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(3, 1, 0, 10));
        grid.add(labelPanel);
        grid.add(accountPanel);
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(-100, 200, 0, 0); // Adjust top inset to lower the grid panel

        this.add(grid, gbc);

    }

    private JButton createCustomButton(BufferedImage buttonImage) {
        // Create a new button with an icon
        JButton button = new JButton(new ImageIcon(buttonImage));
        button.setOpaque(false); // Make the button background transparent
        button.setContentAreaFilled(false); // Don't fill the button area with a color
        button.setBorderPainted(false); // Remove the border
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background (fill the panel with the background image)
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        // Set button positions after the panel is rendered
        if (submit != null && cancel != null) {
            // Center the login button
            int submitX = ((getWidth() - submit.getWidth()) / 2 ) - 90;
            int submitY = ((getHeight() - submit.getHeight()) / 2) + 90;
            submit.setBounds(submitX, submitY, submit.getWidth(), submit.getHeight());

            // Position the create button below the login button
            int cancelX = ((getWidth() - cancel.getWidth()) / 2 ) + 90;
            int cancelY = ((getHeight() - cancel.getHeight()) / 2) + 90;
            cancel.setBounds(cancelX, cancelY, cancel.getWidth(), cancel.getHeight());
        }
    }
}