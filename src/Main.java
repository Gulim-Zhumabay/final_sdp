import account.Account;
import app.Bank;
import decorator.OverdraftDecorator;
import decorator.SmsDecorator;
import factory.AccountFactory;
import observer.CustomerNotifier;
import observer.FraudDetector;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Bank bank = new Bank();

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose: ");
            try {
                switch (choice) {
                    case 1: createAccount(); break;
                    case 2: showAccounts(); break;
                    case 3: deposit(); break;
                    case 4: withdraw(); break;
                    case 5: addOption(); break;
                    case 0: running = false; break;
                    default: System.out.println("Unknown option");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private static void printMenu() {
        System.out.println("\n===== BANKING SYSTEM =====");
        System.out.println("1. Create account");
        System.out.println("2. Show accounts");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Add option to account (overdraft / SMS)");
        System.out.println("0. Exit");
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number");
            }
        }
    }

    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number");
            }
        }
    }

    private static void createAccount() {
        System.out.print("Owner name: ");
        String owner = scanner.nextLine().trim();
        System.out.print("Type (savings / checking): ");
        String type = scanner.nextLine().trim();
        double balance = readDouble("Initial balance: ");

        Account account = AccountFactory.create(type, owner, balance);
        account.addObserver(new CustomerNotifier(owner));
        account.addObserver(new FraudDetector(10000));
        bank.save(account);
        System.out.println("Created: " + account.getDescription());
    }

    private static void showAccounts() {
        if (bank.isEmpty()) {
            System.out.println("No accounts yet.");
            return;
        }
        for (Account account : bank.getAllAccounts()) {
            System.out.println(account.getDescription());
        }
    }

    private static Account chooseAccount() {
        if (bank.isEmpty()) {
            throw new IllegalStateException("No accounts yet. Create one first.");
        }
        showAccounts();
        System.out.print("Account ID (e.g. ACC-1): ");
        Account account = bank.getAccount(scanner.nextLine().trim());
        if (account == null) {
            throw new IllegalStateException("Account not found");
        }
        return account;
    }

    private static void deposit() {
        Account account = chooseAccount();
        double amount = readDouble("Amount: ");
        account.deposit(amount);
        System.out.println("New balance: " + account.getBalance());
    }

    private static void withdraw() {
        Account account = chooseAccount();
        double amount = readDouble("Amount: ");
        account.withdraw(amount);
        System.out.println("New balance: " + account.getBalance());
    }

    private static void addOption() {
        Account account = chooseAccount();
        System.out.println("1. Overdraft");
        System.out.println("2. SMS notifications");
        int option = readInt("Option: ");

        Account decorated;
        if (option == 1) {
            double limit = readDouble("Overdraft limit: ");
            decorated = new OverdraftDecorator(account, limit);
        } else if (option == 2) {
            decorated = new SmsDecorator(account);
        } else {
            throw new IllegalArgumentException("Unknown option");
        }
        bank.save(decorated);
        System.out.println("Updated: " + decorated.getDescription());
    }
}
