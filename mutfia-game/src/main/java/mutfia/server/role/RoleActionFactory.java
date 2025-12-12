package mutfia.server.role;

import mutfia.server.player.enums.Role;

public class RoleActionFactory {

    public static RoleAction from(Role role) {
        if (role == null) {
            return new CitizenAction();
        }

        return switch (role) {
            case MAFIA -> new MafiaAction();
            case DOCTOR -> new DoctorAction();
            case POLICE -> new PoliceAction();
            case CITIZEN -> new CitizenAction();
        };
    }
}
