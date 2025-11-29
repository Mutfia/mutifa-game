package mutfia.server.room;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import mutfia.server.player.Player;
import mutfia.server.room.exception.RoomNotFoundException;

public class RoomManager {
    private static List<GameRoom> rooms = new CopyOnWriteArrayList<>();
    private static long roomIdSequence = 1;

    private static synchronized long nextId() {
        return roomIdSequence++;
    }

    public static GameRoom create(String name, Player creator) {
        GameRoom newRoom = GameRoom.create(nextId(), name, creator);
        rooms.add(newRoom);
        return newRoom;
    }

    public static List<GameRoom> getRooms() {
        return rooms;
    }

    public static GameRoom findRoom(Long id) {
        return rooms.stream()
                .filter(room -> room.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RoomNotFoundException(id));
    }
}
