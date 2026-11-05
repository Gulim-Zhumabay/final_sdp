package account;

import java.time.LocalDateTime;

public class Transaction {
    private final String accountId;
    private final String owner;
    private final TransactionType type;
    private final double amount;
    private final LocalDateTime time;

    public Transaction(String accountId, String owner, TransactionType type, double amount) {
        this(accountId, owner, type, amount, LocalDateTime.now());
    }

    public Transaction(String accountId, String owner, TransactionType type,
                       double amount, LocalDateTime time) {
        this.accountId = accountId;
        this.owner = owner;
        this.type = type;
        this.amount = amount;
        this.time = time;
    }

    public String getAccountId() { return accountId; }
    public String getOwner() { return owner; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTime() { return time; }

    @Override
    public String toString() {
        return type + " " + amount + " on " + accountId + " (" + owner + ")";
    }
}