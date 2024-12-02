package org.espify.server;

import java.util.ArrayList;
import org.espify.models.Song;

public class Room {
    private String name;
    private ArrayList<Song> playlist = new ArrayList<>();
    private ArrayList<ClientHandler> clients = new ArrayList<>();
    private Song currentSong;

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        broadcast("A user has joined the room.");
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcast("A user has left the room.");
    }

    // Play the next song in the playlist
    public synchronized void playNextSong() {
        if (!playlist.isEmpty()) {
            currentSong = playlist.remove(0);
            broadcast("Now Playing: " + currentSong.getName());
            // Implement actual playback logic here if needed
        } else {
            broadcast("No more songs in the playlist.");
            currentSong = null;
        }
    }

    // Add a song and start playing if no song is currently playing
    public synchronized void addSong(Song song) {
        playlist.add(song);
        broadcast("New song added: " + song.getName());
        if (currentSong == null) {
            playNextSong();
        }
    }

    // Play a specific song by name
    public synchronized void playSong(String songName) {
        for (Song song : playlist) {
            if (song.getName().equalsIgnoreCase(songName)) {
                currentSong = song;
                playlist.remove(song);
                broadcast("Now Playing: " + song.getName());
                return;
            }
        }
        broadcast("Song not found: " + songName);
    }

    // Get the current playlist
    public synchronized ArrayList<Song> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    // Broadcast a message to all clients in the room
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}