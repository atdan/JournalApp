package com.example.root.journalapp.user_auth.Note;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.root.journalapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class NewNoteActivity extends AppCompatActivity {

    EditText titleEditTet, notesEditText;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference notesDatabaseReference;

    private static final String TAG = "NewNoteActivity";
    private String noteID;

    private boolean isTrue;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_new_note, menu);
        Menu mainmenu = menu;
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        notesDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Notes").child(firebaseAuth.getCurrentUser().getUid());


        final FloatingActionButton saveFab = findViewById(R.id.saveFab);




        titleEditTet = findViewById(R.id.titleEditText);
        notesEditText = findViewById(R.id.notesEditText);

        try{

            noteID = getIntent().getStringExtra("noteID");

            if (noteID.trim().equals("")){
                isTrue = false;
            }else isTrue = true;

        }catch (Exception e){
            e.printStackTrace();
        }

        Log.i(TAG, "onCreate: The note iD is "+ noteID);

        putData();
        saveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String title = titleEditTet.getText().toString().trim();
                String noteContent = notesEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(noteContent)){
                    createNote(title,noteContent);
                    startActivity(new Intent(NewNoteActivity.this, NotesListActivity.class));
                }else {
                    Snackbar.make(view, "Please fil in Empty fields", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


            }
        });
//        FloatingActionButton addFab = findViewById(R.id.addFab);
//        {
//            addFab.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    saveFab.setVisibility(View.VISIBLE);
//                }
//            });
//        }
    }

    private void putData(){

        if (isTrue){
        notesDatabaseReference.child(noteID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("content")){
                    String title = dataSnapshot.child("title").getValue().toString();
                    String content = dataSnapshot.child("content").getValue().toString();

                    titleEditTet.setText(title);
                    notesEditText.setText(content);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        }
    }
    @SuppressWarnings("unchecked")
    private void createNote(String title, String content) {

        if (firebaseAuth.getCurrentUser() != null){

            if (isTrue){
                //update an existing note
                Map updateMap = new HashMap();
                updateMap.put("title",titleEditTet.getText().toString().trim());
                updateMap.put("content",notesEditText.getText().toString().trim());
                updateMap.put("timestamp", ServerValue.TIMESTAMP);
                Log.i(TAG, "createNote: TitleEditText"+ titleEditTet.getText().toString());
                Log.i(TAG, "createNote: NotesEditText"+ notesEditText.getText().toString());
                Log.i(TAG, "createNote: "+ ServerValue.TIMESTAMP);

                notesDatabaseReference.child(noteID).updateChildren(updateMap);

            }else {
                //Create a new note
                final DatabaseReference newNoteReference = notesDatabaseReference.push();

                final Map noteMap = new HashMap();
                noteMap.put("title", title);
                noteMap.put("content", content);
                noteMap.put("timestamp", ServerValue.TIMESTAMP);

                Thread mainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        newNoteReference.setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                                if (task.isSuccessful()){

                                    Toast.makeText(NewNoteActivity.this,"Added to online database",Toast.LENGTH_SHORT).show();
                                    //startActivity(new Intent(NewNoteActivity.this,NotesListActivity.class));
                                }else if (!task.isSuccessful()){
                                    Toast.makeText(NewNoteActivity.this,"Unsuccessful "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    //startActivity(new Intent(NewNoteActivity.this,NotesListActivity.class));

                                }
                            }
                        });
                    }
                });mainThread.start();
            }


        }else {
            Toast.makeText(this,"User is not signed in",Toast.LENGTH_LONG).show();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){

            case R.id.home:
                finish();
                break;
            case R.id.new_note_delete:
                if (isTrue){
                    AlertDialog.Builder builder = new AlertDialog.Builder(NewNoteActivity.this);
                    builder.setTitle("Delete")
                            .setMessage("Are you sure you want to delete this note?")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteNote();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    recreate();
                                }
                            }).create();
                    builder.show();
                    return true;
                }else {
                    //startActivity(new Intent(NewNoteActivity.this,NotesListActivity.class));
                    finish();
                }
                break;
        }

        return true;
    }

    private void deleteNote(){

        notesDatabaseReference.child(noteID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(NewNoteActivity.this,"Note successfully deleted",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(NewNoteActivity.this, NotesListActivity.class));
                    finish();
                }else {
                    Toast.makeText(NewNoteActivity.this,"Note can't be deleted at this time",Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onComplete: "+ task.getException().toString() );
                }
            }
        });
    }
}
