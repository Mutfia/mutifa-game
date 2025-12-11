package mutfia.server.handler;

import java.util.Map;

import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;
import mutfia.server.room.RoomManager;

public class Handlers {

    // 플레이어 이름 설정
    public static void handleSetName(Player player, CustomProtocolMessage msg) {
        String newName = (String) msg.data.get("name");
        player.setName(newName);

        player.send(CustomProtocolMessage.success(
                "SET_NAME",
                Map.of("name", newName)
        ));
    }

    // 방 생성
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

        player.send(CustomProtocolMessage.success(
                "JOIN_ROOM",
                Map.of(
                        "roomId", room.getId(),
                        "roomName", room.getRoomName(),
                        "players", room.getPlayers().size(),
                        "playerList", room.getPlayerNames()
                )
        ));
    }

    // 방 입장
    public static void handleJoinRoom(Player player, CustomProtocolMessage msg) {
        long roomId = ((Number) msg.data.get("roomId")).longValue();
        GameRoom room = RoomManager.findRoom(roomId);

        boolean success = room.entrancePlayer(player);

        if (!success) {
            player.send(CustomProtocolMessage.error(
                    "JOIN_ROOM",
                    Map.of("message", "방에 입장할 수 없습니다.")
            ));
            return;
        }

        player.setCurrentGameRoom(room);

        player.send(CustomProtocolMessage.success(
                "JOIN_ROOM",
                Map.of(
                        "roomId", room.getId(),
                        "roomName", room.getRoomName(),
                        "players", room.getPlayers().size(),
                        "playerList", room.getPlayerNames()
                )
        ));

        // 방 내 다른 플레이어에게 방송
        ServerBroadcaster.broadcastToRoomExcept(
                room,
                player,
                CustomProtocolMessage.success(
                        "PLAYER_JOIN",
                        Map.of("name", player.getName())
                )
        );

        System.out.println("[Server] 플레이어 입장 : " + player.getName());
    }


    // 방 목록 불러오기
    public static void handleGetRooms(Player player, CustomProtocolMessage msg) {
        player.send(CustomProtocolMessage.success(
                "ROOM_LIST",
                Map.of("rooms", RoomManager.toRoomMapList())
        ));
    }

    // 채팅
    public static void handleChat(Player player, CustomProtocolMessage msg) {
        GameRoom room = player.getCurrentGameRoom();
        if (room == null) return;

        String message = (String) msg.data.get("message");

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success(
                        "CHAT",
                        Map.of(
                                "sender", player.getName(),
                                "message", message
                        )
                )
        );

        System.out.println("[CHAT] " + player.getName() + ": " + message);
    }
}