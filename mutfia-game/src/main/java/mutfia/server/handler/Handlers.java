package mutfia.server.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.player.Player;
import mutfia.server.player.enums.Role;
import mutfia.server.role.RoleAction;
import mutfia.server.role.RoleActionFactory;
import mutfia.server.role.RoleActionResult;
import mutfia.server.room.GameRoom;
import mutfia.server.room.RoomManager;
import mutfia.server.room.enums.Phase;

public class Handlers {

    private static int TIMER = 60000; // 1분

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

        Map<Player, Role> roles = new HashMap<>();

        roles.put(players.get(0), Role.MAFIA);
        roles.put(players.get(1), Role.DOCTOR);
        roles.put(players.get(2), Role.POLICE);
        roles.put(players.get(3), Role.CITIZEN);
        roles.put(players.get(4), Role.CITIZEN);

        room.setRoles(roles);
        room.initializeAliveStates();

        for (Player p : players) {
            p.send(CustomProtocolMessage.success(
                    "ROLE_ASSIGN",
                    Map.of("role", roles.get(p).name())
            ));
        }

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success("GAME_START", Map.of())
        );

        // 첫 번째 밤 시작
        startNight(room);
    }

    private static void startDay(GameRoom room) {
        // 밤 종료 시 의사/마피아 선택 결과 처리
        processNightResults(room);
        
        room.setPhase(Phase.DAY);
        room.resetNightActions(); // 다음 밤을 위해 초기화

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success("PHASE_CHANGE",
                        Map.of("phase", "DAY")
                )
        );

        // 타이머 스레드 시작
        new Thread(() -> {
            int remainingSeconds = TIMER / 1000;
            try {
                while (remainingSeconds > 0) {
                    Thread.sleep(1000); // 1초 대기
                    remainingSeconds--;
                    
                    ServerBroadcaster.broadcastToRoom(
                            room,
                            CustomProtocolMessage.success("TIMER_UPDATE",
                                    Map.of("remainingSeconds", remainingSeconds, "phase", "DAY")
                            )
                    );
                }
                startNight(room);
            } catch (Exception ignored) {}
        }).start();
    }

    // 밤 종료 시 의사/마피아 선택 결과 처리
    private static void processNightResults(GameRoom room) {
        Player mafiaTarget = room.getMafiaTarget();
        Player doctorTarget = room.getDoctorTarget();

        // 마피아가 대상을 선택했고, 의사가 그 대상을 치료하지 않았다면 사망
        if (mafiaTarget != null && room.isAlive(mafiaTarget)) {
            if (doctorTarget == null || !doctorTarget.equals(mafiaTarget)) {
                room.markDead(mafiaTarget);
                ServerBroadcaster.broadcastToRoom(
                        room,
                        CustomProtocolMessage.success(
                                "PLAYER_KILLED",
                                Map.of("name", mafiaTarget.getName())
                        )
                );
            } else {
                ServerBroadcaster.broadcastToRoom(
                        room,
                        CustomProtocolMessage.success(
                                "PLAYER_SAVED",
                                Map.of("name", mafiaTarget.getName())
                        )
                );
            }
        }
    }

    private static void startNight(GameRoom room) {
        room.setPhase(Phase.NIGHT);
        room.resetNightActions(); // 밤 시작 시 능력 사용 상태 초기화

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success("PHASE_CHANGE",
                        Map.of("phase", "NIGHT")
                )
        );

        // 타이머 스레드 시작
        new Thread(() -> {
            int remainingSeconds = TIMER / 1000;
            try {
                while (remainingSeconds > 0) {
                    Thread.sleep(1000); // 1초 대기
                    remainingSeconds--;
                    
                    ServerBroadcaster.broadcastToRoom(
                            room,
                            CustomProtocolMessage.success("TIMER_UPDATE",
                                    Map.of("remainingSeconds", remainingSeconds, "phase", "NIGHT")
                            )
                    );
                }
                startDay(room);
            } catch (Exception ignored) {}
        }).start();
    }

    // 직업 능력 사용
    public static void handleUseAbility(Player player, CustomProtocolMessage msg) {
        GameRoom room = player.getCurrentGameRoom();
        if (room == null) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", "방 안에 있어야 능력을 사용할 수 있습니다.")
            ));
            return;
        }

        if (!room.isAlive(player)) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", "사망한 플레이어는 행동할 수 없습니다.")
            ));
            return;
        }

        if (room.getPhase() != Phase.NIGHT) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", "능력은 밤에만 사용할 수 있습니다.")
            ));
            return;
        }

        // 밤에 이미 능력을 사용했는지 체크
        if (room.hasUsedNightAbility(player)) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", "밤에 능력은 한 번만 사용할 수 있습니다.")
            ));
            return;
        }

        String targetName = (String) msg.data.get("target");
        if (targetName == null || targetName.isBlank()) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", "대상을 지정해야 합니다.")
            ));
            return;
        }

        Player target = room.findPlayerByName(targetName).orElse(null);
        if (target == null) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", "대상을 찾을 수 없습니다.")
            ));
            return;
        }

        if (!room.isAlive(target)) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", targetName + "은(는) 이미 사망했습니다.")
            ));
            return;
        }

        Role role = room.getRole(player);
        RoleAction action = RoleActionFactory.from(role);
        RoleActionResult result = action.use(player, target, room);

        if (!result.success()) {
            player.send(CustomProtocolMessage.error(
                    "USE_ABILITY",
                    Map.of("message", result.message())
            ));
            return;
        }

        // 능력 사용 성공 시 저장
        room.setNightAbilityUsed(player, true);
        room.setNightTarget(player, target);

        player.send(CustomProtocolMessage.success("USE_ABILITY", Map.of("message", result.message())));
    }

    // 플레이어 목록 조회 (전체 플레이어 + 생존 상태)
    public static void handleGetPlayers(Player player, CustomProtocolMessage msg) {
        GameRoom room = player.getCurrentGameRoom();
        if (room == null) {
            player.send(CustomProtocolMessage.error(
                    "GET_PLAYERS",
                    Map.of("message", "방 안에 있어야 합니다.")
            ));
            return;
        }

        List<Map<String, Object>> playersInfo = new ArrayList<>();
        for (Player p : room.getPlayers()) {
            Map<String, Object> playerInfo = new HashMap<>();
            playerInfo.put("name", p.getName());
            playerInfo.put("alive", room.isAlive(p));
            playerInfo.put("isMe", p.getName().equals(player.getName())); // 자기 자신 여부
            playersInfo.add(playerInfo);
        }

        player.send(CustomProtocolMessage.success(
                "PLAYERS_LIST",
                Map.of("players", playersInfo)
        ));
    }
}
