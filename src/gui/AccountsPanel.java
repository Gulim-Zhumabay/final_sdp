package gui;

import account.Account;
import account.Transaction;
import app.Bank;
import service.AccountOptionService;
import service.TransferService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class AccountsPanel extends JPanel implements Refreshable {
    private static final String[] COLUMNS = {"ID", "Type", "Balance", "Term", "Options"};
    private static final String NONE = "-";
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Bank bank;
    private final String owner;
    private final AccountOptionService optionService;
    private final TransferService transferService;
    private final Runnable onChange;
    private final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    public AccountsPanel(Bank bank, String owner, AccountOptionService optionService,
                         TransferService transferService, Runnable onChange) {
        super(new BorderLayout());
        this.bank = bank;
        this.owner = owner;
        this.optionService = optionService;
        this.transferService = transferService;
        this.onChange = onChange;

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        refresh();
    }

    @Override
    public void refresh() {
        int row = table.getSelectedRow();
        String selectedId = row >= 0 ? (String) model.getValueAt(row, 0) : "";

        model.setRowCount(0);
        for (Account account : bank.getAccountsOf(owner)) {
            model.addRow(toRow(account));
        }
        selectRowWithId(selectedId);
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
                ? account.getMonthsElapsed() + "/" + account.getTermMonths() + " months"
                : NONE;
        String options = account.getOptionsCode().isEmpty() ? NONE : account.getOptionsCode();
        return new Object[]{account.getId(), account.getType(),
                String.format(Locale.US, "%.2f", account.getBalance()), term, options};
    }

    private JPanel buildButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        addButton(buttons, "Open account", this::openAccount);
        addButton(buttons, "Top up", this::topUp);
        addButton(buttons, "Withdraw", this::withdraw);
        addButton(buttons, "Transfer", this::transfer);
        addButton(buttons, "Add overdraft", this::addOverdraft);
        addButton(buttons, "Add SMS", this::addSms);
        addButton(buttons, "Statement", this::showStatement);
        return buttons;
    }

    private void addButton(JPanel panel, String title, Runnable action) {
        JButton button = new JButton(title);
        button.addActionListener(e -> Dialogs.run(this, action));
        panel.add(button);
    }

    private Account selectedAccount() {
        int row = table.getSelectedRow();
        if (row < 0) {
            throw new IllegalStateException("Select an account in the table first");
        }
        return bank.getOwnedAccount((String) model.getValueAt(row, 0), owner);
    }

    private void openAccount() {
        JComboBox<String> type = new JComboBox<>(new String[]{"checking", "deposit"});
        JTextField balance = new JTextField("0");
        JTextField term = new JTextField("3");
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Type:"));
        form.add(type);
        form.add(new JLabel("Initial balance:"));
        form.add(balance);
        form.add(new JLabel("Term, months (deposit only):"));
        form.add(term);

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

        StringBuilder text = new StringBuilder();
        for (Transaction transaction : history) {
            text.append(String.format(Locale.US, "%s  %-16s %10.2f%n",
                    transaction.getTime().format(TIME_FORMAT),
                    transaction.getType(),
                    transaction.getAmount()));
        }
        if (history.isEmpty()) {
            text.append("No operations yet.");
        }

        JTextArea area = new JTextArea(text.toString(), 15, 48);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "Statement " + account.getId(), JOptionPane.PLAIN_MESSAGE);
    }
}