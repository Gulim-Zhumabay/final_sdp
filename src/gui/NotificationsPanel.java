package gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationsPanel extends JPanel {
    private static final String[] COLUMNS = {"Time", "Type", "Message"};
    private static final int TYPE_COLUMN = 1;
    private static final int MESSAGE_COLUMN = 2;
    private static final String DEFAULT_TYPE = "INFO";
    private static final String EMPTY_HINT =
            "Alerts, SMS, interest and loan payments will appear here.";
    private static final Pattern TAGGED = Pattern.compile("^\\[([^\\]]+)]\\s*(.*)$");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final DefaultTableModel model = Ui.readOnlyModel(COLUMNS);
    private final JTable table = new JTable(model) {
        @Override
        public String getToolTipText(MouseEvent event) {
            int row = rowAtPoint(event.getPoint());
            return row < 0 ? null : String.valueOf(getValueAt(row, MESSAGE_COLUMN));
        }
    };
    private final JLabel hint = Ui.muted(EMPTY_HINT);
    private PrintStream originalConsole = System.out;
    private Runnable onNew = () -> { };

    public NotificationsPanel() {
        super(new BorderLayout());
        setOpaque(false);

        Ui.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(0).setMaxWidth(110);
        table.getColumnModel().getColumn(TYPE_COLUMN).setPreferredWidth(140);
        table.getColumnModel().getColumn(TYPE_COLUMN).setMaxWidth(180);
        colorTypeColumn();

        add(buildCard(), BorderLayout.CENTER);
    }

    public void setOnNew(Runnable onNew) {
        this.onNew = onNew;
    }

    public void captureConsole() {
        originalConsole = System.out;
        System.setOut(new PrintStream(originalConsole, true) {
            @Override
            public void println(String line) {
                super.println(line);
                receive(line);
            }
        });
    }

    public void restoreConsole() {
        System.setOut(originalConsole);
    }

    private CardPanel buildCard() {
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(Ui.heading("Notifications"));
        titles.add(hint);

        StyledButton clear = new StyledButton("Clear", StyledButton.Kind.SECONDARY);
        clear.addActionListener(e -> clear());
        JPanel clearWrapper = new JPanel(new GridBagLayout());
        clearWrapper.setOpaque(false);
        clearWrapper.add(clear);

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(titles, BorderLayout.WEST);
        head.add(clearWrapper, BorderLayout.EAST);

        CardPanel card = new CardPanel(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        card.add(head, BorderLayout.NORTH);
        card.add(Ui.scroll(table), BorderLayout.CENTER);
        return card;
    }

    private void receive(String line) {
        if (SwingUtilities.isEventDispatchThread()) {
            add(line);
        } else {
            SwingUtilities.invokeLater(() -> add(line));
        }
    }

    private void add(String line) {
        Matcher matcher = TAGGED.matcher(line);
        boolean tagged = matcher.matches();
        String type = tagged ? matcher.group(1) : DEFAULT_TYPE;
        String text = tagged ? matcher.group(2) : line;

        model.insertRow(0, new Object[]{LocalTime.now().format(TIME_FORMAT), type, text});
        hint.setText(model.getRowCount() + " notification(s), newest first");
        onNew.run();
    }

    private void clear() {
        model.setRowCount(0);
        hint.setText(EMPTY_HINT);
    }

    private void colorTypeColumn() {
        TableCellRenderer base = table.getDefaultRenderer(Object.class);
        table.getColumnModel().getColumn(TYPE_COLUMN).setCellRenderer(
                (t, value, selected, focused, row, column) -> {
                    Component cell = base.getTableCellRendererComponent(
                            t, value, selected, focused, row, column);
                    cell.setForeground(colorFor(String.valueOf(value)));
                    cell.setFont(Theme.FONT_BOLD);
                    return cell;
                });
    }

    private Color colorFor(String type) {
        return switch (type) {
            case "FRAUD ALERT", "LOAN OVERDUE" -> Theme.DANGER;
            case "OVERDRAFT" -> Theme.WARNING;
            case "INTEREST" -> Theme.SUCCESS;
            case "SMS", "LOAN" -> Theme.TEAL_DARK;
            default -> Theme.MUTED;
        };
    }
}