package server;

import java.util.*;

public class Room {
    String name;
    ArrayList<String> playlist;
    ArrayList<ClientHandler> clients;

    Room(String name) {
        this.name = name;
        this.playlist = new ArrayList<>();
        this.clients = new ArrayList<>();
    }

    synchronized void addSong(String song) {
        playlist.add(song);
        broadcast("Song added: " + song);
    }

    synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    synchronized void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}
