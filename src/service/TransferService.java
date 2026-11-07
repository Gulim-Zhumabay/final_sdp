package service;

import account.Account;
import account.TransactionType;

public class TransferService {

    public void transfer(Account from, Account to, double amount) {
        if (from.getId().equals(to.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        from.withdraw(amount, TransactionType.TRANSFER_OUT);
        try {
            to.deposit(amount, TransactionType.TRANSFER_IN);
        } catch (IllegalStateException e) {
            from.deposit(amount, TransactionType.REFUND);
            throw e;
        }
    }
}