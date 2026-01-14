package com.example.bingoblast;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OneVsOneSetupActivity extends AppCompatActivity {

    // UI
    GridLayout gridP1, gridP2;
    TextView txtTurn, txtScoreP1, txtScoreP2, txtWin;
    Button btnRestart, btnSound;

    // Grids and marking
    Button[][] btnP1 = new Button[5][5];
    Button[][] btnP2 = new Button[5][5];

    boolean[][] markedP1 = new boolean[5][5];
    boolean[][] markedP2 = new boolean[5][5];

    boolean[] linesP1 = new boolean[12];
    boolean[] linesP2 = new boolean[12];

    // Game state
    boolean isPlayerOneTurn = true;
    boolean gameOver = false;
    int scoreP1 = 0, scoreP2 = 0;

    // Sound
    boolean isSoundOn = true;
    MediaPlayer bgMusic, winSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player); // your XML layout

        // Bind UI
        gridP1 = findViewById(R.id.gridP1);
        gridP2 = findViewById(R.id.gridP2);
        txtTurn = findViewById(R.id.txtTurn);
        txtScoreP1 = findViewById(R.id.txtScoreP1);
        txtScoreP2 = findViewById(R.id.txtScoreP2);
        txtWin = findViewById(R.id.txtWin);
        btnRestart = findViewById(R.id.btnRestart);
        btnSound = findViewById(R.id.btnSound);

        // Background music
        bgMusic = MediaPlayer.create(this, R.raw.bg_music);
        bgMusic.setLooping(true);
        bgMusic.start();

        // Winning sound
        winSound = MediaPlayer.create(this, R.raw.wining_sound);

        // Buttons
        btnRestart.setOnClickListener(v -> restartGame());
        btnSound.setOnClickListener(v -> toggleSound());

        // Create grids
        createBoard(gridP1, btnP1, markedP1);
        createBoard(gridP2, btnP2, markedP2);
        gridP2.setVisibility(View.GONE);

        updateTurnUI();
    }

    // ================= CREATE BOARD =================
    private void createBoard(GridLayout grid, Button[][] btns, boolean[][] marked) {
        grid.removeAllViews();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 25; i++) numbers.add(i);
        Collections.shuffle(numbers);

        int index = 0;
        int size = (int) (getResources().getDisplayMetrics().density * 60);

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                Button btn = new Button(this);
                btn.setText(String.valueOf(numbers.get(index++)));
                btn.setTextSize(16);
                btn.setTextColor(Color.BLACK);
                btn.setBackgroundResource(R.drawable.bingo_tile); // same as single player

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = size;
                params.height = size;
                params.setMargins(6,6,6,6);
                btn.setLayoutParams(params);

                int finalR = r, finalC = c;
                btn.setOnClickListener(v -> {
                    if (gameOver) return;

                    int number = Integer.parseInt(btn.getText().toString());
                    markNumberForBoth(number);

                    checkBingo(true);
                    checkBingo(false);

                    switchTurn();
                });

                btns[r][c] = btn;
                marked[r][c] = false;

                grid.addView(btn);
            }
        }
    }

    // ================= MARK NUMBERS =================
    private void markNumberForBoth(int number) {
        markInGrid(number, btnP1, markedP1);
        markInGrid(number, btnP2, markedP2);
    }

    private void markInGrid(int number, Button[][] btns, boolean[][] marked) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (btns[r][c].getText().toString().equals(String.valueOf(number)) && !marked[r][c]) {
                    btns[r][c].setEnabled(false);
                    btns[r][c].setBackgroundResource(R.drawable.bingo_tile_selected);
                    marked[r][c] = true;
                }
            }
        }
    }

    // ================= SWITCH TURN =================
    private void switchTurn() {
        isPlayerOneTurn = !isPlayerOneTurn;
        updateTurnUI();
    }

    private void updateTurnUI() {
        if (gameOver) return;

        if (isPlayerOneTurn) {
            txtTurn.setText("Player 1 Turn");
            gridP1.setVisibility(View.VISIBLE);
            gridP2.setVisibility(View.GONE);
        } else {
            txtTurn.setText("Player 2 Turn");
            gridP1.setVisibility(View.GONE);
            gridP2.setVisibility(View.VISIBLE);
        }
    }

    // ================= CHECK BINGO =================
    private void checkBingo(boolean isP1) {
        boolean[][] marked = isP1 ? markedP1 : markedP2;
        boolean[] lines = isP1 ? linesP1 : linesP2;

        int idx = 0;
        int total = 0;

        // Rows
        for (int r = 0; r < 5; r++) {
            boolean ok = true;
            for (int c = 0; c < 5; c++) ok &= marked[r][c];
            if (ok && !lines[idx]) lines[idx] = true;
            idx++;
        }

        // Columns
        for (int c = 0; c < 5; c++) {
            boolean ok = true;
            for (int r = 0; r < 5; r++) ok &= marked[r][c];
            if (ok && !lines[idx]) lines[idx] = true;
            idx++;
        }

        // Diagonals
        boolean ok = true;
        for (int i = 0; i < 5; i++) ok &= marked[i][i];
        if (ok && !lines[idx]) lines[idx] = true;
        idx++;

        ok = true;
        for (int i = 0; i < 5; i++) ok &= marked[i][4 - i];
        if (ok && !lines[idx]) lines[idx] = true;

        for (boolean b : lines) if (b) total++;

        if (isP1) {
            scoreP1 = total;
            txtScoreP1.setText("P1: " + scoreP1 + " / 5");
            if (scoreP1 >= 5) declareWinner("PLAYER 1");
        } else {
            scoreP2 = total;
            txtScoreP2.setText("P2: " + scoreP2 + " / 5");
            if (scoreP2 >= 5) declareWinner("PLAYER 2");
        }
    }

    // ================= WIN =================
    private void declareWinner(String player) {
        if (gameOver) return;

        gameOver = true;

        txtTurn.setText(player + " WINS");
        txtWin.setVisibility(View.VISIBLE);
        txtWin.setText("🎉 B I N G O 🎉");

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.win_anim);
        txtWin.startAnimation(anim);

        if (isSoundOn && winSound != null && !winSound.isPlaying()) winSound.start();

        // Show both grids
        gridP1.setVisibility(View.VISIBLE);
        gridP2.setVisibility(View.VISIBLE);

        disableGrid(btnP1);
        disableGrid(btnP2);
    }

    private void disableGrid(Button[][] buttons) {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                buttons[r][c].setEnabled(false);
    }

    // ================= RESTART =================
    private void restartGame() {
        txtWin.clearAnimation();
        txtWin.setVisibility(View.GONE);

        gameOver = false;
        isPlayerOneTurn = true;
        scoreP1 = 0;
        scoreP2 = 0;
        txtScoreP1.setText("P1: 0 / 5");
        txtScoreP2.setText("P2: 0 / 5");
        txtTurn.setText("Player 1 Turn");

        for (int i = 0; i < 12; i++) {
            linesP1[i] = false;
            linesP2[i] = false;
        }

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                markedP1[r][c] = false;
                markedP2[r][c] = false;
            }
        }

        if (winSound != null) {
            if (winSound.isPlaying()) winSound.stop();
            winSound.release();
            winSound = MediaPlayer.create(this, R.raw.wining_sound);
        }

        gridP1.removeAllViews();
        gridP2.removeAllViews();

        createBoard(gridP1, btnP1, markedP1);
        createBoard(gridP2, btnP2, markedP2);

        gridP1.setVisibility(View.VISIBLE);
        gridP2.setVisibility(View.GONE);
    }

    // ================= SOUND TOGGLE =================
    private void toggleSound() {
        isSoundOn = !isSoundOn;
        btnSound.setText(isSoundOn ? "🔊 Sound ON" : "🔇 Sound OFF");

        if (isSoundOn) {
            if (!bgMusic.isPlaying()) bgMusic.start();
        } else {
            if (bgMusic.isPlaying()) bgMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (winSound != null) winSound.release();
        if (bgMusic != null) {
            if (bgMusic.isPlaying()) bgMusic.stop();
            bgMusic.release();
        }
    }
}
