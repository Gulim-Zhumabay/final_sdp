package observer;

import account.Transaction;

public class FraudDetector implements TransactionObserver {
    private final double limit;

    public FraudDetector(double limit) {
        this.limit = limit;
    }

    @Override
    public void onTransaction(Transaction transaction) {
        if (transaction.getAmount() > limit) {
            System.out.println("[FRAUD ALERT] Suspicious transaction: " + transaction);
        }
    }
}
