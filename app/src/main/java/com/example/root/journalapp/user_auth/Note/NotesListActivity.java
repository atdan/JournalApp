package com.example.root.journalapp.user_auth.Note;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.root.journalapp.R;
import com.example.root.journalapp.user_auth.MainActivity;
import com.example.root.journalapp.user_auth.holder.NoteViewHolder;
import com.example.root.journalapp.user_auth.model.NoteModel;
import com.example.root.journalapp.user_auth.setupTime.GeoTime;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class NotesListActivity extends AppCompatActivity {

    private static final String TAG = "NotesListActivity";
    private FirebaseAuth firebaseAuth;
    private RecyclerView notesListRecyclerView;

    private DatabaseReference notesDatabase;

    //String noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false);
        gridLayoutManager.setItemPrefetchEnabled(false);
        notesListRecyclerView = findViewById(R.id.notes_list_recycler);

        notesListRecyclerView.setHasFixedSize(true);
        notesListRecyclerView.setLayoutManager(gridLayoutManager);


        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            notesDatabase = FirebaseDatabase.getInstance().getReference().child("Notes").child(firebaseAuth.getCurrentUser().getUid());
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(NotesListActivity.this, NewNoteActivity.class);
                startActivity(intent);
            }
        });


        setupFirebaseAdadapter();


        updateUI();
    }


    public void setupFirebaseAdadapter() {

        Query query = notesDatabase.orderByChild("timestamp");
        FirebaseRecyclerAdapter<NoteModel, NoteViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NoteModel, NoteViewHolder>(
                NoteModel.class,
                R.layout.single_note_item_layout,
                NoteViewHolder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(final NoteViewHolder viewHolder, NoteModel model, int position) {

                final String noteId = getRef(position).getKey();

                notesDatabase.child(noteId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("title") && dataSnapshot.hasChild("timestamp")) {
                            String title = dataSnapshot.child("title").getValue().toString();
                            String time = dataSnapshot.child("timestamp").getValue().toString();

//                            viewHolder.setNoteTime(time);
                            viewHolder.setNoteTitle(title);

                            GeoTime geoTime = new GeoTime();
                            viewHolder.setNoteTime(GeoTime.getTime(Long.parseLong(time), getApplicationContext()));

                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(NotesListActivity.this, NewNoteActivity.class);
                                    intent.putExtra("noteID", noteId);
                                    startActivity(intent);
                                }
                            });
                            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                                    builder.setTitle("Delete")
                                            .setMessage("Are you sure you want to delete this note?")
                                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    notesDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            Toast.makeText(NotesListActivity.this, "Note successfully deleted", Toast.LENGTH_LONG).show();
                                                            if (task.isSuccessful()) {


                                                            } else {
                                                                Toast.makeText(NotesListActivity.this, "Note can't be deleted at this time", Toast.LENGTH_SHORT).show();
                                                                Log.e(TAG, "onComplete: " + task.getException().toString());
                                                            }
                                                        }
                                                    });
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
                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        notesListRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }
//    public void setupFirebaseAdapter()
//    {
//        Query query = FirebaseDatabase.getInstance()
//                .getReference()
//                .child("Notes")
//                .limitToLast(50);
//
//        FirebaseRecyclerOptions<NoteModel> options =
//                new FirebaseRecyclerOptions.Builder<NoteModel>()
//                        .setQuery(query, NoteModel.class)
//                        .build();
//
//        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NoteModel, NoteViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull final NoteViewHolder holder, int position, @NonNull final NoteModel model) {
//
//                holder.setNoteTitle(model.getNoteTitle());
//                holder.setNoteTime(model.getNoteTime());
//                String noteID = getRef(position).getKey();
//                notesDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
//                        for (DataSnapshot d : dataSnapshots){
//                            String title = String.valueOf(dataSnapshot.child("title").getValue());
//
//                            String timestamp = String.valueOf(dataSnapshot.child("timestamp").getValue());
//
//                            holder.setNoteTitle(model.getNoteTitle());
//                            holder.setNoteTime(model.getNoteTime());
//                            holder.setNoteTitle(title);
//                            holder.setNoteTime(timestamp);
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
////
////                holder.setNoteTime(model.getNoteTime());
////                holder.setNoteTitle(model.getNoteTitle());
//            }
//
//            @NonNull
//            @Override
//            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.single_note_item_layout, parent, false);
//
//                return new NoteViewHolder(view);
//            }
//        };
//
////        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NoteModel,NoteViewHolder>(
////                options
////
////        ) {
////            @NonNull
////            @Override
////            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
////
////                View view = LayoutInflater.from(parent.getContext())
////                        .inflate(R.layout.single_note_item_layout, parent, false);
////
////                return new NoteViewHolder(view);
////
////            }
////
////            @Override
////            protected void onBindViewHolder(@NonNull final NoteViewHolder holder, int position, @NonNull NoteModel model) {
////
////                String noteID = getRef(position).getKey();
////                notesDatabase.child(noteID).addValueEventListener(new ValueEventListener() {
////                    @Override
////                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                        String title = dataSnapshot.child("title").getValue().toString();
////
////                        String timestamp = dataSnapshot.child("timestamp").getValue().toString();
////
////                        holder.setNoteTitle(title);
////                        holder.setNoteTime(timestamp);
////                    }
////
////                    @Override
////                    public void onCancelled(@NonNull DatabaseError databaseError) {
////
////                    }
////                });
////            }
//
//        notesListRecyclerView.setAdapter(firebaseRecyclerAdapter);
//    }

    public void swipe() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //deleteNote();
            }
        }).attachToRecyclerView(notesListRecyclerView);
    }

    @Override
    public void onStart() {
        super.onStart();

        setupFirebaseAdadapter();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateUI() {
        if (firebaseAuth.getCurrentUser() != null) {
            Log.i(TAG, "updateUI: Firebase Auth != null");
        } else {
            Intent startIntent = new Intent(NotesListActivity.this, MainActivity.class);
            startActivity(startIntent);
            finish();
            Log.i(TAG, "updateUI: Auth == null");
        }
    }

    private void signOut() {
        // Firebase sign out
        firebaseAuth.signOut();
        Intent intent = new Intent(NotesListActivity.this, MainActivity.class);
        startActivity(intent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.notesLogout_menubtn:
                AlertDialog.Builder builder = new AlertDialog.Builder(NotesListActivity.this);
                builder.setTitle("Logout")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                signOut();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                recreate();
                            }
                        }).create();
                builder.show();

                break;
        }
        return true;
    }


//    private void deleteNote(){
//
//
//        notesDatabase.child(noteId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                Toast.makeText(NotesListActivity.this,"Note successfully deleted",Toast.LENGTH_LONG).show();
//                if (task.isSuccessful()){
//
//
//                }else {
//                    Toast.makeText(NotesListActivity.this,"Note can't be deleted at this time",Toast.LENGTH_SHORT).show();
//                    Log.e(TAG, "onComplete: "+ task.getException().toString() );
//                }
//            }
//        });
//    }
}

