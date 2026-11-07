package payment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentAdapterTest {

    private static class RecordingVisaApi extends VisaApi {
        private String card;
        private String merchant;
        private double sum;
        private boolean answer = true;

        @Override
        public boolean makeVisaPayment(String cardNumber, String merchant, double sum) {
            this.card = cardNumber;
            this.merchant = merchant;
            this.sum = sum;
            return answer;
        }
    }

    private static class RecordingSwiftApi extends SwiftApi {
        private String iban;
        private String bic;
        private double amount;
        private String currency;
        private String answer = "SWIFT-OK";

        @Override
        public String sendSwiftTransfer(String iban, String bic, double amount, String currency) {
            this.iban = iban;
            this.bic = bic;
            this.amount = amount;
            this.currency = currency;
            return answer;
        }
    }

    @Test
    void visaAdapterIsAPaymentGatewayWithItsName() {
        PaymentGateway gateway = new VisaAdapter(new RecordingVisaApi());

        assertEquals("Visa", gateway.getName());
    }

    @Test
    void visaAdapterTranslatesParametersToVisaApi() {
        RecordingVisaApi api = new RecordingVisaApi();
        PaymentGateway gateway = new VisaAdapter(api);

        boolean result = gateway.pay("ACC-1", "Coffee Shop", 50);

        assertTrue(result);
        assertEquals("ACC-1", api.card);
        assertEquals("Coffee Shop", api.merchant);
        assertEquals(50.0, api.sum);
    }

    @Test
    void visaAdapterPassesRejectionThrough() {
        RecordingVisaApi api = new RecordingVisaApi();
        api.answer = false;

        assertFalse(new VisaAdapter(api).pay("ACC-1", "Coffee Shop", 50));
    }

    @Test
    void swiftAdapterIsAPaymentGatewayWithItsName() {
        PaymentGateway gateway = new SwiftAdapter(new RecordingSwiftApi(), "KASPKZKA", "USD");

        assertEquals("SWIFT", gateway.getName());
    }

    @Test
    void swiftAdapterAddsBicAndCurrencyItWasConfiguredWith() {
        RecordingSwiftApi api = new RecordingSwiftApi();
        PaymentGateway gateway = new SwiftAdapter(api, "KASPKZKA", "USD");

        boolean result = gateway.pay("ACC-1", "KZ1234567890", 200);

        assertTrue(result);
        assertEquals("KZ1234567890", api.iban);
        assertEquals("KASPKZKA", api.bic);
        assertEquals(200.0, api.amount);
        assertEquals("USD", api.currency);
    }

    @Test
    void swiftAdapterConvertsTextAnswerToBoolean() {
        RecordingSwiftApi api = new RecordingSwiftApi();
        api.answer = "SWIFT-ERROR";

        assertFalse(new SwiftAdapter(api, "KASPKZKA", "USD").pay("ACC-1", "KZ1234567890", 200));
    }
}