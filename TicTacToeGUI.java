import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Main GUI: supports multi-player sessions (1..5), single difficulty for all players,
 * each player plays one game vs AI sequentially, then a JTable scoreboard is shown.
 */
public class TicTacToeGUI extends JFrame {
    private JButton[] cells = new JButton[9];
    private GameBoard board = new GameBoard();
    private AIPlayer ai;
    private char humanSymbol = 'X';
    private char aiSymbol = 'O';
    private boolean humanTurn = true;
    private JLabel statusLabel;
    private Player currentPlayer;                        // the player currently playing
    private Map<String, Player> cumulativeScores;        // loaded from leaderboard.txt
    private int difficultyLevel = 3;                     // 1=Easy,2=Medium,3=Hard

    // session players and session-tracking (only these determine "round winner")
    private java.util.List<Player> sessionPlayers = new ArrayList<>();
    private int sessionIndex = 0; // which player's turn in session

    // theme colors
    private final Color BG_TOP = new Color(45, 52, 54);
    private final Color BG_BOTTOM = new Color(85, 239, 196);
    private final Color BTN_COLOR = new Color(255, 192, 203);
    private final Color BTN_HOVER = new Color(244, 182, 193);
    private final Color TEXT_COLOR = Color.WHITE;

    public TicTacToeGUI() {
        super("AI Tic Tac Toe - MultiPlayer Edition");
        // load cumulative leaderboard
        cumulativeScores = ScoreManager.loadScores();

        // register players & choose difficulty
        initPlayerRegistrationAndDifficulty();

        // set AI
        ai = new AIPlayer(difficultyLevel);

        // initialize UI and start first player's game
        initUI();
        startNextPlayerOrShowResults();
    }

    /**
     * Ask for number of players (max 5), then prompt names.
     * Then choose difficulty (once).
     */
    private void initPlayerRegistrationAndDifficulty() {
        int numPlayers = 0;
        while (numPlayers <= 0) {
            String s = JOptionPane.showInputDialog(this, "Enter number of players (max 5):", "Players", JOptionPane.PLAIN_MESSAGE);
            if (s == null) { // user cancelled -> default to 1
                numPlayers = 1;
                break;
            }
            try {
                numPlayers = Integer.parseInt(s.trim());
                if (numPlayers <= 0 || numPlayers > 5) {
                    JOptionPane.showMessageDialog(this, "Enter a number between 1 and 5.", "Invalid", JOptionPane.WARNING_MESSAGE);
                    numPlayers = 0;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Invalid", JOptionPane.WARNING_MESSAGE);
            }
        }

        for (int i = 1; i <= numPlayers; i++) {
            String defaultName = "Player" + i;
            String name = JOptionPane.showInputDialog(this, "Enter name for player " + i + ":", defaultName);
            if (name == null || name.trim().isEmpty()) name = defaultName;
            // reuse cumulative record if exists (we will merge later)
            Player p = new Player(name);
            sessionPlayers.add(p);
        }

        String[] options = {"Easy", "Medium", "Hard"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Select Difficulty Level for all players:",
                "Choose AI Level",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);

        difficultyLevel = choice + 1;
        if (difficultyLevel < 1 || difficultyLevel > 3) difficultyLevel = 3;
    }

    private void initUI() {
        JPanel mainPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("🤖 AI Tic Tac Toe", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI Black", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        mainPanel.add(header, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        boardPanel.setOpaque(false);
        Font cellFont = new Font("Poppins", Font.BOLD, 48);

        for (int i = 0; i < 9; i++) {
            final int idx = i;
            cells[i] = new JButton("");
            cells[i].setFont(cellFont);
            cells[i].setFocusPainted(false);
            cells[i].setForeground(TEXT_COLOR);
            cells[i].setBackground(BTN_COLOR);
            cells[i].setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
            cells[i].setOpaque(true);
            cells[i].setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            cells[i].addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (cells[idx].isEnabled()) cells[idx].setBackground(BTN_HOVER);
                }
                public void mouseExited(MouseEvent e) {
                    if (cells[idx].isEnabled()) cells[idx].setBackground(BTN_COLOR);
                }
            });
            cells[i].addActionListener(e -> {
                if (humanTurn && board.isEmpty(idx)) {
                    doHumanMove(idx);
                }
            });
            boardPanel.add(cells[i]);
        }

        mainPanel.add(boardPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("Welcome", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(Color.WHITE);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        JPanel controlPanel = new JPanel(new FlowLayout());
        controlPanel.setOpaque(false);
        JButton newBtn = createStyledButton("🔁 Restart Player Game");
        JButton skipBtn = createStyledButton("⏭ Skip to Next Player");
        JButton saveBtn = createStyledButton("💾 Save Leaderboard");
        JButton tableBtn = createStyledButton("📊 Show Leaderboard");
        JButton quitBtn = createStyledButton("❌ Quit");
        quitBtn.addActionListener(e -> quitGame());
        controlPanel.add(quitBtn);


        newBtn.addActionListener(e -> resetForCurrentPlayer());
        skipBtn.addActionListener(e -> {
            // treat skipped game as loss for that player (optional). Here we just advance.
            fancyMessage("Skipping " + currentPlayer.getName() + "'s game. No result recorded.");
            proceedToNextPlayer();
        });
        saveBtn.addActionListener(e -> {
            // merge all session players (their session stats) into cumulative
            for (Player p : sessionPlayers) {
                ScoreManager.mergeAndSave(cumulativeScores, p);
            }
            fancyMessage("✅ Leaderboard saved to leaderboard.txt");
        });
        tableBtn.addActionListener(e -> showCumulativeLeaderboardTable());

        controlPanel.add(newBtn);
        controlPanel.add(skipBtn);
        controlPanel.add(saveBtn);
        controlPanel.add(tableBtn);
        mainPanel.add(controlPanel, BorderLayout.PAGE_END);

        add(mainPanel);
        setSize(520, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(new Color(0, 184, 148));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(9, 132, 227)); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(0, 184, 148)); }
        });
        return b;
    }
    private void quitGame() {
    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Exit the game?",
            "Quit",
            JOptionPane.YES_NO_OPTION
    );
    if (confirm == JOptionPane.YES_OPTION) {
        System.exit(0);
    }
}

    // Start the next player's game or show final scoreboard if all done
    private void startNextPlayerOrShowResults() {
        if (sessionIndex >= sessionPlayers.size()) {
            showFinalRoundResultsAndLeaderboard();
            return;
        }
        // set current player
        currentPlayer = sessionPlayers.get(sessionIndex);
        // if cumulativeScores has previous stats, we don't copy them into session player (session only)
        // but user wanted cumulative merged on save — we'll merge later.
        resetForCurrentPlayer();
        statusLabel.setText(currentPlayer.getName() + " - Your move (" + humanSymbol + ") - Difficulty: " + getLevelName());
    }

    private void resetForCurrentPlayer() {
        board.clear();
        for (JButton b : cells) {
            b.setText("");
            b.setEnabled(true);
            b.setBackground(BTN_COLOR);
        }
        humanTurn = true;
        statusLabel.setText(currentPlayer.getName() + " - Your move (" + humanSymbol + ") - Difficulty: " + getLevelName());
    }

    private void doHumanMove(int idx) {
        if (!board.makeMove(idx, humanSymbol)) return;
        cells[idx].setText(String.valueOf(humanSymbol));
        cells[idx].setEnabled(false);
        evaluateGame();
        if (board.checkWinner() == ' ') {
            humanTurn = false;
            SwingUtilities.invokeLater(() -> doAIMove());
        }
    }

    private void doAIMove() {
        int move = ai.findBestMove(board);
        if (move >= 0) {
            board.makeMove(move, aiSymbol);
            cells[move].setText(String.valueOf(aiSymbol));
            cells[move].setEnabled(false);
            cells[move].setBackground(new Color(255, 118, 117));
        }
        evaluateGame();
        humanTurn = true;
    }

    private void evaluateGame() {
        char result = board.checkWinner();
        if (result == humanSymbol) {
            fancyMessage("🎉 " + currentPlayer.getName() + " Wins!");
            currentPlayer.addWin();
            // also update cumulative immediately if desired, but we'll merge at the end or when user clicks save
            endGameAndAdvance();
        } else if (result == aiSymbol) {
            fancyMessage("🤖 AI Wins against " + currentPlayer.getName() + "!");
            currentPlayer.addLoss();
            endGameAndAdvance();
        } else if (result == 'D') {
            fancyMessage("😅 It's a Draw for " + currentPlayer.getName() + "!");
            currentPlayer.addDraw();
            endGameAndAdvance();
        } else {
            // no result yet
        }
    }

    private void endGameAndAdvance() {
        for (JButton b : cells) b.setEnabled(false);

        // merge this player's session stats into cumulative storage (in-memory)
        // We call merge here so progress isn't lost even if user doesn't click Save (but still persisted on Save)
        ScoreManager.mergeAndSave(cumulativeScores, currentPlayer);

        // proceed to next player after a short confirmation
        int opt = JOptionPane.showConfirmDialog(this, "Proceed to next player?", "Next", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            proceedToNextPlayer();
        } else {
            // if user cancels, still advance but allow them to view leaderboard
            proceedToNextPlayer();
        }
    }

    private void proceedToNextPlayer() {
        sessionIndex++;
        startNextPlayerOrShowResults();
    }

    private void fancyMessage(String msg) {
        statusLabel.setText(msg);
        JOptionPane.showMessageDialog(this, msg, "Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getLevelName() {
        return switch (difficultyLevel) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            default -> "Hard";
        };
    }

    /**
     * After all session players have played:
     * - Show session winner(s) (based only on this run's players)
     * - Display JTable with cumulative leaderboard (merged)
     */
    private void showFinalRoundResultsAndLeaderboard() {
        // Determine round winner(s) based on sessionPlayers' session stats (wins primarily)
        int topWins = -1;
        for (Player p : sessionPlayers) {
            topWins = Math.max(topWins, p.getWins());
        }
        // Who has topWins among session players?
        ArrayList<String> winners = new ArrayList<>();
        for (Player p : sessionPlayers) {
            if (p.getWins() == topWins && topWins >= 0) {
                winners.add(p.getName());
            }
        }

        String winnerText;
        if (winners.isEmpty()) {
            winnerText = "No winner this round.";
        } else if (winners.size() == 1) {
            winnerText = "🏆 Winner of this Round: " + winners.get(0);
        } else {
            winnerText = "🏆 Winners of this Round: " + String.join(", ", winners);
        }

        // Show winner
        JOptionPane.showMessageDialog(this, winnerText, "Round Winner", JOptionPane.INFORMATION_MESSAGE);

        // Show cumulative leaderboard JTable (sorted by wins desc)
        showCumulativeLeaderboardTable();

        // Save cumulative to disk (final automatic save)
        ScoreManager.saveScores(cumulativeScores);

        // Final status
        statusLabel.setText(winnerText);
    }

    /**
     * Display cumulative leaderboard in a JTable.
     */
    private void showCumulativeLeaderboardTable() {
        // Build table model
        String[] cols = {"Name", "Wins", "Losses", "Draws"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            // make cells non-editable
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // copy cumulativeScores to list and sort by wins desc
        java.util.List<Player> list = new java.util.ArrayList<>(cumulativeScores.values());

        list.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));

        for (Player p : list) {
            model.addRow(new Object[]{p.getName(), p.getWins(), p.getLosses(), p.getDraws()});
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(400, Math.min(300, list.size()*30 + 40)));

        JOptionPane.showMessageDialog(this, scroll, "Leaderboard (Cumulative)", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToeGUI().setVisible(true));
    }
}
