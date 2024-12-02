package org.espify.server;

import java.util.ArrayList;
import java.util.List;
import org.espify.models.Song;

public class Room {
    private String name;
    private List<Song> playlist = new ArrayList<>();
    private List<ClientHandler> clients = new ArrayList<>();

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

    public synchronized void addSong(Song song) {
        playlist.add(song);
        broadcast("New song added: " + song.getName());
    }

    public synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}