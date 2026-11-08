package account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTest {

    @Test
    void describesOperationInPlainWords() {
        Transaction transaction =
                new Transaction("ACC-1", "gulim", TransactionType.TOP_UP, 20000);

        assertEquals("Top up 20,000.00 on ACC-1", transaction.describe());
    }

    @Test
    void operationLabelsAreReadable() {
        assertEquals("Transfer out", TransactionType.TRANSFER_OUT.getLabel());
        assertEquals("External payment", TransactionType.EXTERNAL_PAYMENT.getLabel());
    }
}