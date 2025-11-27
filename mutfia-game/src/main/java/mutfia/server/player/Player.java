package mutfia.server.player;

public class Player {
    private String name;

    private Player(String name) {
        this.name = name;
    }

    public static Player create(String name) {
        return new Player(name);
    }
}
