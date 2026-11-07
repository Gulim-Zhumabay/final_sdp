package decorator;

import account.Account;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecoratorFactoryTest {

    private Account checking() {
        return AccountFactory.create("checking", "gulim", 100, 0);
    }

    @Test
    void dashMeansNoOptions() {
        Account account = checking();

        assertSame(account, DecoratorFactory.applyOptions(account, "-"));
    }

    @Test
    void blankMeansNoOptions() {
        Account account = checking();

        assertSame(account, DecoratorFactory.applyOptions(account, ""));
    }

    @Test
    void restoresOverdraftWithUsedAmount() {
        Account account = DecoratorFactory.applyOptions(checking(), "OVERDRAFT:500.0:300.0");

        assertTrue(account.getDescription().contains("limit 500.0, used 300.0"));
    }

    @Test
    void restoresWholeChainInOrder() {
        String code = "OVERDRAFT:500.0:300.0|SMS";

        Account account = DecoratorFactory.applyOptions(checking(), code);

        assertEquals(code, account.getOptionsCode());
    }

    @Test
    void unknownOptionIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> DecoratorFactory.applyOptions(checking(), "GOLD"));
    }
}