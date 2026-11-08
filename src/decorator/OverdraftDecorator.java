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
    public OverdraftDecorator(Account wrapped, double overdraftLimit, double overdraftUsed) {
        super(wrapped);
        this.overdraftLimit = overdraftLimit;
        this.overdraftUsed = overdraftUsed;
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
        overdraftUsed += shortfall;
        if (balance > 0) {
            wrapped.withdraw(balance, type);
        }
        System.out.println("[OVERDRAFT] Overdraft used: "
                + String.format(java.util.Locale.US, "%,.2f", shortfall) + " on " + getId());
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription()
                + " + Overdraft(limit " + overdraftLimit + ", used " + overdraftUsed + ")";
    }
    @Override
    protected String getOwnCode() {
        return "OVERDRAFT:" + overdraftLimit + ":" + overdraftUsed;
    }
}