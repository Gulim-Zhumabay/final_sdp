package observer;

import account.Account;
import account.Transaction;
import account.TransactionType;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountObserverTest {

    private static class RecordingObserver implements TransactionObserver {
        private final List<Transaction> received = new ArrayList<>();

        @Override
        public void onTransaction(Transaction transaction) {
            received.add(transaction);
        }
    }

    private Account newAccount() {
        return AccountFactory.create("checking", "gulim", 0, 0);
    }

    @Test
    void observerReceivesTopUp() {
        Account account = newAccount();
        RecordingObserver observer = new RecordingObserver();
        account.addObserver(observer);

        account.deposit(100);

        assertEquals(1, observer.received.size());
        Transaction transaction = observer.received.get(0);
        assertEquals(TransactionType.TOP_UP, transaction.getType());
        assertEquals(100.0, transaction.getAmount());
        assertEquals("gulim", transaction.getOwner());
        assertEquals(account.getId(), transaction.getAccountId());
    }

    @Test
    void observerReceivesWithdrawal() {
        Account account = newAccount();
        account.deposit(100);
        RecordingObserver observer = new RecordingObserver();
        account.addObserver(observer);

        account.withdraw(30);

        assertEquals(1, observer.received.size());
        assertEquals(TransactionType.WITHDRAW, observer.received.get(0).getType());
    }

    @Test
    void everySubscriberIsNotified() {
        Account account = newAccount();
        RecordingObserver first = new RecordingObserver();
        RecordingObserver second = new RecordingObserver();
        account.addObserver(first);
        account.addObserver(second);

        account.deposit(100);

        assertEquals(1, first.received.size());
        assertEquals(1, second.received.size());
    }

    @Test
    void failedOperationDoesNotNotifyAnyone() {
        Account account = newAccount();
        RecordingObserver observer = new RecordingObserver();
        account.addObserver(observer);

        assertThrows(IllegalStateException.class, () -> account.withdraw(50));

        assertTrue(observer.received.isEmpty());
    }

    @Test
    void typedOperationKeepsItsType() {
        Account account = newAccount();
        RecordingObserver observer = new RecordingObserver();
        account.addObserver(observer);

        account.deposit(100, TransactionType.INTEREST);

        assertEquals(TransactionType.INTEREST, observer.received.get(0).getType());
    }
}