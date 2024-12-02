package org.espify.server.commands;

import org.espify.server.ClientHandler;

public class PlaySongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: playSong <songName>");
            return;
        }
        String songName = args[1].trim();
        if (clientHandler.getCurrentRoom() != null) {
            clientHandler.getCurrentRoom().playSong(songName);
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}