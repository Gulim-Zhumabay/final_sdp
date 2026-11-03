package loan;

public class LoanAgreement {
    private final String borrower;
    private final double amount;
    private final int months;
    private final double interestRate;
    private final String guarantor;
    private final boolean insured;

    LoanAgreement(String borrower, double amount, int months,
                  double interestRate, String guarantor, boolean insured) {
        this.borrower = borrower;
        this.amount = amount;
        this.months = months;
        this.interestRate = interestRate;
        this.guarantor = guarantor;
        this.insured = insured;
    }

    public String getBorrower() { return borrower; }
    public double getAmount() { return amount; }
    public int getMonths() { return months; }
    public double getInterestRate() { return interestRate; }
    public String getGuarantor() { return guarantor; }
    public boolean isInsured() { return insured; }

    @Override
    public String toString() {
        return "Loan for " + borrower + ": " + amount + " for " + months + " months at "
                + interestRate + "%"
                + (guarantor != null ? ", guarantor: " + guarantor : "")
                + (insured ? ", insured" : "");
    }
}
