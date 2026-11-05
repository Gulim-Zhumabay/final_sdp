import app.Bank;
import app.ConsoleMenu;
import storage.AccountCsvStore;
import storage.TransactionCsvStore;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        TransactionCsvStore history = new TransactionCsvStore(Path.of("data", "transactions.csv"));
        AccountCsvStore accountStore = new AccountCsvStore(Path.of("data", "accounts.csv"));
        Bank bank = new Bank(history, accountStore);
        new ConsoleMenu(bank).run();
    }
}