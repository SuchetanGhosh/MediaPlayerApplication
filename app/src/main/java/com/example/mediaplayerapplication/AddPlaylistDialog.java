package com.example.mediaplayerapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AddPlaylistDialog extends AppCompatDialogFragment{
    PlaylistDialogInterface playlistDialogInterface;

    private AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialog = builder.create();

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.add_playlist_popup, null);

        EditText playlistName = view.findViewById(R.id.playlistName);

        builder.setView(view).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Adding New Playlist", Toast.LENGTH_SHORT).show();
                String pname = playlistName.getText().toString().trim();
                playlistDialogInterface.apply(pname);
//                startActivity(new Intent(getContext(), SongListActivity.class));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        playlistDialogInterface = (PlaylistDialogInterface) context;
    }

    public interface PlaylistDialogInterface{
        void apply (String pname);
    }
}
