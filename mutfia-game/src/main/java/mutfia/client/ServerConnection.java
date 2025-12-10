package mutfia.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mutfia.global.response.CustomJson;
import mutfia.global.response.CustomProtocolMessage;
import mutfia.global.response.enums.Status;

public class ServerConnection {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    private static Socket socket;
    private static BufferedWriter bw;
    private static BufferedReader br;

    public static void connect(){
        try{
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(ServerConnection::listen).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void listen(){
        try{
            String line;
            while((line=br.readLine())!=null){
                CustomProtocolMessage msg = CustomJson.toCustomProtocolMessage(line);

            }
        } catch (Exception e){

        }
    }
    
    /**
     * 서버에 연결하고 요청을 전송한 후 응답을 받습니다.
     * @param requestType 요청 타입 (SET_NAME, GET_ROOMS, CREATE_ROOM 등)
     * @param data 요청 데이터
     * @return 서버 응답 메시지
     */
    public static CustomProtocolMessage sendRequest(String requestType, Map<String, Object> data) {
        Socket socket = null;
        BufferedWriter bw = null;
        BufferedReader br = null;
        
        try {
            // 서버 연결
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // 요청 생성 및 전송
            CustomProtocolMessage request = CustomProtocolMessage.success(requestType, data);
            String json = CustomJson.toJson(request);
            bw.write(json);
            bw.newLine();
            bw.flush();
            
            // 응답 수신
            String response = br.readLine();
            if (response != null && !response.trim().isEmpty()) {
                return CustomJson.toCustomProtocolMessage(response);
            }
            
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return CustomProtocolMessage.error(requestType, Map.of("message", e.getMessage()));
        } finally {
            // 연결 종료
            try {
                if (bw != null) bw.close();
                if (br != null) br.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 플레이어 이름 설정
     * @param playerName 플레이어 이름
     * @return 성공 여부
     */
    public static boolean setName(String playerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", playerName);
        
        CustomProtocolMessage response = sendRequest("SET_NAME", data);
        return response != null && response.status == Status.OK;
    }
    
    /**
     * 방 목록 조회
     * @return 방 목록 (List<Map<String, Object>>)
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getRooms() {
        CustomProtocolMessage response = sendRequest("GET_ROOMS", null);
        Object roomData = response.data.get("rooms");
        System.out.println("데이터: " + roomData);
        System.out.println("타입: " + (roomData != null ? roomData.getClass().getName() : "null"));
        
        if (response != null && response.status == Status.OK && response.data != null) {
            return (List<Map<String, Object>>) response.data.get("rooms");
        }
        return null;
    }
    
    /**
     * 방 생성
     * @param roomName 방 이름
     * @return 생성된 방 정보 (Map<String, Object>) 또는 null
     */
    public static Map<String, Object> createRoom(String roomName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomName", roomName);
        
        CustomProtocolMessage response = sendRequest("CREATE_ROOM", data);
        
        if (response != null && response.status == Status.OK) {
            return response.data;
        }
        return null;
    }
}

