package mutfia.client;

import java.util.HashMap;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import mutfia.client.handler.ClientMessageHandler;
import mutfia.server.player.Player;

public class GameScreen {
    private JFrame frame;
    private JTextArea logArea;
    private JTextField chatInput;

    private boolean started = false;
    private Map<Player, String> roles = new HashMap<>();
    private String state = "DAY"; // or "NIGHT"

    public GameScreen(Map<String, Object> roomInfo) {
        registerHandlers();

        String roomName = (String) roomInfo.get("roomName");
        long roomId = ((Number) roomInfo.get("roomId")).longValue();

        frame = new JFrame("멋피아 게임방 - " + roomName);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(30, 30, 50));

        // 상태 패널
        JPanel statusPanel = new JPanel();
        statusPanel.setPreferredSize(new Dimension(0, 80));
        statusPanel.setBackground(new Color(45, 45, 70));

        JLabel stateLabel = new JLabel("대기 중...");
        stateLabel.setForeground(Color.WHITE);
        stateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        statusPanel.add(stateLabel);
        frame.add(statusPanel, BorderLayout.NORTH);

        // 채팅 로그
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 40));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane logScroll = new JScrollPane(logArea);
        frame.add(logScroll, BorderLayout.CENTER);

        // 채팅 입력창
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

        // 입장 로그
        appendLog("🦁 방에 입장했습니다. (Room ID: " + roomId + ")");

        frame.setVisible(true);
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
    }

    public JFrame getFrame() {
        return frame;
    }

    public void registerHandlers() {
        // 채팅
        ClientMessageHandler.register("CHAT", msg -> {
            SwingUtilities.invokeLater(() -> {
                String sender = (String) msg.data.get("sender");
                String message = (String) msg.data.get("message");
                appendLog("🦁 " + sender + ": " + message);
            });
        });

        // 플레이어 입장 알림
        ClientMessageHandler.register("PLAYER_JOIN", msg -> {
            SwingUtilities.invokeLater(() -> {
                String name = (String) msg.data.get("name");
                appendLog("🦁 " + name + "님이 입장했습니다.");
            });
        });
    }
}