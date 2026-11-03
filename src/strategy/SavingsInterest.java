package strategy;

public class SavingsInterest implements InterestStrategy {
    private static final double ANNUAL_RATE = 0.03;

    @Override
    public double calculate(double amount, int months) {
        return amount * ANNUAL_RATE * months / 12;
    }
}