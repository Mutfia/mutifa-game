package mutfia.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import mutfia.client.handler.ClientMessageHandler;

public class GameRoomListScreen {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private JFrame mainFrame;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private List<Map<String, Object>> rooms;

    private String playerName;

    public GameRoomListScreen(String playerName) {
        this.playerName = playerName;

        registerHandlers();
        initialize();
        requestRoomList();
    }

    private void registerHandlers() {
        // 방 목록 업데이트
        ClientMessageHandler.register("ROOM_LIST", msg -> {
            SwingUtilities.invokeLater(() -> {
                rooms = (List<Map<String, Object>>) msg.data.get("rooms");
                updateRoomList();
            });
        });

        // 방 생성
        ClientMessageHandler.register("CREATE_ROOM", msg -> {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainFrame,
                        "방이 생성되었습니다: " + msg.data.get("roomName"));
                requestRoomList();
            });
        });

        // 방 입장
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
                GameScreenManager.open(msg.data);
            });
        });

    }

    private void initialize() {
        mainFrame = new JFrame("방 목록 - " + playerName);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("게임 방 목록", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("방 목록"));

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton createButton = new JButton("방 만들기");
        createButton.setPreferredSize(new Dimension(120, 40));
        createButton.addActionListener(e -> createRoom());

        JButton joinButton = new JButton("방 입장");
        joinButton.setPreferredSize(new Dimension(120, 40));
        joinButton.addActionListener(e -> joinRoom());

        JButton refreshButton = new JButton("새로고침");
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.addActionListener(e -> requestRoomList());

        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);
        buttonPanel.add(refreshButton);

        mainFrame.add(titlePanel, BorderLayout.NORTH);
        mainFrame.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame.setVisible(true);
    }

    private void requestRoomList() {
        ServerConnection.send("GET_ROOMS", Map.of());
    }

    private void updateRoomList() {
        roomListModel.clear();

        if (rooms == null || rooms.isEmpty()) {
            roomListModel.addElement("생성된 방이 없습니다.");
            return;
        }

        for (Map<String, Object> room : rooms) {
            String name = (String) room.get("roomName");
            int players = (int) room.get("players");
            int maxPlayers = (int) room.get("maxPlayers");
            boolean isPlaying = (boolean) room.get("isPlaying");

            String display = String.format("%s [%d/%d] %s",
                    name, players, maxPlayers, isPlaying ? "[게임 중]" : "[대기 중]");

            roomListModel.addElement(display);
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
}
