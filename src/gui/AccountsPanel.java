package gui;

import account.Account;
import account.Transaction;
import app.Bank;
import service.AccountOptionService;
import service.TransferService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AccountsPanel extends JPanel implements Refreshable {
    private static final String[] COLUMNS = {"ID", "Type", "Balance", "Term", "Options"};
    private static final int BALANCE_COLUMN = 2;
    private static final String NONE = "-";
    private static final int ACTION_GAP = 10;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Bank bank;
    private final String owner;
    private final AccountOptionService optionService;
    private final TransferService transferService;
    private final Runnable onChange;
    private final DefaultTableModel model = Ui.readOnlyModel(COLUMNS);
    private final JTable table = new JTable(model);
    private final JLabel totalValue = new JLabel();
    private final JLabel countValue = new JLabel();

    public AccountsPanel(Bank bank, String owner, AccountOptionService optionService,
                         TransferService transferService, Runnable onChange) {
        super(new BorderLayout(0, 16));
        this.bank = bank;
        this.owner = owner;
        this.optionService = optionService;
        this.transferService = transferService;
        this.onChange = onChange;

        setOpaque(false);
        Ui.styleTable(table);
        Ui.alignRight(table, BALANCE_COLUMN);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        int row = table.getSelectedRow();
        String selectedId = row >= 0 ? (String) model.getValueAt(row, 0) : "";

        List<Account> accounts = bank.getAccountsOf(owner);
        model.setRowCount(0);
        accounts.forEach(account -> model.addRow(toRow(account)));
        selectRowWithId(selectedId);

        totalValue.setText(Ui.money(accounts.stream().mapToDouble(Account::getBalance).sum()));
        countValue.setText(String.valueOf(accounts.size()));
    }

    private void selectRowWithId(String id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(id)) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private Object[] toRow(Account account) {
        String term = account.getTermMonths() > 0
                ? account.getMonthsElapsed() + " / " + account.getTermMonths() + " months"
                : NONE;
        String options = account.getOptionsCode().isEmpty() ? NONE : account.getOptionsCode();
        return new Object[]{account.getId(), account.getType(),
                Ui.money(account.getBalance()), term, options};
    }

    private JPanel buildHeader() {
        JPanel stats = new JPanel();
        stats.setLayout(new BoxLayout(stats, BoxLayout.X_AXIS));
        stats.setOpaque(false);
        stats.add(statCard("Total balance", totalValue));
        stats.add(Box.createHorizontalStrut(12));
        stats.add(statCard("Accounts", countValue));

        JPanel openWrapper = new JPanel(new GridBagLayout());
        openWrapper.setOpaque(false);
        openWrapper.add(actionButton("+ Open account", StyledButton.Kind.PRIMARY, this::openAccount));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(stats, BorderLayout.WEST);
        header.add(openWrapper, BorderLayout.EAST);
        return header;
    }

    private CardPanel statCard(String caption, JLabel value) {
        value.setFont(Theme.FONT_HEADING.deriveFont(24f));
        value.setForeground(Theme.NAVY);

        CardPanel card = new CardPanel(new BorderLayout(0, 2));
        card.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        card.setPreferredSize(new Dimension(210, 76));
        card.add(Ui.muted(caption), BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private CardPanel buildTableCard() {
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 2));
        head.setOpaque(false);
        head.add(Ui.heading("Your accounts"));
        head.add(Ui.muted("Select an account in the table, then choose an action below."));

        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        card.add(head, BorderLayout.NORTH);
        card.add(Ui.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildActions() {
        JPanel money = actionRow();
        addAction(money, "Top up", this::topUp);
        addAction(money, "Withdraw", this::withdraw);
        addAction(money, "Transfer", this::transfer);
        addAction(money, "Statement", this::showStatement);

        JPanel options = actionRow();
        addAction(options, "Overdraft", this::addOverdraft);
        addAction(options, "SMS", this::addSms);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.add(money, BorderLayout.WEST);
        bar.add(options, BorderLayout.EAST);
        return bar;
    }

    private JPanel actionRow() {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setOpaque(false);
        return row;
    }

    private void addAction(JPanel row, String title, Runnable action) {
        if (row.getComponentCount() > 0) {
            row.add(Box.createHorizontalStrut(ACTION_GAP));
        }
        row.add(actionButton(title, StyledButton.Kind.SECONDARY, action));
    }

    private StyledButton actionButton(String title, StyledButton.Kind kind, Runnable action) {
        StyledButton button = new StyledButton(title, kind);
        button.addActionListener(e -> Dialogs.run(this, action));
        return button;
    }

    private Account selectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalStateException("Select an account in the table first");
        }
        return bank.getOwnedAccount((String) model.getValueAt(row, 0), owner);
    }

    private void openAccount() {
        JComboBox<String> type = Ui.styled(new JComboBox<String>(new String[]{"checking", "deposit"}));
        JTextField balance = Ui.styled(new JTextField("0", 18));
        JTextField term = Ui.styled(new JTextField("3", 18));

        FormPanel form = new FormPanel();
        form.addRow("Account type", type);
        form.addRow("Initial balance", balance);
        form.addRow("Term in months (deposit only)", term);

        int answer = JOptionPane.showConfirmDialog(this, form, "Open account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (answer != JOptionPane.OK_OPTION) {
            return;
        }

        String selectedType = (String) type.getSelectedItem();
        int termMonths = "deposit".equals(selectedType)
                ? Dialogs.parseWholeNumber(term.getText().trim())
                : 0;
        double initial = Dialogs.parseNumber(balance.getText().trim());
        bank.openAccount(selectedType, owner, initial, termMonths);
        onChange.run();
    }

    private void topUp() {
        Account account = selectedAccount();
        Dialogs.askNumber(this, "Amount to top up:").ifPresent(amount -> {
            account.deposit(amount);
            onChange.run();
        });
    }

    private void withdraw() {
        Account account = selectedAccount();
        Dialogs.askNumber(this, "Amount to withdraw:").ifPresent(amount -> {
            account.withdraw(amount);
            onChange.run();
        });
    }

    private void transfer() {
        Account sender = selectedAccount();
        Dialogs.askText(this, "Receiver account ID (any customer):").ifPresent(receiverId ->
                Dialogs.askNumber(this, "Amount:").ifPresent(amount -> {
                    transferService.transfer(sender, bank.getAccount(receiverId), amount);
                    onChange.run();
                }));
    }

    private void addOverdraft() {
        Account account = selectedAccount();
        Dialogs.askNumber(this, "Overdraft limit:").ifPresent(limit -> {
            optionService.addOverdraft(account, limit);
            onChange.run();
        });
    }

    private void addSms() {
        optionService.addSms(selectedAccount());
        onChange.run();
    }

    private void showStatement() {
        Account account = selectedAccount();
        List<Transaction> history = bank.getHistory(account.getId());
        if (history.isEmpty()) {
            Dialogs.showInfo(this, "No operations yet.");
            return;
        }

        DefaultTableModel rows = Ui.readOnlyModel("Date", "Operation", "Amount");
        for (Transaction transaction : history) {
            rows.addRow(new Object[]{
                    transaction.getTime().format(TIME_FORMAT),
                    transaction.getType().name().replace('_', ' '),
                    Ui.money(transaction.getAmount())});
        }
        JTable statement = new JTable(rows);
        Ui.styleTable(statement);
        Ui.alignRight(statement, 2);

        JScrollPane pane = Ui.scroll(statement);
        pane.setPreferredSize(new Dimension(560, 320));
        JOptionPane.showMessageDialog(this, pane,
                "Statement " + account.getId(), JOptionPane.PLAIN_MESSAGE);
    }
}