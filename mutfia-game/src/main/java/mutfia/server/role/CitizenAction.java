package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public class CitizenAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        return RoleActionResult.failure("시민은 사용할 수 있는 능력이 없습니다.");
    }
}
