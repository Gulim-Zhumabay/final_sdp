package account;

import observer.TransactionObserver;
import strategy.InterestStrategy;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAccount implements Account {
    private final String id;
    private final String owner;
    private final List<TransactionObserver> observers = new ArrayList<>();
    private InterestStrategy interestStrategy;
    protected double balance;

    public AbstractAccount(String id, String owner, double initialBalance) {
        this.id = id;
        this.owner = owner;
        this.balance = initialBalance;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getOwner() { return owner; }

    @Override
    public double getBalance() { return balance; }

    @Override
    public String getDescription() {
        return getType() + " account " + id + " (" + owner + "), balance: " + balance;
    }

    @Override
    public void deposit(double amount, TransactionType type) {
        requirePositive(amount);
        balance += amount;
        notifyObservers(new Transaction(id, owner, type, amount));
    }

    @Override
    public void withdraw(double amount, TransactionType type) {
        requirePositive(amount);
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance -= amount;
        notifyObservers(new Transaction(id, owner, type, amount));
    }

    @Override
    public void addObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    @Override
    public void setInterestStrategy(InterestStrategy strategy) {
        this.interestStrategy = strategy;
    }

    @Override
    public double calculateInterest(int months) {
        if (interestStrategy == null) {
            throw new IllegalStateException("This account has no interest");
        }
        return interestStrategy.calculate(balance, months);
    }

    private void requirePositive(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    private void notifyObservers(Transaction transaction) {
        for (TransactionObserver observer : observers) {
            observer.onTransaction(transaction);
        }
    }
}