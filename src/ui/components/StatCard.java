package ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import util.UITheme;

/**
 * Metric card displaying a key performance indicator (KPI) with an accent bar and icon.
 */
public class StatCard extends RoundedPanel {
    private final JLabel titleLabel;
    private final JLabel valueLabel;
    private final JLabel subtitleLabel;
    private final JLabel iconLabel;
    private final Color accentColor;

    public StatCard(String title, String initialValue, String subtitle, String icon, Color accentColor) {
        super(14, UITheme.COLOR_CARD_BG, UITheme.COLOR_BORDER, 1);
        this.accentColor = accentColor;
        setLayout(new BorderLayout(12, 6));
        setPreferredSize(new Dimension(190, 110));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // Center Panel for text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BorderLayout(0, 4));

        // Top Row: Title + Icon
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(UITheme.FONT_CAPTION.deriveFont(Font.BOLD));
        titleLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        topRow.add(titleLabel, BorderLayout.WEST);

        iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        topRow.add(iconLabel, BorderLayout.EAST);

        textPanel.add(topRow, BorderLayout.NORTH);

        // Middle: Large Value
        valueLabel = new JLabel(initialValue);
        valueLabel.setFont(UITheme.FONT_STAT_NUM);
        valueLabel.setForeground(UITheme.COLOR_TEXT_MAIN);
        textPanel.add(valueLabel, BorderLayout.CENTER);

        // Bottom: Subtitle
        subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(UITheme.FONT_CAPTION);
        subtitleLabel.setForeground(UITheme.COLOR_TEXT_MUTED);
        textPanel.add(subtitleLabel, BorderLayout.SOUTH);

        add(textPanel, BorderLayout.CENTER);
    }

    public void setValue(String val) {
        valueLabel.setText(val);
        repaint();
    }

    public void setSubtitle(String sub) {
        subtitleLabel.setText(sub);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw left accent pill bar
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 14, 4, getHeight() - 28, 4, 4);
        g2.dispose();
    }
}
