package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public class VoteAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        // 투표 저장
        room.setVote(actor, target);

        return RoleActionResult.success(target.getName() + "에게 투표했습니다.");
    }
}

