package gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.PrintStream;

public class LogPanel extends CardPanel {
    private final JTextArea area = new JTextArea(5, 60);
    private PrintStream originalConsole = System.out;

    public LogPanel() {
        super(new BorderLayout(0, 8), Theme.NAVY, Theme.NAVY);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel header = new JLabel("NOTIFICATIONS");
        header.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
        header.setForeground(Theme.AQUA);

        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(false);
        area.setFont(Theme.FONT_MONO);
        area.setForeground(Theme.AQUA);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(Theme.NAVY);
        scroll.getViewport().setBackground(Theme.NAVY);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void captureConsole() {
        originalConsole = System.out;
        System.setOut(new PrintStream(originalConsole, true) {
            @Override
            public void println(String line) {
                super.println(line);
                log(line);
            }
        });
    }

    public void restoreConsole() {
        System.setOut(originalConsole);
    }

    private void log(String line) {
        area.append(line + System.lineSeparator());
        area.setCaretPosition(area.getDocument().getLength());
    }
}