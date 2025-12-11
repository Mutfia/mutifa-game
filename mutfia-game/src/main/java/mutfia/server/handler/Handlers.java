package mutfia.server.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;
import mutfia.server.room.RoomManager;
import mutfia.server.room.enums.Phase;

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

        if (room.getPlayers().size() == 5 && !room.isPlaying()) {
            room.setPlaying(true);

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    startGame(room);
                } catch (Exception ignored) {}
            }).start();
        }

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

    // 게임 시작
    public static void startGame(GameRoom room) {

        List<Player> players = new ArrayList<>(room.getPlayers());
        Collections.shuffle(players);

        Map<Player, String> roles = new HashMap<>();

        roles.put(players.get(0), "MAFIA");
        roles.put(players.get(1), "DOCTOR");
        roles.put(players.get(2), "POLICE");
        roles.put(players.get(3), "CITIZEN");
        roles.put(players.get(4), "CITIZEN");

        room.setRoles(roles);

        for (Player p : players) {
            p.send(CustomProtocolMessage.success(
                    "ROLE_ASSIGN",
                    Map.of("role", roles.get(p))
            ));
        }

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success("GAME_START", Map.of())
        );

        // 첫 번째 낮 시작
        startDay(room);
    }

    private static void startDay(GameRoom room) {
        room.setPhase(Phase.DAY);

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success("PHASE_CHANGE",
                        Map.of("phase", "DAY")
                )
        );

        new Thread(() -> {
            try {
                Thread.sleep(60000); // 1분 후 밤 시작
                startNight(room);
            } catch (Exception ignored) {}
        }).start();
    }

    private static void startNight(GameRoom room) {
        room.setPhase(Phase.NIGHT);

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success("PHASE_CHANGE",
                        Map.of("phase", "NIGHT")
                )
        );

        new Thread(() -> {
            try {
                Thread.sleep(60000); // 1분 후 낮 시작
                startDay(room);
            } catch (Exception ignored) {}
        }).start();
    }
}
