package observer;

import account.Account;
import account.Transaction;
import storage.AccountCsvStore;

import java.util.Collection;
import java.util.function.Supplier;

public class AccountSaver implements TransactionObserver {
    private final AccountCsvStore store;
    private final Supplier<Collection<Account>> accounts;

    public AccountSaver(AccountCsvStore store, Supplier<Collection<Account>> accounts) {
        this.store = store;
        this.accounts = accounts;
    }

    @Override
    public void onTransaction(Transaction transaction) {
        store.saveAll(accounts.get());
    }
}
