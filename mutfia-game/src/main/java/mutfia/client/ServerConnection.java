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
import mutfia.client.handler.ClientMessageHandler;
import mutfia.global.response.CustomJson;
import mutfia.global.response.CustomProtocolMessage;
import mutfia.global.response.enums.Status;

public class ServerConnection {
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 9999;

    private static Socket socket;
    private static BufferedWriter bw;
    private static BufferedReader br;

    public static void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            new Thread(ServerConnection::listen).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void listen() {
        try {
            String line;
            while ((line = br.readLine()) != null) {
                CustomProtocolMessage msg = CustomJson.toCustomProtocolMessage(line);
                ClientMessageHandler.dispatch(msg);
            }
        } catch (Exception e) {
            System.out.println("[Error] 서버 연결 끊김");
        }
    }

    public static void send(String type, Map<String, Object> data) {
        try {
            CustomProtocolMessage request = CustomProtocolMessage.success(type, data);
            String json = CustomJson.toJson(request);

            bw.write(json);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.out.println("[Error] 클라이언트 메시지 전송 실패");
        }
    }
}

