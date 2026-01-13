package com.example.bingoblast;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SinglePlayerActivity extends AppCompatActivity {

    // UI
    private GridLayout bingoGrid;
    private TextView txtStatus;
    private Button btnRestart, btnSound;

    // Game data
    private Button[][] buttons = new Button[5][5];
    private boolean[][] marked = new boolean[5][5];
    private boolean[] lineCounted = new boolean[12];

    private int totalLines = 0;
    private boolean gameWon = false;
    private boolean soundOn = true;

    // Number pool
    private List<Integer> numberPool;

    // Sound
    private MediaPlayer winMusic;
    private MediaPlayer bgMusic;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_player);

        initViews();
        initSound();
        initButtons();
        initNumberPool();
        createBoard();
        startBackgroundMusic();
    }

    // 🔹 Initialize Views
    private void initViews() {
        bingoGrid = findViewById(R.id.bingoGrid);
        txtStatus = findViewById(R.id.txtStatus);
        btnRestart = findViewById(R.id.btnRestart);
        btnSound = findViewById(R.id.btnSound);
    }

    // 🔊 Initialize Sound
    private void initSound() {
        winMusic = MediaPlayer.create(this, R.raw.wining_sound);
        bgMusic = MediaPlayer.create(this, R.raw.bg_music); // your background music file
        bgMusic.setLooping(true);
    }

    // 🎮 Buttons Logic
    private void initButtons() {
        btnRestart.setOnClickListener(v -> restartGame());

        btnSound.setOnClickListener(v -> {
            soundOn = !soundOn;
            btnSound.setText(soundOn ? "🔊 Sound ON" : "🔇 Sound OFF");
            toggleBackgroundMusic();
        });
    }

    // 🔢 Initialize Number Pool for auto-selection
    private void initNumberPool() {
        numberPool = new ArrayList<>();
        for (int i = 1; i <= 25; i++) numberPool.add(i);
        Collections.shuffle(numberPool);
    }

    // 🎲 Create Bingo Board
    private void createBoard() {
        bingoGrid.removeAllViews();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 25; i++) numbers.add(i);
        Collections.shuffle(numbers);

        int index = 0;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {

                Button btn = new Button(this);
                btn.setText(String.valueOf(numbers.get(index++)));
                btn.setTextSize(20);
                btn.setTextColor(Color.BLACK);
                btn.setAllCaps(false);
                btn.setBackgroundResource(R.drawable.bingo_tile);
                btn.setElevation(10f);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 160;
                params.height = 160;
                params.setMargins(8, 8, 8, 8);
                btn.setLayoutParams(params);

                int row = r, col = c;
                btn.setOnClickListener(v -> onPlayerSelect(btn, row, col));

                buttons[r][c] = btn;
                bingoGrid.addView(btn);
            }
        }
    }

    // 🟩 Player selects a number
    private void onPlayerSelect(Button btn, int r, int c) {
        if (gameWon || marked[r][c]) return;

        // Mark player's selected number
        markCell(r, c);

    }

    // 🔁 Mark a cell
    private void markCell(int r, int c) {
        if (!marked[r][c]) {
            marked[r][c] = true;
            buttons[r][c].setEnabled(false);
            buttons[r][c].setBackgroundResource(R.drawable.bingo_tile_selected);

            checkBingo();
        }
    }



    // 🧠 Check Bingo Lines
    private void checkBingo() {
        int idx = 0;
        totalLines = 0;

        // Rows
        for (int r = 0; r < 5; r++) {
            if (isRowComplete(r) && !lineCounted[idx]) lineCounted[idx] = true;
            idx++;
        }

        // Columns
        for (int c = 0; c < 5; c++) {
            if (isColumnComplete(c) && !lineCounted[idx]) lineCounted[idx] = true;
            idx++;
        }

        // Diagonals
        if (isDiagonal1Complete() && !lineCounted[idx]) lineCounted[idx] = true;
        idx++;
        if (isDiagonal2Complete() && !lineCounted[idx]) lineCounted[idx] = true;

        for (boolean b : lineCounted) if (b) totalLines++;

        txtStatus.setText("Lines: " + totalLines + " / 5");

        if (totalLines >= 5 && !gameWon) {
            gameWon = true;
            showBingoAnimation();
        }
    }

    // ✔ Helpers
    private boolean isRowComplete(int r) {
        for (int c = 0; c < 5; c++) if (!marked[r][c]) return false;
        return true;
    }

    private boolean isColumnComplete(int c) {
        for (int r = 0; r < 5; r++) if (!marked[r][c]) return false;
        return true;
    }

    private boolean isDiagonal1Complete() {
        for (int i = 0; i < 5; i++) if (!marked[i][i]) return false;
        return true;
    }

    private boolean isDiagonal2Complete() {
        for (int i = 0; i < 5; i++) if (!marked[i][4 - i]) return false;
        return true;
    }

    // 🅱️ B I N G O Animation + Sound
    private void showBingoAnimation() {
        txtStatus.setText("");
        String word = "BINGO";

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.bingo_anim);

        for (int i = 0; i < word.length(); i++) {
            int index = i;
            handler.postDelayed(() -> {
                txtStatus.append(word.charAt(index) + " ");
                txtStatus.startAnimation(anim);
            }, i * 350);
        }

        if (soundOn && winMusic != null) winMusic.start();

        glowGrid();
    }

    // 🌟 Glow Effect
    private void glowGrid() {
        for (Button[] row : buttons) {
            for (Button b : row) {
                b.animate()
                        .scaleX(1.05f)
                        .scaleY(1.05f)
                        .alpha(0.85f)
                        .setDuration(300)
                        .start();
            }
        }
    }

    // 🔄 Restart Game
    private void restartGame() {
        totalLines = 0;
        gameWon = false;

        numberPool.clear();
        for (int i = 1; i <= 25; i++) numberPool.add(i);
        Collections.shuffle(numberPool);

        for (int i = 0; i < 12; i++) lineCounted[i] = false;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                marked[r][c] = false;

        txtStatus.setText("Lines: 0 / 5");

        createBoard();

        // Restart background music if sound is on
        if (soundOn) startBackgroundMusic();
    }

    // 🔊 Background music
    private void startBackgroundMusic() {
        if (bgMusic != null) {
            if (!bgMusic.isPlaying()) bgMusic.start();
        }
    }

    private void toggleBackgroundMusic() {
        if (bgMusic == null) return;

        if (soundOn) {
            if (!bgMusic.isPlaying()) bgMusic.start();
        } else {
            if (bgMusic.isPlaying()) bgMusic.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (winMusic != null) winMusic.release();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.release();
        }
    }
}
