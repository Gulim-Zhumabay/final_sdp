package gui;

import account.Account;
import loan.Loan;
import loan.LoanAgreement;
import service.LoanService;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class LoansPanel extends JPanel implements Refreshable {
    private static final String[] COLUMNS =
            {"ID", "Account", "Amount", "Rate", "Paid", "Status", "Terms"};
    private static final int AMOUNT_COLUMN = 2;
    private static final String NONE = "-";
    private static final String RULES = "<html><div style='width:250px'>"
            + "Rate 12%, or 10% with a guarantor. "
            + "Insurance adds 2% of the amount to the total debt.</div></html>";

    private final String owner;
    private final LoanService loanService;
    private final Runnable onChange;
    private final AccountSelector accountSelector;
    private final JTextField amountField = Ui.styled(new JTextField("1200", 18));
    private final JTextField monthsField = Ui.styled(new JTextField("12", 18));
    private final JTextField guarantorField = Ui.styled(new JTextField(18));
    private final JCheckBox insuranceBox = Ui.styled(new JCheckBox("Add insurance"));
    private final DefaultTableModel model = Ui.readOnlyModel(COLUMNS);
    private final JTable table = new JTable(model);

    public LoansPanel(String owner, Supplier<List<Account>> accounts,
                      LoanService loanService, Runnable onChange) {
        super(new BorderLayout(16, 0));
        this.owner = owner;
        this.loanService = loanService;
        this.onChange = onChange;
        this.accountSelector = Ui.styled(new AccountSelector(accounts));

        setOpaque(false);
        Ui.styleTable(table);
        Ui.alignRight(table, AMOUNT_COLUMN);

        add(buildFormCard(), BorderLayout.WEST);
        add(buildLoansCard(), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        accountSelector.refresh();
        model.setRowCount(0);
        loanService.getLoansOf(owner).forEach(loan -> model.addRow(toRow(loan)));
    }

    private Object[] toRow(Loan loan) {
        LoanAgreement agreement = loan.getAgreement();
        return new Object[]{
                loan.getId(),
                loan.getAccountId(),
                Ui.money(agreement.getAmount()),
                String.format(Locale.US, "%.1f%%", agreement.getInterestRate()),
                loan.getMonthsPaid() + " / " + agreement.getMonths(),
                loan.isRepaid() ? "Repaid" : "Active",
                terms(agreement)};
    }

    private String terms(LoanAgreement agreement) {
        List<String> parts = new ArrayList<>();
        if (agreement.getGuarantor() != null) {
            parts.add("guarantor " + agreement.getGuarantor());
        }
        if (agreement.isInsured()) {
            parts.add("insured");
        }
        return parts.isEmpty() ? NONE : String.join(", ", parts);
    }

    private CardPanel buildFormCard() {
        StyledButton takeLoan = new StyledButton("Take loan", StyledButton.Kind.PRIMARY);
        takeLoan.addActionListener(e -> Dialogs.run(this, this::takeLoan));

        FormPanel form = new FormPanel();
        form.addWide(Ui.heading("New loan"));
        form.addRow("Pay out to account", accountSelector);
        form.addRow("Amount", amountField);
        form.addRow("Term in months", monthsField);
        form.addRow("Guarantor (optional)", guarantorField);
        form.addWide(insuranceBox, 12);
        form.addWide(Ui.muted(RULES), 12);
        form.addWide(takeLoan);

        CardPanel card = new CardPanel(new BorderLayout());
        card.add(form, BorderLayout.NORTH);
        return card;
    }

    private CardPanel buildLoansCard() {
        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        card.add(Ui.heading("My loans"), BorderLayout.NORTH);
        card.add(Ui.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private void takeLoan() {
        Account account = accountSelector.getSelectedAccount();
        double amount = Dialogs.parseNumber(amountField.getText().trim());
        int months = Dialogs.parseWholeNumber(monthsField.getText().trim());
        String guarantor = guarantorField.getText().trim();

        LoanAgreement agreement = loanService.createAgreement(
                owner, amount, months, guarantor, insuranceBox.isSelected());
        Loan loan = loanService.issue(account, agreement);

        Dialogs.showInfo(this, "Loan " + loan.getId() + " approved. "
                + Ui.money(amount) + " was paid out to " + account.getId() + ".");
        onChange.run();
    }
}