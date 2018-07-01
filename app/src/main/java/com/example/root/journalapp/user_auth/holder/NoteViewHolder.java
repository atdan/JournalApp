package com.example.root.journalapp.user_auth.holder;

import android.content.Context;
import android.provider.SyncStateContract;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.root.journalapp.R;
import com.example.root.journalapp.user_auth.model.NoteModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    View view;
    TextView textTitle, textTime;
    Context context;
    public NoteViewHolder(View itemView) {
        super(itemView);

        context = itemView.getContext();
        itemView.setOnClickListener(this);

        view = itemView;
        textTime = view.findViewById(R.id.noteItemTime);
        textTitle = view.findViewById(R.id.noteItemTitle);

    }
    public void setNoteTitle(String title){

        textTitle.setText(title);
    }
    public void setNoteTime(String time){

        textTime.setText(time);
    }

    public void bindNotes(NoteModel noteModel){

        textTime = view.findViewById(R.id.noteItemTime);
        textTitle = view.findViewById(R.id.noteItemTitle);


        textTime.setText(noteModel.getNoteTime());
        textTitle.setText(noteModel.getNoteTitle());

    }

    @Override
    public void onClick(View v) {

    }
}
