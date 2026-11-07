package service;

import account.Account;
import decorator.OverdraftDecorator;
import decorator.SmsDecorator;

import java.util.function.Consumer;

public class AccountOptionService {
    private static final String CHECKING = "Checking";

    private final Consumer<Account> accountReplacer;

    public AccountOptionService(Consumer<Account> accountReplacer) {
        this.accountReplacer = accountReplacer;
    }

    public Account addOverdraft(Account account, double limit) {
        if (!account.getType().equals(CHECKING)) {
            throw new IllegalStateException("Overdraft is available only for checking accounts");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("Overdraft limit must be positive");
        }
        return apply(new OverdraftDecorator(account, limit));
    }

    public Account addSms(Account account) {
        return apply(new SmsDecorator(account));
    }

    private Account apply(Account decorated) {
        accountReplacer.accept(decorated);
        return decorated;
    }
}