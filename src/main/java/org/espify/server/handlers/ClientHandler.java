package org.espify.server.handlers;

import org.espify.server.Server;
import org.espify.server.commands.AddSongCommand;
import org.espify.server.commands.Command;
import org.espify.server.commands.JoinRoomCommand;
import org.espify.server.commands.ListRoomsCommand;
import org.espify.server.commands.ListSongsCommand;
import org.espify.server.commands.SkipSongCommand;
import org.espify.server.Room;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler implements Runnable {
    private String clientID;
    private Socket controlSocket;
    private Socket audioSocket;

    private AudioClientHandler audioClientHandler;
    
    private PrintWriter output;
    private BufferedReader input;
    private ConcurrentHashMap<String, Room> rooms = Server.rooms;
    private HashMap<String, Command> commands;
    private Room currentRoom;

    public ClientHandler(Socket controlSocket) {
        this.clientID = UUID.randomUUID().toString();
        this.controlSocket = controlSocket;

        try {
            this.output = new PrintWriter(controlSocket.getOutputStream(), true);
            this.input = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));

            // Send the clientID to the client
            output.println(clientID);
        } catch (IOException e) {
            System.out.println("Error initializing control streams: " + e.getMessage());
        }
        initializeCommands();
        Server.clients.put(clientID, this);
    }

    private void initializeCommands() {
        commands = new HashMap<>();
        commands.put("join", new JoinRoomCommand());
        commands.put("add", new AddSongCommand());
        commands.put("skip", new SkipSongCommand());
        commands.put("ls", new ListSongsCommand());
        commands.put("lr", new ListRoomsCommand());
    }

    public void setAudioSocket(Socket audioSocket) {
        this.audioSocket = audioSocket;
        this.audioClientHandler = new AudioClientHandler(this, audioSocket);
        new Thread(audioClientHandler).start();
        System.out.println("AudioClientHandler started for client ID: " + clientID);
    }

    @Override
    public void run() {
        String msg;
        try {
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
            try {
                controlSocket.close();
                if (audioSocket != null) {
                    audioClientHandler.close();
                }
            } catch (IOException e) {
                System.out.println("Error closing sockets: " + e.getMessage());
            }
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

    public String getClientID() {
        return clientID;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public AudioClientHandler getAudioClientHandler() {
        return audioClientHandler;
    }

    public void setCurrentRoom(Room currentRoom) {
        this.currentRoom = currentRoom;
    }

    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }
}