package mutfia.client;

import java.util.*;
import java.util.HashMap;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import mutfia.client.handler.ClientMessageHandler;
import mutfia.server.player.Player;

public class GameScreen {
    private JFrame frame;
    private JTextArea logArea;
    private JTextField chatInput;
    private JLabel stateLabel;
    private JLabel timerLabel;
    private JButton abilityButton;
    private JButton voteButton;

    private boolean started = false;
    private Map<Player, String> roles = new HashMap<>();
    private String state = "DAY"; // or "NIGHT"
    private String myRole;
    private List<Map<String, Object>> playersInfo = new ArrayList<>();
    private Consumer<String> pendingPlayerSelectionCallback; // 플레이어 선택 대기 중인 callback
    private boolean nightAbilityUsed = false; // 밤에 능력을 사용했는지
    private boolean voted = false; // 투표했는지

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

        // 능력 버튼
        abilityButton = new JButton("능력 대기");
        abilityButton.setEnabled(false);
        abilityButton.setFocusable(false);
        abilityButton.setBackground(new Color(70, 70, 110));
        abilityButton.setForeground(Color.WHITE);
        abilityButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        abilityButton.addActionListener(e -> promptAbilityTarget());
        
        // 투표 버튼
        voteButton = new JButton("투표");
        voteButton.setEnabled(false);
        voteButton.setFocusable(false);
        voteButton.setBackground(new Color(70, 70, 110));
        voteButton.setForeground(Color.WHITE);
        voteButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        voteButton.addActionListener(e -> promptVoteTarget());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setBackground(new Color(30, 30, 50));
        buttonPanel.add(abilityButton);
        buttonPanel.add(voteButton);
        actionPanel.add(buttonPanel, BorderLayout.WEST);

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
                    nightAbilityUsed = false; // 초기화
                    voted = false; // 초기화
                } else if ("VOTING".equalsIgnoreCase(phase)) {
                    stateLabel.setText("현재 상태: 투표");
                    appendLog("⚖️ 투표 시간이 시작되었습니다.");
                    voted = false; // 투표 초기화
                } else if ("NIGHT".equalsIgnoreCase(phase)) {
                    stateLabel.setText("현재 상태: 밤");
                    appendLog("🌙 밤이 시작되었습니다.");
                    nightAbilityUsed = false; // 초기화
                    voted = false; // 초기화
                } else {
                    stateLabel.setText("현재 상태: " + phase);
                    appendLog("⏱ 단계 전환: " + phase);
                }
                updateAbilityAvailability();
                updateVoteAvailability();
            });
        });

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

        ClientMessageHandler.register("PLAYER_SAVED", msg -> {
            SwingUtilities.invokeLater(() -> {
                String name = (String) msg.data.get("name");
                appendLog("💊 " + name + " 님이 치료되어 생존했습니다.");
            });
        });

        ClientMessageHandler.register("VOTE_RESULT", msg -> {
            SwingUtilities.invokeLater(() -> {
                String message = (String) msg.data.get("message");
                if (message != null) {
                    appendLog("⚖️ " + message);
                }
            });
        });

        ClientMessageHandler.register("USE_ABILITY", msg -> {
            SwingUtilities.invokeLater(() -> {
                String info = msg.data != null ? (String) msg.data.get("message") : null;
                if (info != null) {
                    appendLog("🛠 " + info);
                }
                // 능력 사용 성공 시 버튼 비활성화
                if (msg.status.name().equals("OK")) {
                    nightAbilityUsed = true;
                    updateAbilityAvailability();
                }
            });
        });

        ClientMessageHandler.register("VOTE", msg -> {
            SwingUtilities.invokeLater(() -> {
                String info = msg.data != null ? (String) msg.data.get("message") : null;
                if (info != null) {
                    appendLog("⚖️ " + info);
                }
                // 투표 성공 시 버튼 비활성화
                if (msg.status.name().equals("OK")) {
                    voted = true;
                    updateVoteAvailability();
                }
            });
        });

        ClientMessageHandler.register("PLAYERS_LIST", msg -> {
            SwingUtilities.invokeLater(() -> {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> players = (List<Map<String, Object>>) msg.data.get("players");
                playersInfo = players != null ? new ArrayList<>(players) : new ArrayList<>();
                
                // 대기 중인 callback이 있으면 선택 UI 표시
                if (pendingPlayerSelectionCallback != null) {
                    Consumer<String> callback = pendingPlayerSelectionCallback;
                    pendingPlayerSelectionCallback = null; // 사용 후 초기화
                    selectPlayerFromList("플레이어 선택", "대상 플레이어를 선택하세요", callback);
                }
            });
        });

        ClientMessageHandler.register("GAME_END", msg -> {
            SwingUtilities.invokeLater(() -> {
                String winner = (String) msg.data.get("winner");
                Boolean isWinner = (Boolean) msg.data.get("isWinner");
                String myRole = (String) msg.data.get("myRole");

                String winnerTeam = "MAFIA".equals(winner) ? "마피아" : "시민";
                String message;
                String title;

                if (isWinner != null && isWinner) {
                    title = "승리!";
                    message = String.format("축하합니다! %s 팀이 승리했습니다!\n당신의 역할: %s", winnerTeam, myRole);
                    appendLog("🎉 " + winnerTeam + " 팀 승리! 당신의 역할: " + myRole);
                } else {
                    title = "패배";
                    message = String.format("%s 팀이 승리했습니다.\n당신의 역할: %s", winnerTeam, myRole);
                    appendLog("💀 " + winnerTeam + " 팀 승리. 당신의 역할: " + myRole);
                }

                // 게임 종료 다이얼로그 표시
                JOptionPane.showMessageDialog(
                        frame,
                        message,
                        title,
                        isWinner != null && isWinner ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE
                );

                // 게임 종료 후 버튼 비활성화
                if (abilityButton != null) abilityButton.setEnabled(false);
                if (voteButton != null) voteButton.setEnabled(false);
                if (chatInput != null) chatInput.setEnabled(false);
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

        // 플레이어 목록 요청 (전체 플레이어 + 생존 상태)
        pendingPlayerSelectionCallback = (selectedPlayer) -> {
            if (selectedPlayer != null && !selectedPlayer.isEmpty()) {
                ServerConnection.send("USE_ABILITY", Map.of("target", selectedPlayer));
            }
        };
        ServerConnection.send("GET_PLAYERS", Map.of());
    }

    private void promptVoteTarget() {
        if (!"VOTING".equalsIgnoreCase(state)) {
            appendLog("⚠️ 투표는 투표 시간에만 할 수 있습니다.");
            return;
        }

        // 플레이어 목록 요청 (전체 플레이어 + 생존 상태)
        pendingPlayerSelectionCallback = (selectedPlayer) -> {
            if (selectedPlayer != null && !selectedPlayer.isEmpty()) {
                ServerConnection.send("VOTE", Map.of("target", selectedPlayer));
            }
        };
        ServerConnection.send("GET_PLAYERS", Map.of());
    }

    private void selectPlayerFromList(String title, String message, Consumer<String> callback) {
        if (playersInfo.isEmpty()) {
            appendLog("⚠️ 선택할 수 있는 플레이어가 없습니다.");
            return;
        }

        // 커스텀 다이얼로그 생성 (죽은 플레이어는 회색으로 표시)
        JDialog dialog = new JDialog(frame, title, true);
        dialog.setLayout(new BorderLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel label = new JLabel(message);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(label);
        
        ButtonGroup group = new ButtonGroup();
        JRadioButton[] buttons = new JRadioButton[playersInfo.size()];
        String[] playerNames = new String[playersInfo.size()];
        
        for (int i = 0; i < playersInfo.size(); i++) {
            Map<String, Object> playerInfo = playersInfo.get(i);
            String name = (String) playerInfo.get("name");
            Boolean alive = (Boolean) playerInfo.get("alive");
            Boolean isMe = (Boolean) playerInfo.get("isMe");
            
            playerNames[i] = name;
            buttons[i] = new JRadioButton(name);
            
            if (!alive) {
                // 죽은 플레이어는 회색으로 표시하고 비활성화
                buttons[i].setForeground(Color.GRAY);
                buttons[i].setEnabled(false);
            } else if (isMe != null && isMe) {
                // 자기 자신은 노란색으로 표시
                buttons[i].setForeground(new Color(255, 200, 0)); // 노란색
            } else {
                // 생존 플레이어는 기본 색상
                buttons[i].setForeground(Color.BLACK);
            }
            
            group.add(buttons[i]);
            panel.add(buttons[i]);
        }
        
        // 첫 번째 생존 플레이어 선택
        for (JRadioButton button : buttons) {
            if (button.isEnabled()) {
                button.setSelected(true);
                break;
            }
        }
        
        JButton okButton = new JButton("확인");
        JButton cancelButton = new JButton("취소");
        
        okButton.addActionListener(e -> {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].isSelected() && buttons[i].isEnabled()) {
                    dialog.dispose();
                    if (callback != null) {
                        callback.accept(playerNames[i]);
                    }
                    return;
                }
            }
        });
        
        cancelButton.addActionListener(e -> {
            dialog.dispose();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void updateAbilityAvailability() {
        if (abilityButton == null) return;

        boolean canUse = "NIGHT".equalsIgnoreCase(state)
                && myRole != null
                && !"CITIZEN".equalsIgnoreCase(myRole)
                && !nightAbilityUsed;

        abilityButton.setEnabled(canUse);
        if (myRole == null) {
            abilityButton.setText("능력 대기");
        } else if ("CITIZEN".equalsIgnoreCase(myRole)) {
            abilityButton.setText("능력 없음");
        } else {
            if (nightAbilityUsed) {
                abilityButton.setText("능력 사용 완료 (" + myRole + ")");
            } else {
                abilityButton.setText(canUse ? ("능력 사용 (" + myRole + ")") : ("능력 대기 (" + myRole + ")"));
            }
        }
    }

    private void updateVoteAvailability() {
        if (voteButton == null) return;

        boolean canVote = "VOTING".equalsIgnoreCase(state) && !voted;

        voteButton.setEnabled(canVote);
        if (voted) {
            voteButton.setText("투표 완료");
        } else {
            voteButton.setText("투표");
        }
    }
}
