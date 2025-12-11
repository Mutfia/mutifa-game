package mutfia.client;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GameScreen {

    private JFrame frame;

    public GameScreen(Map<String, Object> roomInfo) {

        frame = new JFrame("게임 방 - " + roomInfo.get("roomName"));
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel label = new JLabel("게임 방 입장 완료! 방 ID: " + roomInfo.get("roomId"));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        frame.add(label, BorderLayout.CENTER);
    }

    public JFrame getFrame() {
        return frame;
    }
}