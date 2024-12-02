package org.espify.server;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import org.espify.models.Song;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private ConcurrentHashMap<String, Room> rooms;
    private Room currentRoom;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Room> rooms) {
        this.socket = socket;
        this.rooms = rooms;
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String msg;
            while ((msg = input.readLine()) != null) {
                handleMessage(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                if (currentRoom != null) {
                    currentRoom.removeClient(this);
                }
                Server.clients.remove(socket.getInetAddress().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void handleMessage(String msg) {
        if (msg.startsWith("joinRoom")) {
            String roomName = msg.split(" ")[1];
            currentRoom = rooms.computeIfAbsent(roomName, Room::new);
            currentRoom.addClient(this);
            sendMessage("Joined room: " + roomName);
        } else if (msg.startsWith("addSong")) {
            String songName = msg.substring(8).trim();
            if (currentRoom != null) {
                currentRoom.addSong(new Song(songName));
                sendMessage("Added song: " + songName);
            } else {
                sendMessage("You are not in a room.");
            }
        } else if (msg.startsWith("playSong")) {
            String songPath = msg.substring(9).trim();
            if (currentRoom != null) {
                currentRoom.broadcast("Now Playing: " + songPath);
            } else {
                sendMessage("You are not in a room.");
            }
        } else {
            sendMessage("Unknown command.");
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }
}