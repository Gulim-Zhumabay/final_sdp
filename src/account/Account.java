package account;

import observer.TransactionObserver;
import strategy.InterestStrategy;

import java.util.Optional;

public interface Account {
    String getId();
    String getOwner();
    double getBalance();
    String getType();
    String getDescription();

    void deposit(double amount, TransactionType type);
    void withdraw(double amount, TransactionType type);

    default void deposit(double amount) {
        deposit(amount, TransactionType.TOP_UP);
    }

    default void withdraw(double amount) {
        withdraw(amount, TransactionType.WITHDRAW);
    }

    void addObserver(TransactionObserver observer);

    void setInterestStrategy(InterestStrategy strategy);
    double calculateInterest(int months);

    default Optional<String> endOfMonth() {
        return Optional.empty();
    }

    default int getTermMonths() {
        return 0;
    }

    default int getMonthsElapsed() {
        return 0;
    }
}