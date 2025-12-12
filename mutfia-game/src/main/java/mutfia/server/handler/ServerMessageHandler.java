package mutfia.server.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.player.Player;

public class ServerMessageHandler {
    private static final Map<String, BiConsumer<Player, CustomProtocolMessage>> handlers = new HashMap<>();

    static {
        handlers.put("SET_NAME", Handlers::handleSetName);
        handlers.put("CREATE_ROOM", Handlers::handleCreateRoom);
        handlers.put("GET_ROOMS", Handlers::handleGetRooms);
        handlers.put("JOIN_ROOM", Handlers::handleJoinRoom);
        handlers.put("CHAT", Handlers::handleChat);
        handlers.put("USE_ABILITY", Handlers::handleUseAbility);
        handlers.put("GET_PLAYERS", Handlers::handleGetPlayers);
        handlers.put("VOTE", Handlers::handleVote);
    }

    public static void dispatch(Player player, CustomProtocolMessage msg){
        BiConsumer<Player, CustomProtocolMessage> handler = handlers.get(msg.type);

        if (handler != null) {
            handler.accept(player, msg);
        } else {
            System.out.println("[Error] 알 수 없는 타입입니다 : " + msg.type);
        }

    }

}
