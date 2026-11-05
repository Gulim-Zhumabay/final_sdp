package service;

import account.Account;
import account.TransactionType;
import strategy.InterestStrategy;

public class InterestService {

    public double accrue(Account account, InterestStrategy strategy, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Months must be positive");
        }
        account.setInterestStrategy(strategy);
        double interest = account.calculateInterest(months);
        if (interest <= 0) {
            throw new IllegalStateException("Nothing to accrue on this balance");
        }
        account.deposit(interest, TransactionType.INTEREST);
        return interest;
    }
}