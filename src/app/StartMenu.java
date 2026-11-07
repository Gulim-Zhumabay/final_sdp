package app;

import customer.Customer;
import service.AuthService;
import service.ExternalPaymentService;
import service.LoanService;

public class StartMenu {
    private final Bank bank;
    private final AuthService authService;
    private final LoanService loanService;
    private final ExternalPaymentService paymentService;
    private final ConsoleInput input = new ConsoleInput();

    public StartMenu(Bank bank, AuthService authService,
                     LoanService loanService, ExternalPaymentService paymentService) {
        this.bank = bank;
        this.authService = authService;
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
        System.out.println("Goodbye!");
    }

    private void execute(int choice) {
        try {
            switch (choice) {
                case 1 -> logIn();
                case 2 -> register();
                default -> System.out.println("Unknown option");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printMenu() {
        System.out.println("\n===== WELCOME TO THE BANK =====");
        System.out.println("1. Log in");
        System.out.println("2. Register");
        System.out.println("0. Exit");
    }

    private void logIn() {
        String username = input.readLine("Username: ");
        String password = input.readLine("Password: ");
        Customer customer = authService.login(username, password);
        new CustomerMenu(bank, customer.getUsername(), input, loanService, paymentService).run();
    }

    private void register() {
        String username = input.readLine("Choose a username: ");
        String password = input.readLine("Choose a password: ");
        authService.register(username, password);
        System.out.println("Registered. You can log in now.");
    }
}