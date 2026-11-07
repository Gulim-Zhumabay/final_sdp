import app.Bank;
import app.StartMenu;
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

        new StartMenu(bank, authService, loanService, paymentService).run();
    }
}