package app;

import account.Account;
import account.Transaction;
import factory.AccountFactory;
import observer.AccountSaver;
import observer.CustomerNotifier;
import observer.FraudDetector;
import observer.TransactionLogger;
import storage.AccountCsvStore;
import storage.TransactionCsvStore;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Bank {
    private static final double FRAUD_LIMIT = 10000;

    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final TransactionCsvStore historyStore;
    private final AccountCsvStore accountStore;

    public Bank(TransactionCsvStore historyStore, AccountCsvStore accountStore) {
        this.historyStore = historyStore;
        this.accountStore = accountStore;
        loadAccounts();
    }

    public Account openAccount(String type, String owner, double initialBalance) {
        validateOwner(owner);
        Account account = AccountFactory.create(type, owner, 0);
        register(account);
        if (initialBalance > 0) {
            account.deposit(initialBalance);
        }
        saveAccounts();
        return account;
    }

    public void replaceAccount(Account decorated) {
        accounts.put(decorated.getId(), decorated);
        saveAccounts();
    }

    public Account getAccount(String id) {
        Account account = accounts.get(id.toUpperCase());
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + id);
        }
        return account;
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public List<Transaction> getHistory(String accountId) {
        return historyStore.findByAccount(accountId);
    }

    private void loadAccounts() {
        accountStore.loadAll().forEach(this::register);
    }

    private void register(Account account) {
        attachObservers(account);
        accounts.put(account.getId(), account);
    }

    private void attachObservers(Account account) {
        account.addObserver(new TransactionLogger(historyStore));
        account.addObserver(new AccountSaver(accountStore, accounts::values));
        account.addObserver(new CustomerNotifier(account.getOwner()));
        account.addObserver(new FraudDetector(FRAUD_LIMIT));
    }

    private void saveAccounts() {
        accountStore.saveAll(accounts.values());
    }

    private void validateOwner(String owner) {
        if (owner.isBlank() || owner.contains(",")) {
            throw new IllegalArgumentException("Owner name must not be empty or contain commas");
        }
    }
}
