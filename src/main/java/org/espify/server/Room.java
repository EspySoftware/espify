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
            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] dataToSend = Arrays.copyOf(buffer, bytesRead);
                for (ClientHandler client : clients) {
                    client.getDataClientHandler().sendMusicStream(dataToSend);
                }
                
                // Streaming rate control
                Thread.sleep(50);
                logger.info("Sent " + bytesRead + " bytes to clients.");
            }
            logger.info("Finished streaming: " + filePath);
            playNextSong();
        } catch (IOException | InterruptedException e) {
            logger.severe("Failed to stream music: " + e.getMessage());
            e.printStackTrace();
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
}