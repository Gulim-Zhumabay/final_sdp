package account;

import observer.TransactionObserver;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAccount implements Account {
    private final String id;
    private final String owner;
    protected double balance;
    private final List<TransactionObserver> observers = new ArrayList<>();

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
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
        notifyObservers(new Transaction(id, TransactionType.DEPOSIT, amount));
    }

    @Override
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalStateException("Insufficient funds");
        }
        balance -= amount;
        notifyObservers(new Transaction(id, TransactionType.WITHDRAW, amount));
    }

    @Override
    public void addObserver(TransactionObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(Transaction transaction) {
        for (TransactionObserver observer : observers) {
            observer.onTransaction(transaction);
        }
    }
}