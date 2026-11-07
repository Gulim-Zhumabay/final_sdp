package service;

import account.Account;
import account.Transaction;
import account.TransactionType;
import factory.AccountFactory;
import org.junit.jupiter.api.Test;
import payment.PaymentGateway;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalPaymentServiceTest {

    private static class FakeGateway implements PaymentGateway {
        private final String name;
        private final boolean succeeds;
        private int calls = 0;

        FakeGateway(String name, boolean succeeds) {
            this.name = name;
            this.succeeds = succeeds;
        }

        @Override
        public boolean pay(String fromAccount, String toAccount, double amount) {
            calls++;
            return succeeds;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private Account checking(double balance) {
        return AccountFactory.create("checking", "gulim", balance, 0);
    }

    @Test
    void successfulPaymentWithdrawsMoney() {
        FakeGateway gateway = new FakeGateway("Fake", true);
        ExternalPaymentService service = new ExternalPaymentService(List.of(gateway));
        Account account = checking(500);

        service.pay(account, "Fake", "Coffee Shop", 120);

        assertEquals(380.0, account.getBalance());
        assertEquals(1, gateway.calls);
    }

    @Test
    void rejectedPaymentReturnsMoneyAndReportsError() {
        FakeGateway gateway = new FakeGateway("Fake", false);
        ExternalPaymentService service = new ExternalPaymentService(List.of(gateway));
        Account account = checking(500);
        List<Transaction> seen = new ArrayList<>();
        account.addObserver(seen::add);

        assertThrows(IllegalStateException.class,
                () -> service.pay(account, "Fake", "Coffee Shop", 120));

        assertEquals(500.0, account.getBalance());
        assertEquals(TransactionType.EXTERNAL_PAYMENT, seen.get(0).getType());
        assertEquals(TransactionType.REFUND, seen.get(1).getType());
    }

    @Test
    void insufficientFundsNeverReachesTheGateway() {
        FakeGateway gateway = new FakeGateway("Fake", true);
        ExternalPaymentService service = new ExternalPaymentService(List.of(gateway));
        Account account = checking(50);

        assertThrows(IllegalStateException.class,
                () -> service.pay(account, "Fake", "Coffee Shop", 120));

        assertEquals(0, gateway.calls);
        assertEquals(50.0, account.getBalance());
    }

    @Test
    void unknownPaymentSystemIsRejected() {
        ExternalPaymentService service =
                new ExternalPaymentService(List.of(new FakeGateway("Fake", true)));
        Account account = checking(500);

        assertThrows(IllegalArgumentException.class,
                () -> service.pay(account, "Bitcoin", "Coffee Shop", 120));

        assertEquals(500.0, account.getBalance());
    }

    @Test
    void blankRecipientIsRejectedBeforeAnyMoneyMoves() {
        FakeGateway gateway = new FakeGateway("Fake", true);
        ExternalPaymentService service = new ExternalPaymentService(List.of(gateway));
        Account account = checking(500);

        assertThrows(IllegalArgumentException.class,
                () -> service.pay(account, "Fake", "  ", 120));

        assertEquals(500.0, account.getBalance());
        assertEquals(0, gateway.calls);
    }

    @Test
    void serviceListsGatewaysInGivenOrder() {
        ExternalPaymentService service = new ExternalPaymentService(List.of(
                new FakeGateway("Visa", true), new FakeGateway("SWIFT", true)));

        assertEquals(List.of("Visa", "SWIFT"), service.getGatewayNames());
    }

    @Test
    void newPaymentSystemWorksWithoutChangingTheService() {
        FakeGateway extra = new FakeGateway("NewPay", true);
        ExternalPaymentService service = new ExternalPaymentService(List.of(extra));
        Account account = checking(500);

        service.pay(account, "NewPay", "Shop", 10);

        assertTrue(service.getGatewayNames().contains("NewPay"));
        assertFalse(account.getBalance() == 500.0);
    }
}