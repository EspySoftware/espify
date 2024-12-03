package org.espify.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static ConcurrentHashMap<String, Room> rooms = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    static final int CONTROL_PORT = 12345;
    static final int DATA_PORT = 12346;

    public static void main(String[] args) {
        // Start Control Socket Server
        new Thread(() -> {
            try (ServerSocket controlServerSocket = new ServerSocket(CONTROL_PORT)) {
                System.out.println("Control server listening on port " + CONTROL_PORT);
                while (true) {
                    Socket controlSocket = controlServerSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(controlSocket, rooms);
                    clients.put(clientHandler.getClientId(), clientHandler);
                    new Thread(clientHandler).start();
                    System.out.println("New control client connected with ID: " + clientHandler.getClientId());
                }
            } catch (IOException e) {
                System.out.println("Error starting control server: " + e.getMessage());
            }
        }).start();

        // Start Data Socket Server
        new Thread(() -> {
            try (ServerSocket dataServerSocket = new ServerSocket(DATA_PORT)) {
                System.out.println("Data server listening on port " + DATA_PORT);
                while (true) {
                    Socket dataSocket = dataServerSocket.accept();
                    BufferedReader dataInput = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                    String idMessage = dataInput.readLine();

                    if (idMessage != null && idMessage.startsWith("CLIENT_ID ")) {
                        String clientId = idMessage.substring("CLIENT_ID ".length());
                        ClientHandler clientHandler = clients.get(clientId);
                        if (clientHandler != null) {
                            DataClientHandler dataClientHandler = new DataClientHandler(dataSocket, clientHandler);
                            clientHandler.setDataClientHandler(dataClientHandler);
                            new Thread(dataClientHandler).start();
                            System.out.println("Data socket associated with client ID: " + clientId);
                        } else {
                            System.out.println("No matching ClientHandler found for client ID: " + clientId);
                            dataSocket.close();
                        }
                    } else {
                        System.out.println("Invalid client ID message.");
                        dataSocket.close();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error starting data server: " + e.getMessage());
            }
        }).start();
    }
}