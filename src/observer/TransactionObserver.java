package observer;

import account.Transaction;

public interface TransactionObserver {
    void onTransaction(Transaction transaction);
}