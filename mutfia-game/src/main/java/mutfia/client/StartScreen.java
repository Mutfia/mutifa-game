package mutfia.client;

import java.awt.*;
import java.net.URL;
import java.util.Map;
import javax.swing.*;
import mutfia.client.handler.ClientMessageHandler;

public class StartScreen {
    private static final String BACKGROUND_IMAGE_PATH = "/images/night.png";
    private static final int WIDTH = 520;
    private static final int HEIGHT = 700;

    private JFrame mainFrame;
    private Image backgroundImage;

    public void initialize() {
        loadBackground();

        mainFrame = new JFrame("멋피아");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);

        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UIStyle.NIGHT_START, getWidth(), getHeight(), UIStyle.NIGHT_END));
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (backgroundImage != null) {
                    g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 80), 0, getHeight(), new Color(0, 0, 0, 180)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        backgroundPanel.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(28, 24, 32, 24));

        JPanel heroCard = UIStyle.glassCard(new BorderLayout());
        heroCard.setLayout(new BoxLayout(heroCard, BoxLayout.Y_AXIS));
        heroCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel logoLabel = new JLabel(UIStyle.loadIcon("/images/mafia_icon.png", 92));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("멋피아");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(UIStyle.displayFont(54));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("서로를 속이고, 서로를 밝혀내세요.");
        subtitleLabel.setForeground(new Color(220, 228, 255));
        subtitleLabel.setFont(UIStyle.bodyFont(16));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        heroCard.add(logoLabel);
        heroCard.add(Box.createVerticalStrut(8));
        heroCard.add(titleLabel);
        heroCard.add(Box.createVerticalStrut(6));
        heroCard.add(subtitleLabel);

        JPanel rolesPanel = UIStyle.glassCard(new GridBagLayout());
        rolesPanel.setBackground(UIStyle.CARD_BG_STRONG);
        rolesPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        rolesPanel.add(createRoleBadge("시민", "토론으로 마피아를 찾아내세요",
                "/images/citizen_icon.png", UIStyle.ACCENT_BLUE), gbc);

        gbc.gridx = 1;
        rolesPanel.add(createRoleBadge("경찰", "밤에 용의자를 조사하세요",
                "/images/police_icon.png", UIStyle.ACCENT_GOLD), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        rolesPanel.add(createRoleBadge("의사", "한 명을 보호해 생존시킵니다",
                "/images/doctor_icon.png", UIStyle.ACCENT_PINK), gbc);

        gbc.gridx = 1;
        rolesPanel.add(createRoleBadge("마피아", "흔적 없이 제거하고 속이세요",
                "/images/mafia_icon.png", new Color(255, 120, 120)), gbc);

        JButton enterButton = UIStyle.primaryButton("게임 입장하기", UIStyle.loadIcon("/images/door.png", 28));
        enterButton.setPreferredSize(new Dimension(360, 70));
        enterButton.setMaximumSize(new Dimension(420, 72));
        enterButton.setForeground(Color.WHITE);
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterButton.addActionListener(e -> onEnterButtonPressed());

        JLabel serverHint = new JLabel("서버에 연결 후 이름을 입력하면 바로 입장합니다 (127.0.0.1:9999)");
        serverHint.setForeground(new Color(210, 218, 240));
        serverHint.setFont(UIStyle.bodyFont(13));
        serverHint.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(heroCard);
        content.add(Box.createVerticalStrut(18));
        content.add(rolesPanel);
        content.add(Box.createVerticalStrut(22));
        content.add(enterButton);
        content.add(Box.createVerticalStrut(12));
        content.add(serverHint);

        backgroundPanel.add(content, BorderLayout.CENTER);

        mainFrame.setContentPane(backgroundPanel);
        mainFrame.setVisible(true);
    }

    private JPanel createRoleBadge(String name, String description, String iconPath, Color accent) {
        JPanel badge = UIStyle.glassCard(new BorderLayout());
        badge.setBackground(new Color(255, 255, 255, 20));
        badge.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));

        JLabel iconLabel = new JLabel(UIStyle.loadIcon(iconPath, 46));
        iconLabel.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel iconWrapper = new JPanel();
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(54, 54));
        iconWrapper.setLayout(new BoxLayout(iconWrapper, BoxLayout.Y_AXIS));
        iconWrapper.add(iconLabel);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(UIStyle.displayFont(16));
        nameLabel.setForeground(accent);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(UIStyle.bodyFont(11));
        descLabel.setForeground(new Color(220, 225, 245));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(descLabel);

        content.add(iconWrapper);
        content.add(Box.createHorizontalStrut(8));
        content.add(textPanel);

        badge.add(content, BorderLayout.NORTH);
        return badge;
    }

    private void loadBackground() {
        try {
            URL imageUrl = getClass().getResource(BACKGROUND_IMAGE_PATH);
            if (imageUrl != null) {
                ImageIcon bgIcon = new ImageIcon(imageUrl);
                backgroundImage = bgIcon.getImage();
            }
        } catch (Exception e) {
            System.err.println("[Error] 배경 이미지 로드 실패: " + e.getMessage());
        }
    }

    private void onEnterButtonPressed() {
        String playerNameInput = JOptionPane.showInputDialog(
                mainFrame,
                "플레이어 이름을 입력하세요:",
                "이름 입력",
                JOptionPane.PLAIN_MESSAGE
        );

        if (playerNameInput == null || playerNameInput.trim().isEmpty()) {
            return;
        }

        final String playerName = playerNameInput.trim();

        ServerConnection.connect();
        ClientMessageHandler.register("SET_NAME", msg -> {
            SwingUtilities.invokeLater(() -> {
                mainFrame.dispose();
                new GameRoomListScreen(playerName);
            });
        });

        ServerConnection.send("SET_NAME", Map.of("name", playerName));
    }
}
