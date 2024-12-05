package org.espify.server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.espify.models.Song;
import org.espify.server.handlers.AudioClientHandler;
import org.espify.server.handlers.ClientHandler;

public class Room {
    private static final Logger logger = Logger.getLogger(Room.class.getName());

    private String name;
    private List<Song> playlist = new ArrayList<>();
    private volatile boolean isPlaying = false;
    private Song currentSong;
    private List<ClientHandler> clients = new ArrayList<>();


    public Room(String name) {
        this.name = name;
        this.clients = new CopyOnWriteArrayList<ClientHandler>();
        this.playlist = new ArrayList<Song>();
        this.isPlaying = false;
    }

    public String getName() {
        return name;
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        broadcast("A new user has joined the room.");
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        broadcast("A user has left the room.");

        if (clients.isEmpty()) {
            stopMusicPlayback();
        }
    }
    
    public void stopMusicPlayback() {
        // Implement logic to stop the music stream
        if (currentSong != null) {
            broadcast("Music playback stopped as the room is now empty.");
            currentSong = null;
            playlist.clear();
        }
    }

    // Add a song and start playing if no song is currently playing
    public synchronized void addSong(Song song) {
        playlist.add(song);
        broadcast("New song added: " + song.getName());
        logger.info("New song added: " + song.getName());
        if (currentSong == null || !isPlaying) {
            playNextSong();
        }
    }

    public void playNextSong() {
        if (!playlist.isEmpty()) {
            Song nextSong = playlist.get(0);
            playSong(nextSong);
        } else {
            broadcast("No more songs in the playlist.");
            logger.info("No more songs in the playlist.");
            currentSong = null;
        }
    }
    
    public void playSong(Song song) {
        currentSong = song;
        playlist.remove(song); // Now safe to remove
        broadcast("Now Playing: " + currentSong.getName());
        logger.info("Now Playing: " + song.getName());

        try {
            // Delegate streaming to AudioClientHandler
            AudioClientHandler audioHandler = getAudioClientHandler();
            if (audioHandler != null) {
                audioHandler.streamSongAsync(currentSong.getFilePath());
            } else {
                logger.severe("Audio handler not available.");
                broadcast("Error: Audio handler not available.");
            }

            if (!playlist.isEmpty()) {
                playNextSong();
            } else {
                currentSong = null;
            }
        } catch (Exception e) {
            broadcast("Error streaming song: " + e.getMessage());
            logger.severe("Error streaming song: " + e.getMessage());
            if (!playlist.isEmpty()) {
                playNextSong();
            } else {
                currentSong = null;
            }
        }
    }

    // Broadcast a message to all clients in the room
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public synchronized ArrayList<Song> getPlaylist() {
        return new ArrayList<>(playlist);
    }

    public synchronized Song getCurrentSong() {
        return currentSong;
    }

    public synchronized void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    public synchronized ArrayList<ClientHandler> getClients() {
        return new ArrayList<>(clients);
    }

    public synchronized int getClientCount() {
        return clients.size();
    }

    private AudioClientHandler getAudioClientHandler() {
        for (ClientHandler client : clients) {
            if (client.getCurrentRoom() == this) {
                return client.getAudioClientHandler();
            }
        }
        return null;
    }
}