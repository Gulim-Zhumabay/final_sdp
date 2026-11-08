package gui;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Locale;

public final class Ui {
    private static final int ROW_HEIGHT = 38;
    private static final int FIELD_HEIGHT = 38;
    private static final int CELL_PADDING = 12;

    private Ui() {
    }

    public static JLabel title(String text) {
        return label(text, Theme.FONT_TITLE, Theme.NAVY);
    }

    public static JLabel heading(String text) {
        return label(text, Theme.FONT_HEADING, Theme.NAVY);
    }

    public static JLabel body(String text) {
        return label(text, Theme.FONT, Theme.NAVY);
    }

    public static JLabel muted(String text) {
        return label(text, Theme.FONT_SMALL, Theme.MUTED);
    }

    public static JLabel formLabel(String text) {
        return label(text, Theme.FONT_SMALL.deriveFont(Font.BOLD), Theme.MUTED);
    }

    public static String money(double amount) {
        return String.format(Locale.US, "%,.2f", amount);
    }

    public static <T extends JTextField> T styled(T field) {
        field.setFont(Theme.FONT);
        field.setForeground(Theme.NAVY);
        field.setBackground(Color.WHITE);
        field.setCaretColor(Theme.TEAL);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, FIELD_HEIGHT));
        return field;
    }

    public static <T extends JComboBox<?>> T styled(T combo) {
        combo.setFont(Theme.FONT);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(combo.getPreferredSize().width, FIELD_HEIGHT));
        return combo;
    }

    public static <T extends JCheckBox> T styled(T box) {
        box.setFont(Theme.FONT);
        box.setForeground(Theme.NAVY);
        box.setOpaque(false);
        box.setFocusPainted(false);
        return box;
    }

    public static JScrollPane scroll(Component view) {
        JScrollPane pane = new JScrollPane(view);
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.getViewport().setBackground(Color.WHITE);
        return pane;
    }

    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(ROW_HEIGHT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(Theme.FONT);
        table.setBackground(Color.WHITE);
        table.setDefaultRenderer(Object.class, new BodyRenderer());

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(0, ROW_HEIGHT));
    }

    public static void alignRight(JTable table, int column) {
        TableCellRenderer base = table.getDefaultRenderer(Object.class);
        table.getColumnModel().getColumn(column).setCellRenderer(
                (t, value, selected, focused, row, col) -> {
                    Component cell = base.getTableCellRendererComponent(
                            t, value, selected, focused, row, col);
                    if (cell instanceof JLabel label) {
                        label.setHorizontalAlignment(SwingConstants.RIGHT);
                    }
                    return cell;
                });
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private static final class BodyRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, CELL_PADDING, 0, CELL_PADDING));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(Theme.FONT);
            setForeground(Theme.NAVY);
            setBackground(selected ? Theme.AQUA : row % 2 == 0 ? Color.WHITE : Theme.ROW_ALT);
            return this;
        }
    }

    private static final class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean selected, boolean focused,
                                                       int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, CELL_PADDING, 0, CELL_PADDING));
            setHorizontalAlignment(SwingConstants.LEFT);
            setFont(Theme.FONT_BOLD);
            setForeground(Color.WHITE);
            setBackground(Theme.NAVY);
            return this;
        }
    }
}