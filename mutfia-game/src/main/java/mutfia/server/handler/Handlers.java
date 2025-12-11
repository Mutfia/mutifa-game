package mutfia.server.handler;

import java.util.Map;

import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;
import mutfia.server.room.RoomManager;

public class Handlers {

    public static void handleSetName(Player player, CustomProtocolMessage msg) {
        String newName = (String) msg.data.get("name");
        player.setName(newName);

        player.send(CustomProtocolMessage.success(
                "SET_NAME",
                Map.of("name", newName)
        ));
    }

    public static void handleCreateRoom(Player player, CustomProtocolMessage msg) {
        String roomName = (String) msg.data.get("roomName");

        GameRoom room = RoomManager.create(roomName, player);
        player.setCurrentGameRoom(room);

        System.out.println("[Server] 방 생성됨 : " + room.getRoomName());

        player.send(CustomProtocolMessage.success(
                "CREATE_ROOM",
                Map.of(
                        "roomId", room.getId(),
                        "roomName", room.getRoomName(),
                        "players", room.getPlayers().size()
                )
        ));
    }

    public static void handleGetRooms(Player player, CustomProtocolMessage msg) {
        player.send(CustomProtocolMessage.success(
                "ROOM_LIST",
                Map.of("rooms", RoomManager.toRoomMapList())
        ));
    }
}