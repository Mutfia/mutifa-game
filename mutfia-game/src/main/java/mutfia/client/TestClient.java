package mutfia.client;

import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 9999);
            System.out.println("[Client] Connected!");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 메시지 전송 함수
            Send(bw, "{\"type\":\"SET_NAME\",\"status\":\"OK\",\"message\":\"\",\"data\":{\"name\":\"hyejeong\"}}");
            Receive(br);

            Send(bw, "{\"type\":\"GET_ROOMS\",\"status\":\"OK\",\"message\":\"\",\"data\":{}}");
            Receive(br);

            Send(bw,
                    "{\"type\":\"CREATE_ROOM\",\"status\":\"OK\",\"message\":\"\",\"data\":{\"roomName\":\"TestRoom1\"}}");
            Receive(br);

            Send(bw, "{\"type\":\"GET_ROOMS\",\"status\":\"OK\",\"message\":\"\",\"data\":{}}");
            Receive(br);

            socket.close();
            System.out.println("[Client] Closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void Send(BufferedWriter bw, String json) throws IOException {
        System.out.println("[Client] SEND → " + json);
        bw.write(json);
        bw.newLine();
        bw.flush();
    }

    private static void Receive(BufferedReader br) throws IOException {
        String line = br.readLine();
        System.out.println("[Client] RECEIVE ← " + line);
    }
}
