package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.player.enums.Role;
import mutfia.server.room.GameRoom;

public class PoliceAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        Role targetRole = room.getRole(target);
        boolean isMafia = targetRole == Role.MAFIA;
        String result = isMafia ? "마피아입니다!" : "마피아가 아닙니다.";
        
        return RoleActionResult.success(target.getName() + "은(는) " + result);
    }
}
