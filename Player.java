import java.io.Serializable;
import java.util.Objects;

// Simple Player class to store name and stats.
public class Player implements Serializable {
    private String name;
    private int wins;
    private int losses;
    private int draws;

    public Player(String name) {
        this.name = (name == null || name.trim().isEmpty()) ? "Player" : name.trim();
        this.wins = 0;
        this.losses = 0;
        this.draws = 0;
    }

    // getters & setters
    public String getName() { return name; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getDraws() { return draws; }

    public void addWin() { wins++; }
    public void addLoss() { losses++; }
    public void addDraw() { draws++; }

    @Override
    public String toString() {
        return name + "," + wins + "," + losses + "," + draws;
    }

    public static Player fromString(String line) {
        // expected format: name,wins,losses,draws
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",");
        if (parts.length < 1) return null;
        String nm = parts[0].trim();
        Player p = new Player(nm.isEmpty() ? "Player" : nm);
        if (parts.length >= 4) {
            try {
                p.wins = Integer.parseInt(parts[1]);
                p.losses = Integer.parseInt(parts[2]);
                p.draws = Integer.parseInt(parts[3]);
            } catch (NumberFormatException e) {
                // ignore — keep defaults
            }
        }
        return p;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;
        Player player = (Player) o;
        return name.equalsIgnoreCase(player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
