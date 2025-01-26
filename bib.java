package XD;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class bib {

    static class User {
        String username;
        String password;
        String fullName; // Full name (Last Name, First Name, Middle Name)
        String securityPin; // Security pin for withdrawing
        Account account;

        public User(String username, String password, String fullName, String securityPin, Account account) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.securityPin = securityPin;
            this.account = account;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getFullName() {
            return fullName;
        }

        public String getSecurityPin() {
            return securityPin;
        }
    }

    static class Account {
        int accountNumber;
        String customerName;
        double balance;
        List<Transaction> transactions;

        public Account(int accountNumber, String customerName, double balance) {
            this.accountNumber = accountNumber;
            this.customerName = customerName;
            this.balance = balance;
            this.transactions = new ArrayList<>();
        }

        public void addTransaction(Transaction transaction) {
            transactions.add(transaction);
        }

        public void deposit(double amount) {
            if (amount > 0) {
                this.balance += amount;
                addTransaction(new Transaction(TransactionType.DEPOSIT, amount));
                System.out.println("Deposit successful. New balance: ₱" + String.format("%.2f", balance));
            } else {
                System.out.println("Invalid deposit amount.");
            }
        }

        public void withdraw(double amount, String enteredPin, User loggedInUser ) {
            // Check if the entered pin matches the user's security pin
            if (!enteredPin.equals(loggedInUser .getSecurityPin())) {
                System.out.println("Invalid security pin. Withdrawal denied.");
                return;
            }

            if (amount > 0 && amount <= balance) {
                this.balance -= amount;
                addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount));
                System.out.println("Withdrawal successful. New balance: ₱" + String.format("%.2f", balance));
            } else {
                System.out.println("Insufficient funds or invalid withdrawal amount.");
            }
        }

        public void viewTransactionHistory() {
            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }
            System.out.println("Transaction History:");
            for (Transaction transaction : transactions) {
                System.out.println(transaction);
            }
        }

        public double getBalance() {
            return balance;
        }

        public void saveToFile(String username) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_account_data.txt"))) {
                writer.write("Account No. " + accountNumber + "\n");
                writer.write("Account Name: " + customerName + "\n");
                writer.write("Balance: ₱" + String.format("%.2f", balance) + "\n");
                writer.write("History: " + transactions.size() + "\n");
                for (Transaction transaction : transactions) {
                    writer.write(transaction.toFileString() + "\n");
                }
            } catch (IOException e) {
                System.out.println("Error saving account data: " + e.getMessage());
            }
        }

        public static Account loadFromFile(String username) {
            try (BufferedReader reader = new BufferedReader(new FileReader(username + "_account_data.txt"))) {
                int accountNumber = Integer.parseInt(reader.readLine().split(" ")[2]);
                String customerName = reader.readLine().split(": ")[1];
                double balance = Double.parseDouble(reader.readLine().split(": ")[1].replace("₱", "").trim());
                int transactionCount = Integer.parseInt(reader.readLine().split(": ")[1]);

                Account account = new Account(accountNumber, customerName, balance);

                for (int i = 0; i < transactionCount; i++) {
                    String line = reader.readLine();
                    if (line != null) {
                        String[] parts = line.split(",");
                        if (parts.length < 3) {
                            System.out.println("Invalid transaction format. Expected at least 3 parts.");
                            continue; // Skip this transaction if the format is invalid
                        }
                        TransactionType type = TransactionType.valueOf(parts[0].trim());
                        double amount = Double.parseDouble(parts[1].trim());
                        LocalDateTime timestamp = LocalDateTime.parse(parts[2].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        Transaction transaction = new Transaction(type, amount);
                        account.addTransaction(transaction);
                    }
                }
                return account;
            } catch (FileNotFoundException e) {
                System.out.println("Account data file not found for user: " + username);
                return null; // Return null if loading fails
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error loading account data: " + e.getMessage());
                return null; // Return null if loading fails
            }
        }
    }

    static class Transaction {
        TransactionType type;
        double amount;
        LocalDateTime timestamp;

        public Transaction(TransactionType type, double amount) {
            this.type = type;
            this.amount = amount;
            this.timestamp = LocalDateTime.now();
        }

        public String toFileString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return type + "," + amount + "," + timestamp.format(formatter);
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss");
            return "Transaction:\n" +
                    "  Type: " + type + "\n" +
                    "  Amount: ₱" + String.format("%.2f", amount) + "\n" +
                    "  Date|Time: " + timestamp.format(formatter);
        }
    }

    enum TransactionType {
        DEPOSIT,
        WITHDRAWAL
    }

    private static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("users.txt"));
            for (int i = 0; i < lines.size(); i += 5) { // Adjusted to read 5 lines for each user
                if (i + 4 < lines.size()) { // Ensure there are enough lines for account number, username, password, full name, and security pin
                    int accountNumber = Integer.parseInt(lines.get(i).split(": ")[1].trim());
                    String username = lines.get(i + 1).split(": ")[1].trim();
                    String password = lines.get(i + 2).split(": ")[1].trim();
                    String fullName = lines.get(i + 3).split(": ")[1].trim();
                    String securityPin = lines.get(i + 4).split(": ")[1].trim();
                    Account account = Account.loadFromFile(username);
                    users.add(new User(username, password, fullName, securityPin, account));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    private static void saveUser (String username, String password, String fullName, String securityPin, int accountNumber) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write("Account Number: " + accountNumber + "\n");
            writer.write("Username: " + username + "\n");
            writer.write("Password: " + password + "\n");
            writer.write("Full Name: " + fullName + "\n");
            writer.write("Security Pin: " + securityPin + "\n");
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<User> users = loadUsers();

        while (true) {
            System.out.println("\nChoose an option: \n 1. Register \n 2. Login \n 3. Exit");
            int choice = 0;

            // Input validation for main menu choice
            while (true) {
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    if (choice < 1 || choice > 3) {
                        System.out.println("Please enter a valid option (1-3).");
                    } else {
                        break; // Valid input, exit the loop
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Invalid input. Please enter a number (1-3).");
                    scanner.nextLine(); // Clear the invalid input
                }
            }

            switch (choice) {
                case 1: // Register
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    System.out.print("Enter full name (Last Name, First Name, Middle Name): ");
                    String fullName = scanner.nextLine();
                    System.out.print("Enter security pin (for withdrawing): ");
                    String securityPin = scanner.nextLine();

                    // Check for existing username
                    if (users.stream().anyMatch(user -> user.getUsername().equals(username))) {
                        System.out.println("Username already exists. Please choose a different one.");
                        break;
                    }

                    int accountNumber = users.size() + 1; // Create a new account number
                    Account newAccount = new Account(accountNumber, username, 0.0);
                    users.add(new User(username, password, fullName, securityPin, newAccount));
                    saveUser (username, password, fullName, securityPin, accountNumber); // Save user to file
                    System.out.println("Registration successful!");
                    break;

                case 2: // Login
                    System.out.print("Enter username: ");
                    String loginUsername = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String loginPassword = scanner.nextLine();
                    User loggedInUser  = null;

                    // Check if the user exists
                    for (User  user : users) {
                        if (user.getUsername().equals(loginUsername) && user.getPassword().equals(loginPassword)) {
                            loggedInUser  = user;
                            break;
                        }
                    }

                    if (loggedInUser  == null) {
                        System.out.println("Invalid username or password.");
                        break;
                    }

                    // User is logged in, greet the user
                    System.out.println("Welcome, " + loggedInUser .getFullName() + "!");

                    // Proceed with account operations
                    Account account = loggedInUser .account;

                    while (true) {
                        System.out.println("\nChoose an option: \n 1. Deposit \n 2. Withdraw \n 3. View Transactions \n 4. View Balance \n 5. Logout");
                        int accountChoice = 0;

                        // Input validation for account operations choice
                        while (true) {
                            try {
                                accountChoice = scanner.nextInt();
                                scanner.nextLine(); // Consume newline
                                if (accountChoice < 1 || accountChoice > 5) {
                                    System.out.println("Please enter a valid option (1-5).");
                                } else {
                                    break; // Valid input, exit the loop
                                }
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input. Please enter a number (1-5).");
                                scanner.nextLine(); // Clear the invalid input
                            }
                        }

                        switch (accountChoice) {
                            case 1:
                                System.out.print("Enter amount to deposit: ");
                                double depositAmount = scanner.nextDouble();
                                account.deposit(depositAmount);
                                break;
                            case 2:
                                System.out.print("Enter amount to withdraw: ");
                                double withdrawAmount = scanner.nextDouble();
                                System.out.print("Enter your security pin: ");
                                String enteredPin = scanner.next();
                                account.withdraw(withdrawAmount, enteredPin, loggedInUser ); // Pass the entered pin to the withdraw method
                                break;
                            case 3:
                                account.viewTransactionHistory();
                                break;
                            case 4:
                                System.out.println("\nCurrent balance: ₱" + String.format("%.2f", account.getBalance()));
                                break;
                            case 5:
                                account.saveToFile(loggedInUser .getUsername());
                                System.out.println("Logging out...");
                                break;
                            default:
                                System.out.println("Invalid option. Please try again.");
                        }

                        if (accountChoice == 5) {
                            break; // Exit the account operations loop
                        }
                    }
                    break;

                case 3: // Exit
                    System.out.println("Exiting...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}