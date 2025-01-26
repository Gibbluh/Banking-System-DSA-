package dsa.bank.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class Login extends JFrame {
    private static final Color LABEL_COLOR = new Color(47, 48, 132);
    private static final Color BUTTON_COLOR = new Color(255, 87, 87);
    private static final Color TEXT_FIELD_COLOR = new Color(47, 48, 132);
    private static final Color PANEL_COLOR = new Color(228, 234, 246); // Updated RGB color for the rounded panel (E4EAF6)

    public Login() {
        int frameWidth = 1280;
        int frameHeight = 720;

        Font akrobatBold = loadFont("F:\\NetBeansProjects\\DSA Bank System\\Fonts\\Akrobat-Bold.otf").deriveFont(Font.BOLD, 50f);
        Font akrobatPlain = loadFont("F:\\NetBeansProjects\\DSA Bank System\\Fonts\\Akrobat-Regular.otf").deriveFont(Font.PLAIN, 14f);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(frameWidth, frameHeight);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);

        // Add left-side illustration
        ImageIcon imageIcon = new ImageIcon("F:\\NetBeansProjects\\Login-SignUp-java\\Design.png");
        Image scaledImage = imageIcon.getImage().getScaledInstance(720, 450, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setBounds(0, 25, 900, 600);
        add(imageLabel);

        // Create the rounded panel
        RoundedPanel loginPanel = new RoundedPanel();
        loginPanel.setLayout(new GridBagLayout());
        loginPanel.setOpaque(false);
        loginPanel.setBounds(825, 100, 355, 475);
        add(loginPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10); // Adjusted padding

        // Add title and subtitle
        JLabel titleLabel = new JLabel("WELCOME", SwingConstants.CENTER);
        titleLabel.setFont(akrobatBold);
        titleLabel.setForeground(LABEL_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel("DSA BANKING", SwingConstants.CENTER);
        subtitleLabel.setFont(akrobatPlain.deriveFont(Font.PLAIN, 18f));
        subtitleLabel.setForeground(LABEL_COLOR);
        gbc.gridy = 1;
        loginPanel.add(subtitleLabel, gbc);

        // Username label and field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(akrobatBold.deriveFont(Font.PLAIN, 18f));
        usernameLabel.setForeground(LABEL_COLOR);
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        loginPanel.add(usernameLabel, gbc);

        JTextField usernameField = new JTextField();
        usernameField.setFont(akrobatPlain);
        usernameField.setPreferredSize(new Dimension(300, 30)); // Set preferred size (length increased)
        usernameField.setBorder(BorderFactory.createLineBorder(TEXT_FIELD_COLOR, 2));
        gbc.gridy = 3;
        loginPanel.add(usernameField, gbc);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(akrobatBold.deriveFont(Font.PLAIN, 18f));
        passwordLabel.setForeground(LABEL_COLOR);
        gbc.gridy = 4;
        loginPanel.add(passwordLabel, gbc);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(akrobatPlain.deriveFont(Font.PLAIN, 25f));
        passwordField.setPreferredSize(new Dimension(300, 30)); // Set preferred size (length increased)
        passwordField.setBorder(BorderFactory.createLineBorder(TEXT_FIELD_COLOR, 2));
        gbc.gridy = 5;
        loginPanel.add(passwordField, gbc);

        // Sign in button
        JButton signInButton = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(BUTTON_COLOR);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        signInButton.setForeground(Color.WHITE);
        signInButton.setFont(akrobatPlain);
        signInButton.setBorderPainted(false);
        signInButton.setContentAreaFilled(false);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        loginPanel.add(signInButton, gbc);

        signInButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            if (validateCredentials(username, password)) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                new Home(username).setVisible(true); // Open Home window
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Sign up button
        JButton signUpButton = new JButton("SIGN UP");
        signUpButton.setFont(akrobatPlain);
        signUpButton.setContentAreaFilled(false);
        signUpButton.setBorderPainted(false);
        signUpButton.setForeground(new Color(55, 55, 255));
        gbc.gridy = 7;
        loginPanel.add(signUpButton, gbc);

        signUpButton.addActionListener(e -> {
            new Signup().setVisible(true);
            dispose();
        });

        setLocationRelativeTo(null); // Center the frame
        setVisible(true); // Show the window
    }

    private boolean validateCredentials(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Username: " + username)) {
                    String userPassword = reader.readLine(); // Read the next line for password
                    if (userPassword != null && userPassword.contains("Password: " + password)) {
                        return true; // Valid credentials
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log the error for debugging
        }
        return false; // Invalid credentials
    }

    private Font loadFont(String fontPath) {
        try {
            File fontFile = new File(fontPath);
            FileInputStream fis = new FileInputStream(fontFile);
            return Font.createFont(Font.TRUETYPE_FONT, fis);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, 14); // Fallback font
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login());
    }

    // Custom JPanel with rounded corners
    class RoundedPanel extends JPanel {
        private int cornerRadius = 30; // Adjust the corner radius as needed

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(PANEL_COLOR);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        }
    }
}