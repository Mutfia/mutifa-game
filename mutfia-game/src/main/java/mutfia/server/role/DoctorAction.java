package mutfia.server.role;

import mutfia.server.player.Player;
import mutfia.server.room.GameRoom;

public class DoctorAction implements RoleAction {
    @Override
    public RoleActionResult use(Player actor, Player target, GameRoom room) {
        // 의사는 선택만 저장 (밤 종료 시 결과 처리)
        return RoleActionResult.success("능력을 사용합니다. 치료 대상: " + target.getName());
    }
}
