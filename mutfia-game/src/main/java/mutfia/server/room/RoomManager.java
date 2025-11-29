package mutfia.server.room;

import java.util.*;
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

    public static void removeRoom(GameRoom room) {
        rooms.remove(room);
        System.out.println("[Server] 빈 방 삭제됨 → roomId=" + room.getId());
    }

    public static List<Map<String, Object>> toRoomMapList() {
        List<Map<String, Object>> list = new ArrayList<>();

        for (GameRoom room : rooms) {
            Map<String, Object> map = new HashMap<>();
            map.put("roomId", room.getId());
            map.put("roomName", room.getRoomName());
            map.put("players", room.getPlayers().size());
            map.put("maxPlayers", room.getMaxPlayersCount());
            map.put("isPlaying", room.isPlaying());

            list.add(map);
        }

        return list;
    }
}