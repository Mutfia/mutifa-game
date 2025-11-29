package mutfia.server.player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import mutfia.global.response.CustomResponse;
import mutfia.server.room.GameRoom;

public class Player implements Runnable{
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

    private void handleRequest(CustomResponse request){

    }

    @Override
    public void run() {

    }
}
