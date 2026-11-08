package gui;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.io.PrintStream;

public class LogPanel extends JPanel {
    private final JTextArea area = new JTextArea(6, 60);
    private PrintStream originalConsole = System.out;

    public LogPanel() {
        super(new BorderLayout());
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        setBorder(BorderFactory.createTitledBorder("Notifications"));
        add(new JScrollPane(area), BorderLayout.CENTER);
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