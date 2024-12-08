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
            broadcast("Music playback stopped (room is empty).");
            currentSong = null;
            isPlaying = false;
            // Additional logic to stop streaming if necessary
        }
    }

    // Add a song and start playing if no song is currently playing
    public synchronized void addSong(Song song) {
        playlist.add(song);
        broadcast("Added song: " + song.getName());
        logger.info("Added song: " + song.getName());
    
        if (!isPlaying) {
            playNextSong();
        }
    }

    public synchronized void playNextSong() {
        if (!playlist.isEmpty()) {
            Song nextSong = playlist.get(0);
            playSong(nextSong);
        } else {
            broadcast("No more songs in the playlist.");
            logger.info("No more songs in the playlist.");
            currentSong = null;
            isPlaying = false;
        }
    }

    public synchronized void onPlaybackComplete() {
        if (!isPlaying) {
            return; // Already processed completion
        }
    
        isPlaying = false;
    
        // Remove the finished song from the playlist
        if (!playlist.isEmpty()) {
            playlist.remove(0);
        }
    
        currentSong = null;
    
        // Start playing the next song if available
        playNextSong();
    }
    
    public synchronized void playSong(Song song) {
        if (isPlaying) {
            broadcast("Music is already playing.");
            return;
        }

        currentSong = song;
        isPlaying = true;
        long timestamp = System.currentTimeMillis() + 1000; // 1-second delay for synchronization
        broadcastWithTimestamp("play", timestamp);

        // Start streaming the song
        AudioClientHandler audioHandler = getAudioClientHandler();
        if (audioHandler != null) {
            audioHandler.streamSongAsync(currentSong.getFilePath());
            logger.info("Started playing song: " + currentSong.getName());
        } else {
            broadcast("Error: Audio handler not available.");
            isPlaying = false;
        }
    }

    public synchronized void pauseSong() {
        if (!isPlaying) {
            broadcast("Music is not playing.");
            return;
        }
    
        isPlaying = false;
        long timestamp = System.currentTimeMillis() + 500; // For synchronization
        broadcastWithTimestamp("pause", timestamp);
    
        // Pause streaming for all clients
        for (ClientHandler client : clients) {
            AudioClientHandler audioHandler = client.getAudioClientHandler();
            if (audioHandler != null) {
                audioHandler.pauseStreaming();
            }
        }
    
        logger.info("Paused song: " + currentSong.getName());
    }
    
    public synchronized void resumeSong() {
        if (isPlaying) {
            broadcast("Music is already playing.");
            return;
        }
    
        isPlaying = true;
        long timestamp = System.currentTimeMillis() + 1000; // For synchronization
        broadcastWithTimestamp("play", timestamp);
    
        // Resume streaming for all clients
        for (ClientHandler client : clients) {
            AudioClientHandler audioHandler = client.getAudioClientHandler();
            if (audioHandler != null) {
                audioHandler.resumeStreaming();
            }
        }
    
        logger.info("Resumed song: " + currentSong.getName());
    }

    public synchronized void skipSong() {
        if (isPlaying) {
            isPlaying = false;
            AudioClientHandler audioHandler = getAudioClientHandler();
            if (audioHandler != null) {
                audioHandler.setStreaming(false);
            }
            broadcast("Song skipped: " + currentSong.getName());
            logger.info("Song skipped: " + currentSong.getName());
    
            // Remove the current song from the playlist
            if (!playlist.isEmpty()) {
                playlist.remove(0);
            }
            currentSong = null;
    
            // Start the next song if available
            playNextSong();
        } else {
            broadcast("No song is currently playing.");
        }
    }

    // Broadcast a message with a timestamp for synchronization
    private synchronized void broadcastWithTimestamp(String action, long timestamp) {
        String message = action + " " + timestamp;
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
        logger.info("Broadcasted action: " + message + " to all clients in room: " + name);
    }

    // Broadcast a message to all clients in the room
    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
        logger.info("Broadcasted message: " + message + " to all clients in room: " + name);
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

    private synchronized AudioClientHandler getAudioClientHandler() {
        for (ClientHandler client : clients) {
            if (client.getCurrentRoom() == this) {
                return client.getAudioClientHandler();
            }
        }
        return null;
    }
}