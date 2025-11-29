package mutfia.global.response;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mutfia.global.response.enums.Status;

public class CustomJson {

    public static String toJson(CustomProtocolMessage customProtocolMessage) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("type", customProtocolMessage.type);
        jsonMap.put("status", customProtocolMessage.status.toString());
        jsonMap.put("message", customProtocolMessage.message);
        jsonMap.put("data", customProtocolMessage.data == null ? new HashMap<>() : customProtocolMessage.data);

        return mapToJson(jsonMap);
    }

    public static CustomProtocolMessage toCustomProtocolMessage(String json) {
        String type = extractValue(json, "type");
        Status status = Status.valueOf(extractValue(json, "status"));
        String message = extractValue(json, "message");

        String dataBody = extractObject(json, "data");
        Map<String, Object> data = parseData(dataBody);

        return new CustomProtocolMessage(type, status, message, data);
    }

    private static String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        int i = 0;
        for (String key : map.keySet()) {
            sb.append("\"").append(safe(key)).append("\":");
            sb.append(valueToJson(map.get(key)));

            if (i < map.size() - 1) {
                sb.append(",");
            }
            i++;
        }

        sb.append("}");
        return sb.toString();
    }

    private static String valueToJson(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof String) {
            return "\"" + safe(value.toString()) + "\"";
        }

        if (value instanceof Number) {
            return value.toString();
        }

        if (value instanceof Boolean) {
            return value.toString();
        }

        if (value instanceof Map) {
            return mapToJson((Map<String, Object>) value);
        }

        if (value instanceof List) {
            return listToJson((List<?>) value);
        }

        return "\"" + safe(value.toString()) + "\"";
    }

    private static String listToJson(List<?> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);

            if (item instanceof Map) {
                sb.append(mapToJson((Map<String, Object>) item));
            } else {
                sb.append(valueToJson(item));
            }

            if (i < list.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
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
