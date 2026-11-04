package account;

import java.time.LocalDateTime;

public class Transaction {
    private final String accountId;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime time;

    public Transaction(String accountId, TransactionType type, double amount) {
        this(
                accountId,
                type,
                amount,
                LocalDateTime.now());
    }

    public Transaction(String accountId, TransactionType type, double amount, LocalDateTime time) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.time = time;
    }

    public String getAccountId() { return accountId; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }

    @Override
    public String toString() {
        return type + " " + amount + " on " + accountId;
    }
}