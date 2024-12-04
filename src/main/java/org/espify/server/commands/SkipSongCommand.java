package org.espify.server.commands;

import org.espify.server.handlers.ClientHandler;

public class SkipSongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (clientHandler.getCurrentRoom() != null) {
            clientHandler.getCurrentRoom().playNextSong();
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}