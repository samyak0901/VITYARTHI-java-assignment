package ui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import util.UITheme;

/**
 * Modern custom-rendered JButton featuring smooth hover states, rounded geometry, and custom color themes.
 */
public class StyledButton extends JButton {
    public enum ButtonType {
        PRIMARY, SECONDARY, SUCCESS, DANGER, WARNING, SIDEBAR
    }

    private ButtonType type;
    private Color baseBg;
    private Color hoverBg;
    private Color pressedBg;
    private Color textNormal;
    private Color textHover;
    private Color borderColor;
    private int cornerRadius = 10;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isActive = false;

    public StyledButton(String text) {
        this(text, ButtonType.PRIMARY);
    }

    public StyledButton(String text, ButtonType type) {
        super(text);
        this.type = type;
        initStyle();
    }

    private void initStyle() {
        setFont(UITheme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        applyPalette();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    private void applyPalette() {
        switch (type) {
            case PRIMARY:
                baseBg = UITheme.COLOR_PRIMARY;
                hoverBg = UITheme.COLOR_PRIMARY_DARK;
                pressedBg = UITheme.COLOR_PRIMARY_DARK.darker();
                textNormal = Color.WHITE;
                textHover = Color.WHITE;
                borderColor = null;
                break;
            case SECONDARY:
                baseBg = Color.WHITE;
                hoverBg = new Color(241, 245, 249);
                pressedBg = new Color(226, 232, 240);
                textNormal = UITheme.COLOR_TEXT_MAIN;
                textHover = UITheme.COLOR_PRIMARY;
                borderColor = UITheme.COLOR_BORDER;
                break;
            case SUCCESS:
                baseBg = UITheme.COLOR_SUCCESS;
                hoverBg = new Color(5, 150, 105);
                pressedBg = new Color(4, 120, 87);
                textNormal = Color.WHITE;
                textHover = Color.WHITE;
                borderColor = null;
                break;
            case DANGER:
                baseBg = UITheme.COLOR_DANGER;
                hoverBg = new Color(220, 38, 38);
                pressedBg = new Color(185, 28, 28);
                textNormal = Color.WHITE;
                textHover = Color.WHITE;
                borderColor = null;
                break;
            case WARNING:
                baseBg = UITheme.COLOR_WARNING;
                hoverBg = new Color(217, 119, 6);
                pressedBg = new Color(180, 83, 9);
                textNormal = Color.WHITE;
                textHover = Color.WHITE;
                borderColor = null;
                break;
            case SIDEBAR:
                baseBg = UITheme.COLOR_SIDEBAR_BG;
                hoverBg = UITheme.COLOR_SIDEBAR_ACTIVE;
                pressedBg = new Color(51, 65, 85);
                textNormal = new Color(203, 213, 225); // Slate 300
                textHover = Color.WHITE;
                borderColor = null;
                cornerRadius = 8;
                break;
        }
        setForeground(textNormal);
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (type == ButtonType.SIDEBAR) {
            if (active) {
                baseBg = UITheme.COLOR_PRIMARY;
                hoverBg = UITheme.COLOR_PRIMARY_DARK;
                textNormal = Color.WHITE;
                setForeground(Color.WHITE);
            } else {
                baseBg = UITheme.COLOR_SIDEBAR_BG;
                hoverBg = UITheme.COLOR_SIDEBAR_ACTIVE;
                textNormal = new Color(203, 213, 225);
                setForeground(textNormal);
            }
        }
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Determine background
        Color fill;
        if (isPressed) {
            fill = pressedBg;
        } else if (isHovered || isActive) {
            fill = hoverBg;
        } else {
            fill = baseBg;
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);

        // Border if applicable
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, width - 1, height - 1, cornerRadius, cornerRadius);
        }

        // Set text color
        if (isHovered || isActive) {
            setForeground(textHover);
        } else {
            setForeground(textNormal);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
