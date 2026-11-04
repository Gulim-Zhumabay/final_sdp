package decorator;

import account.Account;
import account.TransactionType;
import observer.TransactionObserver;
import strategy.InterestStrategy;

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
    public void deposit(double amount, TransactionType type) { wrapped.deposit(amount, type); }

    @Override
    public void withdraw(double amount, TransactionType type) { wrapped.withdraw(amount, type); }

    @Override
    public void addObserver(TransactionObserver observer) { wrapped.addObserver(observer); }

    @Override
    public void setInterestStrategy(InterestStrategy strategy) { wrapped.setInterestStrategy(strategy); }

    @Override
    public double calculateInterest(int months) { return wrapped.calculateInterest(months); }
}