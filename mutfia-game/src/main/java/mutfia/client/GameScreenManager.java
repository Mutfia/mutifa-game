package mutfia.client;

import javax.swing.*;
import java.util.Map;

public class GameScreenManager {

    private static JFrame currentFrame;

    public static void open(Map<String, Object> roomInfo) {
        if (currentFrame != null) {
            currentFrame.dispose();
        }

        GameScreen newScreen = new GameScreen(roomInfo);

        currentFrame = newScreen.getFrame();
        currentFrame.setVisible(true);
    }

    public static void close() {
        if (currentFrame != null) {
            currentFrame.dispose();
            currentFrame = null;
        }
    }
}