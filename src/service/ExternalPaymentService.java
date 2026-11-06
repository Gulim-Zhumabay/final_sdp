package service;

import account.Account;
import account.TransactionType;
import payment.PaymentGateway;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExternalPaymentService {
    private final Map<String, PaymentGateway> gateways = new LinkedHashMap<>();

    public ExternalPaymentService(List<PaymentGateway> available) {
        for (PaymentGateway gateway : available) {
            gateways.put(gateway.getName(), gateway);
        }
    }

    public List<String> getGatewayNames() {
        return new ArrayList<>(gateways.keySet());
    }

    public void pay(Account from, String gatewayName, String recipient, double amount) {
        PaymentGateway gateway = gateways.get(gatewayName);
        if (gateway == null) {
            throw new IllegalArgumentException("Unknown payment system: " + gatewayName);
        }
        if (recipient.isBlank()) {
            throw new IllegalArgumentException("Recipient must not be empty");
        }

        from.withdraw(amount, TransactionType.EXTERNAL_PAYMENT);
        if (!gateway.pay(from.getId(), recipient, amount)) {
            from.deposit(amount, TransactionType.REFUND);
            throw new IllegalStateException(gatewayName + " rejected the payment, money returned");
        }
    }
}