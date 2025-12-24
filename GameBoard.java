public class GameBoard {
    private char[] board; // length 9

    public GameBoard() {
        board = new char[9];
        clear();
    }

    public void clear() {
        for (int i = 0; i < 9; i++) board[i] = ' ';
    }

    public boolean isEmpty(int idx) {
        return board[idx] == ' ';
    }

    public boolean makeMove(int idx, char symbol) {
        if (idx < 0 || idx >= 9 || symbol == ' ' || !isEmpty(idx)) return false;
        board[idx] = symbol;
        return true;
    }

    public void undoMove(int idx) {
        if (idx >= 0 && idx < 9) board[idx] = ' ';
    }

    public char[] getBoardCopy() {
        return board.clone();
    }

    // Returns 'X' if X wins, 'O' if O wins, 'D' if draw, ' ' if game ongoing
    public char checkWinner() {
        int[][] lines = {
            {0,1,2},{3,4,5},{6,7,8}, // rows
            {0,3,6},{1,4,7},{2,5,8}, // cols
            {0,4,8},{2,4,6}          // diagonals
        };
        for (int[] line : lines) {
            char a = board[line[0]], b = board[line[1]], c = board[line[2]];
            if (a != ' ' && a == b && b == c) return a;
        }
        for (char c : board) if (c == ' ') return ' '; // still moves
        return 'D'; // draw
    }

    public boolean isFull() {
        for (char c : board) if (c == ' ') return false;
        return true;
    }
}
