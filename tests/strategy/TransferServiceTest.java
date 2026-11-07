package service;

import account.Account;
import account.Transaction;
import account.TransactionType;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferServiceTest {
    private final TransferService service = new TransferService();

    private Account checking(double balance) {
        return AccountFactory.create("checking", "gulim", balance, 0);
    }

    @Test
    void movesMoneyBetweenAccounts() {
        Account sender = checking(500);
        Account receiver = checking(100);

        service.transfer(sender, receiver, 200);

        assertEquals(300.0, sender.getBalance());
        assertEquals(300.0, receiver.getBalance());
    }

    @Test
    void sameAccountIsRejected() {
        Account account = checking(500);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(account, account, 100));
        assertEquals(500.0, account.getBalance());
    }

    @Test
    void insufficientFundsChangesNothing() {
        Account sender = checking(50);
        Account receiver = checking(10);

        assertThrows(IllegalStateException.class, () -> service.transfer(sender, receiver, 100));

        assertEquals(50.0, sender.getBalance());
        assertEquals(10.0, receiver.getBalance());
    }

    @Test
    void negativeAmountIsRejected() {
        Account sender = checking(500);
        Account receiver = checking(0);

        assertThrows(IllegalArgumentException.class, () -> service.transfer(sender, receiver, -5));
    }

    @Test
    void observersSeeBothSidesOfTransfer() {
        Account sender = checking(500);
        Account receiver = checking(0);
        List<Transaction> seen = new ArrayList<>();
        sender.addObserver(seen::add);
        receiver.addObserver(seen::add);

        service.transfer(sender, receiver, 100);

        assertEquals(2, seen.size());
        assertEquals(TransactionType.TRANSFER_OUT, seen.get(0).getType());
        assertEquals(TransactionType.TRANSFER_IN, seen.get(1).getType());
    }

    @Test
    void moneyIsReturnedWhenReceiverRefuses() {
        Account sender = checking(500);
        Account lockedDeposit = AccountFactory.create("deposit", "aiymay", 1000, 3);
        List<Transaction> seen = new ArrayList<>();
        sender.addObserver(seen::add);

        assertThrows(IllegalStateException.class, () -> service.transfer(sender, lockedDeposit, 100));

        assertEquals(500.0, sender.getBalance());
        assertEquals(1000.0, lockedDeposit.getBalance());
        assertEquals(TransactionType.TRANSFER_OUT, seen.get(0).getType());
        assertEquals(TransactionType.REFUND, seen.get(1).getType());
    }

    @Test
    void cannotTransferOutOfLockedDeposit() {
        Account lockedDeposit = AccountFactory.create("deposit", "gulim", 1000, 3);
        Account receiver = checking(0);

        assertThrows(IllegalStateException.class, () -> service.transfer(lockedDeposit, receiver, 100));

        assertEquals(1000.0, lockedDeposit.getBalance());
        assertEquals(0.0, receiver.getBalance());
    }
}