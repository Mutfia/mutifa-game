package mutfia.client;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

public final class UIStyle {
    public static final Color NIGHT_START = new Color(10, 15, 32);
    public static final Color NIGHT_END = new Color(43, 54, 88);
    public static final Color ACCENT_GOLD = new Color(255, 199, 109);
    public static final Color ACCENT_BLUE = new Color(99, 149, 222);
    public static final Color ACCENT_PINK = new Color(219, 114, 162);
    public static final Color CARD_BG = new Color(255, 255, 255, 34);
    public static final Color CARD_BG_STRONG = new Color(255, 255, 255, 70);
    public static final Color CARD_BORDER = new Color(255, 255, 255, 80);

    private UIStyle() {
    }

    public static java.awt.Font displayFont(int size) {
        return new java.awt.Font("Noto Sans KR", java.awt.Font.BOLD, size);
    }

    public static java.awt.Font bodyFont(int size) {
        return new java.awt.Font("Noto Sans KR", java.awt.Font.PLAIN, size);
    }

    public static JPanel gradientPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint paint = new GradientPaint(0, 0, NIGHT_START, getWidth(), getHeight(), NIGHT_END);
                g2.setPaint(paint);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        return panel;
    }

    public static JPanel glassCard(LayoutManager layout) {
        JPanel panel = new JPanel(layout) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return panel;
    }

    public static JButton primaryButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        styleButton(btn, new Color(255, 255, 255, 230), new Color(17, 23, 45));
        return btn;
    }

    public static JButton ghostButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        styleButton(btn, new Color(68, 82, 125, 210), Color.WHITE);
        return btn;
    }

    public static void styleButton(JButton btn, Color background, Color foreground) {
        btn.setBackground(background);
        btn.setForeground(foreground);
        btn.setFont(bodyFont(16).deriveFont(java.awt.Font.BOLD));
        btn.setFocusPainted(false);
        btn.setBorder(new RoundedBorder(14, new Color(255, 255, 255, 90)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static ImageIcon loadIcon(String path, int size) {
        URL url = UIStyle.class.getResource(path);
        if (url == null) {
            return null;
        }
        ImageIcon raw = new ImageIcon(url);
        if (size <= 0) {
            return raw;
        }
        Image scaled = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c, Insets insets) {
            insets.left = insets.right = insets.top = insets.bottom = radius / 2;
            return insets;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            RoundRectangle2D.Double rect = new RoundRectangle2D.Double(x + 0.5, y + 0.5, width - 1, height - 1, radius, radius);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(rect);
            g2.dispose();
        }
    }
}
