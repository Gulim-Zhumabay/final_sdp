package gui;

import account.Account;
import app.Bank;
import service.AccountOptionService;
import service.ExternalPaymentService;
import service.LoanService;
import service.TransferService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class MainFrame extends JFrame {
    private final Bank bank;
    private final LoanService loanService;
    private final Runnable onLogout;
    private final LogPanel logPanel = new LogPanel();
    private final List<Refreshable> panels;

    public MainFrame(Bank bank, String owner, LoanService loanService,
                     ExternalPaymentService paymentService, Runnable onLogout) {
        super("Banking System - " + owner);
        this.bank = bank;
        this.loanService = loanService;
        this.onLogout = onLogout;

        Runnable onChange = this::changed;
        Supplier<List<Account>> ownAccounts = () -> bank.getAccountsOf(owner);

        AccountsPanel accounts = new AccountsPanel(bank, owner,
                new AccountOptionService(bank::replaceAccount), new TransferService(), onChange);
        LoansPanel loans = new LoansPanel(owner, ownAccounts, loanService, onChange);
        PaymentsPanel payments = new PaymentsPanel(ownAccounts, paymentService, onChange);
        this.panels = List.of(accounts, loans, payments);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Accounts", accounts);
        tabs.addTab("Loans", loans);
        tabs.addTab("Payments", payments);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(buildToolbar(owner), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(logPanel, BorderLayout.SOUTH);

        logPanel.captureConsole();
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildToolbar(String owner) {
        JButton nextMonth = new JButton("Next month");
        nextMonth.addActionListener(e -> Dialogs.run(this, this::nextMonth));
        JButton logOut = new JButton("Log out");
        logOut.addActionListener(e -> logOut());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        toolbar.add(new JLabel("Logged in as: " + owner));
        toolbar.add(nextMonth);
        toolbar.add(logOut);
        return toolbar;
    }

    private void changed() {
        bank.save();
        panels.forEach(Refreshable::refresh);
    }

    private void nextMonth() {
        List<String> reports = new ArrayList<>(bank.closeMonth());
        reports.addAll(loanService.collectPayments());
        if (reports.isEmpty()) {
            System.out.println("A month has passed. No active deposits or loans.");
        }
        reports.forEach(System.out::println);
        changed();
    }

    private void logOut() {
        logPanel.restoreConsole();
        dispose();
        onLogout.run();
    }
}