package dsa.bank.system;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class Signup extends JFrame {
    private static final Color CUSTOM_COLOR = new Color(228, 234, 246);
    private static final Color LABEL_COLOR = new Color(47, 48, 132);

    public Signup() {
        int frameWidth = 480;
        int frameHeight = 600;

        getContentPane().setBackground(CUSTOM_COLOR);
        setSize(frameWidth, frameHeight);
        setLayout(null);

        JLabel titleLabel = new JLabel("DSA BANKING", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 50));
        titleLabel.setForeground(LABEL_COLOR);
        titleLabel.setBounds(0, 40, frameWidth, 50);
        this.add(titleLabel);

        JLabel subLabel = new JLabel("Account Registration", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subLabel.setForeground(LABEL_COLOR);
        subLabel.setBounds(0, 90, frameWidth, 20);
        this.add(subLabel);

        JLabel fullnameLabel = new JLabel("Full Name (Last, First, Middle)");
        fullnameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        fullnameLabel.setForeground(LABEL_COLOR);
        fullnameLabel.setBounds(30, 130, 300, 20);
        this.add(fullnameLabel);

        JTextField fullnameField = new JTextField();
        fullnameField.setBounds(30, 150, 395, 35);
        fullnameField.setFont(new Font("Arial", Font.PLAIN, 15));
        fullnameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LABEL_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        this.add(fullnameField);

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        usernameLabel.setForeground(LABEL_COLOR);
        usernameLabel.setBounds(30, 200, 300, 20);
        this.add(usernameLabel);

        JTextField usernameField = new JTextField();
        usernameField.setBounds(30, 220, 395, 35);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 15));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LABEL_COLOR, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        this.add(usernameField);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passwordLabel.setForeground(LABEL_COLOR);
        passwordLabel.setBounds(30, 270, 300, 20);
        this.add(passwordLabel);

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(30, 290, 395, 35);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordField.setBorder(BorderFactory.createLineBorder(LABEL_COLOR, 2));
        this.add(passwordField);

        JLabel securityPinLabel = new JLabel("Security Pin (5 digits only)");
        securityPinLabel.setFont(new Font("Arial", Font.BOLD, 15));
        securityPinLabel.setForeground(LABEL_COLOR);
        securityPinLabel.setBounds(30, 350, 300, 20);
        this.add(securityPinLabel);

        JPasswordField securityField = new JPasswordField();
        securityField.setBounds(30, 370, 395, 35);
        securityField.setFont(new Font("Arial", Font.PLAIN, 15));
        securityField.setBorder(BorderFactory.createLineBorder(LABEL_COLOR, 2));
        this.add(securityField);

        // Rounded Register Button
        JButton registerButton = new JButton("REGISTER") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(255, 102, 102)); // Button color
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); // Rounded edges
                super.paintComponent(g);
            }
        };
        registerButton.setBounds(30, 430, 395, 40);
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Arial", Font.BOLD, 15));
        registerButton.setBorderPainted(false);
        registerButton.setContentAreaFilled(false);
        this.add(registerButton);

        registerButton.addActionListener(e -> {
            String fullName = fullnameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String securityPin = new String(securityField.getPassword()).trim();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || securityPin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!securityPin.matches("\\d{5}")) {
                JOptionPane.showMessageDialog(this, "Security Pin must be exactly 5 digits.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int accountNumber = getNextAccountNumber();
            saveUser (username, password, fullName, securityPin, accountNumber);
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);

            fullnameField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            securityField.setText("");
        });

        // Transparent Go Back Button
        JButton goBackButton = new JButton("Go Back");
        goBackButton.setBounds(30, 480, 100, 40);
        goBackButton.setBackground(new Color(0, 0, 0, 0)); // Transparent background
        goBackButton.setForeground(LABEL_COLOR);
        goBackButton.setFont(new Font("Arial", Font.BOLD, 15));
        goBackButton.setBorderPainted(false); // No border
        goBackButton.setContentAreaFilled(false); // No background
        goBackButton.addActionListener(e -> {
            dispose(); // Close the signup window
            new Login().setVisible(true); // Open the login window
        });
        this.add(goBackButton);

        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private boolean isUsernameTaken(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Username: " + username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int getNextAccountNumber() {
        int maxAccountNumber = 0;
        File userFile = new File("users.txt");

        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Account Number: ")) {
                        int accountNumber = Integer.parseInt(line.split(": ")[1].trim());
                        if (accountNumber > maxAccountNumber) {
                            maxAccountNumber = accountNumber;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return maxAccountNumber + 1;
    }

    private void saveUser (String username, String password, String fullName, String securityPin, int accountNumber) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write("Account Number: " + accountNumber);
            writer.newLine();
            writer.write("Username: " + username);
            writer.newLine();
            writer.write("Password: " + password); // Note: Storing passwords in plain text is not secure
            writer.newLine();
            writer.write("Full Name: " + fullName);
            writer.newLine();
            writer.write("Security Pin: " + securityPin);
            writer.newLine();
            writer.write("----------"); // Separator for different users
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Signup signup = new Signup();
            signup.setVisible(true);
        });
    }
}