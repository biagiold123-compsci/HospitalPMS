package hospital.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Static helper methods for consistent Swing styling across all panels.
 */
public final class UIHelper {

    // ── Colour palette ────────────────────────────────────────────────────────
    public static final Color PRIMARY      = new Color(0x1E6FA8);
    public static final Color PRIMARY_DARK = new Color(0x145080);
    public static final Color ACCENT       = new Color(0x28A745);
    public static final Color DANGER       = new Color(0xDC3545);
    public static final Color WARN         = new Color(0xFFC107);
    public static final Color BG           = new Color(0xF4F6F9);
    public static final Color SURFACE      = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(0x1A1A2E);
    public static final Color TEXT_MUTED   = new Color(0x6C757D);
    public static final Color BORDER       = new Color(0xDEE2E6);

    // ── Fonts ──────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 12);

    private UIHelper() {}

    /** Configures FlatLaf-style global defaults (works with standard Metal too). */
    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Panel.background", BG);
        UIManager.put("TextField.font",   FONT_BODY);
        UIManager.put("Label.font",       FONT_BODY);
        UIManager.put("Button.font",      FONT_BODY);
        UIManager.put("ComboBox.font",    FONT_BODY);
        UIManager.put("Table.font",       FONT_BODY);
        UIManager.put("TableHeader.font", FONT_HEADER);
    }

    // ── Button factory ─────────────────────────────────────────────────────────

    public static JButton primaryButton(String text) {
        return styledButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, ACCENT, Color.WHITE);
    }

    public static JButton neutralButton(String text) {
        return styledButton(text, new Color(0xE9ECEF), TEXT_PRIMARY);
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFont(FONT_BODY);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        return btn;
    }

    // ── Label factory ──────────────────────────────────────────────────────────

    public static JLabel headerLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    // ── Field factory ──────────────────────────────────────────────────────────

    public static JTextField field(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    public static JTextArea textArea(int rows, int cols) {
        JTextArea ta = new JTextArea(rows, cols);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(5, 8, 5, 8));
        return ta;
    }

    // ── Card panel ─────────────────────────────────────────────────────────────

    public static JPanel cardPanel() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 16, 16, 16)));
        return p;
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }
}