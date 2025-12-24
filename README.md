# AI-based-Tic-tac-toe
This project is a Java Swing–based AI Tic Tac Toe game where multiple human players can play individually against an intelligent AI opponent.
The AI uses the Minimax algorithm with Alpha-Beta pruning, making it unbeatable at higher difficulty levels.


# 🎯 Features
👥 Player Registration

At game start, the application asks:

Number of players (max 5)

Each player’s name individually

Each player plays one game vs AI per session.

🧠 AI Difficulty Levels

Difficulty is selected once for all players:

Easy → Random moves

Medium → Semi-smart AI

Hard → Unbeatable AI (Minimax + Alpha-Beta pruning)

🏆 Score Tracking

Each player's result is recorded as:

Win
Loss
Draw

Scores are stored during the session and also persisted across runs.

📊 Leaderboard (Persistent Storage)

All scores are saved in:
leaderboard.txt


When the game restarts, previous scores are automatically loaded and merged.

A JTable-based leaderboard displays:

Player Name

Wins

Losses

Draws

🥇 Winner Announcement

After all players in the current session finish:

The player with the highest wins is declared:

🏆 Winner of this Round


Winner is decided only among players entered in the current session.

# 🖥️ GUI Navigation

Restart game

Save leaderboard

View leaderboard

Exit application

(Optional) Return to Main Menu

# 🛠️ Technologies Used

Java

Java Swing (GUI)

OOP Concepts

Minimax Algorithm

Alpha-Beta Pruning

# File I/O
Project Files
Player.java
GameBoard.java
AIPlayer.java
ScoreManager.java
TicTacToeGUI.java
leaderboard.txt

# How to Run
javac *.java
java TicTacToeGUI

# Author
Aditi Aggarwal

