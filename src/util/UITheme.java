package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import model.StockStatus;

/**
 * Central design system and styling utility for MEDICARE.
 * Provides harmonious medical-themed color palettes, typography, and table styling.
 */
public final class UITheme {

    // Palette: Medical Emerald & Slate Theme
    public static final Color COLOR_PRIMARY = new Color(13, 148, 136);       // #0D9488 - Emerald Teal
    public static final Color COLOR_PRIMARY_DARK = new Color(15, 118, 110);  // #0F766E - Deep Teal
    public static final Color COLOR_PRIMARY_LIGHT = new Color(204, 251, 241); // #CCFBF1 - Soft Mint
    public static final Color COLOR_PRIMARY_TINT = new Color(240, 253, 250); // #F0FDFA - Ice Teal

    public static final Color COLOR_ACCENT = new Color(37, 99, 235);        // #2563EB - Royal Blue
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // #10B981 - Green
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // #F59E0B - Amber
    public static final Color COLOR_ORANGE = new Color(249, 115, 22);       // #F97316 - Warm Orange
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // #EF4444 - Coral Red

    public static final Color COLOR_BG = new Color(248, 250, 252);          // #F8FAFC - Main Slate Canvas
    public static final Color COLOR_CARD_BG = Color.WHITE;                  // #FFFFFF - Surface
    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);     // #0F172A - Deep Midnight Navy
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(30, 41, 59); // #1E293B
    public static final Color COLOR_HEADER_BG = Color.WHITE;

    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // #0F172A - High Contrast
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // #64748B - Slate Muted
    public static final Color COLOR_BORDER = new Color(226, 232, 240);      // #E2E8F0 - Divider Border
    public static final Color COLOR_ROW_ALT = new Color(248, 250, 252);     // Table Striping
    public static final Color COLOR_ROW_SELECT = new Color(204, 251, 241);  // Table Row Selection

    // Typography
    public static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_CAPTION = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private UITheme() {
    }

    /**
     * Initializes system properties for high-DPI and smooth anti-aliased font rendering.
     */
    public static void setupSystemSettings() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    /**
     * Styles any JTable into a sleek, modern, readable data grid.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(FONT_BODY);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setBackground(COLOR_CARD_BG);
        table.setSelectionBackground(COLOR_ROW_SELECT);
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(COLOR_BORDER);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(COLOR_TEXT_MUTED);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        // Default cell alignment and padding
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? COLOR_CARD_BG : COLOR_ROW_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);
    }

    /**
     * Pill Badge Renderer for rendering Stock Status indicators nicely inside tables.
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(FONT_CAPTION.deriveFont(Font.BOLD));

            if (value instanceof StockStatus) {
                StockStatus status = (StockStatus) value;
                label.setText(status.getLabel().toUpperCase());
                label.setForeground(Color.decode(status.getHexColor()));
                label.setBackground(Color.decode(status.getBgHexColor()));
            } else if (value != null) {
                String str = value.toString();
                label.setText(str);
                if ("EXPIRED".equalsIgnoreCase(str)) {
                    label.setForeground(COLOR_DANGER);
                    label.setBackground(new Color(254, 242, 242));
                } else if ("LOW STOCK".equalsIgnoreCase(str)) {
                    label.setForeground(COLOR_WARNING);
                    label.setBackground(new Color(255, 251, 235));
                } else if ("EXPIRING SOON".equalsIgnoreCase(str)) {
                    label.setForeground(COLOR_ORANGE);
                    label.setBackground(new Color(255, 247, 237));
                } else {
                    label.setForeground(COLOR_SUCCESS);
                    label.setBackground(new Color(236, 253, 245));
                }
            }

            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
            return label;
        }
    }

    // Friendly Alert / Dialog Methods
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int res = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return res == JOptionPane.YES_OPTION;
    }
}
