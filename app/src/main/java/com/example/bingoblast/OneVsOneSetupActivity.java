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
    Button btnRestart, btnSound, btnPower;

    // Power Move (OPTION 5)
    boolean powerP1Used = false;
    boolean powerP2Used = false;
    boolean powerActive = false;
    int powerClickCount = 0;

    // Grids
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

    // Animations (OPTION 4)
    Animation fadeIn, fadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player);

        // Bind UI
        gridP1 = findViewById(R.id.gridP1);
        gridP2 = findViewById(R.id.gridP2);
        txtTurn = findViewById(R.id.txtTurn);
        txtScoreP1 = findViewById(R.id.txtScoreP1);
        txtScoreP2 = findViewById(R.id.txtScoreP2);
        txtWin = findViewById(R.id.txtWin);
        btnRestart = findViewById(R.id.btnRestart);
        btnSound = findViewById(R.id.btnSound);
        btnPower = findViewById(R.id.btnPower);

        // Animations
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        // Sounds
        bgMusic = MediaPlayer.create(this, R.raw.bg_music);
        bgMusic.setLooping(true);
        bgMusic.start();

        winSound = MediaPlayer.create(this, R.raw.wining_sound);

        // Buttons
        btnRestart.setOnClickListener(v -> restartGame());
        btnSound.setOnClickListener(v -> toggleSound());

        btnPower.setOnClickListener(v -> activatePower());

        // Create boards
        createBoard(gridP1, btnP1, markedP1);
        createBoard(gridP2, btnP2, markedP2);

        gridP2.setVisibility(View.GONE);
        updateTurnUI();
    }

    // ================= POWER MOVE =================
    private void activatePower() {
        if (gameOver) return;

        if (isPlayerOneTurn && !powerP1Used) {
            powerActive = true;
        } else if (!isPlayerOneTurn && !powerP2Used) {
            powerActive = true;
        }

        if (powerActive) {
            powerClickCount = 0;
            btnPower.setText("⚡ Select 2 Numbers");
        }
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
                btn.setTextColor(Color.BLACK);
                btn.setTextSize(16);
                btn.setBackgroundResource(R.drawable.bingo_tile);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = size;
                params.height = size;
                params.setMargins(6, 6, 6, 6);
                btn.setLayoutParams(params);

                btn.setOnClickListener(v -> handleCellClick(btn));

                btns[r][c] = btn;
                marked[r][c] = false;
                grid.addView(btn);
            }
        }
    }

    // ================= CELL CLICK =================
    private void handleCellClick(Button btn) {
        if (gameOver) return;

        int number = Integer.parseInt(btn.getText().toString());
        markNumberForBoth(number);

        checkBingo(true);
        checkBingo(false);

        if (powerActive) {
            powerClickCount++;

            if (powerClickCount == 2) {
                powerActive = false;

                if (isPlayerOneTurn) powerP1Used = true;
                else powerP2Used = true;

                btnPower.setText("⚡ Used");
                btnPower.setEnabled(false);

                switchTurn();
            }
        } else {
            switchTurn();
        }
    }

    // ================= MARK =================
    private void markNumberForBoth(int number) {
        mark(number, btnP1, markedP1);
        mark(number, btnP2, markedP2);
    }

    private void mark(int number, Button[][] btns, boolean[][] marked) {
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

    // ================= TURN SWITCH =================
    private void switchTurn() {
        isPlayerOneTurn = !isPlayerOneTurn;
        updateTurnUI();
    }

    private void updateTurnUI() {
        if (gameOver) return;

        if (isPlayerOneTurn) {
            txtTurn.setText("Player 1 Turn");
            gridP2.setVisibility(View.GONE);
            gridP1.setVisibility(View.VISIBLE);
            gridP1.startAnimation(fadeIn);
        } else {
            txtTurn.setText("Player 2 Turn");
            gridP1.setVisibility(View.GONE);
            gridP2.setVisibility(View.VISIBLE);
            gridP2.startAnimation(fadeIn);
        }
    }

    // ================= CHECK BINGO =================
    private void checkBingo(boolean isP1) {
        boolean[][] marked = isP1 ? markedP1 : markedP2;
        boolean[] lines = isP1 ? linesP1 : linesP2;

        int idx = 0, total = 0;

        for (int r = 0; r < 5; r++) {
            boolean ok = true;
            for (int c = 0; c < 5; c++) ok &= marked[r][c];
            if (ok) lines[idx] = true;
            idx++;
        }

        for (int c = 0; c < 5; c++) {
            boolean ok = true;
            for (int r = 0; r < 5; r++) ok &= marked[r][c];
            if (ok) lines[idx] = true;
            idx++;
        }

        boolean ok = true;
        for (int i = 0; i < 5; i++) ok &= marked[i][i];
        if (ok) lines[idx++] = true;

        ok = true;
        for (int i = 0; i < 5; i++) ok &= marked[i][4 - i];
        if (ok) lines[idx] = true;

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
        txtWin.setText("🎉 B I N G O 🎉");
        txtWin.setVisibility(View.VISIBLE);
        txtWin.startAnimation(AnimationUtils.loadAnimation(this, R.anim.win_anim));

        if (isSoundOn) winSound.start();

        gridP1.setVisibility(View.VISIBLE);
        gridP2.setVisibility(View.VISIBLE);
    }

    // ================= RESTART =================
    private void restartGame() {
        recreate();
    }

    // ================= SOUND =================
    private void toggleSound() {
        isSoundOn = !isSoundOn;
        if (isSoundOn) bgMusic.start();
        else bgMusic.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgMusic != null) bgMusic.release();
        if (winSound != null) winSound.release();
    }
}
