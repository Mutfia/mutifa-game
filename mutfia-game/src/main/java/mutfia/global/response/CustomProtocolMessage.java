package mutfia.global.response;

import java.util.Map;
import mutfia.global.response.enums.Status;

public class CustomProtocolMessage {
    public String type;
    public Status status;
    public String message;
    public Map<String, Object> data;

    public CustomProtocolMessage(String type, Status status, String message, Map<String, Object> data) {
        this.type = type;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static CustomProtocolMessage success(String type) {
        return new CustomProtocolMessage(type, Status.OK, Status.OK.getMessage(), null);
    }

    public static CustomProtocolMessage success(String type, Map<String, Object> data) {
        return new CustomProtocolMessage(type, Status.OK, Status.OK.getMessage(), data);
    }

    public static CustomProtocolMessage success(String type, String message, Map<String, Object> data) {
        return new CustomProtocolMessage(type, Status.OK, message, data);
    }

    public static CustomProtocolMessage error(String type) {
        return new CustomProtocolMessage(type, Status.ERROR, Status.ERROR.getMessage(), null);
    }

    public static CustomProtocolMessage error(String type, String message) {
        return new CustomProtocolMessage(type, Status.ERROR, message, null);
    }

    public static CustomProtocolMessage error(String type, Map<String, Object> data) {
        return new CustomProtocolMessage(type, Status.ERROR, Status.ERROR.getMessage(), data);
    }

    public static CustomProtocolMessage error(String type, String message, Map<String, Object> data) {
        return new CustomProtocolMessage(type, Status.ERROR, message, data);
    }
}
