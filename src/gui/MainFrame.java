package gui;

import account.Account;
import app.Bank;
import service.AccountOptionService;
import service.ExternalPaymentService;
import service.LoanService;
import service.TransferService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MainFrame extends JFrame {
    private static final String ACCOUNTS = "Accounts";
    private static final String LOANS = "Loans";
    private static final String PAYMENTS = "Payments";
    private static final String NOTIFICATIONS = "Notifications";
    private static final int SIDEBAR_WIDTH = 230;

    private final Bank bank;
    private final LoanService loanService;
    private final Runnable onLogout;
    private final NotificationsPanel notifications = new NotificationsPanel();
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHost = new JPanel(pages);
    private final JLabel pageTitle = Ui.title(ACCOUNTS);
    private final Map<String, StyledButton> navButtons = new LinkedHashMap<>();
    private final List<Refreshable> panels;
    private String currentPage = ACCOUNTS;
    private int unread = 0;

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

        pageHost.setOpaque(false);
        pageHost.add(accounts, ACCOUNTS);
        pageHost.add(loans, LOANS);
        pageHost.add(payments, PAYMENTS);
        pageHost.add(notifications, NOTIFICATIONS);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(buildSidebar(owner), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        notifications.setOnNew(this::onNewNotification);
        notifications.captureConsole();
        showPage(ACCOUNTS);
        setSize(1120, 700);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
    }

    private JPanel buildSidebar(String owner) {
        JLabel brand = new JLabel("BANK");
        brand.setFont(Theme.FONT_TITLE.deriveFont(30f));
        brand.setForeground(Color.WHITE);

        JLabel caption = Ui.muted("Signed in as");
        caption.setForeground(Theme.AQUA);

        JLabel name = Ui.body(owner);
        name.setFont(Theme.FONT_BOLD);
        name.setForeground(Color.WHITE);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.NAVY);
        sidebar.setBorder(BorderFactory.createEmptyBorder(28, 16, 20, 16));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));

        sidebar.add(left(brand));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(left(caption));
        sidebar.add(left(name));
        sidebar.add(Box.createVerticalStrut(32));
        for (String page : List.of(ACCOUNTS, LOANS, PAYMENTS, NOTIFICATIONS)) {
            sidebar.add(left(navButton(page)));
            sidebar.add(Box.createVerticalStrut(6));
        }
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(left(nextMonthButton()));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(left(logOutButton()));
        return sidebar;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        content.add(pageTitle, BorderLayout.NORTH);
        content.add(pageHost, BorderLayout.CENTER);
        return content;
    }

    private StyledButton navButton(String page) {
        StyledButton button = new StyledButton(page, StyledButton.Kind.SIDEBAR);
        button.addActionListener(e -> showPage(page));
        navButtons.put(page, button);
        return stretch(button);
    }

    private StyledButton nextMonthButton() {
        StyledButton button = new StyledButton("Next month", StyledButton.Kind.PRIMARY);
        button.setToolTipText("Simulate the end of a month: pay deposit interest "
                + "and collect loan payments");
        button.addActionListener(e -> Dialogs.run(this, this::nextMonth));
        return stretch(button);
    }

    private StyledButton logOutButton() {
        StyledButton button = new StyledButton("Log out", StyledButton.Kind.SIDEBAR);
        button.addActionListener(e -> logOut());
        return stretch(button);
    }

    private StyledButton stretch(StyledButton button) {
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        return button;
    }

    private <T extends JComponent> T left(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    private void showPage(String name) {
        currentPage = name;
        if (NOTIFICATIONS.equals(name)) {
            unread = 0;
            updateNotificationsButton();
        }
        pages.show(pageHost, name);
        pageTitle.setText(name);
        navButtons.forEach((title, button) -> button.setActive(title.equals(name)));
    }

    private void onNewNotification() {
        if (!NOTIFICATIONS.equals(currentPage)) {
            unread++;
            updateNotificationsButton();
        }
    }

    private void updateNotificationsButton() {
        String text = unread == 0 ? NOTIFICATIONS : NOTIFICATIONS + " (" + unread + ")";
        navButtons.get(NOTIFICATIONS).setText(text);
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
        notifications.restoreConsole();
        dispose();
        onLogout.run();
    }
}