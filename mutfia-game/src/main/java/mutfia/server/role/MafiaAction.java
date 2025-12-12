package mutfia.server.role;

import java.util.Map;
import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.handler.ServerBroadcaster;
import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public class MafiaAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        if (!room.isAlive(target)) {
            return RoleActionResult.failure(target.getName() + "은(는) 이미 사망했습니다.");
        }

        if (actor.equals(target)) {
            return RoleActionResult.failure("자기 자신을 공격할 수 없습니다.");
        }

        room.markDead(target);

        ServerBroadcaster.broadcastToRoom(
                room,
                CustomProtocolMessage.success(
                        "PLAYER_KILLED",
                        Map.of("name", target.getName())
                )
        );

        return RoleActionResult.success(target.getName() + "을(를) 제거했습니다.");
    }
}
