package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.player.enums.Role;
import mutfia.server.room.GameRoom;

public class PoliceAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        Role targetRole = room.getRole(target);
        if (targetRole == null) {
            return RoleActionResult.failure("대상의 직업을 알 수 없습니다.");
        }

        return RoleActionResult.success(target.getName() + "의 직업은 [" + targetRole.name() + "] 입니다.");
    }
}
