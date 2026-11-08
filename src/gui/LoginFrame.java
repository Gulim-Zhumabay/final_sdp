package gui;

import customer.Customer;
import service.AuthService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final Consumer<Customer> onLogin;
    private final JTextField usernameField = Ui.styled(new JTextField(20));
    private final JPasswordField passwordField = Ui.styled(new JPasswordField(20));
    private final StyledButton loginButton = new StyledButton("Log in", StyledButton.Kind.PRIMARY);
    private final StyledButton registerButton =
            new StyledButton("Create account", StyledButton.Kind.SECONDARY);

    public LoginFrame(AuthService authService, Consumer<Customer> onLogin) {
        super("Banking System");
        this.authService = authService;
        this.onLogin = onLogin;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));
        add(buildBrandPanel());
        add(buildFormPanel());
        getRootPane().setDefaultButton(loginButton);

        setSize(820, 480);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private JPanel buildBrandPanel() {
        JLabel brand = new JLabel("BANK");
        brand.setFont(Theme.FONT_TITLE.deriveFont(52f));
        brand.setForeground(Color.WHITE);

        JPanel accent = new JPanel();
        accent.setBackground(Theme.TEAL);
        accent.setPreferredSize(new Dimension(64, 5));
        accent.setMaximumSize(new Dimension(64, 5));

        JLabel tagline = new JLabel("<html><div style='width:240px'>"
                + "Accounts, loans and payments in one place.</div></html>");
        tagline.setFont(Theme.FONT.deriveFont(17f));
        tagline.setForeground(Theme.AQUA);

        for (JComponent part : List.of(brand, accent, tagline)) {
            part.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(brand);
        content.add(Box.createVerticalStrut(12));
        content.add(accent);
        content.add(Box.createVerticalStrut(16));
        content.add(tagline);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.NAVY);
        panel.add(content);
        return panel;
    }

    private JPanel buildFormPanel() {
        loginButton.addActionListener(e -> logIn());
        registerButton.addActionListener(e -> register());

        FormPanel form = new FormPanel();
        form.addWide(Ui.title("Welcome"));
        form.addWide(Ui.muted("Log in or create a new account"), 4);
        form.addRow("Username", usernameField);
        form.addRow("Password", passwordField);
        form.addWide(loginButton, 22);
        form.addWide(registerButton, 10);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND);
        panel.add(form);
        return panel;
    }

    private void logIn() {
        Dialogs.run(this, () -> {
            Customer customer = authService.login(username(), password());
            passwordField.setText("");
            dispose();
            onLogin.accept(customer);
        });
    }

    private void register() {
        Dialogs.run(this, () -> {
            authService.register(username(), password());
            Dialogs.showInfo(this, "Registered. You can log in now.");
        });
    }

    private String username() {
        return usernameField.getText().trim();
    }

    private String password() {
        return new String(passwordField.getPassword());
    }
}