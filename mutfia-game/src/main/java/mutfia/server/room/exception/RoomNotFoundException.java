package mutfia.server.room.exception;

import mutfia.server.exception.BaseException;

public class RoomNotFoundException extends BaseException {
    public RoomNotFoundException(Long id){
        super("존재하지 않는 방입니다. (id=" + id + ")");
    }
}
