package gui;

import account.Account;
import service.ExternalPaymentService;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Supplier;

public class PaymentsPanel extends JPanel implements Refreshable {
    private static final String HELP = "<html><div style='width:260px'>"
            + "Visa: enter a merchant name.<br><br>"
            + "SWIFT: enter the recipient IBAN.<br><br>"
            + "The money is taken from your account first. If the payment system "
            + "declines the payment, the money is returned automatically "
            + "and you will see a REFUND in the statement.</div></html>";

    private final ExternalPaymentService paymentService;
    private final Runnable onChange;
    private final AccountSelector accountSelector;
    private final JComboBox<String> systemBox;
    private final JTextField recipientField = Ui.styled(new JTextField(18));
    private final JTextField amountField = Ui.styled(new JTextField("50", 18));

    public PaymentsPanel(Supplier<List<Account>> accounts,
                         ExternalPaymentService paymentService, Runnable onChange) {
        super(new BorderLayout());
        this.paymentService = paymentService;
        this.onChange = onChange;
        this.accountSelector = Ui.styled(new AccountSelector(accounts));
        this.systemBox = Ui.styled(new JComboBox<String>(
                paymentService.getGatewayNames().toArray(new String[0])));

        setOpaque(false);
        JPanel cards = new JPanel(new GridLayout(1, 2, 16, 0));
        cards.setOpaque(false);
        cards.add(buildFormCard());
        cards.add(buildHelpCard());
        add(cards, BorderLayout.NORTH);
    }

    @Override
    public void refresh() {
        accountSelector.refresh();
    }

    private CardPanel buildFormCard() {
        StyledButton pay = new StyledButton("Send payment", StyledButton.Kind.PRIMARY);
        pay.addActionListener(e -> Dialogs.run(this, this::pay));

        FormPanel form = new FormPanel();
        form.addWide(Ui.heading("New payment"));
        form.addRow("Pay from account", accountSelector);
        form.addRow("Payment system", systemBox);
        form.addRow("Recipient", recipientField);
        form.addRow("Amount", amountField);
        form.addWide(pay);

        CardPanel card = new CardPanel(new BorderLayout());
        card.add(form, BorderLayout.NORTH);
        return card;
    }

    private CardPanel buildHelpCard() {
        FormPanel help = new FormPanel();
        help.addWide(Ui.heading("How it works"));
        help.addWide(Ui.body(HELP), 12);

        CardPanel card = new CardPanel(new BorderLayout());
        card.add(help, BorderLayout.NORTH);
        return card;
    }

    private void pay() {
        Account account = accountSelector.getSelectedAccount();
        String system = (String) systemBox.getSelectedItem();
        double amount = Dialogs.parseNumber(amountField.getText().trim());

        paymentService.pay(account, system, recipientField.getText().trim(), amount);

        Dialogs.showInfo(this, "Payment sent. New balance: " + Ui.money(account.getBalance()));
        onChange.run();
    }
}