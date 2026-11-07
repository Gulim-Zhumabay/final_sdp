package service;

import account.Account;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountOptionServiceTest {
    private final List<Account> replaced = new ArrayList<>();
    private final AccountOptionService service = new AccountOptionService(replaced::add);

    private Account checking() {
        return AccountFactory.create("checking", "gulim", 100, 0);
    }

    @Test
    void addsOverdraftToCheckingAndReplacesTheAccount() {
        Account result = service.addOverdraft(checking(), 500);

        assertTrue(result.getDescription().contains("Overdraft"));
        assertEquals(1, replaced.size());
        assertSame(result, replaced.get(0));
    }

    @Test
    void overdraftIsRejectedForDeposit() {
        Account deposit = AccountFactory.create("deposit", "gulim", 1000, 3);

        assertThrows(IllegalStateException.class, () -> service.addOverdraft(deposit, 500));

        assertTrue(replaced.isEmpty());
    }

    @Test
    void overdraftLimitMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> service.addOverdraft(checking(), 0));

        assertTrue(replaced.isEmpty());
    }

    @Test
    void smsCanBeAddedToAnyAccount() {
        Account deposit = AccountFactory.create("deposit", "gulim", 1000, 3);

        Account result = service.addSms(deposit);

        assertTrue(result.getDescription().endsWith("+ SMS"));
        assertEquals(deposit.getId(), result.getId());
    }

    @Test
    void optionsStackInOrder() {
        Account withOverdraft = service.addOverdraft(checking(), 500);

        Account withBoth = service.addSms(withOverdraft);

        assertEquals("OVERDRAFT:500.0:0.0|SMS", withBoth.getOptionsCode());
        assertEquals(2, replaced.size());
    }
}