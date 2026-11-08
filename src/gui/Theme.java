package gui;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Font;

public final class Theme {
    public static final Color NAVY = new Color(0x071B2B);
    public static final Color NAVY_LIGHT = new Color(0x12304A);
    public static final Color TEAL = new Color(0x167D8D);
    public static final Color TEAL_DARK = new Color(0x0F6270);
    public static final Color AQUA = new Color(0xB9E4E8);
    public static final Color BACKGROUND = new Color(0xEEEEEE);
    public static final Color ROW_ALT = new Color(0xF5FAFB);
    public static final Color BORDER = new Color(0xD3DADD);
    public static final Color MUTED = new Color(0x5E7080);
    public static final Color DANGER = new Color(0xB03A2E);
    public static final Color WARNING = new Color(0xB9770E);
    public static final Color SUCCESS = new Color(0x1E8449);

    public static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BOLD = FONT.deriveFont(Font.BOLD);
    public static final Font FONT_SMALL = FONT.deriveFont(12f);
    public static final Font FONT_HEADING = FONT.deriveFont(Font.BOLD, 16f);
    public static final Font FONT_TITLE = FONT.deriveFont(Font.BOLD, 24f);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private Theme() {
    }

    public static void install() {
        UIManager.put("control", BACKGROUND);
        UIManager.put("info", Color.WHITE);
        UIManager.put("nimbusBase", TEAL);
        UIManager.put("nimbusBlueGrey", new Color(0x8FB5BB));
        UIManager.put("nimbusFocus", TEAL);
        UIManager.put("nimbusLightBackground", Color.WHITE);
        UIManager.put("nimbusSelectionBackground", TEAL);
        UIManager.put("text", NAVY);
        UIManager.put("defaultFont", FONT);
        selectNimbus();
    }

    private static void selectNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
        }
    }
}