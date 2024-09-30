package com.willie.mancala;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MAX_SEEDS = 999;
    private TextView outputText;
    private EditText inputText;
    private List<Integer> board;
    private int pits;
    private int seeds_per_pit;
    private int player;
    private boolean gameStarted = false;
    private StringBuilder output = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = findViewById(R.id.outputText);
        inputText = findViewById(R.id.inputText);

        inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                handleInput(inputText.getText().toString());
                inputText.setText("");
                return true;
            }
            return false;
        });

        printWelcomeMessage();
    }

    private void printWelcomeMessage() {
        appendToOutput("Type 'rule' to view the rules, 'info' to view the information, or 'exit' to quit anytime.\n\n");
        appendToOutput("Enter the number of pits each player has: ");
    }

    private void handleInput(String input) {
        if (input.equals("exit")) {
            handleExit();
        } else if (input.equals("rule")) {
            printRules();
        } else if (input.equals("info")) {
            printInfo();
        } else if (!gameStarted) {
            setupGame(input);
        } else {
            playGame(input);
        }
    }

    private void setupGame(String input) {
        try {
            int value = Integer.parseInt(input);
            if (!gameStarted) {
                if (pits == 0) {
                    if (value > 0) {
                        pits = value;
                        appendToOutput("Enter the number of seeds per pit: ");
                    } else {
                        appendToOutput("Invalid input. Please enter a positive integer.\n");
                    }
                } else {
                    if (value > 0 && value * pits <= MAX_SEEDS) {
                        seeds_per_pit = value;
                        initializeGame();
                    } else {
                        appendToOutput("Invalid input. Please enter a positive integer between 1 and " + MAX_SEEDS / pits + ".\n");
                    }
                }
            }
        } catch (NumberFormatException e) {
            appendToOutput("Invalid input. Please enter a positive integer.\n");
        }
    }

    private void initializeGame() {
        int total_pits = pits * 2 + 2;
        board = new ArrayList<>(total_pits);
        for (int i = 0; i < total_pits; i++) {
            board.add(seeds_per_pit);
        }
        board.set(pits, 0);
        board.set(total_pits - 1, 0);

        appendToOutput("\nGame setup:\n");
        appendToOutput("  - Player 1's pits start on the left side of the board.\n");
        appendToOutput("  - Player 2's pits start on the right side of the board.\n");
        appendToOutput("  - Player 1's store is at the right end of the board.\n");
        appendToOutput("  - Player 2's store is at the left end of the board.\n");
        appendToOutput("  - The pits and stores are arranged in a line, with Player 1's pits followed by Player 1's store, then Player 2's pits and Player 2's store.\n");
        appendToOutput("  - Each player has " + pits + " pits and " + seeds_per_pit + " seeds in each pit initially.\n");
        appendToOutput("Type 'rule' to view the rules, 'info' to view the information, or 'exit' to quit anytime.\n\n");

        gameStarted = true;
        player = 0;
        printBoard();
        appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + pits + ") : ");
    }

    private void playGame(String input) {
        try {
            int pit = Integer.parseInt(input) - 1;
            if (pit < 0 || pit >= pits) {
                appendToOutput("Invalid pit number. Try again.\n");
                return;
            }

            if (makeMove(pit)) {
                if (checkGameOver()) {
                    endGame();
                    return;
                }

                if (pit != pits) {
                    player = (player + 1) % 2;
                }
            } else {
                appendToOutput("Invalid move. Try again.\n");
            }

            printBoard();
            appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + pits + ") : ");
        } catch (NumberFormatException e) {
            appendToOutput("Invalid input. Please enter a pit number, 'exit', or 'rule'.\n");
        }
    }

    private void printBoard() {
        int total_pits = pits * 2 + 2;
        StringBuilder boardString = new StringBuilder("\nPlayer 2\n ");
        for (int i = total_pits - 2; i > pits; i--) {
            boardString.append(" ").append(board.get(i)).append(" ");
        }
        boardString.append("\n ").append(board.get(total_pits - 1));
        for (int i = 0; i < pits + 1; i++) {
            boardString.append("  ");
        }
        boardString.append("   ").append(board.get(pits)).append("\n ");
        for (int i = 0; i < pits; i++) {
            boardString.append(" ").append(board.get(i)).append(" ");
        }
        boardString.append("\nPlayer 1\n\n");
        appendToOutput(boardString.toString());
    }

    private boolean makeMove(int pit) {
        int total_pits = pits * 2 + 2;
        int start = player * (pits + 1);
        int seeds = board.get(start + pit);

        if (seeds == 0) return false;

        board.set(start + pit, 0);
        int pos = start + pit;

        while (seeds > 0) {
            pos = (pos + 1) % total_pits;
            if (pos == (player == 0 ? total_pits - 1 : pits)) continue;
            board.set(pos, board.get(pos) + 1);
            seeds--;
        }

        if (pos != pits && pos != total_pits - 1) {
            if (player == pos / (pits + 1) && board.get(pos) == 1) {
                int opposite = total_pits - 2 - pos;
                if (board.get(opposite) > 0) {
                    board.set(player == 0 ? pits : total_pits - 1, board.get(player == 0 ? pits : total_pits - 1) + board.get(opposite) + 1);
                    board.set(opposite, 0);
                    board.set(pos, 0);
                }
            }
        }

        return (player == 0 && pos == pits) || (player == 1 && pos == total_pits - 1);
    }

    private boolean checkGameOver() {
        int total_pits = pits * 2 + 2;
        int sum1 = 0, sum2 = 0;
        for (int i = 0; i < pits; i++) {
            sum1 += board.get(i);
            sum2 += board.get(pits + 1 + i);
        }

        if (sum1 == 0 || sum2 == 0) {
            for (int i = 0; i < pits; i++) {
                board.set(pits, board.get(pits) + board.get(i));
                board.set(total_pits - 1, board.get(total_pits - 1) + board.get(pits + 1 + i));
                board.set(i, 0);
                board.set(pits + 1 + i, 0);
            }
            return true;
        }
        return false;
    }

    private void endGame() {
        int player1_store = board.get(pits);
        int player2_store = board.get(pits * 2 + 1);

        if (player1_store > player2_store) {
            appendToOutput("Player 1 wins with " + player1_store + " seeds!\n");
        } else if (player2_store > player1_store) {
            appendToOutput("Player 2 wins with " + player2_store + " seeds!\n");
        } else {
            appendToOutput("It's a tie!\n");
        }

        gameStarted = false;
        pits = 0;
        seeds_per_pit = 0;
        printWelcomeMessage();
    }

    private void handleExit() {
        appendToOutput("Are you sure you want to quit? (y/n): ");
        inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String choice = inputText.getText().toString();
                if (choice.equals("y") || choice.equals("Y")) {
                    finish();
                } else {
                    appendToOutput("\n");
                    inputText.setOnEditorActionListener(null);
                    if (gameStarted) {
                        printBoard();
                        appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + pits + ") : ");
                    } else {
                        printWelcomeMessage();
                    }
                }
                inputText.setText("");
                return true;
            }
            return false;
        });
    }

    private void printRules() {
        appendToOutput("\nMancala Rules:\n");
        appendToOutput("1. The board has two rows of pits, one for each player. Each player has a row of pits and a store.\n");
        appendToOutput("2. Each player has a certain number of pits (defined at the beginning of the game) and each pit starts with the same number of seeds (also defined at the beginning).\n");
        appendToOutput("3. The board setup is as follows:\n");
        appendToOutput("  - Player 1's pits start on the left side of the board.\n");
        appendToOutput("  - Player 2's pits start on the right side of the board.\n");
        appendToOutput("  - Player 1's store is at the right end of the board.\n");
        appendToOutput("  - Player 2's store is at the left end of the board.\n");
        appendToOutput("  - The pits and stores are arranged in a line, with Player 1's pits followed by Player 1's store, then Player 2's pits and Player 2's store.\n");
        appendToOutput("4. Players take turns selecting one of their pits to move the seeds. The seeds are distributed counter-clockwise around the board.\n");
        appendToOutput("5. Seeds are placed one by one in each subsequent pit, including the player's store but excluding the opponent's store.\n");
        appendToOutput("6. If the last seed lands in the player's store, they get another turn.\n");
        appendToOutput("7. If the last seed lands in an empty pit on the player's side, they capture all seeds in the opposite pit (if it contains seeds) and add them to their store.\n");
        appendToOutput("8. The game ends when all pits on one side are empty. The remaining seeds are moved to the respective stores.\n");
        appendToOutput("9. The player with the most seeds in their store at the end of the game wins.\n");
        appendToOutput("10. If both players have the same number of seeds in their stores, the game is a tie.\n");
        appendToOutput("Type 'rule' to view the rules, 'info' to view the information, or 'exit' to quit anytime.\n\n");

        if (gameStarted) {
            printBoard();
            appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + pits + ") : ");
        } else {
            printWelcomeMessage();
        }
    }

    private void printInfo() {
        appendToOutput("\nThis is Mancala made by https://github.com/Willie169.\n\n");
        if (gameStarted) {
            printBoard();
            appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + pits + ") : ");
        } else {
            printWelcomeMessage();
        }
    }

    private void appendToOutput(String text) {
        output.append(text);
        outputText.setText(output.toString());
    }
}