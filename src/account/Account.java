package account;

import observer.TransactionObserver;
import strategy.InterestStrategy;

public interface Account {
    String getId();
    String getOwner();
    double getBalance();
    String getType();
    String getDescription();

    void deposit(double amount);
    void withdraw(double amount);

    void addObserver(TransactionObserver observer);

    void setInterestStrategy(InterestStrategy strategy);
    double calculateInterest(int months);
}