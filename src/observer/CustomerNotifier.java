package observer;

import account.Transaction;

public class CustomerNotifier implements TransactionObserver {
    private final String customerName;

    public CustomerNotifier(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public void onTransaction(Transaction transaction) {
        System.out.println("[NOTIFICATION for " + customerName + "] " + transaction);
    }
}
