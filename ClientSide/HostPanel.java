package ClientSide;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.InetAddress;

import javax.imageio.ImageIO;
import javax.swing.*;

public class HostPanel extends JPanel {

    private double scaleF = 1.5;
    private BufferedImage background;
    private JButton cancel;

    private Color purpleD = new Color(59, 5, 58);

    private String uniqueSessionPassword;
    private JLabel hostPasswordLabel;

    public HostPanel(HostControl hc) throws IOException {
        this.setPreferredSize(new Dimension(1080,624));
        this.setLayout(new GridBagLayout());

        // get original image
        BufferedImage original_background_image = ImageIO.read(new File("./assets/background_3.png"));
        BufferedImage original_cancel_image = ImageIO.read(new File("./assets/cancel_button.png"));

        // get re-scaled width and height
        int newWidthB = (int) (original_background_image.getWidth() * scaleF);
        int newHeightB = (int) (original_background_image.getHeight() * scaleF);
        int newWidthC = (int) (original_cancel_image.getWidth() * scaleF);
        int newHeightC = (int) (original_cancel_image.getHeight() * scaleF);

        // resizing
        background = new BufferedImage(newWidthB, newHeightB, BufferedImage.TYPE_INT_ARGB);
        BufferedImage cancelButtonImage = new BufferedImage(newWidthC, newHeightC, BufferedImage.TYPE_INT_ARGB);

        // draw BACKGROUND!!!
        Graphics2D g2b = background.createGraphics();
        g2b.drawImage(original_background_image, 0, 0, newWidthB, newHeightB, null);
        g2b.dispose();

        // draw CREATE
        Graphics2D g2c = cancelButtonImage.createGraphics();
        g2c.drawImage(original_cancel_image, 0, 0, newWidthC, newHeightC, null);
        g2c.dispose();

        // ---------------------------------------------------

        cancel = createCustomButton(cancelButtonImage);

        cancel.setActionCommand("Cancel");

        cancel.addActionListener(hc);

        this.add(cancel);

        // ---------------------------------------------------


        JPanel labelPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        String user_ip = InetAddress.getLocalHost().getHostAddress();

        // ip label
        JLabel ip_label = new JLabel();

        hostPasswordLabel = new JLabel();
        hostPasswordLabel.setForeground(purpleD);
        hostPasswordLabel.setFont(new Font("Arial", Font.BOLD, 48));

        ip_label.setForeground(purpleD);
        ip_label.setFont(new Font("Arial", Font.BOLD, 48));
        // info label
        JLabel infoLabel = new JLabel("Waiting for another player.",JLabel.CENTER);
        infoLabel.setForeground(purpleD);

        labelPanel.setOpaque(false);

        labelPanel.add(hostPasswordLabel);
        labelPanel.add(infoLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(-110, 0, 0, 0); // Adjust top inset to lower the grid panel

        this.add(labelPanel, gbc);

    }

    private JButton createCustomButton(BufferedImage buttonImage) {
        // Create a new button with an icon
        JButton button = new JButton(new ImageIcon(buttonImage));
        button.setOpaque(false); // Make the button background transparent
        button.setContentAreaFilled(false); // Don't fill the button area with a color
        button.setBorderPainted(false); // Remove the border
        return button;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background (fill the panel with the background image)
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        if (cancel != null) {
            int cancelX = (getWidth() - cancel.getWidth()) / 2;
            int cancelY = ((getHeight() - cancel.getHeight()) / 2) + 50;
            cancel.setBounds(cancelX, cancelY, cancel.getWidth(), cancel.getHeight());
        }

    }

    //is called by the server
    public void setUniqueSessionPassword(String password){
        this.hostPasswordLabel.setText(password);
    }

}
