package gui;

import account.Account;
import loan.Loan;
import loan.LoanAgreement;
import service.LoanService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LoansPanel extends JPanel implements Refreshable {
    private static final String RULES =
            "Rate 12%, with a guarantor 10%. Insurance adds 2% of the amount to the debt.";

    private final String owner;
    private final LoanService loanService;
    private final Runnable onChange;
    private final AccountSelector accountSelector;
    private final JTextField amountField = new JTextField("1200", 10);
    private final JTextField monthsField = new JTextField("12", 10);
    private final JTextField guarantorField = new JTextField(10);
    private final JCheckBox insuranceBox = new JCheckBox("Add insurance");
    private final JTextArea loansArea = new JTextArea();

    public LoansPanel(String owner, Supplier<List<Account>> accounts,
                      LoanService loanService, Runnable onChange) {
        super(new BorderLayout(8, 8));
        this.owner = owner;
        this.loanService = loanService;
        this.onChange = onChange;
        this.accountSelector = new AccountSelector(accounts);

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        loansArea.setEditable(false);
        loansArea.setLineWrap(true);
        loansArea.setWrapStyleWord(true);
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(loansArea), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        accountSelector.refresh();
        List<Loan> loans = loanService.getLoansOf(owner);
        loansArea.setText(loans.isEmpty()
                ? "You have no loans."
                : loans.stream()
                .map(Loan::getDescription)
                .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator())));
    }

    private JPanel buildForm() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.add(new JLabel("Pay out to account:"));
        grid.add(accountSelector);
        grid.add(new JLabel("Amount:"));
        grid.add(amountField);
        grid.add(new JLabel("Term (months):"));
        grid.add(monthsField);
        grid.add(new JLabel("Guarantor (optional):"));
        grid.add(guarantorField);
        grid.add(new JLabel(""));
        grid.add(insuranceBox);

        JButton takeLoan = new JButton("Take loan");
        takeLoan.addActionListener(e -> Dialogs.run(this, this::takeLoan));
        grid.add(new JLabel(""));
        grid.add(takeLoan);

        JPanel form = new JPanel(new BorderLayout(8, 8));
        form.add(new JLabel(RULES), BorderLayout.NORTH);
        form.add(grid, BorderLayout.CENTER);
        return form;
    }

    private void takeLoan() {
        Account account = accountSelector.getSelectedAccount();
        double amount = Dialogs.parseNumber(amountField.getText().trim());
        int months = Dialogs.parseWholeNumber(monthsField.getText().trim());
        String guarantor = guarantorField.getText().trim();

        LoanAgreement agreement = loanService.createAgreement(
                owner, amount, months, guarantor, insuranceBox.isSelected());
        Loan loan = loanService.issue(account, agreement);

        Dialogs.showInfo(this, "Loan approved:\n" + loan.getDescription());
        onChange.run();
    }
}