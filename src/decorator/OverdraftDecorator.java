package decorator;

import account.Account;
import account.TransactionType;

public class OverdraftDecorator extends AccountDecorator {
    private final double overdraftLimit;
    private double overdraftUsed = 0;

    public OverdraftDecorator(Account wrapped, double overdraftLimit) {
        super(wrapped);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount, TransactionType type) {
        double balance = wrapped.getBalance();
        if (amount <= balance) {
            wrapped.withdraw(amount, type);
            return;
        }

        double shortfall = amount - balance;
        if (shortfall > overdraftLimit - overdraftUsed) {
            throw new IllegalStateException("Overdraft limit exceeded");
        }
        if (balance > 0) {
            wrapped.withdraw(balance, type);
        }
        overdraftUsed += shortfall;
        System.out.println("[OVERDRAFT] Used " + shortfall + " of overdraft");
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription()
                + " + Overdraft(limit " + overdraftLimit + ", used " + overdraftUsed + ")";
    }
}