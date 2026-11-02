package payment;
public interface PaymentGateway {
    boolean pay(String fromAccount, String toAccount, double amount);
    String getName();
}
