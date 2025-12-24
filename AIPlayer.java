import java.util.*;

/**
 * AIPlayer using Minimax with Alpha-Beta pruning.
 * Difficulty levels:
 *   1 - Easy: Random moves
 *   2 - Medium: Semi-smart (random + limited minimax)
 *   3 - Hard: Full Minimax with Alpha-Beta pruning (unbeatable)
 */
public class AIPlayer {
    private char aiPlayer = 'O';
    private char humanPlayer = 'X';
    private int difficulty = 3; // default Hard
    private Random rand = new Random();

    public AIPlayer() {}
    public AIPlayer(int level) { setDifficulty(level); }

    public void setDifficulty(int level) {
        if (level < 1 || level > 3) level = 3;
        this.difficulty = level;
    }

    public int findBestMove(GameBoard board) {
        char[] b = board.getBoardCopy();

        // Level 1 → Random
        if (difficulty == 1) {
            return randomMove(b);
        }

        // Level 2 → Mix of random and limited depth minimax
        if (difficulty == 2) {
            if (rand.nextInt(100) < 40) { // 40% random
                return randomMove(b);
            } else {
                return minimaxBestMove(b, 2); // shallow search
            }
        }

        // Level 3 → Full Minimax with Alpha-Beta pruning
        return minimaxBestMove(b, 9); // full-depth search
    }

    private int minimaxBestMove(char[] b, int maxDepth) {
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < 9; i++) {
            if (b[i] == ' ') {
                b[i] = aiPlayer;
                int score = minimax(b, 0, false, Integer.MIN_VALUE, Integer.MAX_VALUE, maxDepth);
                b[i] = ' ';
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }
        return bestMove >= 0 ? bestMove : randomMove(b);
    }

    /**
     * Minimax with Alpha–Beta pruning and depth-based scoring.
     */
    private int minimax(char[] b, int depth, boolean isMax, int alpha, int beta, int maxDepth) {
        char result = evaluateBoard(b);
        if (result != ' ' || depth >= maxDepth) {
            return score(result, depth);
        }

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (b[i] == ' ') {
                    b[i] = aiPlayer;
                    int val = minimax(b, depth + 1, false, alpha, beta, maxDepth);
                    b[i] = ' ';
                    best = Math.max(best, val);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break; // pruning
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (b[i] == ' ') {
                    b[i] = humanPlayer;
                    int val = minimax(b, depth + 1, true, alpha, beta, maxDepth);
                    b[i] = ' ';
                    best = Math.min(best, val);
                    beta = Math.min(beta, best);
                    if (beta <= alpha) break; // pruning
                }
            }
            return best;
        }
    }

    private int score(char result, int depth) {
        if (result == aiPlayer) return 10 - depth;    // Prefer fast win
        if (result == humanPlayer) return depth - 10; // Delay loss
        return 0; // Draw
    }

    private int randomMove(char[] b) {
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < 9; i++) if (b[i] == ' ') empty.add(i);
        if (empty.isEmpty()) return -1;
        return empty.get(rand.nextInt(empty.size()));
    }

    private char evaluateBoard(char[] b) {
        int[][] lines = {
            {0,1,2}, {3,4,5}, {6,7,8},
            {0,3,6}, {1,4,7}, {2,5,8},
            {0,4,8}, {2,4,6}
        };
        for (int[] line : lines) {
            char a = b[line[0]], c = b[line[1]], d = b[line[2]];
            if (a != ' ' && a == c && c == d) return a;
        }
        for (char ch : b) if (ch == ' ') return ' ';
        return 'D'; // Draw
    }
}
