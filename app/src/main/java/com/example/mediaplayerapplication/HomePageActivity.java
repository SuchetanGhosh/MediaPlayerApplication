package com.example.mediaplayerapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class HomePageActivity extends AppCompatActivity implements AddPlaylistDialog.PlaylistDialogInterface, EditPlaylistDialog.EditPlaylistDialogInterface{
    private FirebaseAuth mAuth;
//    private Boolean newUser, newPlaylist;
    private String emailID, password;
    private int playlistCount;
    private String user = "";
    private String old_pname = "";
    private String new_pname = "";
    private String delete_pname = "";
    Toolbar toolbar;
//    final Intent newPlaylistIntent = new Intent(HomePageActivity.this, SongListActivity.class);

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser==null){
            startActivity(new Intent(HomePageActivity.this, LoginActivity.class));
//            newUser = true;
            emailID = getIntent().getStringExtra("emailID");
        } else {
//            newUser = false;
            emailID = currentUser.getEmail();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        mAuth = FirebaseAuth.getInstance();
        ListView displayList = findViewById(R.id.displayList);
        toolbar = findViewById(R.id.toolbar);
        password = getIntent().getStringExtra("password");

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        ArrayList<PlaylistListItem> list = new ArrayList<>();
        PlaylistArrayAdapter arrayAdapter = new PlaylistArrayAdapter(this, 0, list, getSupportFragmentManager());
        displayList.setAdapter(arrayAdapter);
        Intent intent = new Intent(HomePageActivity.this, SongListActivity.class);


        displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlaylistListItem playlistListItem = list.get(position);
                intent.putExtra("playlist_name", playlistListItem.getPlaylistName());
                startActivity(intent);
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (Objects.equals(dataSnapshot.getKey(), "UPDATE PLAYLIST")) {
                        ref.child("UPDATE PLAYLIST").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshots) {
                                for (DataSnapshot SNAPSHOTS : snapshots.getChildren()) {
                                    if (Objects.equals(SNAPSHOTS.getKey(), "OLD")){
                                        old_pname = SNAPSHOTS.getValue(String.class);
                                    } else {
                                        new_pname = SNAPSHOTS.getValue(String.class);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ref.child("UPDATE PLAYLIST").removeValue();
                    } else if (Objects.equals(dataSnapshot.getKey(), "DELETE PLAYLIST")) {
                        delete_pname = dataSnapshot.getValue(String.class);
                        ref.child("DELETE PLAYLIST").removeValue();
                    } else if (Objects.equals(dataSnapshot.getKey(), "UPDATE SONG") || Objects.equals(dataSnapshot.getKey(), "DELETE SONG")) {
                        continue;
                    } else if (Objects.equals(dataSnapshot.getValue(String.class), emailID)) {
                        user = dataSnapshot.getKey();
                        intent.putExtra("username", user);
                        if (getSupportActionBar() != null){
                            getSupportActionBar().setTitle("Welcome " + user);
                        }
                    }
                }

                DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference().child(user);

                currentUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            playlistCount = (int) snapshot.getChildrenCount();
                        } else {
                            playlistCount = 0;
                        }
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            if (!old_pname.isEmpty() && !new_pname.isEmpty()){
                                copyRecord(currentUser.child(old_pname), currentUser.child(new_pname));
                                currentUser.child(old_pname).removeValue();
                                old_pname = "";
                                new_pname = "";
                            } else if (!delete_pname.isEmpty()) {
                                currentUser.child(delete_pname).removeValue();
                                delete_pname = "";
                            }

                            String string = dataSnapshot.getKey();
                            list.add(new PlaylistListItem(string, R.drawable.icon_menu_dot_2));
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(HomePageActivity.this, LoginActivity.class));
    }

    public void deleteAccount(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(emailID, password);

        currentUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(user).removeValue();
        ref.child(user).removeValue();
        startActivity(new Intent(HomePageActivity.this, LoginActivity.class));
    }

    private void copyRecord(DatabaseReference fromPath, final DatabaseReference toPath) {
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                toPath.setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            Toast.makeText(getApplicationContext(), "Playlist name changed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to change playlist name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage());
            }
        };
        fromPath.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.opt_menu_playlist, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == R.id.opt_logout){
            Toast.makeText(this, "Logging Out", Toast.LENGTH_SHORT).show();
            logout();
        } else if (itemID == R.id.opt_add) {
            openAddDialog(findViewById(R.id.mainLayout));
//            startActivity(newPlaylistIntent);
//            Toast.makeText(this, "Adding New Playlist", Toast.LENGTH_SHORT).show();
        } else if (itemID == R.id.opt_delete) {
            deleteAccount();
        }

        return super.onOptionsItemSelected(item);
    }

    public void openAddDialog(View view){
        AddPlaylistDialog addPlaylistDialog = new AddPlaylistDialog();
        addPlaylistDialog.show(getSupportFragmentManager(), "Add Playlist Dialog");
    }

    @Override
    public void apply(String pname) {
        System.out.println(pname);
        System.out.println(playlistCount);

//        Intent newPlaylistIntent = new Intent(HomePageActivity.this, SongListActivity.class);
//        newPlaylistIntent.putExtra("new_playlist", "yes");
        DatabaseReference defaultS = FirebaseDatabase.getInstance().getReference().child(user).child(pname).child("DEFAULT SONG");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("SongName", "default");
        hashMap.put("ArtistName", "default");
        hashMap.put("FileName", "default");
        defaultS.updateChildren(hashMap);
    }

    @Override
    public void applyEdit(String pname) {
        System.out.println(pname);
        FirebaseDatabase.getInstance().getReference().child("Users").child("UPDATE PLAYLIST").child("NEW").setValue(pname);
    }
}