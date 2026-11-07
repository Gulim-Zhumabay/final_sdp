package factory;

import account.Account;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountFactoryTest {

    @Test
    void createsCheckingAccountWithBalance() {
        Account account = AccountFactory.create("checking", "gulim", 1000, 0);

        assertEquals("Checking", account.getType());
        assertEquals("gulim", account.getOwner());
        assertEquals(1000.0, account.getBalance());
    }

    @Test
    void createsDepositWithTerm() {
        Account account = AccountFactory.create("deposit", "gulim", 1000, 3);

        assertEquals("Deposit", account.getType());
        assertEquals(3, account.getTermMonths());
        assertEquals(0, account.getMonthsElapsed());
    }

    @Test
    void typeNameIsCaseInsensitive() {
        Account account = AccountFactory.create("CHECKING", "gulim", 0, 0);

        assertEquals("Checking", account.getType());
    }

    @Test
    void unknownTypeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> AccountFactory.create("gold", "gulim", 0, 0));
    }

    @Test
    void savingsTypeIsNoLongerSupported() {
        assertThrows(IllegalArgumentException.class,
                () -> AccountFactory.create("savings", "gulim", 0, 0));
    }

    @Test
    void everyAccountGetsItsOwnId() {
        Account first = AccountFactory.create("checking", "gulim", 0, 0);
        Account second = AccountFactory.create("checking", "gulim", 0, 0);

        assertNotEquals(first.getId(), second.getId());
    }

    @Test
    void restoredAccountKeepsIdAndNextIdContinuesAfterIt() {
        Account restored = AccountFactory.restore("ACC-500", "checking", "gulim", 70, 0, 0);
        Account next = AccountFactory.create("checking", "gulim", 0, 0);

        assertEquals("ACC-500", restored.getId());
        assertEquals("ACC-501", next.getId());
    }

    @Test
    void depositGetsInterestStrategy() {
        Account account = AccountFactory.create("deposit", "gulim", 1200, 12);

        assertEquals(15.0, account.calculateInterest(1), 0.001);
    }

    @Test
    void checkingHasNoInterest() {
        Account account = AccountFactory.create("checking", "gulim", 1000, 0);

        assertThrows(IllegalStateException.class, () -> account.calculateInterest(1));
    }
}