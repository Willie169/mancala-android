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
    private TextView outputText;
    private EditText inputText;
    private static final int MAX_SEEDS = 999;
    private int numberOfPits, seedsPerPit, totalPits, pit;
    private byte status = 0;
    private byte pre = 0;
    private byte gameOver = 0;
    private byte player = 0;
    private List<Integer> board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputText = findViewById(R.id.outputText);
        inputText = findViewById(R.id.inputText);

        appendToOutput("Type 'rule' to view the rules, 'info' to view the information, or 'exit' to quit and optionally start another game anytime.\n\n");
        appendToOutput("Enter the number of pits each player has: ");

        inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                handleInput(inputText.getText().toString().replaceAll("\\s+$", ""));
                inputText.setText("");
                return true;
            }
            return false;
        });
    }

    private void appendToOutput(String text) {
        outputText.append(text);
    }

    private void handleInput(String input) {
        if (input == "") {
            appendToOutput("\n");
            return;
        }

        if (status == -1) {
            if (handleExit(input)) {
                status = 3;
            } else {
                status = pre;
                reOutput();
            }
            return;
        }

        if (status == 0) {
            appendToOutput("\n" + input + "\n");
            if (preParse(input)) return;
            try {
                numberOfPits = Integer.parseInt(input);
                if (numberOfPits > 0) {
                    status = 1;
                    appendToOutput("Enter the number of seeds per pit: ");
                } else appendToOutput("Invalid input. Please enter a positive integer.\n");
            } catch (NumberFormatException e) {
                appendToOutput("Invalid input. Please enter a positive integer.\n");
            }
            return;
        }

        if (status == 1) {
            appendToOutput("\n" + input + "\n");
            if (preParse(input)) return;
            try {
                seedsPerPit = Integer.parseInt(input);
                if (seedsPerPit > 0 && seedsPerPit * numberOfPits <= MAX_SEEDS) {
                    status = 2;
                    preGame();
                }
                else appendToOutput("Invalid input. Please enter a positive integer between 1 and " + MAX_SEEDS / numberOfPits + ".\n");
            } catch (NumberFormatException e) {
                appendToOutput("Invalid input. Please enter a positive integer between 1 and " + MAX_SEEDS / numberOfPits + ".\n");
            }
            return;
        }

        if (status == 2) {
            appendToOutput("\n" + input + "\n");
            if (preParse(input)) return;
            // Player makes a move
            try {
                pit = Integer.parseInt(input) - 1; // Adjust for 0-indexing
                if (pit < 0 || pit >= numberOfPits) {
                    appendToOutput("Invalid pit number. Try again.\n");
                    return;
                }

                byte anotherTurn = makeMove();
                gameOver = checkGameOver();          

                printBoard();
                if (gameOver == 1) {
                    endGame();
                    appendToOutput("Do you want to start another game? (y/n): ");
                    status = 3;
                    return;
                } else if (anotherTurn == 0) {
                    player = (byte)((player + 1) % 2);  // Switch players if the last seed didn't land in the player's store
                }
                appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + numberOfPits + ") : ");
            } catch (NumberFormatException e) {
                appendToOutput("Invalid input. Please enter a pit number, 'exit', or 'rule'.\n");
            }
            return;
        }

        if (status == 3) {
            appendToOutput(input + "\n");
            if (input.equalsIgnoreCase("y")) {
                appendToOutput("\nNew game started:\n");
                appendToOutput("Enter the number of pits each player has: ");
                status = 0;
                return;
            } else if (input.equalsIgnoreCase("n")) finish();
            else {
                appendToOutput("\nDo you want to start another game? (y/n): ");
                return;
            }
        }
    }

    private boolean preParse(String input) {
        if (input.equals("exit")) {
            appendToOutput("Are you sure you want to quit? (y/n): ");
            pre = status;
            status = -1;
            return true;
        } else if (input.equals("rule")) {
            printRules();
            reOutput();
            return true;
        } else if (input.equals("info")) {
            printInfo();
            reOutput();
            return true;
        } else {
            return false;
        }
    }

    private void reOutput() {
        if (status == 0) appendToOutput("Enter the number of pits each player has: ");
        else if (status == 1) appendToOutput("Enter the number of seeds per pit: ");
        else if (status == 2) {
            printBoard();
            appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + numberOfPits + ") : ");
        }
    }

    private void endGame() {
        // Determine the winner
        int player1Store = board.get(numberOfPits);
        int player2Store = board.get(totalPits - 1);

        if (player1Store > player2Store) {
            appendToOutput("Player 1 wins with " + player1Store + " seeds!\n\n");
        } else if (player2Store > player1Store) {
            appendToOutput("Player 2 wins with " + player2Store + " seeds!\n\n");
        } else {
            appendToOutput("It's a tie!\n\n");
        }
        return;
    }

    private void preGame() {
        totalPits = numberOfPits * 2 + 2; // 2 stores plus the pits
        board = new ArrayList<>(totalPits);
        for (int i = 0; i < totalPits; i++) {
            board.add(seedsPerPit);
        }
        board.set(numberOfPits, 0); // Player 1's store
        board.set(totalPits - 1, 0); // Player 2's store

        // Explain the board setup
        appendToOutput("\nGame setup:\n");
        appendToOutput("  - Player 1's pits start on the left side of the board.\n");
        appendToOutput("  - Player 2's pits start on the right side of the board.\n");
        appendToOutput("  - Player 1's store is at the right end of the board.\n");
        appendToOutput("  - Player 2's store is at the left end of the board.\n");
        appendToOutput("  - The pits and stores are arranged in a line, with Player 1's pits followed by Player 1's store, then Player 2's pits and Player 2's store.\n");
        appendToOutput("  - Each player has " + numberOfPits + " pits and " + seedsPerPit + " seeds in each pit initially.\n");
        appendToOutput("Type 'rule' to view the rules, 'info' to view the information, or 'exit' to quit and optionally start another game anytime.\n\n");
        printBoard();
        appendToOutput("Player " + (player + 1) + ", choose a pit (1-" + numberOfPits + ") : ");
    }

    private void printBoard() {
        appendToOutput("\nPlayer 2\n");
        appendToOutput(" ");
        for (int i = totalPits - 2; i > numberOfPits; i--) {
            appendToOutput(" " + board.get(i) + " ");
        }
        appendToOutput("\n " + board.get(totalPits - 1));
        for (int i = 0; i < numberOfPits + 1; i++) {
            appendToOutput("  ");
        }
        appendToOutput("   " + board.get(numberOfPits) + "\n ");
        for (int i = 0; i < numberOfPits; i++) {
            appendToOutput(" " + board.get(i) + " ");
        }
        appendToOutput("\nPlayer 1\n\n");
    }

    private byte makeMove() {
        int start = player == 0 ? 0 : numberOfPits + 1;
        int pos = start + pit;
        int seeds = board.get(pos);

        if (seeds == 0) return 1; // Type move again if the pit is empty

        board.set(pos, 0);

        while (seeds > 0) {
            pos = (pos + 1) % totalPits;
            if (pos == (player == 0 ? totalPits - 1 : numberOfPits)) continue; // Skip opponent's store
            board.set(pos, board.get(pos) + 1);
            seeds--;
        }

        // Check for capture
        if (pos != numberOfPits && pos != totalPits - 1) {  // Last seed not in a store
            if (player == pos / (numberOfPits + 1) && board.get(pos) == 1) {  // Last seed in player's empty pit
                int opposite = totalPits - 2 - pos;
                if (board.get(opposite) > 0) {
                    board.set(player == 0 ? numberOfPits : totalPits - 1, board.get(player == 0 ? numberOfPits : totalPits - 1) + board.get(opposite) + 1);
                    board.set(opposite, 0);
                    board.set(pos, 0);
                }
            }
        }

        // Return 1 if the last seed was placed in the player's store
        return (byte)((player == 0 && pos == numberOfPits) || (player == 1 && pos == totalPits - 1) ? 1 : 0);
    }

    private byte checkGameOver() {
        int sum1 = 0, sum2 = 0;
        for (int i = 0; i < numberOfPits; i++) {
            sum1 += board.get(i);
            sum2 += board.get(numberOfPits + 1 + i);
        }

        if (sum2 == 0) {
            for (int i = 0; i < numberOfPits; i++) {
                board.set(numberOfPits, board.get(numberOfPits) + board.get(i));
                board.set(i, 0);
            }
            return 1;
        }

        if (sum1 == 0) {
            for (int i = numberOfPits + 1; i < totalPits - 1; i++) {
                board.set(totalPits - 1, board.get(totalPits - 1) + board.get(i));
                board.set(i, 0);
            }
            return 1;
        }

        return 0;
    }

    private boolean handleExit(String choice) {
        if (choice.substring(0, 1).equalsIgnoreCase("y")) {
            appendToOutput("Exiting the game...\n");
            appendToOutput("\nDo you want to start another game? (y/n): ");
            return true;
        } else {
            appendToOutput("Cancelled.\n");
            return false;
        }
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
        appendToOutput("Type 'rule' to view the rules, 'info' to view the information, or 'exit' to quit and optionally start another game anytime.\n\n");
    }

    private void printInfo() {
        appendToOutput("\nThis is Mancala made by `https://github.com/Willie169`.\n\n");
    }
}
