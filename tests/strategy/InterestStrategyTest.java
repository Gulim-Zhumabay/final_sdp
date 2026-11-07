package strategy;

import account.Account;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterestStrategyTest {

    @Test
    void depositInterestForOneMonth() {
        InterestStrategy strategy = new DepositInterest();

        assertEquals(15.0, strategy.calculate(1200, 1), 0.001);
    }

    @Test
    void depositInterestIsCompoundedOverAYear() {
        InterestStrategy strategy = new DepositInterest();

        assertEquals(160.75, strategy.calculate(1000, 12), 0.02);
    }

    @Test
    void depositInterestIsHigherThanSimpleInterestOfSameRate() {
        double compound = new DepositInterest().calculate(1000, 12);
        double simple = 1000 * 0.15;

        assertTrue(compound > simple);
    }

    @Test
    void loanInterestUsesRateFromAgreement() {
        assertEquals(500.0, new LoanInterest(10).calculate(5000, 12), 0.001);
        assertEquals(600.0, new LoanInterest(12).calculate(5000, 12), 0.001);
    }

    @Test
    void loanInterestIsProportionalToTerm() {
        assertEquals(250.0, new LoanInterest(10).calculate(5000, 6), 0.001);
    }

    @Test
    void loanInterestSupportsFractionalRate() {
        assertEquals(1050.0, new LoanInterest(10.5).calculate(10000, 12), 0.001);
    }

    @Test
    void zeroMonthsMeansZeroInterest() {
        assertEquals(0.0, new LoanInterest(12).calculate(5000, 0), 0.001);
        assertEquals(0.0, new DepositInterest().calculate(5000, 0), 0.001);
    }

    @Test
    void accountResultChangesWhenStrategyIsSwapped() {
        Account account = AccountFactory.create("checking", "gulim", 1000, 0);

        account.setInterestStrategy(new LoanInterest(12));
        double loanLike = account.calculateInterest(12);
        account.setInterestStrategy(new DepositInterest());
        double depositLike = account.calculateInterest(12);

        assertEquals(120.0, loanLike, 0.001);
        assertEquals(160.75, depositLike, 0.02);
    }
}