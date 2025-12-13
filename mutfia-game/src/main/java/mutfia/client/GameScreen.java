package mutfia.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.border.EmptyBorder;
import mutfia.client.handler.ClientMessageHandler;

public class GameScreen {
    private final String playerName;
    private boolean isAlive = true;
    private JFrame frame;
    private JTextArea logArea;
    private JTextField chatInput;
    private JLabel stateLabel;
    private JLabel stateIconLabel;
    private JLabel timerLabel;
    private JLabel roleNameLabel;
    private JLabel roleIconLabel;
    private JLabel playerLabel;
    private JButton abilityButton;
    private JButton voteButton;
    private JButton shortenTimerButton;

    private String state = "DAY"; // or "NIGHT"
    private String myRole;
    private String myName;
    private List<Map<String, Object>> playersInfo = new ArrayList<>();
    private Consumer<String> pendingPlayerSelectionCallback; // 플레이어 선택 대기 중인 callback
    private boolean nightAbilityUsed = false; // 밤에 능력을 사용했는지
    private boolean voted = false; // 투표했는지

    public GameScreen(Map<String, Object> roomInfo, String playerName) {
        this.playerName = playerName;
        registerHandlers();

        String roomName = (String) roomInfo.get("roomName");
        long roomId = ((Number) roomInfo.get("roomId")).longValue();

        frame = new JFrame("멋피아 게임방 - " + roomName);
        frame.setSize(1040, 760);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel background = UIStyle.gradientPanel(new BorderLayout(12, 12));
        background.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel statusPanel = UIStyle.glassCard(new BorderLayout(10, 0));
        statusPanel.setBackground(UIStyle.CARD_BG_STRONG);
        statusPanel.setPreferredSize(new Dimension(0, 90));

        stateIconLabel = new JLabel(UIStyle.loadIcon("/images/day.png", 54));
        stateIconLabel.setBorder(new EmptyBorder(4, 12, 4, 12));

        JPanel stateTextPanel = new JPanel();
        stateTextPanel.setOpaque(false);
        stateTextPanel.setLayout(new BoxLayout(stateTextPanel, BoxLayout.Y_AXIS));
        stateLabel = new JLabel("대기 중...");
        stateLabel.setForeground(Color.WHITE);
        stateLabel.setFont(UIStyle.displayFont(26));
        JLabel roomLabel = new JLabel("방: " + roomName + " (#" + roomId + ")");
        roomLabel.setForeground(new Color(210, 220, 240));
        roomLabel.setFont(UIStyle.bodyFont(14));
        stateTextPanel.add(stateLabel);
        stateTextPanel.add(roomLabel);

        timerLabel = new JLabel("", JLabel.RIGHT);
        timerLabel.setForeground(UIStyle.ACCENT_GOLD);
        timerLabel.setFont(UIStyle.displayFont(30));
        timerLabel.setBorder(new EmptyBorder(0, 0, 0, 6));

        statusPanel.add(stateIconLabel, BorderLayout.WEST);
        statusPanel.add(stateTextPanel, BorderLayout.CENTER);
        statusPanel.add(timerLabel, BorderLayout.EAST);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBackground(new Color(12, 16, 32, 160));
        logArea.setForeground(Color.WHITE);
        logArea.setFont(UIStyle.bodyFont(14));
        logArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setOpaque(false);
        logScroll.getViewport().setOpaque(false);
        logScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel logCard = UIStyle.glassCard(new BorderLayout());
        logCard.add(logScroll, BorderLayout.CENTER);

        JPanel sidePanel = UIStyle.glassCard(new BorderLayout(0, 14));
        sidePanel.setPreferredSize(new Dimension(260, 0));
        sidePanel.setBackground(UIStyle.CARD_BG_STRONG);

        JPanel rolePanel = new JPanel(new BorderLayout(10, 4));
        rolePanel.setOpaque(false);
        roleIconLabel = new JLabel(UIStyle.loadIcon("/images/citizen_icon.png", 48));
        roleNameLabel = new JLabel("역할을 기다리는 중");
        roleNameLabel.setForeground(Color.WHITE);
        roleNameLabel.setFont(UIStyle.displayFont(20));
        playerLabel = new JLabel("플레이어: " + playerName);
        playerLabel.setForeground(new Color(210, 220, 240));
        playerLabel.setFont(UIStyle.bodyFont(13));
        JLabel roleHint = new JLabel("밤엔 능력, 낮엔 토론과 투표!");
        roleHint.setForeground(new Color(210, 220, 240));
        roleHint.setFont(UIStyle.bodyFont(13));
        JPanel roleText = new JPanel();
        roleText.setOpaque(false);
        roleText.setLayout(new BoxLayout(roleText, BoxLayout.Y_AXIS));
        roleText.add(roleNameLabel);
        roleText.add(playerLabel);
        roleText.add(roleHint);

        rolePanel.add(roleIconLabel, BorderLayout.WEST);
        rolePanel.add(roleText, BorderLayout.CENTER);

        abilityButton = UIStyle.ghostButton("능력 대기", UIStyle.loadIcon("/images/night.png", 20));
        abilityButton.setEnabled(false);
        abilityButton.setPreferredSize(new Dimension(220, 48));
        abilityButton.setMaximumSize(new Dimension(300, 48));
        abilityButton.addActionListener(e -> promptAbilityTarget());

        voteButton = UIStyle.primaryButton("투표", UIStyle.loadIcon("/images/vote.png", 20));
        voteButton.setEnabled(false);
        voteButton.setPreferredSize(new Dimension(220, 48));
        voteButton.setMaximumSize(new Dimension(300, 48));
        voteButton.addActionListener(e -> promptVoteTarget());

        shortenTimerButton = UIStyle.ghostButton("시간 단축", UIStyle.loadIcon("/images/day.png", 18));
        shortenTimerButton.setEnabled(true);
        shortenTimerButton.setPreferredSize(new Dimension(220, 40));
        shortenTimerButton.setMaximumSize(new Dimension(300, 40));
        shortenTimerButton.addActionListener(e -> {
            appendLog("⏱ 시간을 단축 요청했습니다.");
            ServerConnection.send("SHORTEN_TIMER", Map.of());
        });

        JPanel actionButtons = new JPanel();
        actionButtons.setOpaque(false);
        actionButtons.setLayout(new BoxLayout(actionButtons, BoxLayout.Y_AXIS));
        actionButtons.add(abilityButton);
        actionButtons.add(Box.createVerticalStrut(10));
        actionButtons.add(voteButton);
        actionButtons.add(Box.createVerticalStrut(8));
        actionButtons.add(shortenTimerButton);
        actionButtons.add(Box.createVerticalStrut(12));
        JLabel tipLabel = new JLabel("엔터로 채팅을 보내고, 아이콘으로 상태를 확인하세요.");
        tipLabel.setForeground(new Color(200, 210, 235));
        tipLabel.setFont(UIStyle.bodyFont(12));
        tipLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtons.add(tipLabel);

        sidePanel.add(rolePanel, BorderLayout.NORTH);
        sidePanel.add(actionButtons, BorderLayout.CENTER);

        JPanel mainArea = new JPanel(new BorderLayout(12, 0));
        mainArea.setOpaque(false);
        mainArea.add(logCard, BorderLayout.CENTER);
        mainArea.add(sidePanel, BorderLayout.EAST);

        JPanel actionPanel = UIStyle.glassCard(new BorderLayout(10, 0));
        actionPanel.setBackground(UIStyle.CARD_BG_STRONG);
        chatInput = new JTextField();
        chatInput.setFont(UIStyle.bodyFont(14));
        chatInput.setBackground(new Color(20, 26, 50, 200));
        chatInput.setForeground(Color.WHITE);
        chatInput.setCaretColor(Color.WHITE);
        chatInput.setBorder(new EmptyBorder(10, 12, 10, 12));
        chatInput.addActionListener(e -> {
            if (!canSendChat()) {
                appendLog("💬 지금은 채팅을 보낼 수 없습니다.");
                chatInput.setText("");
                return;
            }
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                ServerConnection.send("CHAT", Map.of("message", text));
                chatInput.setText("");
            }
        });
        JLabel chatHint = new JLabel("엔터로 보내기");
        chatHint.setForeground(new Color(190, 205, 230));
        chatHint.setFont(UIStyle.bodyFont(12));
        actionPanel.add(chatHint, BorderLayout.WEST);
        actionPanel.add(chatInput, BorderLayout.CENTER);

        background.add(statusPanel, BorderLayout.NORTH);
        background.add(mainArea, BorderLayout.CENTER);
        background.add(actionPanel, BorderLayout.SOUTH);

        frame.setContentPane(background);

        updatePhaseArtwork();
        updateRoleBadge();
        updateAbilityAvailability();
        updateVoteAvailability();
        updateChatAvailability();
        updateShortenAvailability();

        appendLog("🦁 방에 입장했습니다. (Room ID: " + roomId + ")");

        frame.setVisible(true);
    }

    public void appendLog(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
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
                myRole = role;
                updateRoleBadge();
                updateAbilityAvailability();
            });
        });

        ClientMessageHandler.register("GAME_START", msg -> {
            SwingUtilities.invokeLater(() -> {
                appendLog("🎮 게임이 시작되었습니다! 낮에는 토론, 밤에는 능력을 사용하세요.");
            });
        });

        ClientMessageHandler.register("PHASE_CHANGE", msg -> {
            SwingUtilities.invokeLater(() -> {
                String phase = (String) msg.data.get("phase");
                state = phase;

                if ("DAY".equalsIgnoreCase(phase)) {
                    stateLabel.setText("밝은 낮 - 모두의 발언 시간");
                    appendLog("🌞 낮이 시작되었습니다.");
                    nightAbilityUsed = false;
                    voted = false;
                } else if ("VOTING".equalsIgnoreCase(phase)) {
                    stateLabel.setText("투표 - 의심되는 사람을 선택하세요");
                    appendLog("⚖️ 투표 시간이 시작되었습니다.");
                    voted = false;
                } else if ("NIGHT".equalsIgnoreCase(phase)) {
                    stateLabel.setText("어두운 밤 - 은밀한 능력 시간");
                    appendLog("🌙 밤이 시작되었습니다.");
                    nightAbilityUsed = false;
                    voted = false;
                } else {
                    stateLabel.setText("현재 상태: " + phase);
                    appendLog("⏱ 단계 전환: " + phase);
                }
                updatePhaseArtwork();
                updateAbilityAvailability();
                updateVoteAvailability();
                updateChatAvailability();
                updateShortenAvailability();
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
                if (name != null && name.equals(playerName)) {
                    isAlive = false;
                    appendLog("🩸 당신은 사망했습니다. 채팅과 행동이 제한됩니다.");
                    updateAbilityAvailability();
                    updateVoteAvailability();
                    updateChatAvailability();
                    updateShortenAvailability();
                }
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

                updateMyStatusFromPlayers();
                updateAbilityAvailability();
                updateVoteAvailability();
                updateChatAvailability();
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
                if (shortenTimerButton != null) shortenTimerButton.setEnabled(false);

                frame.dispose();
                GameScreenManager.close();
                new GameRoomListScreen(playerName);
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

    private void updateRoleBadge() {
        if (roleNameLabel == null || roleIconLabel == null) {
            return;
        }

        if (myRole == null) {
            roleNameLabel.setText("역할을 기다리는 중");
            roleIconLabel.setIcon(UIStyle.loadIcon("/images/citizen_icon.png", 48));
            return;
        }

        roleNameLabel.setText("내 역할: " + localizedRoleName());
        ImageIcon icon = resolveRoleIcon(myRole);
        if (icon != null) {
            roleIconLabel.setIcon(icon);
        }
    }

    private ImageIcon resolveRoleIcon(String role) {
        if (role == null) {
            return UIStyle.loadIcon("/images/citizen_icon.png", 48);
        }
        return switch (role.toUpperCase()) {
            case "MAFIA" -> UIStyle.loadIcon("/images/mafia_icon.png", 48);
            case "POLICE" -> UIStyle.loadIcon("/images/police_icon.png", 48);
            case "DOCTOR" -> UIStyle.loadIcon("/images/doctor_icon.png", 48);
            default -> UIStyle.loadIcon("/images/citizen_icon.png", 48);
        };
    }

    private String localizedRoleName() {
        if (myRole == null) {
            return "";
        }
        return switch (myRole.toUpperCase()) {
            case "MAFIA" -> "마피아";
            case "POLICE" -> "경찰";
            case "DOCTOR" -> "의사";
            case "CITIZEN" -> "시민";
            default -> myRole;
        };
    }

    private void updatePhaseArtwork() {
        if (stateIconLabel == null) {
            return;
        }
        ImageIcon icon;
        Color accent;

        if ("NIGHT".equalsIgnoreCase(state)) {
            icon = UIStyle.loadIcon("/images/night.png", 54);
            accent = UIStyle.ACCENT_PINK;
        } else if ("VOTING".equalsIgnoreCase(state)) {
            icon = UIStyle.loadIcon("/images/vote.png", 50);
            accent = UIStyle.ACCENT_GOLD;
        } else {
            icon = UIStyle.loadIcon("/images/day.png", 54);
            accent = UIStyle.ACCENT_BLUE;
        }
        stateIconLabel.setIcon(icon);
        timerLabel.setForeground(accent);
    }

    private boolean canSendChat() {
        return isAlive && !"NIGHT".equalsIgnoreCase(state);
    }

    private void updateChatAvailability() {
        if (chatInput == null) {
            return;
        }
        boolean enabled = canSendChat();
        chatInput.setEnabled(enabled);
        if (!enabled) {
            chatInput.setText("");
            String reason = !isAlive ? "사망하여 채팅이 비활성화되었습니다." : "밤에는 채팅을 보낼 수 없습니다.";
            chatInput.setToolTipText(reason);
        } else {
            chatInput.setToolTipText("메시지를 입력하고 Enter");
        }
    }

    private void updateMyStatusFromPlayers() {
        if (playersInfo == null || playersInfo.isEmpty()) {
            return;
        }
        for (Map<String, Object> info : playersInfo) {
            String name = (String) info.get("name");
            Boolean alive = (Boolean) info.get("alive");
            Boolean isMe = (Boolean) info.get("isMe");
            if ((name != null && name.equals(playerName)) || (isMe != null && isMe)) {
                myName = name;
                if (playerLabel != null && name != null) {
                    playerLabel.setText("플레이어: " + name);
                }
                if (alive != null) {
                    isAlive = alive;
                }
                break;
            }
        }
    }

    private void updateAbilityAvailability() {
        if (abilityButton == null) return;

        boolean canUse = "NIGHT".equalsIgnoreCase(state)
                && myRole != null
                && !"CITIZEN".equalsIgnoreCase(myRole)
                && !nightAbilityUsed
                && isAlive;

        abilityButton.setEnabled(canUse);
        String localizedRole = localizedRoleName();
        if (myRole == null) {
            abilityButton.setText("능력 대기");
        } else if ("CITIZEN".equalsIgnoreCase(myRole)) {
            abilityButton.setText("능력 없음");
        } else if (!isAlive) {
            abilityButton.setText("사망 - 사용 불가");
        } else {
            if (nightAbilityUsed) {
                abilityButton.setText("능력 사용 완료 (" + localizedRole + ")");
            } else {
                abilityButton.setText(canUse ? ("능력 사용 (" + localizedRole + ")") : ("능력 대기 (" + localizedRole + ")"));
            }
        }
        abilityButton.setBackground(canUse ? UIStyle.ACCENT_PINK : new Color(68, 82, 125, 210));
        abilityButton.setForeground(Color.WHITE);
    }

    private void updateVoteAvailability() {
        if (voteButton == null) return;

        boolean canVote = "VOTING".equalsIgnoreCase(state) && !voted && isAlive;

        voteButton.setEnabled(canVote);
        if (voted) {
            voteButton.setText("투표 완료");
        } else if (!isAlive) {
            voteButton.setText("사망 - 투표 불가");
        } else {
            voteButton.setText("투표");
        }
        voteButton.setBackground(canVote ? UIStyle.ACCENT_GOLD : UIStyle.CARD_BG_STRONG);
        voteButton.setForeground(canVote ? Color.BLACK : Color.WHITE);
    }

    private void updateShortenAvailability() {
        if (shortenTimerButton == null) {
            return;
        }
        shortenTimerButton.setEnabled(isAlive);
        shortenTimerButton.setText(isAlive ? "시간 단축" : "사망 - 단축 불가");
    }
}
