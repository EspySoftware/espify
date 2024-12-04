package org.espify.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import org.espify.models.Song;

public class Room {
    private static final Logger logger = Logger.getLogger(Room.class.getName());

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

    // Play the next song in the playlist
    public synchronized void playNextSong() {
        if (!playlist.isEmpty()) {
            currentSong = playlist.remove(0);
            broadcast("Now Playing: " + currentSong.getName());
            logger.info("Now Playing: " + currentSong.getName());
            // Start streaming in a new thread
            new Thread(() -> streamMusicToClients(currentSong.getFilePath())).start();
        } else {
            broadcast("No more songs in the playlist.");
            logger.info("No more songs in the playlist.");
            currentSong = null;
        }
    }

    // Add a song and start playing if no song is currently playing
    public synchronized void addSong(Song song) {
        playlist.add(song);
        broadcast("New song added: " + song.getName());
        logger.info("New song added: " + song.getName());
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
                broadcast("Now Playing: " + currentSong.getName());
                logger.info("Now Playing: " + currentSong.getName());
                streamMusicToClients(currentSong.getFilePath());
                return;
            }
        }
        broadcast("Song not found: " + songName);
        logger.info("Song not found: " + songName);
    }

    public synchronized void streamMusicToClients(String filePath) {
        logger.info("Streaming song: " + filePath);
        byte[] buffer = new byte[4096];
        try (FileInputStream fis = new FileInputStream(filePath)) {
            int bytesRead;
    
            // Read and send the music stream to all clients in the room
            while ((bytesRead = fis.read(buffer)) != -1 && !clients.isEmpty()) {
                byte[] dataToSend = Arrays.copyOf(buffer, bytesRead);
                for (ClientHandler client : new ArrayList<>(clients)) {
                    if (clients.contains(client) && client.getDataClientHandler() != null) {
                        client.getDataClientHandler().sendMusicStream(dataToSend);
                    }
                }
                Thread.sleep(50); // Control streaming rate
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Failed to stream music: " + e.getMessage());
        } finally {
            playNextSong();
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

    public synchronized int getClientCount() {
        return clients.size();
    }
}