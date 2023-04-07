package com.example.mediaplayerapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SongArrayAdapter extends ArrayAdapter<SongListItem> {
    FragmentManager mFragmentManager;
    public SongArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<SongListItem> songListItems, FragmentManager fragmentManager) {
        super(context, resource, songListItems);
        mFragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView==null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.song_list_item, parent, false);
        }

        SongListItem songListItem = getItem(position);

        ImageView imageView = listItemView.findViewById(R.id.threeDot);
        imageView.setImageResource(songListItem.getMenuImage());
        TextView textView1 = listItemView.findViewById(R.id.songName);
        textView1.setText(songListItem.getSongName());
        TextView textView2 = listItemView.findViewById(R.id.artist);
        textView2.setText(songListItem.getArtistName());

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getContext(), imageView);

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.button_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemID = menuItem.getItemId();
                        if (itemID == R.id.opt_edit){
                            openEditDialog(textView1);
                            return true;
                        } else if (itemID == R.id.opt_delete) {
                            deleteSong(textView1);
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });

        return listItemView;
    }

    private void deleteSong(TextView textView) {
        String song_name = textView.getText().toString();
        FirebaseDatabase.getInstance().getReference().child("Users").child("DELETE SONG").setValue(song_name);
    }


    public void openEditDialog(TextView textView){
        String song_name = textView.getText().toString();
        System.out.println("This is old name : " +  song_name);
        FirebaseDatabase.getInstance().getReference().child("Users").child("UPDATE SONG").child("OLD").setValue(song_name);
        EditSongDialog editSongDialog = new EditSongDialog();
        editSongDialog.show(mFragmentManager, "Edit Playlist Dialog");
    }
}
