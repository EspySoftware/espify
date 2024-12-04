package org.espify.server.commands;

import org.espify.server.Room;
import org.espify.server.handlers.ClientHandler;

public class JoinRoomCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: joinRoom <roomName>");
            return;
        }
        String roomName = args[1];

        // Remove the client from the current room if they're already in one
        Room currentRoom = clientHandler.getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.removeClient(clientHandler);
            clientHandler.setCurrentRoom(null);
            clientHandler.sendMessage("Left room: " + currentRoom.getName());
        }

        // Join the new room
        Room newRoom = clientHandler.getRooms().computeIfAbsent(roomName, Room::new);
        newRoom.addClient(clientHandler);
        clientHandler.setCurrentRoom(newRoom);
        clientHandler.sendMessage("Joined room: " + roomName);
    }
}