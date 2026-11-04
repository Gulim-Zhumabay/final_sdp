import app.Bank;
import app.ConsoleMenu;
import storage.TransactionCsvStore;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        TransactionCsvStore history = new TransactionCsvStore(Path.of("data", "transactions.csv"));
        Bank bank = new Bank(history);
        new ConsoleMenu(bank).run();
    }
}
