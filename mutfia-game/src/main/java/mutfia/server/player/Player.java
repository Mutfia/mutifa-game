package mutfia.server.player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import mutfia.global.response.CustomJson;
import mutfia.global.response.CustomProtocolMessage;
import mutfia.server.handler.ServerMessageHandler;
import mutfia.server.room.GameRoom;
import mutfia.server.room.RoomManager;

public class Player implements Runnable {
    private BufferedReader br;
    private BufferedWriter bw;
    private Socket socket;

    private String name;
    private GameRoom currentGameRoom;

    private Player(Socket socket) {
        try {
            this.socket = socket;
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException e) {
            System.out.println("[Player] 소켓 생성 오류: " + e.getMessage());
        }
    }

    public static Player create(Socket socket) {
        return new Player(socket);
    }

    public void send(CustomProtocolMessage response) {
        try {
            String json = CustomJson.toJson(response);
            bw.write(json);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            System.out.println("[Player] 메시지 전송 오류");
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCurrentGameRoom(GameRoom currentGameRoom) {
        this.currentGameRoom = currentGameRoom;
    }

    public GameRoom getCurrentGameRoom() {
        return currentGameRoom;
    }

    private void cleanUp() {
        try {
            if (currentGameRoom != null) {
                currentGameRoom.removePlayer(this);

                if (currentGameRoom.isEmpty()) {
                    RoomManager.removeRoom(currentGameRoom);
                }
            }
            socket.close();
            br.close();
            bw.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                CustomProtocolMessage request = CustomJson.toCustomProtocolMessage(line);

                ServerMessageHandler.dispatch(this, request);
            }
        } catch (IOException e) {
            System.out.println("[Player] 연결 종료");
        } finally {
            cleanUp();
        }
    }
}
