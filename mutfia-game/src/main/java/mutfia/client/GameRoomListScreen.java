package mutfia.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import mutfia.client.handler.ClientMessageHandler;

public class GameRoomListScreen {
    private static final int WIDTH = 980;
    private static final int HEIGHT = 720;

    private JFrame mainFrame;
    private JList<Map<String, Object>> roomList;
    private DefaultListModel<Map<String, Object>> roomListModel;
    private List<Map<String, Object>> rooms;

    private String playerName;

    public GameRoomListScreen(String playerName) {
        this.playerName = playerName;

        registerHandlers();
        initialize();
        requestRoomList();
    }

    private void registerHandlers() {
        ClientMessageHandler.register("ROOM_LIST", msg -> {
            SwingUtilities.invokeLater(() -> {
                rooms = (List<Map<String, Object>>) msg.data.get("rooms");
                updateRoomList();
            });
        });

        ClientMessageHandler.register("CREATE_ROOM", msg -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainFrame,
                        "방이 생성되었습니다: " + msg.data.get("roomName"));
                requestRoomList();
            });
        });

        ClientMessageHandler.register("JOIN_ROOM", msg -> {
            SwingUtilities.invokeLater(() -> {
                if (msg.status.name().equals("ERROR")) {
                    JOptionPane.showMessageDialog(mainFrame,
                            msg.message,
                            "입장 실패",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                mainFrame.dispose();
                GameScreenManager.open(msg.data, playerName);
            });
        });

    }

    private void initialize() {
        mainFrame = new JFrame("방 목록 - " + playerName);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);

        JPanel background = UIStyle.gradientPanel(new BorderLayout(14, 14));
        background.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = UIStyle.glassCard(new BorderLayout(10, 2));
        JLabel iconLabel = new JLabel(UIStyle.loadIcon("/images/mutfia_icon.png", 52));
        JLabel titleLabel = new JLabel("게임 방 선택");
        titleLabel.setFont(UIStyle.displayFont(30));
        titleLabel.setForeground(Color.WHITE);
        JLabel subtitleLabel = new JLabel("친구들과 빠르게 시작하거나 새 방을 만드세요");
        subtitleLabel.setForeground(new Color(210, 220, 245));
        subtitleLabel.setFont(UIStyle.bodyFont(14));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new GridLayout(2, 1));
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        header.add(iconLabel, BorderLayout.WEST);
        header.add(textPanel, BorderLayout.CENTER);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setCellRenderer(new RoomCardRenderer());
        roomList.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel listCard = UIStyle.glassCard(new BorderLayout());
        listCard.setBackground(UIStyle.CARD_BG_STRONG);
        listCard.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton createButton = UIStyle.primaryButton("방 만들기", UIStyle.loadIcon("/images/add.png", 20));
        createButton.setPreferredSize(new Dimension(150, 48));
        createButton.addActionListener(e -> createRoom());

        JButton joinButton = UIStyle.ghostButton("방 입장", UIStyle.loadIcon("/images/door.png", 20));
        joinButton.setPreferredSize(new Dimension(140, 48));
        joinButton.addActionListener(e -> joinRoom());

        JButton refreshButton = UIStyle.ghostButton("새로고침", UIStyle.loadIcon("/images/reload.png", 20));
        refreshButton.setPreferredSize(new Dimension(150, 48));
        refreshButton.addActionListener(e -> requestRoomList());

        buttonPanel.add(refreshButton);
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);

        background.add(header, BorderLayout.NORTH);
        background.add(listCard, BorderLayout.CENTER);
        background.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame.setContentPane(background);
        mainFrame.setVisible(true);
    }

    private void requestRoomList() {
        ServerConnection.send("GET_ROOMS", Map.of());
    }

    private void updateRoomList() {
        roomListModel.clear();

        if (rooms == null || rooms.isEmpty()) {
            roomList.setEnabled(false);
            roomListModel.addElement(Map.of("placeholder", true, "roomName", "생성된 방이 없습니다."));
            return;
        }

        roomList.setEnabled(true);
        for (Map<String, Object> room : rooms) {
            roomListModel.addElement(room);
        }
    }

    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(
                mainFrame,
                "방 이름을 입력하세요:",
                "방 만들기",
                JOptionPane.PLAIN_MESSAGE
        );

        if (roomName == null || roomName.trim().isEmpty()) {
            return;
        }

        ServerConnection.send("CREATE_ROOM", Map.of("roomName", roomName.trim()));
    }

    private void joinRoom() {
        int idx = roomList.getSelectedIndex();

        if (idx < 0 || rooms == null || idx >= rooms.size()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "입장할 방을 선택해주세요.");
            return;
        }

        Map<String, Object> room = rooms.get(idx);

        boolean isPlaying = (boolean) room.get("isPlaying");
        if (isPlaying) {
            JOptionPane.showMessageDialog(mainFrame,
                    "이미 게임 중인 방입니다!");
            return;
        }

        long roomId = ((Number) room.get("roomId")).longValue();

        ServerConnection.send("JOIN_ROOM", Map.of("roomId", roomId));
    }

    private static class RoomCardRenderer extends JPanel implements ListCellRenderer<Map<String, Object>> {
        private final JLabel title = new JLabel();
        private final JLabel meta = new JLabel();
        private final JLabel status = new JLabel();

        RoomCardRenderer() {
            setOpaque(false);
            setLayout(new BorderLayout(10, 2));
            title.setForeground(Color.WHITE);
            title.setFont(UIStyle.displayFont(18));
            meta.setForeground(new Color(210, 215, 235));
            meta.setFont(UIStyle.bodyFont(13));
            status.setForeground(UIStyle.ACCENT_GOLD);
            status.setFont(UIStyle.bodyFont(13).deriveFont(java.awt.Font.BOLD));
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList<? extends Map<String, Object>> list, Map<String, Object> value, int index, boolean isSelected, boolean cellHasFocus) {
            removeAll();

            boolean placeholder = value != null && Boolean.TRUE.equals(value.get("placeholder"));
            JPanel card = UIStyle.glassCard(new BorderLayout(8, 4));
            card.setOpaque(false);
            card.setBackground(isSelected ? UIStyle.CARD_BG_STRONG : UIStyle.CARD_BG);

            if (placeholder) {
                title.setText((String) value.getOrDefault("roomName", "방이 없습니다."));
                title.setHorizontalAlignment(SwingConstants.CENTER);
                meta.setText("친구보다 먼저 멋진 첫 방을 만들어보세요.");
                meta.setHorizontalAlignment(SwingConstants.CENTER);
                status.setIcon(UIStyle.loadIcon("/images/day.png", 22));
                status.setText("대기");
            } else {
                title.setHorizontalAlignment(SwingConstants.LEFT);
                meta.setHorizontalAlignment(SwingConstants.LEFT);

                String name = (String) value.get("roomName");
                int players = ((Number) value.get("players")).intValue();
                int maxPlayers = ((Number) value.get("maxPlayers")).intValue();
                boolean isPlaying = (boolean) value.get("isPlaying");

                title.setText(name);
                meta.setText(String.format("%d / %d 플레이어", players, maxPlayers));

                Icon statusIcon = UIStyle.loadIcon(isPlaying ? "/images/night.png" : "/images/day.png", 24);
                status.setIcon(statusIcon);
                status.setText(isPlaying ? "게임 중" : "대기 중");
                status.setForeground(isPlaying ? UIStyle.ACCENT_PINK : UIStyle.ACCENT_GOLD);
            }

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);
            textPanel.add(title);
            textPanel.add(meta);

            card.add(textPanel, BorderLayout.CENTER);
            card.add(status, BorderLayout.EAST);

            add(card, BorderLayout.CENTER);
            setBorder(new EmptyBorder(6, 4, 6, 4));

            return this;
        }
    }
}
