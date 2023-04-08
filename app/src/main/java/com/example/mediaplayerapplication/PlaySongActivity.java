package com.example.mediaplayerapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PlaySongActivity extends AppCompatActivity {

    MediaPlayer song;
    private static final long RELEASE_DELAY_MS = TimeUnit.SECONDS.toMillis(10);
    private CountDownTimer releaseTimer;
    private String songName, playlistName, userName, fileName, packageName;
    private Integer songNo, songCount, fileIndex;
    private TextView currentTime, totalTime;
    private SeekBar seekBar;
    private ImageView btnMain, btnPrevious, btnNext;
    private ArrayList<String> songNames, fileNames;
    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        songNo = getIntent().getIntExtra("song_num", 0);
        songCount = getIntent().getIntExtra("song_count", 0);
        songName = getIntent().getStringExtra("song_name");
        playlistName = getIntent().getStringExtra("playlist_name");
        userName = getIntent().getStringExtra("user_name");
        fileName = getIntent().getStringExtra("file_name");

        fileIndex = songNo-1;
        songNames = (ArrayList<String>) getIntent().getSerializableExtra("song_names");
        fileNames = (ArrayList<String>) getIntent().getSerializableExtra("file_names");

        currentTime = findViewById(R.id.currentTime);
        totalTime = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.seekBar);

        btnMain = findViewById(R.id.btnPlayPause);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(songName);
        }


        if (song != null) {
            song.stop();
            song.release();
            song = null;
            cancelReleaseTimer();
        }

        packageName = getPackageName();
        int resID = getResources().getIdentifier(fileName, "raw", packageName);
        song = MediaPlayer.create(getApplicationContext(), resID);
        totalTime.setText(convertToMMSS(String.valueOf(song.getDuration())));

        seekBar.setProgress(0);
        seekBar.setMax(song.getDuration());

        btnMain.setImageResource(R.drawable.baseline_pause_24);
        song.start();
        song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playSong(1);
            }
        });

        PlaySongActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (song != null){
                    seekBar.setProgress(song.getCurrentPosition());
                    currentTime.setText(convertToMMSS(String.valueOf(song.getCurrentPosition())));
                }
                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (song != null && fromUser){
                    song.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (song.isPlaying()){
                    btnMain.setImageResource(R.drawable.baseline_play_arrow_24);
                    song.pause();
                } else {
                    song.start();
                    btnMain.setImageResource(R.drawable.baseline_pause_24);
                }
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songNo < songCount){
                    System.out.println("Next clicked");
                    playSong(1);
                } else {
                    Toast.makeText(PlaySongActivity.this, "There are no songs after this", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (songNo > 1){
                    System.out.println("Previous clicked");
                    playSong(-1);
                } else {
                    Toast.makeText(PlaySongActivity.this, "There are no songs before this", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void playSong(int i) {
        if (songNo >= 1 && songNo <= songCount) {

            if (i == 1){
                songNo += 1;
                fileIndex += 1;
            } else if (i == -1){
                songNo -= 1;
                fileIndex -= 1;
            }

            if (song.isPlaying()){
                song.stop();
            } else {
                btnMain.setImageResource(R.drawable.baseline_pause_24);
            }
            song.reset();
            song.release();
            song = null;
            cancelReleaseTimer();

            try{
                if (getSupportActionBar() != null){
                    getSupportActionBar().setTitle(songNames.get(fileIndex));
                }

                int resID = getResources().getIdentifier(fileNames.get(fileIndex), "raw", packageName);
                song = MediaPlayer.create(getApplicationContext(), resID);
                song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if (Objects.equals(songNo, songCount)) {
                            startReleaseTimer();
                        } else {
                            playSong(1);
                        }
                    }
                });
                seekBar.setProgress(0);
                seekBar.setMax(song.getDuration());

                song.start();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if (songCount == 1){
            song.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    startReleaseTimer();
                }
            });
        }
    }

    public static String convertToMMSS(String duration){
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private Song_Object convertToSongObject(DataSnapshot dataSnapshot) {
        Song_Object object = dataSnapshot.getValue(Song_Object.class);
        String json = gson.toJson(object);
        return gson.fromJson(json, Song_Object.class);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.general_back, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    private void startReleaseTimer() {
        cancelReleaseTimer();
        releaseTimer = new CountDownTimer(RELEASE_DELAY_MS, RELEASE_DELAY_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                if (song != null && !song.isPlaying()) {
                    song.release();
                    song = null;
                    Toast.makeText(PlaySongActivity.this, "Media player resources released.", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void cancelReleaseTimer() {
        if (releaseTimer != null) {
            releaseTimer.cancel();
            releaseTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (song != null) {
            song.release();
            song = null;
        }
        cancelReleaseTimer();
    }
}