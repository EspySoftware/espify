package org.espify.server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.espify.server.commands.AddSongCommand;
import org.espify.server.commands.Command;
import org.espify.server.commands.JoinRoomCommand;
import org.espify.server.commands.ListSongsCommand;
import org.espify.server.commands.SkipSongCommand;
import org.espify.server.commands.PlaySongCommand;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private ConcurrentHashMap<String, Room> rooms;
    private HashMap<String, Command> commands;
    private Room currentRoom;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, Room> rooms) {
        this.socket = socket;
        this.rooms = rooms;
        initializeCommands();
    }

    private void initializeCommands() {
        commands = new HashMap<>();
        commands.put("joinRoom", new JoinRoomCommand());
        commands.put("addSong", new AddSongCommand());
        commands.put("playSong", new PlaySongCommand());
        commands.put("skipSong", new SkipSongCommand());
        commands.put("listSongs", new ListSongsCommand()); // Registering the new command

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

    public ConcurrentHashMap<String, Room> getRooms() {
        return rooms;
    }
}