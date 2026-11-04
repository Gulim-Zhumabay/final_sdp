package observer;

import account.Transaction;
import storage.TransactionCsvStore;

public class TransactionLogger implements TransactionObserver {
    private final TransactionCsvStore store;

    public TransactionLogger(TransactionCsvStore store) {
        this.store = store;
    }

    @Override
    public void onTransaction(Transaction transaction) {
        store.append(transaction);
    }
}
