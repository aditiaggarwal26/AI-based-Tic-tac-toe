import java.io.*;
import java.util.*;

/**
 * ScoreManager: load/save merged leaderboard from/to leaderboard.txt
 * File format: name,wins,losses,draws per line
 */
public class ScoreManager {
    private static final String LEADERBOARD_FILE = "leaderboard.txt";

    // load players from file (cumulative)
    public static Map<String, Player> loadScores() {
        Map<String, Player> map = new HashMap<>();
        File f = new File(LEADERBOARD_FILE);
        if (!f.exists()) return map;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                Player p = Player.fromString(line);
                if (p != null) map.put(p.getName(), p);
            }
        } catch (IOException e) {
            System.err.println("Error loading leaderboard: " + e.getMessage());
        }
        return map;
    }

    // save players map to file (overwrites)
    public static void saveScores(Map<String, Player> map) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (Player p : map.values()) {
                bw.write(p.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving leaderboard: " + e.getMessage());
        }
    }

    /**
     * Merge session player's stats into cumulative map and persist.
     * This will add stats if player exists, or create new entry.
     */
    public static void mergeAndSave(Map<String, Player> cumulativeMap, Player sessionPlayer) {
        Player existing = cumulativeMap.get(sessionPlayer.getName());
        if (existing == null) {
            // clone sessionPlayer into cumulative map
            Player copy = new Player(sessionPlayer.getName());
            for (int i = 0; i < sessionPlayer.getWins(); i++) copy.addWin();
            for (int i = 0; i < sessionPlayer.getLosses(); i++) copy.addLoss();
            for (int i = 0; i < sessionPlayer.getDraws(); i++) copy.addDraw();
            cumulativeMap.put(copy.getName(), copy);
        } else {
            // increment counts
            for (int i = 0; i < sessionPlayer.getWins(); i++) existing.addWin();
            for (int i = 0; i < sessionPlayer.getLosses(); i++) existing.addLoss();
            for (int i = 0; i < sessionPlayer.getDraws(); i++) existing.addDraw();
        }
        saveScores(cumulativeMap);
    }
}
