package gui;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StyledButton extends JButton {
    public enum Kind { PRIMARY, SECONDARY, SIDEBAR }

    private static final int ARC = 12;
    private static final Color NO_FILL = new Color(0, 0, 0, 0);
    private static final Color DISABLED_FILL = new Color(0xC9D2D6);

    private final Kind kind;
    private boolean hovered;
    private boolean active;

    public StyledButton(String text, Kind kind) {
        super(text);
        this.kind = kind;
        setFont(Theme.FONT_BOLD);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics metrics = getFontMetrics(getFont());
        int width = metrics.stringWidth(getText()) + 2 * horizontalPadding();
        int height = metrics.getHeight() + 2 * verticalPadding();
        return new Dimension(width, height);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(fillColor());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
        if (kind == Kind.SECONDARY) {
            g2.setColor(isEnabled() ? Theme.TEAL : DISABLED_FILL);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
        }

        g2.setFont(getFont());
        g2.setColor(textColor());
        FontMetrics metrics = g2.getFontMetrics();
        int x = kind == Kind.SIDEBAR
                ? horizontalPadding()
                : (getWidth() - metrics.stringWidth(getText())) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(getText(), x, y);
        g2.dispose();
    }

    private Color fillColor() {
        if (!isEnabled()) {
            return kind == Kind.SIDEBAR ? NO_FILL : DISABLED_FILL;
        }
        return switch (kind) {
            case PRIMARY -> hovered ? Theme.TEAL_DARK : Theme.TEAL;
            case SECONDARY -> hovered ? Theme.AQUA : Color.WHITE;
            case SIDEBAR -> active ? Theme.TEAL : hovered ? Theme.NAVY_LIGHT : NO_FILL;
        };
    }

    private Color textColor() {
        if (!isEnabled()) {
            return Theme.MUTED;
        }
        return switch (kind) {
            case PRIMARY -> Color.WHITE;
            case SECONDARY -> Theme.TEAL_DARK;
            case SIDEBAR -> active ? Color.WHITE : Theme.AQUA;
        };
    }

    private int horizontalPadding() {
        return kind == Kind.SIDEBAR ? 20 : 18;
    }

    private int verticalPadding() {
        return kind == Kind.SIDEBAR ? 12 : 9;
    }
}