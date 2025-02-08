package ClientSide;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

public class HostOrJoinGamePanel extends JPanel {

    private double scaleF = 1.5;
    private BufferedImage background;

    public JButton host;
    public JButton join;
    public JButton logout;
    public JLabel errorLabel;

    // constructor
    public HostOrJoinGamePanel(HostOrJoinGameControl hj) throws IOException {
        this.setPreferredSize(new Dimension(1080, 624));

        // get original images
        BufferedImage original_background_image = ImageIO.read(new File("./assets/background.png"));
        BufferedImage original_host_image = ImageIO.read(new File("./assets/host_button.png"));
        BufferedImage original_join_image = ImageIO.read(new File("./assets/join_button.png"));
        BufferedImage original_logout_image = ImageIO.read(new File("./assets/logout_button.png"));

        int newWidthB = (int) (original_background_image.getWidth() * scaleF);
        int newHeightB = (int) (original_background_image.getHeight() * scaleF);
        int newWidth_button = (int) (original_host_image.getWidth() * scaleF);
        int newHeight_button = (int) (original_host_image.getHeight() * scaleF);
        int newWidthL = (int) (original_logout_image.getWidth() * scaleF);
        int newHeightL = (int) (original_logout_image.getHeight() * scaleF);

        // Resizing
        background = new BufferedImage(newWidthB, newHeightB, BufferedImage.TYPE_INT_ARGB);
        BufferedImage hostButtonImage = new BufferedImage(newWidth_button, newHeight_button, BufferedImage.TYPE_INT_ARGB);
        BufferedImage joinButtonImage = new BufferedImage(newWidth_button, newHeight_button, BufferedImage.TYPE_INT_ARGB);
        BufferedImage logoutButtonImage = new BufferedImage(newWidthL, newHeightL, BufferedImage.TYPE_INT_ARGB);

        // draw BACKGROUND!!!
        Graphics2D g2b = background.createGraphics();
        g2b.drawImage(original_background_image, 0, 0, newWidthB, newHeightB, null);
        g2b.dispose();

        // draw HOST
        Graphics2D g2h = hostButtonImage.createGraphics();
        g2h.drawImage(original_host_image, 0, 0, newWidth_button, newHeight_button, null);
        g2h.dispose();

        // draw JOIN
        Graphics2D g2j = joinButtonImage.createGraphics();
        g2j.drawImage(original_join_image, 0, 0, newWidth_button, newHeight_button, null);
        g2j.dispose();

        // draw LOGOUT
        Graphics2D g2l = logoutButtonImage.createGraphics();
        g2l.drawImage(original_logout_image, 0, 0, newWidthL, newHeightL, null);
        g2l.dispose();

        // ---------------------------------------------------

        // error label
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);

        // Create buttons with resized images
        host = createCustomButton(hostButtonImage);
        join = createCustomButton(joinButtonImage);
        logout = createCustomButton(logoutButtonImage);

        host.setActionCommand("Host");
        join.setActionCommand("Join");
        logout.setActionCommand("Logout");

        host.addActionListener(hj);
        join.addActionListener(hj);
        logout.addActionListener(hj);

        this.add(host);
        this.add(join);
        this.add(logout);
        this.add(errorLabel);
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
        if (host != null && join != null && logout != null) {
            // Center login button
            int hostX = (getWidth() - host.getWidth()) / 2;
            int hostY = ((getHeight() - host.getHeight()) / 2) - 100;
            host.setBounds(hostX, hostY, host.getWidth(), host.getHeight());

            // Position create button below the login button
            int joinX = (getWidth() - join.getWidth()) / 2;
            int joinY = hostY + host.getHeight();
            join.setBounds(joinX, joinY, join.getWidth(), join.getHeight());

            // Position logout button below the join button
            int logoutX = (getWidth() - logout.getWidth()) / 2;
            int logoutY = joinY + join.getHeight();
            logout.setBounds(logoutX, logoutY, logout.getWidth(), logout.getHeight());
        }
    }

    public void setError(String error_message){
        this.errorLabel.setText(error_message);
    }
}
