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
    private JLabel stateLabel;
    private JLabel timerLabel;
    private JButton abilityButton;

    private boolean started = false;
    private Map<Player, String> roles = new HashMap<>();
    private String state = "DAY"; // or "NIGHT"
    private String myRole;

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
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(0, 80));
        statusPanel.setBackground(new Color(45, 45, 70));

        stateLabel = new JLabel("대기 중...", JLabel.CENTER);
        stateLabel.setForeground(Color.WHITE);
        stateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        timerLabel = new JLabel("", JLabel.RIGHT);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        timerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 30)); // 오른쪽 여백 30픽셀

        statusPanel.add(stateLabel, BorderLayout.CENTER);
        statusPanel.add(timerLabel, BorderLayout.EAST);
        frame.add(statusPanel, BorderLayout.NORTH);

        // 채팅 로그
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(new Color(20, 20, 40));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        JScrollPane logScroll = new JScrollPane(logArea);
        frame.add(logScroll, BorderLayout.CENTER);

        // 입력/능력 패널
        JPanel actionPanel = new JPanel(new BorderLayout(10, 0));
        actionPanel.setBackground(new Color(30, 30, 50));

        abilityButton = new JButton("능력 대기");
        abilityButton.setEnabled(false);
        abilityButton.setFocusable(false);
        abilityButton.setBackground(new Color(70, 70, 110));
        abilityButton.setForeground(Color.WHITE);
        abilityButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        abilityButton.addActionListener(e -> promptAbilityTarget());
        actionPanel.add(abilityButton, BorderLayout.WEST);

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
        actionPanel.add(chatInput, BorderLayout.CENTER);

        frame.add(actionPanel, BorderLayout.SOUTH);

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

        ClientMessageHandler.register("ROLE_ASSIGN", msg -> {
            SwingUtilities.invokeLater(() -> {
                String role = (String) msg.data.get("role");
                appendLog("📌 당신의 역할은 [" + role + "] 입니다.");
                stateLabel.setText("내 역할: " + role);
                myRole = role;
                updateAbilityAvailability();
            });
        });

        ClientMessageHandler.register("GAME_START", msg -> {
            SwingUtilities.invokeLater(() -> {
                appendLog("🎮 게임이 시작되었습니다!");
            });
        });

        ClientMessageHandler.register("PHASE_CHANGE", msg -> {
            SwingUtilities.invokeLater(() -> {
                String phase = (String) msg.data.get("phase");
                state = phase;

                if ("DAY".equalsIgnoreCase(phase)) {
                    stateLabel.setText("현재 상태: 낮");
                    appendLog("🌞 낮이 시작되었습니다.");
                } else if ("NIGHT".equalsIgnoreCase(phase)) {
                    stateLabel.setText("현재 상태: 밤");
                    appendLog("🌙 밤이 시작되었습니다.");
                } else {
                    stateLabel.setText("현재 상태: " + phase);
                    appendLog("⏱ 단계 전환: " + phase);
                }
                updateAbilityAvailability();
            });
        });

        // 타이머 업데이트
        ClientMessageHandler.register("TIMER_UPDATE", msg -> {
            SwingUtilities.invokeLater(() -> {
                int remainingSeconds = ((Number) msg.data.get("remainingSeconds")).intValue();
                // String phase = (String) msg.data.get("phase");
                
                int minutes = remainingSeconds / 60;
                int seconds = remainingSeconds % 60;
                String timeText = String.format("%02d:%02d", minutes, seconds);
                
                timerLabel.setText(timeText);
            });
        });

        ClientMessageHandler.register("PLAYER_KILLED", msg -> {
            SwingUtilities.invokeLater(() -> {
                String name = (String) msg.data.get("name");
                appendLog("☠️ " + name + " 님이 제거되었습니다.");
            });
        });

        ClientMessageHandler.register("USE_ABILITY", msg -> {
            SwingUtilities.invokeLater(() -> {
                String info = msg.data != null ? (String) msg.data.get("message") : null;
                if (info != null) {
                    appendLog("🛠 " + info);
                }
            });
        });
    }

    private void promptAbilityTarget() {
        if (!"NIGHT".equalsIgnoreCase(state)) {
            appendLog("⚠️ 능력은 밤에만 사용할 수 있습니다.");
            return;
        }
        if (myRole == null || "CITIZEN".equalsIgnoreCase(myRole)) {
            appendLog("⚠️ 사용할 수 있는 능력이 없습니다.");
            return;
        }

        String target = JOptionPane.showInputDialog(frame, "대상 플레이어 이름을 입력하세요", "능력 사용", JOptionPane.QUESTION_MESSAGE);
        if (target == null) return; // 취소

        String trimmed = target.trim();
        if (trimmed.isEmpty()) {
            appendLog("⚠️ 대상을 입력해야 합니다.");
            return;
        }

        ServerConnection.send("USE_ABILITY", Map.of("target", trimmed));
        appendLog("🛠 능력을 사용합니다. 대상: " + trimmed);
    }

    private void updateAbilityAvailability() {
        if (abilityButton == null) return;

        boolean canUse = "NIGHT".equalsIgnoreCase(state)
                && myRole != null
                && !"CITIZEN".equalsIgnoreCase(myRole);

        abilityButton.setEnabled(canUse);
        if (myRole == null) {
            abilityButton.setText("능력 대기");
        } else if ("CITIZEN".equalsIgnoreCase(myRole)) {
            abilityButton.setText("능력 없음");
        } else {
            abilityButton.setText(canUse ? ("능력 사용 (" + myRole + ")") : ("능력 대기 (" + myRole + ")"));
        }
    }
}
