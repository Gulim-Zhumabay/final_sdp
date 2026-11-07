package service;

import account.Account;
import factory.AccountFactory;
import loan.Loan;
import loan.LoanAgreement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import storage.LoanCsvStore;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanServiceTest {
    @TempDir
    Path tempDir;

    private final Map<String, Account> accounts = new HashMap<>();
    private LoanCsvStore store;
    private LoanService service;
    private Account account;

    @BeforeEach
    void setUp() {
        store = new LoanCsvStore(tempDir.resolve("loans.csv"));
        service = new LoanService(store, accounts::get);
        account = AccountFactory.create("checking", "gulim", 0, 0);
        accounts.put(account.getId(), account);
    }

    private Loan issueStandardLoan() {
        LoanAgreement agreement = service.createAgreement("gulim", 1200, 12, "", false);
        return service.issue(account, agreement);
    }

    @Test
    void agreementWithoutGuarantorHasBaseRate() {
        LoanAgreement agreement = service.createAgreement("gulim", 1200, 12, "", false);

        assertEquals(12.0, agreement.getInterestRate());
        assertFalse(agreement.isInsured());
    }

    @Test
    void guarantorLowersTheRate() {
        LoanAgreement agreement = service.createAgreement("gulim", 1200, 12, "aiymay", false);

        assertEquals(10.0, agreement.getInterestRate());
        assertEquals("aiymay", agreement.getGuarantor());
    }

    @Test
    void insuranceFlagIsKept() {
        LoanAgreement agreement = service.createAgreement("gulim", 1200, 12, "", true);

        assertTrue(agreement.isInsured());
    }

    @Test
    void invalidAmountIsRejected() {
        assertThrows(IllegalStateException.class,
                () -> service.createAgreement("gulim", -1, 12, "", false));
    }

    @Test
    void issuingPaysTheAmountToTheAccount() {
        Loan loan = issueStandardLoan();

        assertEquals(1200.0, account.getBalance());
        assertEquals("LOAN-1", loan.getId());
        assertEquals(account.getId(), loan.getAccountId());
        assertEquals(0, loan.getMonthsPaid());
    }

    @Test
    void monthlyPaymentIsTotalDebtDividedByTerm() {
        Loan loan = issueStandardLoan();
        assertEquals(112.0, loan.nextPayment(), 0.001);
    }

    @Test
    void insuranceAddsTwoPercentToTheDebt() {
        LoanAgreement agreement = service.createAgreement("gulim", 1200, 12, "", true);
        Loan loan = service.issue(account, agreement);
        assertEquals(114.0, loan.nextPayment(), 0.001);
    }

    @Test
    void collectingTakesOnePaymentFromTheAccount() {
        Loan loan = issueStandardLoan();

        List<String> reports = service.collectPayments();

        assertEquals(1088.0, account.getBalance(), 0.001);
        assertEquals(1, loan.getMonthsPaid());
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).contains("payment 1 of 12"));
    }

    @Test
    void missedPaymentIsReportedAndNotCounted() {
        Loan loan = issueStandardLoan();
        account.withdraw(1150);

        List<String> reports = service.collectPayments();

        assertTrue(reports.get(0).contains("LOAN OVERDUE"));
        assertEquals(0, loan.getMonthsPaid());
        assertEquals(50.0, account.getBalance(), 0.001);
    }

    @Test
    void loanIsRepaidAfterAllPaymentsAndThenStopsCollecting() {
        Loan loan = issueStandardLoan();
        account.deposit(144);

        List<String> last = List.of();
        for (int month = 0; month < 12; month++) {
            last = service.collectPayments();
        }

        assertTrue(loan.isRepaid());
        assertTrue(last.get(0).contains("fully repaid"));
        assertEquals(0.0, account.getBalance(), 0.001);
        assertTrue(service.collectPayments().isEmpty());
    }

    @Test
    void loanCannotBePaidToSomeoneElsesAccount() {
        LoanAgreement agreement = service.createAgreement("aiymay", 1200, 12, "", false);

        assertThrows(IllegalArgumentException.class, () -> service.issue(account, agreement));
        assertEquals(0.0, account.getBalance());
    }

    @Test
    void loanCannotBePaidToTermDeposit() {
        Account deposit = AccountFactory.create("deposit", "gulim", 0, 3);
        accounts.put(deposit.getId(), deposit);
        LoanAgreement agreement = service.createAgreement("gulim", 1200, 12, "", false);

        assertThrows(IllegalStateException.class, () -> service.issue(deposit, agreement));
    }

    @Test
    void loansAreListedPerBorrower() {
        issueStandardLoan();

        assertEquals(1, service.getLoansOf("gulim").size());
        assertTrue(service.getLoansOf("aiymay").isEmpty());
    }

    @Test
    void loansSurviveRestartWithTheirProgress() {
        issueStandardLoan();
        service.collectPayments();

        LoanService restarted = new LoanService(store, accounts::get);

        List<Loan> loans = restarted.getLoansOf("gulim");
        assertEquals(1, loans.size());
        assertEquals(1, loans.get(0).getMonthsPaid());
        assertEquals(12, loans.get(0).getAgreement().getMonths());
    }
}