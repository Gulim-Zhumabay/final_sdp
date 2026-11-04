package app;

import account.Account;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Bank {
    private final Map<String, Account> accounts = new LinkedHashMap<>();

    public void save(Account account) {
        accounts.put(account.getId(), account);
    }

    public Account getAccount(String id) {
        return accounts.get(id.toUpperCase());
    }

    public Collection<Account> getAllAccounts() {
        return accounts.values();
    }

    public boolean isEmpty() {
        return accounts.isEmpty();
    }
}
