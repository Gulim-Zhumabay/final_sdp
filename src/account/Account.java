package account;

import observer.TransactionObserver;
import strategy.InterestStrategy;

public interface Account {
    String getId();
    String getOwner();
    double getBalance();
    String getType();
    String getDescription();

    void deposit(double amount, TransactionType type);
    void withdraw(double amount, TransactionType type);

    default void deposit(double amount) {
        deposit(amount, TransactionType.DEPOSIT);
    }

    default void withdraw(double amount) {
        withdraw(amount, TransactionType.WITHDRAW);
    }

    void addObserver(TransactionObserver observer);

    void setInterestStrategy(InterestStrategy strategy);
    double calculateInterest(int months);
}