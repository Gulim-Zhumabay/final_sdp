package app;

import account.Account;
import account.Transaction;
import decorator.OverdraftDecorator;
import decorator.SmsDecorator;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsoleMenu {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Bank bank;
    private final ConsoleInput input = new ConsoleInput();

    public ConsoleMenu(Bank bank) {
        this.bank = bank;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = input.readInt("Choose: ");
            if (choice == 0) {
                running = false;
            } else {
                execute(choice);
            }
        }
        System.out.println("Goodbye!");
    }

    private void execute(int choice) {
        try {
            switch (choice) {
                case 1 -> createAccount();
                case 2 -> showAccounts();
                case 3 -> deposit();
                case 4 -> withdraw();
                case 5 -> addOption();
                case 6 -> showStatement();
                default -> System.out.println("Unknown option");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("\n===== BANKING SYSTEM =====");
        System.out.println("1. Open account");
        System.out.println("2. Show accounts");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Add option (overdraft / SMS)");
        System.out.println("6. Account statement");
        System.out.println("0. Exit");
    }

    private void createAccount() {
        String owner = input.readLine("Owner name: ");
        String type = input.readLine("Type (savings / checking): ");
        double balance = input.readDouble("Initial balance: ");
        Account account = bank.openAccount(type, owner, balance);
        System.out.println("Opened: " + account.getDescription());
    }

    private void showAccounts() {
        if (bank.getAllAccounts().isEmpty()) {
            System.out.println("No accounts yet.");
            return;
        }
        bank.getAllAccounts().forEach(account -> System.out.println(account.getDescription()));
    }

    private Account chooseAccount() {
        if (bank.getAllAccounts().isEmpty()) {
            throw new IllegalStateException("No accounts yet. Open one first.");
        }
        showAccounts();
        return bank.getAccount(input.readLine("Account ID (e.g. ACC-1): "));
    }

    private void deposit() {
        Account account = chooseAccount();
        account.deposit(input.readDouble("Amount: "));
        System.out.println("New balance: " + account.getBalance());
    }

    private void withdraw() {
        Account account = chooseAccount();
        account.withdraw(input.readDouble("Amount: "));
        System.out.println("New balance: " + account.getBalance());
    }

    private void addOption() {
        Account account = chooseAccount();
        System.out.println("1. Overdraft");
        System.out.println("2. SMS notifications");
        int option = input.readInt("Option: ");

        Account decorated = switch (option) {
            case 1 -> new OverdraftDecorator(account, input.readDouble("Overdraft limit: "));
            case 2 -> new SmsDecorator(account);
            default -> throw new IllegalArgumentException("Unknown option");
        };
        bank.replaceAccount(decorated);
        System.out.println("Updated: " + decorated.getDescription());
    }

    private void showStatement() {
        Account account = chooseAccount();
        List<Transaction> history = bank.getHistory(account.getId());
        if (history.isEmpty()) {
            System.out.println("No operations yet.");
            return;
        }
        for (Transaction transaction : history) {
            System.out.printf("%s  %-16s %10.2f%n",
                    transaction.getTime().format(TIME_FORMAT),
                    transaction.getType(),
                    transaction.getAmount());
        }
    }
}
