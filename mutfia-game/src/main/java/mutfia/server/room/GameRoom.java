package mutfia.server.room;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mutfia.server.player.Player;
import mutfia.server.player.enums.Role;
import mutfia.server.room.enums.Phase;

public class GameRoom {
    private static final int MAX_PLAYERS_COUNT = 5;
    private Long id;
    private String roomName;
    private boolean isPlaying;
    private List<Player> players;
    private int maxPlayersCount;
    private Phase phase = Phase.DAY;

    private Map<Player, Role> roles = new HashMap<>();
    private Map<Player, Boolean> aliveStates = new HashMap<>();

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
        aliveStates.remove(player);
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
        return new ArrayList<>(players);
    }

    public List<String> getPlayerNames() {
        return players.stream()
                .map(Player::getName)
                .toList();
    }

    public int getMaxPlayersCount() {
        return maxPlayersCount;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setRoles(Map<Player, Role> roles) {
        this.roles = roles;
    }

    public Role getRole(Player player) {
        return roles.get(player);
    }

    public Map<Player, Role> getRoles() {
        return roles;
    }

    public void initializeAliveStates() {
        for (Player player : players) {
            aliveStates.put(player, true);
        }
    }

    public boolean isAlive(Player player) {
        return aliveStates.getOrDefault(player, true);
    }

    public void markDead(Player player) {
        aliveStates.put(player, false);
    }

    public void heal(Player player) {
        aliveStates.put(player, true);
    }

    public List<Player> getAlivePlayers() {
        return players.stream()
                .filter(this::isAlive)
                .toList();
    }

    public Optional<Player> findPlayerByName(String name) {
        return players.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst();
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }
}
