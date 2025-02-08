package ClientSide;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class JoinPanel extends JPanel {

    private double scaleF = 1.5;
    private BufferedImage background;

    private static JTextField ipField;
    private JButton submit;
    private JButton cancel;
    private JLabel errorLabel;

    private Color purpleD = new Color(59, 5, 58);
    private Color purpleL = new Color(152, 129, 137);

    public static String getHostPassword() {
        return ipField.getText();
    }

    public void setError(String error)
    {
        errorLabel.setText(error);
    }

    public JoinPanel(JoinControl jc) throws IOException {

        this.setPreferredSize(new Dimension(1080,624));
        this.setLayout(new GridBagLayout());

        // get original image
        BufferedImage original_background_image = ImageIO.read(new File("./assets/background_3.png"));
        BufferedImage original_submit_image = ImageIO.read(new File("./assets/submit_button.png"));
        BufferedImage original_cancel_image = ImageIO.read(new File("./assets/cancel_button.png"));

        // get re-scaled width and height
        int newWidthB = (int) (original_background_image.getWidth() * scaleF);
        int newHeightB = (int) (original_background_image.getHeight() * scaleF);
        int newWidth_button = (int) (original_submit_image.getWidth() * scaleF);
        int newHeight_button = (int) (original_submit_image.getHeight() * scaleF);
        // resizing
        background = new BufferedImage(newWidthB, newHeightB, BufferedImage.TYPE_INT_ARGB);
        BufferedImage submitButton = new BufferedImage(newWidth_button, newHeight_button, BufferedImage.TYPE_INT_ARGB);
        BufferedImage cancelButton = new BufferedImage(newWidth_button, newHeight_button, BufferedImage.TYPE_INT_ARGB);

        // draw BACKGROUND!!!
        Graphics2D g2b = background.createGraphics();
        g2b.drawImage(original_background_image, 0, 0, newWidthB, newHeightB, null);
        g2b.dispose();

        // draw SUBMIT
        Graphics2D g2s = submitButton.createGraphics();
        g2s.drawImage(original_submit_image, 0, 0, newWidth_button, newHeight_button, null);
        g2s.dispose();

        // draw CANCEL
        Graphics2D g2c = cancelButton.createGraphics();
        g2c.drawImage(original_cancel_image, 0, 0, newWidth_button, newHeight_button, null);
        g2c.dispose();

        // ---------------------------------------------------

        // Create buttons with resized images
        submit = createCustomButton(submitButton);
        cancel = createCustomButton(cancelButton);

        submit.setActionCommand("Submit");
        cancel.setActionCommand("Cancel");

        submit.addActionListener(jc);
        cancel.addActionListener(jc);

        this.add(submit);
        this.add(cancel);

        // ---------------------------------------------------

        JPanel labelPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        // error label
        errorLabel = new JLabel("", JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        // info label
        JLabel infoLabel = new JLabel("Enter host password.",JLabel.CENTER);
        infoLabel.setForeground(purpleD);
        // ip text area
        ipField = new JTextField(10);
        ipField.setBackground(purpleL);
        ipField.setBorder(BorderFactory.createLineBorder(purpleL, 1));
        ipField.setForeground(purpleD);
        ipField.setFont(new Font("Arial", Font.PLAIN, 32));

        labelPanel.add(errorLabel);
        labelPanel.add(infoLabel);
        labelPanel.add(ipField);
        labelPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(-170, 200, 0, 0); // Adjust top inset to lower the grid panel

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

        // Set button positions after the panel is rendered
        if (submit != null && cancel != null) {
            // Center the login button
            int submitX = ((getWidth() - submit.getWidth()) / 2 ) - 90;
            int submitY = ((getHeight() - submit.getHeight()) / 2) + 80;
            submit.setBounds(submitX, submitY, submit.getWidth(), submit.getHeight());

            int cancelX = ((getWidth() - cancel.getWidth()) / 2 ) + 90;
            int cancelY = ((getHeight() - cancel.getHeight()) / 2) + 80;
            cancel.setBounds(cancelX, cancelY, cancel.getWidth(), cancel.getHeight());
        }
    }
}
