package org.espify.server.commands;

import org.espify.server.ClientHandler;
import org.espify.server.Room;

public class JoinRoomCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: joinRoom <roomName>");
            return;
        }
        String roomName = args[1];
        Room currentRoom = clientHandler.getRooms().computeIfAbsent(roomName, Room::new);
        currentRoom.addClient(clientHandler);
        clientHandler.setCurrentRoom(currentRoom);
        clientHandler.sendMessage("Joined room: " + roomName);
    }
}