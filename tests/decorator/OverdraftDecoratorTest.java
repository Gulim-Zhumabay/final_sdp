package decorator;

import account.Account;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverdraftDecoratorTest {

    private Account checkingWith100() {
        return AccountFactory.create("checking", "gulim", 100, 0);
    }

    @Test
    void withdrawWithinBalanceDoesNotUseOverdraft() {
        Account account = new OverdraftDecorator(checkingWith100(), 500);

        account.withdraw(40);

        assertEquals(60.0, account.getBalance());
        assertTrue(account.getDescription().contains("used 0.0"));
    }

    @Test
    void withdrawBeyondBalanceUsesOverdraft() {
        Account account = new OverdraftDecorator(checkingWith100(), 500);

        account.withdraw(400);

        assertEquals(0.0, account.getBalance());
        assertTrue(account.getDescription().contains("used 300.0"));
    }

    @Test
    void withdrawBeyondLimitIsRejectedAndBalanceIsKept() {
        Account account = new OverdraftDecorator(checkingWith100(), 500);

        assertThrows(IllegalStateException.class, () -> account.withdraw(700));

        assertEquals(100.0, account.getBalance());
    }

    @Test
    void overdraftLimitIsSharedBetweenWithdrawals() {
        Account account = new OverdraftDecorator(checkingWith100(), 500);
        account.withdraw(400);

        assertThrows(IllegalStateException.class, () -> account.withdraw(300));
    }

    @Test
    void decoratorKeepsIdentityOfWrappedAccount() {
        Account original = checkingWith100();
        Account account = new OverdraftDecorator(original, 500);

        assertEquals(original.getId(), account.getId());
        assertEquals("gulim", account.getOwner());
        assertEquals("Checking", account.getType());
    }

    @Test
    void optionsCodeDescribesOverdraft() {
        Account account = new OverdraftDecorator(checkingWith100(), 500);
        account.withdraw(400);

        assertEquals("OVERDRAFT:500.0:300.0", account.getOptionsCode());
    }

    @Test
    void decoratorsCanBeStackedAndKeepTheirOrder() {
        Account account = new SmsDecorator(new OverdraftDecorator(checkingWith100(), 500));

        assertEquals("OVERDRAFT:500.0:0.0|SMS", account.getOptionsCode());
        assertTrue(account.getDescription().contains("Overdraft"));
        assertTrue(account.getDescription().endsWith("+ SMS"));
    }

    @Test
    void plainAccountHasNoOptions() {
        assertEquals("", checkingWith100().getOptionsCode());
    }
}