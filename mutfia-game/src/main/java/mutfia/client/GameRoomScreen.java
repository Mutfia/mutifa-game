package mutfia.client;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class GameRoomScreen {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    
    private JFrame mainFrame;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private List<Map<String, Object>> rooms;

    private String playerName;
    
    public GameRoomScreen(String playerName) {
        this.playerName = playerName;
        
        initialize();
        refreshRoomList();
    }
    
    private void initialize() {
        mainFrame = new JFrame("방 목록 - " + playerName);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setResizable(false);
        mainFrame.setLayout(new BorderLayout());
        
        // 상단 패널 (제목)
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        JLabel titleLabel = new JLabel("게임 방 목록", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // 중앙 패널 (방 목록)
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("방 목록"));
        
        // 하단 패널 (버튼들)
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JButton createButton = new JButton("방 만들기");
        createButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        createButton.setPreferredSize(new Dimension(120, 40));
        createButton.addActionListener(e -> createRoom());
        
        JButton joinButton = new JButton("방 입장");
        joinButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        joinButton.setPreferredSize(new Dimension(120, 40));
        joinButton.addActionListener(e -> joinRoom());
        
        JButton refreshButton = new JButton("새로고침");
        refreshButton.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        refreshButton.setPreferredSize(new Dimension(120, 40));
        refreshButton.addActionListener(e -> refreshRoomList());
        
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);
        buttonPanel.add(refreshButton);
        
        mainFrame.add(titlePanel, BorderLayout.NORTH);
        mainFrame.add(scrollPane, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
        
        mainFrame.setVisible(true);
    }
    
    private void refreshRoomList() {
        // ServerConnection을 사용하여 방 목록 조회
        rooms = ServerConnection.getRooms();
        
        if (rooms == null) {
            JOptionPane.showMessageDialog(mainFrame,
                "방 목록을 불러오는데 실패했습니다.",
                "오류",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        updateRoomList();
    }
    
    private void updateRoomList() {
        roomListModel.clear();
        
        if (rooms == null || rooms.isEmpty()) {
            roomListModel.addElement("생성된 방이 없습니다.");
            return;
        }
        
        for (Map<String, Object> room : rooms) {
            String roomName = (String) room.get("roomName");
            Integer players = (Integer) room.get("players");
            Integer maxPlayers = (Integer) room.get("maxPlayers");
            Boolean isPlaying = (Boolean) room.get("isPlaying");
            
            String displayText = String.format("%s [%d/%d] %s",
                roomName,
                players,
                maxPlayers,
                isPlaying ? "[게임 중]" : "[대기 중]"
            );
            
            roomListModel.addElement(displayText);
        }
    }
    
    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(mainFrame,
            "방 이름을 입력하세요:",
            "방 만들기",
            JOptionPane.PLAIN_MESSAGE);
        
        if (roomName != null && !roomName.trim().isEmpty()) {
            // ServerConnection을 사용하여 방 생성
            Map<String, Object> roomInfo = ServerConnection.createRoom(roomName.trim());
            
            if (roomInfo != null) {
                JOptionPane.showMessageDialog(mainFrame,
                    "방이 생성되었습니다!\n방 이름: " + roomName,
                    "방 생성 성공",
                    JOptionPane.INFORMATION_MESSAGE);
                
                refreshRoomList();
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                    "방 생성에 실패했습니다.",
                    "방 생성 실패",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void joinRoom() {
        int selectedIndex = roomList.getSelectedIndex();
        
        if (selectedIndex < 0 || rooms == null || selectedIndex >= rooms.size()) {
            JOptionPane.showMessageDialog(mainFrame,
                "입장할 방을 선택해주세요.",
                "알림",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Map<String, Object> selectedRoom = rooms.get(selectedIndex);
        Boolean isPlaying = (Boolean) selectedRoom.get("isPlaying");
        
        if (isPlaying != null && isPlaying) {
            JOptionPane.showMessageDialog(mainFrame,
                "이미 게임이 진행 중인 방입니다.",
                "입장 불가",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // TODO: 방 입장 로직 구현
        JOptionPane.showMessageDialog(mainFrame,
            "방 입장 기능은 아직 구현되지 않았습니다.",
            "알림",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
