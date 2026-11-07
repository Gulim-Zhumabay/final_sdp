package account;

import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DepositAccountTest {

    private Account deposit1200For3Months() {
        return AccountFactory.create("deposit", "gulim", 1200, 3);
    }

    @Test
    void newDepositIsLockedUntilTermEnds() {
        Account account = deposit1200For3Months();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> account.withdraw(100));

        assertTrue(error.getMessage().contains("3 month(s) left"));
        assertEquals(1200.0, account.getBalance());
    }

    @Test
    void lockMessageShowsRemainingMonths() {
        Account account = deposit1200For3Months();
        account.endOfMonth();

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> account.withdraw(100));

        assertTrue(error.getMessage().contains("2 month(s) left"));
    }

    @Test
    void depositCannotBeToppedUpAfterOpening() {
        Account account = deposit1200For3Months();

        assertThrows(IllegalStateException.class, () -> account.deposit(100));
    }

    @Test
    void emptyDepositAcceptsFirstFundingOnly() {
        Account account = AccountFactory.create("deposit", "gulim", 0, 3);

        account.deposit(500);

        assertEquals(500.0, account.getBalance());
        assertThrows(IllegalStateException.class, () -> account.deposit(100));
    }

    @Test
    void refundIsAllowedEvenOnFundedDeposit() {
        Account account = deposit1200For3Months();

        account.deposit(50, TransactionType.REFUND);

        assertEquals(1250.0, account.getBalance());
    }

    @Test
    void firstMonthPaysFifteenPercentPerYearDividedByTwelve() {
        Account account = deposit1200For3Months();

        Optional<String> report = account.endOfMonth();

        assertTrue(report.isPresent());
        assertTrue(report.get().contains("month 1 of 3"));
        assertEquals(1215.0, account.getBalance(), 0.001);
        assertEquals(1, account.getMonthsElapsed());
    }

    @Test
    void interestIsCompoundedMonthByMonth() {
        Account account = deposit1200For3Months();

        account.endOfMonth();
        account.endOfMonth();
        account.endOfMonth();
        assertEquals(1245.57, account.getBalance(), 0.001);
    }

    @Test
    void lastMonthReportsMaturityAndInterestStopsAfterwards() {
        Account account = deposit1200For3Months();
        account.endOfMonth();
        account.endOfMonth();

        Optional<String> last = account.endOfMonth();

        assertTrue(last.get().contains("matured"));
        assertTrue(account.endOfMonth().isEmpty());
        assertEquals(3, account.getMonthsElapsed());
    }

    @Test
    void fundsAreAvailableAfterMaturity() {
        Account account = deposit1200For3Months();
        account.endOfMonth();
        account.endOfMonth();
        account.endOfMonth();

        account.withdraw(500);

        assertEquals(745.57, account.getBalance(), 0.001);
    }

    @Test
    void emptyDepositEarnsNothing() {
        Account account = AccountFactory.create("deposit", "gulim", 0, 3);

        assertTrue(account.endOfMonth().isEmpty());
        assertEquals(0, account.getMonthsElapsed());
    }

    @Test
    void termMustBePositive() {
        assertThrows(IllegalArgumentException.class,
                () -> AccountFactory.create("deposit", "gulim", 100, 0));
    }

    @Test
    void descriptionShowsTermProgress() {
        Account account = deposit1200For3Months();
        account.endOfMonth();

        assertTrue(account.getDescription().contains("term: 1/3 months"));
    }

    @Test
    void checkingAccountIgnoresEndOfMonth() {
        Account account = AccountFactory.create("checking", "gulim", 1000, 0);

        assertTrue(account.endOfMonth().isEmpty());
        assertEquals(1000.0, account.getBalance());
    }
}