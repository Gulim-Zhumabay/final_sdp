package strategy;

public class LoanInterest implements InterestStrategy {
    private final double annualRatePercent;

    public LoanInterest(double annualRatePercent) {
        this.annualRatePercent = annualRatePercent;
    }

    @Override
    public double calculate(double amount, int months) {
        return amount * (annualRatePercent / 100) * months / 12;
    }
}