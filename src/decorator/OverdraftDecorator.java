package decorator;

import account.Account;

public class OverdraftDecorator extends AccountDecorator {
    private final double overdraftLimit;
    private double overdraftUsed = 0;

    public OverdraftDecorator(Account wrapped, double overdraftLimit) {
        super(wrapped);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount) {
        double balance = wrapped.getBalance();
        if (amount <= balance) {
            wrapped.withdraw(amount);
        } else if (amount <= balance + overdraftLimit - overdraftUsed) {
            double extra = amount - balance;
            if (balance > 0) {
                wrapped.withdraw(balance);
            }
            overdraftUsed += extra;
            System.out.println("[OVERDRAFT] Used " + extra + " of overdraft");
        } else {
            throw new IllegalStateException("Overdraft limit exceeded");
        }
    }

    @Override
    public String getDescription() {
        return wrapped.getDescription() + " + Overdraft(limit " + overdraftLimit + ", used " + overdraftUsed + ")";
    }
}