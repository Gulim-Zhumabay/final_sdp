package service;

import account.Account;
import account.TransactionType;
import loan.Loan;
import loan.LoanAgreement;
import loan.LoanAgreementBuilder;
import storage.LoanCsvStore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LoanService {
    private static final double GUARANTOR_RATE = 10.0;
    private static final String ID_PREFIX = "LOAN-";

    private final LoanCsvStore store;
    private final Function<String, Account> accountLookup;
    private final List<Loan> loans;

    public LoanService(LoanCsvStore store, Function<String, Account> accountLookup) {
        this.store = store;
        this.accountLookup = accountLookup;
        this.loans = new ArrayList<>(store.loadAll());
    }

    public LoanAgreement createAgreement(String borrower, double amount, int months,
                                         String guarantor, boolean insured) {
        LoanAgreementBuilder builder = new LoanAgreementBuilder(borrower, amount, months);
        if (!guarantor.isBlank()) {
            builder.guarantor(guarantor).interestRate(GUARANTOR_RATE);
        }
        if (insured) {
            builder.withInsurance();
        }
        return builder.build();
    }

    public Loan issue(Account account, LoanAgreement agreement) {
        requireEligible(account, agreement);
        Loan loan = new Loan(nextId(), account.getId(), agreement, 0);
        account.deposit(agreement.getAmount(), TransactionType.LOAN_PAYOUT);
        loans.add(loan);
        store.saveAll(loans);
        return loan;
    }

    public List<Loan> getLoansOf(String borrower) {
        return loans.stream()
                .filter(loan -> loan.getAgreement().getBorrower().equals(borrower))
                .collect(Collectors.toList());
    }

    public List<String> collectPayments() {
        List<String> reports = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isRepaid()) {
                reports.add(collect(loan));
            }
        }
        store.saveAll(loans);
        return reports;
    }

    private String collect(Loan loan) {
        Account account = accountLookup.apply(loan.getAccountId());
        double payment = loan.nextPayment();
        String borrower = loan.getAgreement().getBorrower();
        try {
            account.withdraw(payment, TransactionType.LOAN_PAYMENT);
        } catch (IllegalStateException e) {
            return String.format("[LOAN OVERDUE] %s (%s): payment %.2f not collected, "
                    + "not enough money on %s", loan.getId(), borrower, payment, account.getId());
        }
        loan.recordPayment();
        String message = String.format("[LOAN] %s (%s): paid %.2f from %s, payment %d of %d.",
                loan.getId(), borrower, payment, account.getId(),
                loan.getMonthsPaid(), loan.getAgreement().getMonths());
        return loan.isRepaid() ? message + " Loan fully repaid." : message;
    }

    private void requireEligible(Account account, LoanAgreement agreement) {
        if (!agreement.getBorrower().equals(account.getOwner())) {
            throw new IllegalArgumentException("The loan can be paid out only to your own account");
        }
        if (account.getType().equals("Deposit")) {
            throw new IllegalStateException("A loan cannot be paid out to a term deposit");
        }
    }

    private String nextId() {
        return ID_PREFIX + (loans.size() + 1);
    }
}