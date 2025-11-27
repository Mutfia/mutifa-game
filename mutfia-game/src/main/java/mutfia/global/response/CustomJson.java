package mutfia.global.response;

import java.util.HashMap;
import java.util.Map;
import mutfia.global.response.enums.Status;

public class CustomJson {

    public static String toJson(CustomResponse response) {
        String type = response.type;
        String status = response.status.toString();
        String message = response.message;
        Map<String, Object> data = response.data == null ? new HashMap<>() : response.data;

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        sb.append("\"type\":\"").append(type).append("\",");
        sb.append("\"status\":\"").append(status).append("\",");
        sb.append("\"message\":\"").append(message).append("\",");

        sb.append("\"data\":{");

        int i = 0;
        for (String key : data.keySet()) {
            sb.append("\"").append(safe(key)).append("\":\"")
                    .append(safe(String.valueOf(data.get(key))))
                    .append("\"");

            if (i < data.size() - 1) {
                sb.append(",");
            }
            i++;
        }
        sb.append("}");

        sb.append("}");
        return sb.toString();
    }

    public static CustomResponse toCustomResponse(String json) {
        String type = extractValue(json, "type");
        Status status = Status.valueOf(extractValue(json, "status"));
        String message = extractValue(json, "message");

        String dataBody = extractObject(json, "data");
        Map<String, Object> data = parseData(dataBody);

        return new CustomResponse(type, status, message, data);
    }

    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":\"";

        int start = json.indexOf(search);
        if (start == -1) {
            return "";
        }

        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static String extractObject(String json, String key) {
        String search = "\"" + key + "\":{";

        int start = json.indexOf(search);
        if (start == -1) {
            return "";
        }

        start += search.length();
        int end = json.indexOf("}", start);
        return json.substring(start, end);
    }

    private static Map<String, Object> parseData(String body) {
        Map<String, Object> map = new HashMap<>();

        if (body == null || body.isEmpty()) {
            return map;
        }

        String[] pairs = body.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                String key = kv[0].replace("\"", "").trim();
                String val = kv[1].replace("\"", "").trim();
                map.put(key, val);
            }
        }
        return map;
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"");
    }

}
