package org.espify.models;

import java.util.ArrayList;

public class Playlist {
    ArrayList<Song> songs = new ArrayList<>();

    synchronized void addSong(Song song) {
        songs.add(song);
    }

    synchronized ArrayList<Song> getSongs() {
        return new ArrayList<>(songs);
    }
}