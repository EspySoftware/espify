package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    Map<String, Room> rooms;

    public ClientHandler(Socket socket, Map<String, Room> rooms) {
        this.socket = socket;
        this.rooms = rooms;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Handle client input
            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("Received: " + input);
                handleInput(input);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    void handleInput(String input) {
        // TODO: Handle commands like "joinRoom Room1", "addSong SongName", etc.
    }

    void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
    }
}
