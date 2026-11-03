package decorator;

import account.Account;
import observer.TransactionObserver;

public abstract class AccountDecorator implements Account {
    protected final Account wrapped;

    public AccountDecorator(Account wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getId() { return wrapped.getId(); }

    @Override
    public String getOwner() { return wrapped.getOwner(); }

    @Override
    public double getBalance() { return wrapped.getBalance(); }

    @Override
    public String getType() { return wrapped.getType(); }

    @Override
    public String getDescription() { return wrapped.getDescription(); }

    @Override
    public void deposit(double amount) { wrapped.deposit(amount); }

    @Override
    public void withdraw(double amount) { wrapped.withdraw(amount); }

    @Override
    public void addObserver(TransactionObserver observer) { wrapped.addObserver(observer); }
}