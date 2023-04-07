package com.example.mediaplayerapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SongListActivity extends AppCompatActivity implements AddSongDialog.SongDialogInterface, EditSongDialog.EditSongDialogInterface{
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    Gson gson = new Gson();
    Toolbar toolbar;
    private int songCount, songNum;
    private String playlist, username, old_sname = "", delete_sname="";
    private String new_sname = "", new_aname = "", new_fname = "";
    private ArrayList<String> SongNames, FileNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        mAuth = FirebaseAuth.getInstance();
        ListView displayList = findViewById(R.id.displayList);
        playlist = getIntent().getStringExtra("playlist_name");
        username = getIntent().getStringExtra("username");


        Intent playSongIntent = new Intent(SongListActivity.this, PlaySongActivity.class);
        SongNames = new ArrayList<>();
        FileNames = new ArrayList<>();


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(playlist);
        }


        ArrayList<SongListItem> list = new ArrayList<>();
        SongArrayAdapter arrayAdapter = new SongArrayAdapter(this, 0, list, getSupportFragmentManager());
        displayList.setAdapter(arrayAdapter);

        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    if (Objects.equals(dataSnapshot.getKey(), "UPDATE SONG")){
                        FirebaseDatabase.getInstance().getReference().child("Users").child("UPDATE SONG").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshots) {
                                for (DataSnapshot SNAPSHOTS : snapshots.getChildren()){
                                    if (Objects.equals(SNAPSHOTS.getKey(), "OLD")){
                                        old_sname = SNAPSHOTS.getValue(String.class);
                                    } else if (Objects.equals(SNAPSHOTS.getKey(), "NewSongName")){
                                        new_sname = SNAPSHOTS.getValue(String.class);
                                    } else if (Objects.equals(SNAPSHOTS.getKey(), "NewArtistName")) {
                                        new_aname = SNAPSHOTS.getValue(String.class);
                                    } else if (Objects.equals(SNAPSHOTS.getKey(), "NewFileName")) {
                                        new_fname = SNAPSHOTS.getValue(String.class);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        FirebaseDatabase.getInstance().getReference().child("Users").child("UPDATE SONG").removeValue();
                    } else if (Objects.equals(dataSnapshot.getKey(), "DELETE SONG")) {
                        delete_sname = dataSnapshot.getValue(String.class);
                        FirebaseDatabase.getInstance().getReference().child("Users").child("DELETE SONG").removeValue();
                    }
                }


                FirebaseDatabase.getInstance().getReference().child(username).child(playlist).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            String key = dataSnapshot.getKey();
                            for (DataSnapshot SNAPSHOTS : dataSnapshot.getChildren()){
                                if (Objects.equals(SNAPSHOTS.getKey(), "SongName") && Objects.equals(SNAPSHOTS.getValue(String.class), old_sname)){
                                    if (!Objects.equals(new_sname, "EMPTY")){
                                        FirebaseDatabase.getInstance().getReference().child(username).child(playlist).child(key).child("SongName").setValue(new_sname);
                                    }
                                    if (!Objects.equals(new_aname, "EMPTY")) {
                                        FirebaseDatabase.getInstance().getReference().child(username).child(playlist).child(key).child("ArtistName").setValue(new_aname);
                                    }
                                    if (!Objects.equals(new_fname, "EMPTY")) {
                                        FirebaseDatabase.getInstance().getReference().child(username).child(playlist).child(key).child("FileName").setValue(new_fname);
                                    }

                                } else if (Objects.equals(SNAPSHOTS.getKey(), "SongName") && Objects.equals(SNAPSHOTS.getValue(String.class), delete_sname)) {
                                    FirebaseDatabase.getInstance().getReference().child(username).child(playlist).child(key).removeValue();

                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                old_sname = "";
                new_sname = "";
                new_aname = "";
                new_fname = "";
                delete_sname = "";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FileNames.clear();
        displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                songNum = 1;
                SongListItem songListItem = list.get(position);
                playSongIntent.putExtra("song_name", songListItem.getSongName());
                playSongIntent.putExtra("playlist_name", playlist);
                playSongIntent.putExtra("user_name", username);
                FirebaseDatabase.getInstance().getReference().child(username).child(playlist).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Song_Object song_object = convertToSongObject(dataSnapshot);
                            SongNames.add(song_object.getSongName());
                            FileNames.add(song_object.getFileName());
                        }
                        playSongIntent.putExtra("song_names", SongNames);
                        playSongIntent.putExtra("file_names", FileNames);

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Song_Object song = convertToSongObject(dataSnapshot);
                            if (Objects.equals(song.getSongName(), songListItem.getSongName())){
                                playSongIntent.putExtra("file_name", song.getFileName());
                                playSongIntent.putExtra("song_num", songNum);
                                playSongIntent.putExtra("song_count", songCount);
                                songNum = 1;
                                startActivity(playSongIntent);
                            } else {
                                songNum += 1;
                            }
                            System.out.println("Song Number = " + songNum);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        ref = FirebaseDatabase.getInstance().getReference().child(username).child(playlist);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && !snapshot.hasChild("DEFAULT SONG")){
                    songCount = (int) snapshot.getChildrenCount();
                } else {
                    songCount = 0;
                }
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    String key = dataSnapshot.getKey();

                    if (Objects.equals(key, "DEFAULT SONG")){
                        openDialog(findViewById(R.id.songLayout));
                        if (songCount == 0){
                            removeDefault(true);
                        } else {
                            startActivity(new Intent(SongListActivity.this, HomePageActivity.class));
                        }

                    } else {
                        Song_Object song = convertToSongObject(dataSnapshot);
                        String songName = song.getSongName();
                        String artistName = song.getArtistName();
                        list.add(new SongListItem(songName, artistName, R.drawable.icon_menu_dot_2));
                    }
                }
//                System.out.println(list);
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removeDefault(boolean b) {
        if (b){
            ref.child("DEFAULT SONG").removeValue();
        }
    }

    private Song_Object convertToSongObject(DataSnapshot dataSnapshot) {
        Song_Object object = dataSnapshot.getValue(Song_Object.class);
        String json = gson.toJson(object);
        return gson.fromJson(json, Song_Object.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.opt_menu_song, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.opt_add) {
            openDialog(findViewById(R.id.songLayout));
        } else if (itemID == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDialog(View view){
        AddSongDialog addSongDialog = new AddSongDialog();
        addSongDialog.show(getSupportFragmentManager(), "Test Custom Dialog");
    }

    @Override
    public void apply(String sname, String aname, String fname) {
        System.out.println(sname);
        System.out.println(aname);
        System.out.println(fname);
        System.out.println(songCount);
        String newSongCount = "Song " + Integer.toString(songCount+1);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(username).child(playlist).child(newSongCount);
        ref.child("SongName").setValue(sname);
        ref.child("ArtistName").setValue(aname);
        ref.child("FileName").setValue(fname);
    }

    @Override
    public void applyEditSong(String sname, String aname, String fname) {
        HashMap<String, Object> hashMap = new HashMap<>();
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference().child("Users").child("UPDATE SONG");

        hashMap.put("NewSongName", sname);
        hashMap.put("NewArtistName", aname);
        hashMap.put("NewFileName", fname);
        updateRef.updateChildren(hashMap);

        System.out.println("This is new song name " + sname);
        System.out.println("This is new artist name " + aname);
        System.out.println("This is new file name " + fname);

    }
}