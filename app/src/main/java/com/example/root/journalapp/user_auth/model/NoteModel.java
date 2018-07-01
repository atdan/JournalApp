package com.example.root.journalapp.user_auth.model;

public class NoteModel {
    public String noteTitle;
    public String noteTime;

    public NoteModel(){

    }
    public NoteModel(String noteTitle,String noteTime){
        this.noteTitle = noteTitle;
        this.noteTime = noteTime;
    }

    public void setNoteTitle(String noteTitle) {
        this.noteTitle = noteTitle;
    }

    public String getNoteTime() {
        return noteTime;
    }

    public void setNoteTime(String noteTime) {
        this.noteTime = noteTime;
    }

    public String getNoteTitle() {
        return noteTitle;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
