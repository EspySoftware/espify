package org.espify.server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.espify.server.commands.*;

import java.util.UUID;

public class ClientHandler implements Runnable {
    private String clientId;
    private Socket controlSocket;
    private BufferedReader input;
    private PrintWriter output;
    private DataClientHandler dataClientHandler;
    private ConcurrentHashMap<String, Room> rooms;
    private HashMap<String, Command> commands;
    Room currentRoom;

    public ClientHandler(Socket controlSocket, ConcurrentHashMap<String, Room> rooms) {
        this.clientId = UUID.randomUUID().toString();
        this.controlSocket = controlSocket;
        this.rooms = rooms;
        initializeCommands();
    }

    private void initializeCommands() {
        commands = new HashMap<>();
        commands.put("joinRoom", new JoinRoomCommand());
        commands.put("addSong", new AddSongCommand());
        commands.put("skipSong", new SkipSongCommand());
        commands.put("listSongs", new ListSongsCommand());
        commands.put("listRooms", new ListRoomsCommand());
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            output = new PrintWriter(controlSocket.getOutputStream(), true);
    
            // Send the unique client ID to the client
            sendMessage("CLIENT_ID " + clientId);
    
            String msg;
            while ((msg = input.readLine()) != null) {
                handleMessage(msg);
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            // Cleanup on disconnect
            if (currentRoom != null) {
                currentRoom.removeClient(this);
            }
            if (dataClientHandler != null) {
                dataClientHandler.close();
            }
            Server.clients.remove(clientId);
            System.out.println("Client disconnected: " + controlSocket.getInetAddress());
        }
    }

    void handleMessage(String msg) {
        String[] parts = msg.split(" ");
        String commandName = parts[0];

        Command command = commands.get(commandName);
        if (command != null) {
            command.execute(parts, this);
        } else {
            sendMessage("Unknown command.");
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public DataClientHandler getDataClientHandler() {
        return dataClientHandler;
    }

    public void setDataClientHandler(DataClientHandler dataClientHandler) {
        this.dataClientHandler = dataClientHandler;
    }

    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }

    public String getClientId() {
        return clientId;
    }
}