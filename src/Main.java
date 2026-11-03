import account.Account;
import decorator.OverdraftDecorator;
import decorator.SmsDecorator;
import factory.AccountFactory;
import loan.LoanAgreement;
import loan.LoanAgreementBuilder;
import observer.CustomerNotifier;
import observer.FraudDetector;
import payment.PaymentGateway;
import payment.SwiftAdapter;
import payment.SwiftApi;
import payment.VisaAdapter;
import payment.VisaApi;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== 1. FACTORY: creating accounts ===");
        Account savings = AccountFactory.create("savings", "Gulim", 5000);
        Account checking = AccountFactory.create("checking", "Gulim", 100);
        System.out.println(savings.getDescription());
        System.out.println(checking.getDescription());

        System.out.println("\n=== 2. OBSERVER: subscribing to operations ===");
        savings.addObserver(new CustomerNotifier("Gulim"));
        savings.addObserver(new FraudDetector(10000));
        savings.deposit(300);
        savings.deposit(15000);

        System.out.println("\n=== 3. DECORATOR: adding options ===");
        Account decorated = new SmsDecorator(new OverdraftDecorator(checking, 500));
        System.out.println(decorated.getDescription());
        decorated.withdraw(400);
        System.out.println(decorated.getDescription());

        System.out.println("\n=== 4. BUILDER: loan agreement ===");
        LoanAgreement loan = new LoanAgreementBuilder("Gulim", 5000, 12)
                .interestRate(10.5)
                .guarantor("Aiymay")
                .withInsurance()
                .build();
        System.out.println(loan);

        System.out.println("\n=== 5. ADAPTER: external payments ===");
        PaymentGateway visa = new VisaAdapter(new VisaApi());
        PaymentGateway swift = new SwiftAdapter(new SwiftApi(), "KASPKZKA", "USD");

        visa.pay("4111-1111", "Coffee Shop", 50);
        if (swift.pay(savings.getId(), "KZ1234567890", 200)) {
            savings.withdraw(200);
        }
    }
}
