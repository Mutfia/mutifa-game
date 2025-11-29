package mutfia.server.room;

import java.util.ArrayList;
import java.util.List;
import mutfia.server.player.Player;

public class GameRoom {
    private static final int MAX_PLAYERS_COUNT = 5;
    private Long id;
    private String roomName;
    private boolean isPlaying;
    private List<Player> players;
    private int maxPlayersCount;

    private GameRoom(Long id, String roomName, Player creator) {
        this.id = id;
        this.roomName = roomName;
        this.isPlaying = false;
        this.maxPlayersCount = MAX_PLAYERS_COUNT;

        this.players = new ArrayList<>();
        this.players.add(creator);
    }

    public static GameRoom create(Long id, String roomName, Player creator) {
        return new GameRoom(id, roomName, creator);
    }

    public synchronized boolean entrancePlayer(Player player) {
        if (this.players.size() >= MAX_PLAYERS_COUNT || isPlaying) {
            return false;
        }

        this.players.add(player);
        return true;
    }

    public synchronized void removePlayer(Player player) {
        this.players.remove(player);
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getMaxPlayersCount() {
        return maxPlayersCount;
    }
}