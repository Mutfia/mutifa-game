package mutfia.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import mutfia.server.player.Player;

public class ServerMain {
    private static final int PORT = 9999;

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("[Server] 포트 번호  : " + PORT);

            while (true){
                Socket client = serverSocket.accept();
                Player player = Player.create(client);
                new Thread(player).start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
