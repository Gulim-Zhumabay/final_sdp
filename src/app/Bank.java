package app;

import account.Account;
import account.Transaction;
import factory.AccountFactory;
import observer.CustomerNotifier;
import observer.FraudDetector;
import observer.TransactionLogger;
import storage.TransactionCsvStore;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Bank {
    private static final double FRAUD_LIMIT = 10000;

    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final TransactionCsvStore historyStore;

    public Bank(TransactionCsvStore historyStore) {
        this.historyStore = historyStore;
    }

    public Account openAccount(String type, String owner, double initialBalance) {
        Account account = AccountFactory.create(type, owner, 0);
        attachObservers(account);
        accounts.put(account.getId(), account);
        if (initialBalance > 0) {
            account.deposit(initialBalance);
        }
        return account;
    }

    public void replaceAccount(Account decorated) {
        accounts.put(decorated.getId(), decorated);
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

    private void attachObservers(Account account) {
        account.addObserver(new TransactionLogger(historyStore));
        account.addObserver(new CustomerNotifier(account.getOwner()));
        account.addObserver(new FraudDetector(FRAUD_LIMIT));
    }
}
