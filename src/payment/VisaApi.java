package payment;

public class VisaApi {
    public boolean makeVisaPayment(String cardNumber, String merchant, double sum) {
        System.out.println("[VISA] " + sum + " from card " + cardNumber + " to " + merchant);
        return true;
    }
}
