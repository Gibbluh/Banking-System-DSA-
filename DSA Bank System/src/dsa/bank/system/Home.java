package dsa.bank.system;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Home extends JFrame {
    private static final Color LABEL_COLOR = new Color(47, 48, 132);
    private static final Color BUTTON_COLOR = new Color(47, 48, 132);
    private static final Color BACKGROUND_COLOR = new Color(230, 230, 250);
    private static final int FRAME_WIDTH = 1000; // Reduced width
    private static final int FRAME_HEIGHT = 720;
    private String username;
    private double balance = 0.0;
    private DefaultTableModel transactionModel;

    public Home(String username) {
        this.username = username;

        // Initialize transaction model before loading transactions
        transactionModel = new DefaultTableModel(new Object[]{"Date", "Type", "Amount"}, 0);

        // Initialize balance from file if exists
        loadBalanceAndTransactions();

        setTitle("DSA Banking");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLayout(null);
        getContentPane().setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Top panel
        JPanel topPanel = new JPanel(null);
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBounds(0, 0, FRAME_WIDTH, 80);

        JLabel titleLabel = new JLabel("DSA Banking");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(LABEL_COLOR);
        titleLabel.setBounds(20, 20, 200, 40);
        topPanel.add(titleLabel);

        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBackground(BUTTON_COLOR);
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setOpaque(true);
        logoutButton.setBounds(FRAME_WIDTH - 120, 20, 100, 40);
        logoutButton.addActionListener(e -> saveAndLogout());
        topPanel.add(logoutButton);

        add(topPanel);

        // Balance Label
        JLabel balanceLabel = new JLabel("Balance");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 36));
        balanceLabel.setForeground(LABEL_COLOR);
        balanceLabel.setBounds(420, 140, 200, 40); // Adjusted width
        add(balanceLabel);

        // Amount label
        JLabel amountLabel = new JLabel(String.format("%.2f PHP", balance));
        amountLabel.setFont(new Font("Arial", Font.BOLD, 48)); // Larger font size
        amountLabel.setForeground(LABEL_COLOR);
        amountLabel.setBounds(400, 190, 400, 50); // Positioned below the balance label
        add(amountLabel);

        // Adjusted Deposit Button
        JButton depositButton = new JButton("Deposit");
        depositButton.setBackground(BUTTON_COLOR);
        depositButton.setForeground(Color.WHITE);
        depositButton.setBounds(200, 300, 200, 100); // Adjusted position
        depositButton.setFont(new Font("Arial", Font.BOLD, 18));
        depositButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0) {
                        String securityPin = JOptionPane.showInputDialog(this, "Enter Security Pin:");
                        if (securityPin != null && validateSecurityPin(securityPin)) {
                            balance += amount;
                            amountLabel.setText(String.format("%.2f PHP", balance)); // Update amount label
                            addTransaction("Deposit", amount);
                            saveBalanceAndTransactions();
                            JOptionPane.showMessageDialog(this, "Deposited: " + amountStr + " PHP");
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid Security Pin.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(depositButton);

        // Adjusted Withdraw Button
        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setBackground(BUTTON_COLOR);
        withdrawButton.setForeground(Color.WHITE);
        withdrawButton.setBounds(580, 300, 200, 100); // Adjusted position
        withdrawButton.setFont(new Font("Arial", Font.BOLD, 18));
        withdrawButton.addActionListener(e -> {
            String amountStr = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
            if (amountStr != null && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0 && amount <= balance) {
                        String securityPin = JOptionPane.showInputDialog(this, "Enter Security Pin:");
                        if (securityPin != null && validateSecurityPin(securityPin)) {
                            balance -= amount;
                            amountLabel.setText(String.format("%.2f PHP", balance)); // Update amount label
                            addTransaction("Withdraw", amount);
                            saveBalanceAndTransactions();
                            JOptionPane.showMessageDialog(this, "Withdrawn: " + amountStr + " PHP");
                        } else {
                            JOptionPane.showMessageDialog(this, "Invalid Security Pin.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Invalid amount or insufficient balance.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(withdrawButton);

        // Transactions table
        JTable transactionTable = new JTable(transactionModel);
        transactionTable.setFont(new Font("Arial", Font.PLAIN, 18)); // Set font size for table cells
        transactionTable.setRowHeight(30); // Set row height for better visibility

        // Set the table header font
        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 18)); // Set font size for header
        header.setForeground(Color.WHITE);
        header.setBackground(BUTTON_COLOR); // Set header background color

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BUTTON_COLOR, 2)); // Set border color
        scrollPane.setBounds(140, 440, 700, 200); // Adjusted size to fit new frame width
        scrollPane.setOpaque(false); // Make the scroll pane transparent
        scrollPane.getViewport().setOpaque(false); // Make the viewport transparent
        add(scrollPane);

        setLocationRelativeTo(null); // Center the frame
        setVisible(true); // Show the window
    }

    private void addTransaction(String type, double amount) {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        transactionModel.addRow(new Object[]{date, type, String.format("%.2f PHP", amount)});
    }

    private void saveBalanceAndTransactions() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_data.txt"))) {
            writer.write("Balance: " + balance + "\n");
            writer.write("Transactions:\n");
            for (int i = 0; i < transactionModel.getRowCount(); i++) {
                writer.write(transactionModel.getValueAt(i, 0) + ", " +
                             transactionModel.getValueAt(i, 1) + ", " +
                             transactionModel.getValueAt(i, 2) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveAndLogout() {
        saveBalanceAndTransactions();
        JOptionPane.showMessageDialog(this, "Data saved successfully.", "Info", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    private void loadBalanceAndTransactions() {
        File file = new File(username + "_data.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Balance: ")) {
                        balance = Double.parseDouble(line.substring(9));
                    } else if (line.startsWith("Transactions:")) {
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(", ");
                            if (parts.length == 3) {
                                transactionModel.addRow(new Object[]{parts[0], parts[1], parts[2]});
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateSecurityPin(String securityPin)
    {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            boolean foundUser  = false;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Username: " + username)) {
                    foundUser  = true; // User found
                    // Read the next lines to find the security pin
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Security Pin: " + securityPin)) {
                            return true; // Valid security pin
                        }
                        if (line.contains("----------")) {
                            break; // End of user data
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Log the error for debugging
        }
        return false; // Invalid security pin
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Home("testUser ")); // Replace "testUser " with actual username
    }
}