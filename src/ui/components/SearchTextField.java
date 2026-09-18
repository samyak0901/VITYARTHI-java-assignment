package ui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import util.UITheme;

/**
 * Modern styled text input field with placeholder support, rounded border, and focus glow.
 */
public class SearchTextField extends JTextField {
    private String placeholder;
    private boolean isFocused = false;
    private final int cornerRadius = 10;

    public SearchTextField(String placeholder) {
        this(placeholder, 20);
    }

    public SearchTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        setFont(UITheme.FONT_BODY);
        setForeground(UITheme.COLOR_TEXT_MAIN);
        setCaretColor(UITheme.COLOR_PRIMARY);
        setOpaque(false);
        setBorder(new EmptyBorder(8, 14, 8, 14));

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(Color.WHITE);
        g2.fillRoundRect(1, 1, width - 2, height - 2, cornerRadius, cornerRadius);

        // Border
        if (isFocused) {
            g2.setColor(UITheme.COLOR_PRIMARY);
            g2.setStroke(new java.awt.BasicStroke(1.5f));
        } else {
            g2.setColor(UITheme.COLOR_BORDER);
        }
        g2.drawRoundRect(1, 1, width - 3, height - 3, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);

        // Paint placeholder if empty and not focused
        if (getText().isEmpty() && !isFocused && placeholder != null) {
            Graphics2D gPlaceholder = (Graphics2D) g.create();
            gPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gPlaceholder.setColor(UITheme.COLOR_TEXT_MUTED);
            gPlaceholder.setFont(getFont());
            Insets insets = getInsets();
            int fontAscent = gPlaceholder.getFontMetrics().getAscent();
            int y = insets.top + (height - insets.top - insets.bottom - gPlaceholder.getFontMetrics().getHeight()) / 2 + fontAscent;
            gPlaceholder.drawString(placeholder, insets.left, y);
            gPlaceholder.dispose();
        }
    }
}
