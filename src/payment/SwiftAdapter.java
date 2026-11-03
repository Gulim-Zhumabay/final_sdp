package payment;

public class SwiftAdapter implements PaymentGateway {
    private final SwiftApi swiftApi;
    private final String bic;
    private final String currency;

    public SwiftAdapter(SwiftApi swiftApi, String bic, String currency) {
        this.swiftApi = swiftApi;
        this.bic = bic;
        this.currency = currency;
    }

    @Override
    public boolean pay(String fromAccount, String toAccount, double amount) {
        String result = swiftApi.sendSwiftTransfer(toAccount, bic, amount, currency);
        return result.equals("SWIFT-OK");
    }

    @Override
    public String getName() {
        return "SWIFT";
    }
}
