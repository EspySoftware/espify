package org.espify.server.commands;

import org.espify.models.Song;
import org.espify.server.ClientHandler;

public class AddSongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: addSong <songName>");
            return;
        }
        String songName = args[1].trim();
        if (clientHandler.getCurrentRoom() != null) {
            clientHandler.getCurrentRoom().addSong(new Song(songName));
            clientHandler.sendMessage("Added song: " + songName);
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}