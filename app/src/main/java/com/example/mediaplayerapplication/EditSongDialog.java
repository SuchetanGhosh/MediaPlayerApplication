package com.example.mediaplayerapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class EditSongDialog extends AppCompatDialogFragment {
    EditSongDialogInterface editSongDialogInterface;

    private AlertDialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialog = builder.create();

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.edit_song_popup, null);

        EditText songName, artistName, fileName;

        songName = view.findViewById(R.id.songName);
        artistName = view.findViewById(R.id.artistName);
        fileName = view.findViewById(R.id.fileName);

        builder.setView(view).setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Editing Song", Toast.LENGTH_SHORT).show();
                String sname = songName.getText().toString().trim();
                String aname = artistName.getText().toString().trim();
                String fname = fileName.getText().toString().trim();
                if (sname.isEmpty()) {
                    sname = "EMPTY";
                }
                if (aname.isEmpty()) {
                    aname = "EMPTY";
                }
                if (fname.isEmpty()) {
                    fname = "EMPTY";
                }
                editSongDialogInterface.applyEditSong(sname, aname, fname);
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
        editSongDialogInterface = (EditSongDialogInterface) context;
    }

    public interface EditSongDialogInterface{
        void applyEditSong (String sname, String aname, String fname);
    }
}

