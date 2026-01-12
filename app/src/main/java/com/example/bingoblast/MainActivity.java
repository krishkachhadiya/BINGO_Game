package com.example.bingoblast;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnSingle, btnOneVsOne;
    private MediaPlayer bgMusic;
    private boolean soundOn = true; // default ON

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔹 Find buttons
        btnSingle = findViewById(R.id.btnSingle);
        btnOneVsOne = findViewById(R.id.btnOneVsOne);

        // 🔊 Initialize background music
        bgMusic = MediaPlayer.create(this, R.raw.bg_music); // your bg music file
        bgMusic.setLooping(true);
        bgMusic.start();

        // 🎮 Button click listeners
        btnSingle.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SinglePlayerActivity.class);
            startActivity(i);
        });

        btnOneVsOne.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, OneVsOneSetupActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop bg music when leaving this activity
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume music if sound is ON
        if (soundOn && bgMusic != null) {
            bgMusic.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
    }
}
