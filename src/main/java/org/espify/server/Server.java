package org.espify.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import org.espify.server.handlers.ClientHandler;

public class Server {
    public static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static final int CONTROL_PORT = 12345;
    public static final int AUDIO_PORT = 12346;

    public static void main(String[] args) {
        Server espifyServer = new Server();
        espifyServer.start();
    }

    public void start() {
        System.out.println(" Starting Espify Server...\r\n");

        // Start Control Server
        new Thread(() -> {
            try (ServerSocket controlServerSocket = new ServerSocket(CONTROL_PORT)) {
                System.out.println("Control server listening on port " + CONTROL_PORT);

                while (true) {
                    Socket controlSocket = controlServerSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(controlSocket);
                    new Thread(clientHandler).start();
                    System.out.println("New control client connected with ID: " + clientHandler.getClientID());
                }
            } catch (IOException e) {
                System.out.println("Error starting control server: " + e.getMessage());
            }
        }).start();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            System.out.println("Error sleeping main thread: " + e.getMessage());
        }

        // Start Audio Server
        new Thread(() -> {
            try (ServerSocket audioServerSocket = new ServerSocket(AUDIO_PORT)) {
                System.out.println("Audio server listening on port " + AUDIO_PORT);

                while (true) {
                    Socket audioSocket = audioServerSocket.accept();

                    // Expect the client to send their clientID upon connecting
                    BufferedReader reader = new BufferedReader(new InputStreamReader(audioSocket.getInputStream()));
                    String clientID = reader.readLine();

                    // Necessary to wait for the client to send the clientID
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        System.out.println("Error sleeping main thread: " + e.getMessage());
                    }

                    ClientHandler clientHandler = clients.get(clientID);
                    if (clientHandler != null) {
                        clientHandler.setAudioSocket(audioSocket);
                        System.out.println("Audio client connected for client ID: " + clientID);
                    } else {
                        System.out.println("Unknown client ID: " + clientID);
                        audioSocket.close();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error starting audio server: " + e.getMessage());
            }
        }).start();
    }
    
    public synchronized Room getOrCreateRoom(String name) {
        return rooms.computeIfAbsent(name, id -> new Room(id));
    }
}