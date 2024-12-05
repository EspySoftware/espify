package org.espify.server.commands;

import org.espify.server.handlers.ClientHandler;
import org.espify.server.Room;

public class PauseCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        Room currentRoom = clientHandler.getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.pauseSong();
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}