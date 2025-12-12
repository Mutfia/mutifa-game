package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public class DoctorAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        room.heal(target);
        return RoleActionResult.success(target.getName() + "을(를) 치료했습니다.");
    }
}
