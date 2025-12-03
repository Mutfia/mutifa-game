package mutfia.client;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                StartScreen startScreen = new StartScreen();
                startScreen.initialize();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "클라이언트 초기화 중 오류가 발생했습니다: " + e.getMessage(),
                    "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
