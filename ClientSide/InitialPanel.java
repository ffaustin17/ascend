package ClientSide;

import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class InitialPanel extends JPanel {

    private double scaleF = 1.5;

    private BufferedImage background;
    private BufferedImage title;

    private JButton login;
    private JButton create;

    // Constructor for the initial panel.
    public InitialPanel(InitialControl ic) throws IOException {
        this.setPreferredSize(new Dimension(1080, 624));

        // get original images
        BufferedImage original_background_image = ImageIO.read(new File("./assets/background.png"));
        BufferedImage original_title_image = ImageIO.read(new File("./assets/ASCEND.png"));
        BufferedImage original_login_image = ImageIO.read(new File("./assets/login_button.png"));
        BufferedImage original_create_image = ImageIO.read(new File("./assets/create_button.png"));

        // get re-scaled width and height
        int newWidthB = (int) (original_background_image.getWidth() * scaleF);
        int newHeightB = (int) (original_background_image.getHeight() * scaleF);
        int newWidthT = (int) (original_title_image.getWidth() * scaleF);
        int newHeightT = (int) (original_title_image.getHeight() * scaleF);
        int newWidth_button = (int) (original_login_image.getWidth() * scaleF);
        int newHeight_button = (int) (original_login_image.getHeight() * scaleF);

        // Resizing
        background = new BufferedImage(newWidthB, newHeightB, BufferedImage.TYPE_INT_ARGB);
        title = new BufferedImage(newWidthT, newHeightT, BufferedImage.TYPE_INT_ARGB);
        BufferedImage loginButtonImage = new BufferedImage(newWidth_button, newHeight_button, BufferedImage.TYPE_INT_ARGB);
        BufferedImage createButtonImage = new BufferedImage(newWidth_button, newHeight_button, BufferedImage.TYPE_INT_ARGB);

        // draw BACKGROUND!!!
        Graphics2D g2b = background.createGraphics();
        g2b.drawImage(original_background_image, 0, 0, newWidthB, newHeightB, null);
        g2b.dispose();

        // draw TITLE!!!
        Graphics2D g2t = title.createGraphics();
        g2t.drawImage(original_title_image, 0, 0, newWidthT, newHeightT, null);
        g2t.dispose();

        // draw LOGIN
        Graphics2D g2l = loginButtonImage.createGraphics();
        g2l.drawImage(original_login_image, 0, 0, newWidth_button, newHeight_button, null);
        g2l.dispose();

        // draw CREATE
        Graphics2D g2c = createButtonImage.createGraphics();
        g2c.drawImage(original_create_image, 0, 0, newWidth_button, newHeight_button, null);
        g2c.dispose();

        // ---------------------------------------------------

        // Create buttons with resized images
        login = createCustomButton(loginButtonImage);
        create = createCustomButton(createButtonImage);

        login.setActionCommand("Login");
        create.setActionCommand("Create");

        login.addActionListener(ic);
        create.addActionListener(ic);

        this.add(login);
        this.add(create);

        // TEMP BUTTON
//        JButton test = new JButton("test");
//        test.setActionCommand("test");
//        test.addActionListener(ic);
//        this.add(test);

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

        // Draw title over the background (do not stretch the title)
        int titleX = (getWidth() - title.getWidth()) / 2;  // Center title horizontally
        int titleY = (getHeight() - title.getHeight()) / 4; // Center title vertically
        g.drawImage(title, titleX, titleY, this);

        // Set button positions after the panel is rendered
        if (login != null && create != null) {
            // Center the login button
            int loginX = (getWidth() - login.getWidth()) / 2;
            int loginY = (getHeight() - login.getHeight()) / 2;
            login.setBounds(loginX, loginY, login.getWidth(), login.getHeight());

            // Position the create button below the login button
            int createX = (getWidth() - create.getWidth()) / 2;
            int createY = loginY + login.getHeight();
            create.setBounds(createX, createY, create.getWidth(), create.getHeight());
        }
    }
}
