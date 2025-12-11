package mutfia.server.handler;

import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public class ServerBroadcaster {

    // 방에 있는 모든 플레이어에게 메시지 전송
    public static void broadcastToRoom(GameRoom room, CustomProtocolMessage msg) {
        for (Player p : room.getPlayers()) {
            p.send(msg);
        }
    }

    // 특정 플레이어를 제외하고 메시지 전송
    public static void broadcastToRoomExcept(GameRoom room, Player except, CustomProtocolMessage msg) {
        for (Player p : room.getPlayers()) {
            if (p != except) {
                p.send(msg);
            }
        }
    }

    // 서버 전체 연결된 플레이어에게 브로드캐스트
    public static void broadcastToAll(Iterable<Player> allPlayers, CustomProtocolMessage msg) {
        for (Player p : allPlayers) {
            p.send(msg);
        }
    }
}