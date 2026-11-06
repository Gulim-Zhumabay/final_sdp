import app.Bank;
import app.StartMenu;
import security.PasswordHasher;
import service.AuthService;
import storage.AccountCsvStore;
import storage.CustomerCsvStore;
import storage.TransactionCsvStore;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        TransactionCsvStore history = new TransactionCsvStore(Path.of("data", "transactions.csv"));
        AccountCsvStore accountStore = new AccountCsvStore(Path.of("data", "accounts.csv"));
        CustomerCsvStore customerStore = new CustomerCsvStore(Path.of("data", "customers.csv"));

        Bank bank = new Bank(history, accountStore);
        AuthService authService = new AuthService(customerStore, new PasswordHasher());

        new StartMenu(bank, authService).run();
    }
}