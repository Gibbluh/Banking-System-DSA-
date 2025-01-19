package school;

import java.util.Scanner;
import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.io.*; // Import for file handling
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {

    static class User {
        String username;
        String password;
        Account account;

        public User(String username, String password, Account account) {
            this.username = username;
            this.password = password;
            this.account = account;
        }
    }

    static class Account {
        int accountNumber;
        String customerName; // Store customer name
        double balance;
        Transaction[] transactions;
        int transactionIndex; // Track the current position in the transaction history

        public Account(int accountNumber, String customerName, double balance) {
            this.accountNumber = accountNumber;
            this.customerName = customerName;
            this.balance = balance;
            this.transactions = new Transaction[10]; // Initialize with a fixed size array
            this.transactionIndex = 0;
        }

        // Add a transaction to the account
        public void addTransaction(Transaction transaction) {
            if (transactionIndex < transactions.length) {
                transactions[transactionIndex++] = transaction;
            } else {
                System.out.println("Transaction history is full.");
            }
        }

        // Deposit money
        public void deposit(double amount) {
            if (amount > 0) {
                this.balance += amount;
                addTransaction(new Transaction(TransactionType.DEPOSIT, amount));
                System.out.println("Deposit successful. New balance: " + balance);
            } else {
                System.out.println("Invalid deposit amount.");
            }
        }

        // Withdraw money
        public void withdraw(double amount) {
            if (amount > 0 && amount <= balance) {
                this.balance -= amount;
                addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount));
                System.out.println("Withdrawal successful. \nNew balance: " + balance);
            } else {
                System.out.println("Insufficient funds or invalid withdrawal amount.");
            }
        }

        // View transaction history
        public void viewTransactionHistory() {
            if (transactionIndex == 0) {
                System.out.println("No transactions found.");
                return;
            }
            System.out.println("Transaction History:");
            for (int i = 0; i < transactionIndex; i++) {
                if (transactions[i] != null) {
                    System.out.println(transactions[i]);
                }
            }
        }

        // Method to get the current balance
        public double getBalance() {
            return balance;
        }

        // Save account data to a file
        public void saveToFile(String username) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(username + "_account_data.txt"))) {
                writer.write("Account No. " + accountNumber + "\n");
                writer.write("Account Name: " + customerName + "\n");
                writer.write("Balance: " + balance + "\n");
                writer.write("History: " + transactionIndex + "\n");
                for (int i = 0; i < transactionIndex; i++) {
                    if (transactions[i] != null) {
                        writer.write(transactions[i].toFileString() + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Error saving account data: " + e.getMessage());
            }
        }

        // Load account data from a file
        public static Account loadFromFile(String username) {
            try (BufferedReader reader = new BufferedReader(new FileReader(username + "_account_data.txt"))) {
                int accountNumber = Integer.parseInt(reader.readLine().split(" ")[2]);
                String customerName = reader.readLine().split(": ")[1];
                double balance = Double.parseDouble(reader.readLine().split(": ")[1]);
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
                        transaction.timestamp = timestamp; // Set the timestamp from the file
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
        LocalDateTime timestamp; // Add timestamp field

        public Transaction(TransactionType type, double amount) {
            this.type = type;
            this.amount = amount;
            this.timestamp = LocalDateTime.now(); // Initialize timestamp with current date and time
        }

        // Convert transaction to a string for saving
        public String toFileString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return "Transaction Type: " + type + "\nAmount: " + amount + "\nDate|Time: " + timestamp.format(formatter);
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss"); // Define the date format
            return "    Transaction:\n      " +
                    type +
                    "\n     -----------\n    amount = " + amount +
                    "\n     -----------\n      Date|Time:\n   " + timestamp.format(formatter) + // Format the timestamp
                    "\n     -----------";
        }
    }

    enum TransactionType {
        DEPOSIT,
        WITHDRAWAL
    }

    // Method to load users from a file
    private static List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get("users.txt"));
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length == 3) { // Expecting username, password, and account number
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    int accountNumber = Integer.parseInt(parts[2].trim());
                    Account account = Account.loadFromFile(username); // Load account data
                    users.add(new User(username, password, account)); // Store user
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    // Method to save a user to a file
    private static void saveUser (String username, String password, int accountNumber) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("users.txt", true))) {
            writer.write(username + "\n" + password + "\n" + accountNumber + "\n");
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<User> users = loadUsers(); // Load existing users from the file

        while (true) {
            System.out.println("\nChoose an option: \n 1. Register \n 2. Login \n 3. Exit");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1: // Register
                    System.out.print("Enter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    int accountNumber = users.size() + 1; // Create a new account number
                    Account newAccount = new Account(accountNumber, username, 0.0); // Create a new account
                    users.add(new User(username, password, newAccount)); // Store user
                    saveUser (username, password, accountNumber); // Save user to file
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
                        if (user.username.equals(loginUsername) && user.password.equals(loginPassword)) {
                            loggedInUser  = user;
                            break;
                        }
                    }

                    if (loggedInUser  == null) {
                        System.out.println("Invalid username or password.");
                        break;
                    }

                    // User is logged in, proceed with account operations
                    Account account = loggedInUser .account;

                    while (true) {
                        System.out.println("\nChoose an option: \n 1. Deposit \n 2. Withdraw \n 3. View Transactions \n 4. View Balance \n 5. Logout");
                        int accountChoice = scanner.nextInt();

                        switch (accountChoice) {
                            case 1:
                                System.out.print("Enter amount to deposit: ");
                                double depositAmount = scanner.nextDouble();
                                account.deposit(depositAmount);
                                break;
                            case 2:
                                System.out.print("Enter amount to withdraw: ");
                                double withdrawAmount = scanner.nextDouble();
                                account.withdraw(withdrawAmount);
                                break;
                            case 3:
                                account.viewTransactionHistory();
                                break;
                            case 4:
                                System.out.println("\nCurrent balance: " + account.getBalance());
                                break;
                            case 5:
                                account.saveToFile(loggedInUser .username); // Pass the username to save account data
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