package mutfia.global.response;

import java.util.Map;
import mutfia.global.response.enums.Status;

public class CustomResponse {
    public String type;
    public Status status;
    public String message;
    public Map<String, Object> data;

    public CustomResponse(String type, Status status, String message, Map<String, Object> data) {
        this.type = type;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static CustomResponse success(String type) {
        return new CustomResponse(type, Status.OK, Status.OK.getMessage(), null);
    }

    public static CustomResponse success(String type, Map<String, Object> data) {
        return new CustomResponse(type, Status.OK, Status.OK.getMessage(), data);
    }

    public static CustomResponse success(String type, String message, Map<String, Object> data) {
        return new CustomResponse(type, Status.OK, message, data);
    }

    public static CustomResponse error(String type) {
        return new CustomResponse(type, Status.ERROR, Status.ERROR.getMessage(), null);
    }

    public static CustomResponse error(String type, String message) {
        return new CustomResponse(type, Status.ERROR, message, null);
    }

    public static CustomResponse error(String type, Map<String, Object> data) {
        return new CustomResponse(type, Status.ERROR, Status.ERROR.getMessage(), data);
    }

    public static CustomResponse error(String type, String message, Map<String, Object> data) {
        return new CustomResponse(type, Status.ERROR, message, data);
    }
}
