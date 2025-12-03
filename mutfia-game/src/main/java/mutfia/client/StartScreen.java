package mutfia.client;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class StartScreen {
    // 상수 선언
    // 절대 경로 (클래스패스 루트 기준: src/main/resources)
    private static final String BACKGROUND_IMAGE_PATH = "/images/night.png";
    private static final int WIDTH = 400; // 창 너비
    private static final int HEIGHT = 800; // 창 높이
    private static final int TITLE_LABEL_BORDER_TOP = 100; // 타이틀 레이블 위쪽 여백
    private static final int BUTTON_PANEL_BORDER_TOP = TITLE_LABEL_BORDER_TOP + 240; // 버튼 패널 위쪽 여백

    private JFrame mainFrame;
    private JLabel titleLabel;
    private JButton enterButton;
    private ImageIcon backgroundImage;


    public void initialize() {
        // 배경 이미지 로드
        try {
            java.net.URL imageUrl = getClass().getResource(BACKGROUND_IMAGE_PATH);
            if (imageUrl != null) {
                ImageIcon bgIcon = new ImageIcon(imageUrl);
                Image scaledImage = bgIcon.getImage().getScaledInstance(WIDTH, HEIGHT, Image.SCALE_SMOOTH);
                backgroundImage = new ImageIcon(scaledImage);
            } else {
                System.out.println("배경 이미지 URL을 찾을 수 없습니다: " + BACKGROUND_IMAGE_PATH);
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 실패: " + e.getMessage());
        }

        // 메인 프레임 생성
        mainFrame = new JFrame("멋피아");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(WIDTH, HEIGHT);
        mainFrame.setLocationRelativeTo(null); // 화면 중앙 배치 
        mainFrame.setResizable(false);

        // 배경 이미지가 있는 패널
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } else {
                    // 이미지가 없을 경우 어두운 배경
                    g.setColor(new Color(20, 30, 50));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // 타이틀 레이블
        titleLabel = new JLabel("멋피아", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 68));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(TITLE_LABEL_BORDER_TOP, 0, 0, 0));

        // 게임 입장하기 버튼
        enterButton = new JButton("게임 입장하기");
        enterButton.setFont(new Font("맑은 고딕", Font.PLAIN, 24));
        enterButton.setPreferredSize(new Dimension(300, 60));
        enterButton.setBackground(Color.WHITE);
        enterButton.setForeground(new Color(50, 30, 20));
        enterButton.setFocusPainted(false);
        enterButton.setBorderPainted(false); // 기본 3D 테두리 제거
        enterButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        enterButton.addActionListener(e -> {
            String playerName = JOptionPane.showInputDialog(mainFrame, 
                "플레이어 이름을 입력하세요:", 
                "이름 입력", 
                JOptionPane.PLAIN_MESSAGE);
            
            if (playerName != null && !playerName.trim().isEmpty()) {
                handleSetName(playerName.trim());
            }
        });

        // 레이아웃 설정
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // 투명한 패널
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(BUTTON_PANEL_BORDER_TOP, 0, 0, 0));
        buttonPanel.add(enterButton);

        backgroundPanel.add(titleLabel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);

        mainFrame.setContentPane(backgroundPanel);
        mainFrame.setVisible(true);
    }

    private void handleSetName(String playerName) {
        // 서버에 이름 설정 요청
        if (ServerConnection.setName(playerName)) {
            // 방 목록 화면으로 이동
            mainFrame.dispose();
            new GameRoomScreen(playerName);
        } else {
            JOptionPane.showMessageDialog(mainFrame, 
                "이름 설정에 실패했습니다.",
                "오류",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

