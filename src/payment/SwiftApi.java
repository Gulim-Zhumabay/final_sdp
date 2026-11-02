package payment;

public class SwiftApi {
    public String sendSwiftTransfer(String iban, String bic, double amount, String currency) {
        System.out.println("[SWIFT] " + amount + " " + currency + " to " + iban + " (" + bic + ")");
        return "SWIFT-OK";
    }
}
