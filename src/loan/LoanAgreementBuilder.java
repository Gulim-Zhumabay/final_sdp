package loan;

public class LoanAgreementBuilder {
    private final String borrower;
    private final double amount;
    private final int months;

    private double interestRate = 12.0;
    private String guarantor = null;
    private boolean insured = false;

    public LoanAgreementBuilder(String borrower, double amount, int months) {
        this.borrower = borrower;
        this.amount = amount;
        this.months = months;
    }

    public LoanAgreementBuilder interestRate(double interestRate) {
        this.interestRate = interestRate;
        return this;
    }

    public LoanAgreementBuilder guarantor(String guarantor) {
        this.guarantor = guarantor;
        return this;
    }

    public LoanAgreementBuilder withInsurance() {
        this.insured = true;
        return this;
    }

    public LoanAgreement build() {
        if (amount <= 0 || months <= 0) {
            throw new IllegalStateException("Amount and months must be positive");
        }
        return new LoanAgreement(borrower, amount, months, interestRate, guarantor, insured);
    }
}