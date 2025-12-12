package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public interface RoleAction {
    RoleActionResult use(Player actor, Player target, GameRoom room);
}
