package account;

import java.util.Optional;

public class DepositAccount extends AbstractAccount {
    private final int termMonths;
    private int monthsElapsed;

    public DepositAccount(String id, String owner, double initialBalance,
                          int termMonths, int monthsElapsed) {
        super(id, owner, initialBalance);
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Deposit term must be positive");
        }
        this.termMonths = termMonths;
        this.monthsElapsed = monthsElapsed;
    }

    @Override
    public String getType() {
        return "Deposit";
    }

    @Override
    public int getTermMonths() {
        return termMonths;
    }

    @Override
    public int getMonthsElapsed() {
        return monthsElapsed;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + ", term: " + monthsElapsed + "/" + termMonths + " months";
    }

    @Override
    public void deposit(double amount, TransactionType type) {
        if (type != TransactionType.INTEREST && balance > 0) {
            throw new IllegalStateException("A term deposit cannot be topped up");
        }
        super.deposit(amount, type);
    }

    @Override
    public void withdraw(double amount, TransactionType type) {
        if (!isMatured()) {
            throw new IllegalStateException(
                    "Deposit is locked, " + (termMonths - monthsElapsed) + " month(s) left");
        }
        super.withdraw(amount, type);
    }

    @Override
    public Optional<String> endOfMonth() {
        if (isMatured() || balance <= 0) {
            return Optional.empty();
        }
        double interest = Math.round(calculateInterest(1) * 100) / 100.0;
        monthsElapsed++;
        deposit(interest, TransactionType.INTEREST);
        return Optional.of(monthlyMessage(interest));
    }

    private boolean isMatured() {
        return monthsElapsed >= termMonths;
    }

    private String monthlyMessage(double interest) {
        String message = String.format("[INTEREST] %s (%s): +%.2f for month %d of %d. Balance: %.2f",
                getId(), getOwner(), interest, monthsElapsed, termMonths, balance);
        return isMatured() ? message + " Deposit matured, funds are available." : message;
    }
}