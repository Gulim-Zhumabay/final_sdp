package observer;

import account.Transaction;

public class CustomerNotifier implements TransactionObserver {

    @Override
    public void onTransaction(Transaction transaction) {
        System.out.println("[ACTIVITY] " + transaction.describe());
    }
}