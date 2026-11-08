package account;
import java.util.Locale;

public enum TransactionType {
    TOP_UP,
    WITHDRAW,
    TRANSFER_IN,
    TRANSFER_OUT,
    INTEREST,
    LOAN_PAYOUT,
    LOAN_PAYMENT,
    EXTERNAL_PAYMENT,
    REFUND,
    OVERDRAFT,
    OVERDRAFT_REPAY;

    public String getLabel() {
        String words = name().replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }
}
