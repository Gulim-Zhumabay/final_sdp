package gui;

import account.Account;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class AccountSelector extends JComboBox<Account> implements Refreshable {
    private final Supplier<List<Account>> source;

    public AccountSelector(Supplier<List<Account>> source) {
        this.source = source;
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Account account) {
                    setText(account.getId() + "  " + account.getType() + "  "
                            + String.format(Locale.US, "%.2f", account.getBalance()));
                }
                return this;
            }
        });
        refresh();
    }

    public Account getSelectedAccount() {
        if (getSelectedItem() instanceof Account account) {
            return account;
        }
        throw new IllegalStateException("You have no accounts yet. Open one on the Accounts tab.");
    }

    @Override
    public void refresh() {
        String selectedId = getSelectedItem() instanceof Account account ? account.getId() : "";
        removeAllItems();
        List<Account> accounts = source.get();
        accounts.forEach(this::addItem);
        accounts.stream()
                .filter(account -> account.getId().equals(selectedId))
                .findFirst()
                .ifPresent(this::setSelectedItem);
    }
}