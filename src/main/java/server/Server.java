package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    int PORT = 12345;
    Map<String, Room> rooms = new HashMap<>();

    public static void main(String[] args) {
        new MusicSyncServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");
                new Thread(new ClientHandler(clientSocket, rooms)).start();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
