package gui;

import account.Account;
import service.ExternalPaymentService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class PaymentsPanel extends JPanel implements Refreshable {
    private static final String HINT =
            "Recipient: a merchant name (Visa) or an IBAN (SWIFT).";

    private final ExternalPaymentService paymentService;
    private final Runnable onChange;
    private final AccountSelector accountSelector;
    private final JComboBox<String> systemBox;
    private final JTextField recipientField = new JTextField(15);
    private final JTextField amountField = new JTextField("50", 15);

    public PaymentsPanel(Supplier<List<Account>> accounts,
                         ExternalPaymentService paymentService, Runnable onChange) {
        super(new BorderLayout(8, 8));
        this.paymentService = paymentService;
        this.onChange = onChange;
        this.accountSelector = new AccountSelector(accounts);
        this.systemBox = new JComboBox<>(paymentService.getGatewayNames().toArray(new String[0]));

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildForm(), BorderLayout.NORTH);
    }

    @Override
    public void refresh() {
        accountSelector.refresh();
    }

    private JPanel buildForm() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.add(new JLabel("Pay from account:"));
        grid.add(accountSelector);
        grid.add(new JLabel("Payment system:"));
        grid.add(systemBox);
        grid.add(new JLabel("Recipient:"));
        grid.add(recipientField);
        grid.add(new JLabel("Amount:"));
        grid.add(amountField);

        JButton pay = new JButton("Pay");
        pay.addActionListener(e -> Dialogs.run(this, this::pay));
        grid.add(new JLabel(""));
        grid.add(pay);

        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.add(new JLabel(HINT), BorderLayout.NORTH);
        form.add(grid, BorderLayout.CENTER);
        return form;
    }

    private void pay() {
        Account account = accountSelector.getSelectedAccount();
        String system = (String) systemBox.getSelectedItem();
        double amount = Dialogs.parseNumber(amountField.getText().trim());

        paymentService.pay(account, system, recipientField.getText().trim(), amount);

        Dialogs.showInfo(this, "Payment sent. New balance: "
                + String.format(Locale.US, "%.2f", account.getBalance()));
        onChange.run();
    }
}