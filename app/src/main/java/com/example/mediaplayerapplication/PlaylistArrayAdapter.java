package com.example.mediaplayerapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class PlaylistArrayAdapter extends ArrayAdapter<PlaylistListItem>{
    private FragmentManager mFragmentManager;
    public PlaylistArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<PlaylistListItem> playlistListItems, FragmentManager fragmentManager) {
        super(context, resource, playlistListItems);
        mFragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView==null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.playlist_list_item, parent, false);
        }

        PlaylistListItem playlistListItem = getItem(position);

        ImageView imageView = listItemView.findViewById(R.id.imbPopUp);
        imageView.setImageResource(playlistListItem.getMenuImage());
        TextView textView = listItemView.findViewById(R.id.playlistName);
        textView.setText(playlistListItem.getPlaylistName());

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
                            openEditDialog(textView);
                            return true;
                        } else if (itemID == R.id.opt_delete) {
                            deletePlaylist(textView);
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

    private void deletePlaylist(TextView textView) {
        String playlist_name = textView.getText().toString();
        FirebaseDatabase.getInstance().getReference().child("Users").child("DELETE PLAYLIST").setValue(playlist_name);
    }


    public void openEditDialog(TextView textView){
        String playlist_name = textView.getText().toString();
        System.out.println("This is old name : " +  playlist_name);
        FirebaseDatabase.getInstance().getReference().child("Users").child("UPDATE PLAYLIST").child("OLD").setValue(playlist_name);
        EditPlaylistDialog editPlaylistDialog = new EditPlaylistDialog();
        editPlaylistDialog.show(mFragmentManager, "Edit Playlist Dialog");
    }
}
