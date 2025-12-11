package mutfia.client;

import java.util.Map;

public class GameScreenManager {

    private static GameScreen current;

    public static void open(Map<String, Object> roomInfo) {
        current = new GameScreen(roomInfo);
        current.registerHandlers();
    }

    public static GameScreen get() {
        return current;
    }

    public static void close() {
        if (current != null && current.getFrame() != null) {
            current.getFrame().dispose();
        }
        current = null;
    }
}