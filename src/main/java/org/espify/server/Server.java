package org.espify.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Start socket server in a new thread
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(12345)) {
                System.out.println("Socket server is listening on port 12345");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New socket client connected: " + clientSocket.getInetAddress());

                    // Create and start a new ClientHandler thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket, rooms);
                    clients.put(clientSocket.getInetAddress().toString(), clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}