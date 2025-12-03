package mutfia.global.response;

import java.util.ArrayList;
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
        
        // depth tracking으로 중첩된 중괄호 처리
        // start 위치는 이미 { 안에 있으므로 depth를 1로 시작
        int depth = 1;
        int end = start;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '{') depth++;
            else if (json.charAt(i) == '}') depth--;
            if (depth == 0) {
                end = i;
                break;
            }
        }
        
        // 중괄호 제거하고 내용만 반환
        return json.substring(start, end);
    }

    // key 추출
    private static String extractKey(String keyPart) {
        keyPart = keyPart.trim();
        // "key" 형태인지 확인
        if (keyPart.startsWith("\"") && keyPart.endsWith("\"")) {
            // 앞뒤 따옴표 제거
            return keyPart.substring(1, keyPart.length() - 1);
        }
        // 따옴표가 없거나 부분적으로만 있는 경우
        return keyPart.replace("\"", "");
    }

    private static Map<String, Object> parseData(String body) {
        Map<String, Object> map = new HashMap<>();

        if (body == null || body.isEmpty()) {
            return map;
        }

        String[] pairs = splitByComma(body);
        for (String pair : pairs) {
            int colonIndex = pair.indexOf(':');
            if (colonIndex == -1) continue;
            
            String keyPart = pair.substring(0, colonIndex).trim();
            String valuePart = pair.substring(colonIndex + 1).trim();
            
            String key = extractKey(keyPart);
            
            Object value = parseValue(valuePart);
            map.put(key, value);
        }
        return map;
    }
    
    // 쉼표로 분리하되, 중괄호/대괄호 안의 쉼표는 무시
    private static String[] splitByComma(String str) {
        List<String> result = new ArrayList<>();
        int depth = 0;  // 중괄호/대괄호 깊이
        int start = 0;
        
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            if (c == '{' || c == '[') {
                depth++;  // 열림 → 깊이 증가
            } else if (c == '}' || c == ']') {
                depth--;  // 닫힘 → 깊이 감소
            } else if (c == ',' && depth == 0) {
                // depth가 0일 때만 쉼표로 분리 (최상위 레벨의 쉼표만)
                result.add(str.substring(start, i).trim());
                start = i + 1;
            }
        }
        
        // 마지막 부분 추가
        if (start < str.length()) {
            result.add(str.substring(start).trim());
        }
        
        return result.toArray(new String[0]);
    }

    // 값의 타입을 판단하고 파싱
    private static Object parseValue(String valueStr) {
        valueStr = valueStr.trim();
        if (valueStr.isEmpty()) return "";
        
        // 문자열: "..." 형태
        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }
        
        // 배열: [...] 형태
        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            // 대괄호 제거하고 배열 파싱
            String arrayBody = valueStr.substring(1, valueStr.length() - 1);
            return parseArray(arrayBody);
        }
        
        // 객체: {...} 형태
        if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
            // 중괄호 제거하고 재귀적으로 파싱
            String objectBody = valueStr.substring(1, valueStr.length() - 1);
            return parseData(objectBody);
        }
        
        // boolean
        if (valueStr.equals("true")) return true;
        if (valueStr.equals("false")) return false;
        
        // 숫자
        try {
            return Integer.valueOf(valueStr);
        } catch (NumberFormatException e) {
            // 숫자가 아니면 문자열로 처리
            return valueStr;
        }
    }

    private static List<Map<String, Object>> parseArray(String arrayBody) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (arrayBody == null || arrayBody.trim().isEmpty()) {
            return list;
        }
        
        // 배열 내부의 객체들을 쉼표로 분리 (depth tracking 사용)
        String[] items = splitByComma(arrayBody);
        for (String item : items) {
            item = item.trim();
            // 객체 형태인지 확인
            if (item.startsWith("{") && item.endsWith("}")) {
                // 중괄호 제거하고 파싱
                String objectBody = item.substring(1, item.length() - 1);
                Map<String, Object> map = parseData(objectBody);
                list.add(map);
            }
        }
        return list;
    }

    private static String safe(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\"", "\\\"");
    }

}
