package observer;

import account.Account;
import account.Transaction;
import account.TransactionType;
import factory.AccountFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FraudDetectorTest {
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @BeforeEach
    void captureConsole() {
        originalOut = System.out;
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    void restoreConsole() {
        System.setOut(originalOut);
    }

    private Transaction topUp(double amount) {
        return new Transaction("ACC-1", "gulim", TransactionType.TOP_UP, amount);
    }

    @Test
    void alertsOnAmountAboveLimit() {
        new FraudDetector(10000).onTransaction(topUp(15000));

        assertTrue(output.toString().contains("FRAUD ALERT"));
    }

    @Test
    void staysSilentOnSmallAmount() {
        new FraudDetector(10000).onTransaction(topUp(500));

        assertFalse(output.toString().contains("FRAUD ALERT"));
    }

    @Test
    void amountEqualToLimitIsNotSuspicious() {
        new FraudDetector(10000).onTransaction(topUp(10000));

        assertFalse(output.toString().contains("FRAUD ALERT"));
    }

    @Test
    void alertMentionsOwnerAndAccount() {
        new FraudDetector(10000).onTransaction(topUp(15000));

        assertTrue(output.toString().contains("gulim"));
        assertTrue(output.toString().contains("ACC-1"));
    }

    @Test
    void worksWhenSubscribedToRealAccount() {
        Account account = AccountFactory.create("checking", "gulim", 0, 0);
        account.addObserver(new FraudDetector(10000));

        account.deposit(20000);

        assertTrue(output.toString().contains("FRAUD ALERT"));
    }
}