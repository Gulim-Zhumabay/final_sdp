package app;

import account.Account;
import account.Transaction;
import decorator.OverdraftDecorator;
import decorator.SmsDecorator;
import service.TransferService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import loan.Loan;
import loan.LoanAgreement;
import service.ExternalPaymentService;
import service.LoanService;

import java.util.ArrayList;

public class CustomerMenu {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Bank bank;
    private final String owner;
    private final ConsoleInput input;
    private final LoanService loanService;
    private final ExternalPaymentService paymentService;
    private final TransferService transferService = new TransferService();

    public CustomerMenu(Bank bank, String owner, ConsoleInput input,
                        LoanService loanService, ExternalPaymentService paymentService) {
        this.bank = bank;
        this.owner = owner;
        this.input = input;
        this.loanService = loanService;
        this.paymentService = paymentService;
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
        System.out.println("Logged out.");
    }

    private void execute(int choice) {
        try {
            switch (choice) {
                case 1 -> createAccount();
                case 2 -> showAccounts();
                case 3 -> topUp();
                case 4 -> withdraw();
                case 5 -> addOption();
                case 6 -> showStatement();
                case 7 -> transfer();
                case 8 -> nextMonth();
                case 9 -> takeLoan();
                case 10 -> showLoans();
                case 11 -> externalPayment();
                default -> System.out.println("Unknown option");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("\n===== BANKING SYSTEM | " + owner + " =====");
        System.out.println("1. Open account");
        System.out.println("2. My accounts");
        System.out.println("3. Top up account");
        System.out.println("4. Withdraw money");
        System.out.println("5. Add option (overdraft / SMS)");
        System.out.println("6. Account statement");
        System.out.println("7. Transfer money");
        System.out.println("8. Next month (pay deposit interest)");
        System.out.println("9. Take a loan");
        System.out.println("10. My loans");
        System.out.println("11. External payment (Visa / SWIFT)");
        System.out.println("0. Log out");
    }

    private void createAccount() {
        String type = input.readLine("Type (savings / checking / deposit): ");
        int termMonths = type.equalsIgnoreCase("deposit")
                ? input.readInt("Deposit term (months): ")
                : 0;
        double balance = input.readDouble("Initial balance: ");

        Account account = bank.openAccount(type, owner, balance, termMonths);
        System.out.println("Opened: " + account.getDescription());
    }

    private void showAccounts() {
        List<Account> accounts = bank.getAccountsOf(owner);
        if (accounts.isEmpty()) {
            System.out.println("You have no accounts yet.");
            return;
        }
        accounts.forEach(account -> System.out.println(account.getDescription()));
    }

    private Account chooseAccount(String prompt) {
        if (bank.getAccountsOf(owner).isEmpty()) {
            throw new IllegalStateException("You have no accounts yet. Open one first.");
        }
        showAccounts();
        return bank.getOwnedAccount(input.readLine(prompt), owner);
    }

    private Account chooseAccount() {
        return chooseAccount("Account ID (e.g. ACC-1): ");
    }

    private void topUp() {
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
        System.out.println("1. Overdraft (checking accounts only)");
        System.out.println("2. SMS notifications");
        int option = input.readInt("Option: ");

        Account decorated = switch (option) {
            case 1 -> new OverdraftDecorator(requireChecking(account),
                    input.readDouble("Overdraft limit: "));
            case 2 -> new SmsDecorator(account);
            default -> throw new IllegalArgumentException("Unknown option");
        };
        bank.replaceAccount(decorated);
        System.out.println("Updated: " + decorated.getDescription());
    }

    private Account requireChecking(Account account) {
        if (!account.getType().equals("Checking")) {
            throw new IllegalStateException("Overdraft is available only for checking accounts");
        }
        return account;
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

    private void transfer() {
        Account sender = chooseAccount("From your account ID: ");
        Account receiver = bank.getAccount(input.readLine("To account ID (any customer): "));
        double amount = input.readDouble("Amount: ");

        transferService.transfer(sender, receiver, amount);
        System.out.println("Your account " + sender.getId() + " balance: " + sender.getBalance());
    }

    private void nextMonth() {
        List<String> reports = new ArrayList<>(bank.closeMonth());
        reports.addAll(loanService.collectPayments());
        if (reports.isEmpty()) {
            System.out.println("A month has passed. No active deposits or loans.");
            return;
        }
        reports.forEach(System.out::println);
    }
    private void takeLoan() {
        Account account = chooseAccount("Account to receive the loan: ");
        double amount = input.readDouble("Loan amount: ");
        int months = input.readInt("Term (months): ");
        String guarantor = input.readLine("Guarantor name (Enter to skip): ");
        boolean insured = input.readLine("Add insurance? (y/n): ").equalsIgnoreCase("y");

        LoanAgreement agreement = loanService.createAgreement(owner, amount, months, guarantor, insured);
        Loan loan = loanService.issue(account, agreement);
        System.out.println("Loan approved: " + loan.getDescription());
        System.out.println("Paid out to " + account.getId() + ". New balance: " + account.getBalance());
    }

    private void showLoans() {
        List<Loan> loans = loanService.getLoansOf(owner);
        if (loans.isEmpty()) {
            System.out.println("You have no loans.");
            return;
        }
        loans.forEach(loan -> System.out.println(loan.getDescription()));
    }

    private void externalPayment() {
        Account account = chooseAccount("Pay from account ID: ");
        List<String> systems = paymentService.getGatewayNames();
        for (int i = 0; i < systems.size(); i++) {
            System.out.println((i + 1) + ". " + systems.get(i));
        }
        int choice = input.readInt("Payment system: ");
        if (choice < 1 || choice > systems.size()) {
            throw new IllegalArgumentException("Unknown payment system");
        }
        String recipient = input.readLine("Recipient (card / IBAN / merchant): ");
        double amount = input.readDouble("Amount: ");

        paymentService.pay(account, systems.get(choice - 1), recipient, amount);
        System.out.println("Payment sent. New balance: " + account.getBalance());
    }
}