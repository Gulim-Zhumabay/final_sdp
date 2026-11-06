package loan;

import strategy.InterestStrategy;
import strategy.LoanInterest;

public class Loan {
    private static final double INSURANCE_RATE = 0.02;

    private final String id;
    private final String accountId;
    private final LoanAgreement agreement;
    private final double totalToRepay;
    private final double monthlyPayment;
    private int monthsPaid;

    public Loan(String id, String accountId, LoanAgreement agreement, int monthsPaid) {
        this.id = id;
        this.accountId = accountId;
        this.agreement = agreement;
        this.monthsPaid = monthsPaid;

        InterestStrategy strategy = new LoanInterest(agreement.getInterestRate());
        double interest = strategy.calculate(agreement.getAmount(), agreement.getMonths());
        double insuranceFee = agreement.isInsured() ? agreement.getAmount() * INSURANCE_RATE : 0;
        this.totalToRepay = round(agreement.getAmount() + interest + insuranceFee);
        this.monthlyPayment = round(totalToRepay / agreement.getMonths());
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public LoanAgreement getAgreement() { return agreement; }
    public int getMonthsPaid() { return monthsPaid; }

    public boolean isRepaid() {
        return monthsPaid >= agreement.getMonths();
    }

    public double nextPayment() {
        if (isRepaid()) {
            throw new IllegalStateException("Loan is already repaid");
        }
        boolean lastPayment = monthsPaid == agreement.getMonths() - 1;
        return lastPayment ? round(totalToRepay - monthlyPayment * monthsPaid) : monthlyPayment;
    }

    public void recordPayment() {
        monthsPaid++;
    }

    public String getDescription() {
        return String.format("%s on %s: %s | total to repay: %.2f, monthly: %.2f, paid %d/%d",
                id, accountId, agreement, totalToRepay, monthlyPayment,
                monthsPaid, agreement.getMonths());
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}