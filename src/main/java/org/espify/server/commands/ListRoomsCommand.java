package org.espify.server.commands;

import java.util.concurrent.ConcurrentHashMap;

import org.espify.server.Room;
import org.espify.server.handlers.ClientHandler;

public class ListRoomsCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        ConcurrentHashMap<String, Room> rooms = clientHandler.getRooms();
        if (rooms.isEmpty()) {
            clientHandler.sendMessage("No active rooms.");
            return;
        }

        StringBuilder roomList = new StringBuilder("Active Rooms:");
        for (String roomName : rooms.keySet()) {
            Room room = rooms.get(roomName);
            roomList.append("\n").append(roomName).append(" (").append(room.getClientCount()).append(" users)");
        }
        clientHandler.sendMessage(roomList.toString());
    }
}