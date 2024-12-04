package org.espify.server.commands;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.espify.models.Song;
import org.espify.server.handlers.ClientHandler;

public class AddSongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: addSong <filePath>");
            return;
        }
        String filePath = args[1].trim();
        String songName = filePath;

        // Check if the file exists
        if (!Files.exists(Paths.get(filePath))) {
            clientHandler.sendMessage("File does not exist: " + filePath);
            return;
        }

        // Check if the file is an mp3 file
        if (!filePath.endsWith(".mp3")) {
            clientHandler.sendMessage("File is not an mp3 file: " + filePath);
            return;
        }

        // Check if the client is in a room
        if (clientHandler.getCurrentRoom() != null) {
            Song newSong = new Song(songName, filePath);
            clientHandler.getCurrentRoom().addSong(newSong);
            clientHandler.sendMessage("Added song: " + songName);
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}