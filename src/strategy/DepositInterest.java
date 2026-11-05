package strategy;

public class DepositInterest implements InterestStrategy {
    private static final double ANNUAL_RATE = 0.15;

    @Override
    public double calculate(double amount, int months) {
        double monthlyRate = ANNUAL_RATE / 12;
        return amount * (Math.pow(1 + monthlyRate, months) - 1);
    }
}