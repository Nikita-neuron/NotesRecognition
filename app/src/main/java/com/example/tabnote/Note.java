package com.example.tabnote;

public class Note {
    public int string;
    public int fret;

    public Note(int string, int fret) {
        this.string = string;
        this.fret = fret;
    }

    @Override
    public String toString() {
        return (string + 1) + "_" + fret;
    }
}
