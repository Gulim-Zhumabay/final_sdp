import app.Bank;
import gui.LoginFrame;
import gui.MainFrame;
import payment.SwiftAdapter;
import payment.SwiftApi;
import payment.VisaAdapter;
import payment.VisaApi;
import security.PasswordHasher;
import service.AuthService;
import service.ExternalPaymentService;
import service.LoanService;
import storage.AccountCsvStore;
import storage.CustomerCsvStore;
import storage.LoanCsvStore;
import storage.TransactionCsvStore;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        TransactionCsvStore history = new TransactionCsvStore(Path.of("data", "transactions.csv"));
        AccountCsvStore accountStore = new AccountCsvStore(Path.of("data", "accounts.csv"));
        CustomerCsvStore customerStore = new CustomerCsvStore(Path.of("data", "customers.csv"));
        LoanCsvStore loanStore = new LoanCsvStore(Path.of("data", "loans.csv"));

        Bank bank = new Bank(history, accountStore);
        AuthService authService = new AuthService(customerStore, new PasswordHasher());
        LoanService loanService = new LoanService(loanStore, bank::getAccount);
        ExternalPaymentService paymentService = new ExternalPaymentService(List.of(
                new VisaAdapter(new VisaApi()),
                new SwiftAdapter(new SwiftApi(), "KASPKZKA", "USD")));

        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            showLogin(bank, authService, loanService, paymentService);
        });
    }

    private static void showLogin(Bank bank, AuthService authService,
                                  LoanService loanService, ExternalPaymentService paymentService) {
        Runnable backToLogin = () -> showLogin(bank, authService, loanService, paymentService);
        new LoginFrame(authService, customer ->
                new MainFrame(bank, customer.getUsername(), loanService, paymentService, backToLogin)
                        .setVisible(true)
        ).setVisible(true);
    }

    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {

        }
    }
}