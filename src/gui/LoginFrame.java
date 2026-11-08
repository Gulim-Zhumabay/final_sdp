package gui;

import customer.Customer;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class LoginFrame extends JFrame {
    private final AuthService authService;
    private final Consumer<Customer> onLogin;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);
    private final JButton loginButton = new JButton("Log in");
    private final JButton registerButton = new JButton("Register");

    public LoginFrame(AuthService authService, Consumer<Customer> onLogin) {
        super("Banking System - Login");
        this.authService = authService;
        this.onLogin = onLogin;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(buildForm(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);
        return form;
    }

    private JPanel buildButtons() {
        loginButton.addActionListener(e -> logIn());
        registerButton.addActionListener(e -> register());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(loginButton);
        buttons.add(registerButton);
        return buttons;
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