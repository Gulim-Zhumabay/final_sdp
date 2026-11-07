package gui;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.util.Optional;

public final class Dialogs {

    private Dialogs() {
    }

    public static void run(Component parent, Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(parent, e.getMessage());
        }
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Banking System",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static Optional<String> askText(Component parent, String prompt) {
        return Optional.ofNullable(JOptionPane.showInputDialog(parent, prompt))
                .map(String::trim);
    }

    public static Optional<Double> askNumber(Component parent, String prompt) {
        return askText(parent, prompt).map(Dialogs::parseNumber);
    }

    public static Optional<Integer> askWholeNumber(Component parent, String prompt) {
        return askText(parent, prompt).map(Dialogs::parseWholeNumber);
    }

    private static double parseNumber(String text) {
        try {
            return Double.parseDouble(text.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a number");
        }
    }

    private static int parseWholeNumber(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a whole number");
        }
    }
}