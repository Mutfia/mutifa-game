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

    private void handleRequest(CustomProtocolMessage request) {
        try {
            switch (request.type) {
                case "SET_NAME":
                    String newName = (String) request.data.get("name");
                    this.name = newName;
                    send(CustomProtocolMessage.success("SET_NAME",
                            Map.of("name", this.name)));
                    break;
                case "CREATE_ROOM":
                    String roomName = (String) request.data.get("roomName");
                    GameRoom newRoom = RoomManager.create(roomName, this);
                    this.currentGameRoom = newRoom;

                    System.out.println("[SERVER] 방 생성됨 : " + newRoom.getRoomName());
                    send(CustomProtocolMessage.success("CREATE_ROOM",
                            Map.of(
                                    "roomId", newRoom.getId(),
                                    "roomName", newRoom.getRoomName(),
                                    "players", newRoom.getPlayers().size()
                            )
                    ));

                    break;

                case "GET_ROOMS": {
                    send(CustomProtocolMessage.success(
                            "ROOM_LIST",
                            Map.of("rooms", RoomManager.toRoomMapList())
                    ));
                    break;
                }

            }
        } catch (Exception e) {
            send(CustomProtocolMessage.error("[ERROR]", Map.of("message", e.getMessage())));
        }
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
                CustomProtocolMessage request = CustomJson.toCustomProtocolMessage(line);
                handleRequest(request);
            }
        } catch (IOException e) {
            System.out.println("[Player] 연결 종료");
        } finally {
            cleanUp();
        }
    }
}
