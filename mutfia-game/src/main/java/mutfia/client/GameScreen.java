package mutfia.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import mutfia.client.handler.ClientMessageHandler;

public class GameScreen {

    private JFrame frame;

    private DefaultListModel<String> playerListModel;
    private JTextArea logArea;
    private JTextField chatInput;

    public GameScreen(Map<String, Object> roomInfo) {
        registerHandler();

        String roomName = (String) roomInfo.get("roomName");
        long roomId = ((Number) roomInfo.get("roomId")).longValue();
        List<String> players = (List<String>) roomInfo.get("playerList");

        frame = new JFrame("멋피아 게임방 - " + roomName);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 상태 패널 (역할, 밤/낮 표시)
        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(0, 80));
        statusPanel.setBackground(new Color(45, 45, 70));
        statusPanel.setLayout(new FlowLayout());

        JLabel stateLabel = new JLabel("대기 중...");
        stateLabel.setForeground(Color.WHITE);
        stateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        statusPanel.add(stateLabel);

        // 채팅 로그
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 40));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane logScroll = new JScrollPane(logArea);

        // 채팅 입력창
        chatInput = new JTextField();
        chatInput.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                ServerConnection.send("CHAT", Map.of("message", text));
                chatInput.setText("");
            }
        });

        // 레이아웃에 배치
        frame.add(statusPanel, BorderLayout.NORTH);
        frame.add(logScroll, BorderLayout.CENTER);
        frame.add(chatInput, BorderLayout.SOUTH);

        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(30, 30, 50));

        chatInput = new JTextField();
        chatInput.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        chatInput.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                ServerConnection.send("CHAT", Map.of("message", text));
                chatInput.setText("");
            }
        });

        frame.add(chatInput, BorderLayout.SOUTH);

        JPanel playerPanel = new JPanel();
        playerPanel.setPreferredSize(new Dimension(200, 0));
        playerPanel.setBackground(new Color(40, 40, 70));
        playerPanel.setLayout(new BorderLayout());

        JLabel playerLabel = new JLabel("플레이어 목록", SwingConstants.CENTER);
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        playerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        playerListModel = new DefaultListModel<>();
        if (players != null) {
            for (String p : players) {
                playerListModel.addElement("🦁 " + p);
            }
        }

        JList<String> playerList = new JList<>(playerListModel);
        playerList.setBackground(new Color(50, 50, 90));
        playerList.setForeground(Color.WHITE);

        playerPanel.add(playerLabel, BorderLayout.NORTH);
        playerPanel.add(new JScrollPane(playerList), BorderLayout.CENTER);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 40));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane logScroll = new JScrollPane(logArea);

        appendLog("🦁 방에 입장했습니다. (Room ID: " + roomId + ")");

        frame.add(playerPanel, BorderLayout.WEST);
        frame.add(logScroll, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
    }

    public void addPlayer(String name) {
        playerListModel.addElement("🦁 " + name);
        appendLog("🦁 " + name + "님이 방에 입장했습니다.");
    }

    public JFrame getFrame() {
        return frame;
    }

    private void registerHandler() {
        // 다른 플레이어 입장
        ClientMessageHandler.register("PLAYER_JOIN", msg -> {
            SwingUtilities.invokeLater(() -> {
                String name = (String) msg.data.get("name");
                addPlayer(name);
            });
        });

        // 채팅
        ClientMessageHandler.register("CHAT", msg -> {
            SwingUtilities.invokeLater(() -> {
                String sender = (String) msg.data.get("sender");
                String message = (String) msg.data.get("message");
                appendLog("🦁 " + sender + ": " + message);
            });
        });
    }
}