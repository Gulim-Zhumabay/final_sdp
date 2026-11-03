package payment;

public class VisaAdapter implements PaymentGateway {
    private final VisaApi visaApi;

    public VisaAdapter(VisaApi visaApi) {
        this.visaApi = visaApi;
    }

    @Override
    public boolean pay(String fromAccount, String toAccount, double amount) {
        return visaApi.makeVisaPayment(fromAccount, toAccount, amount);
    }

    @Override
    public String getName() {
        return "Visa";
    }
}
