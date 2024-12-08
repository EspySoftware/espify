package org.espify.server.commands;

import org.espify.server.Room;
import org.espify.server.handlers.ClientHandler;

public class SkipSongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        Room currentRoom = clientHandler.getCurrentRoom();
        if (currentRoom != null) {
            currentRoom.skipSong();
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}