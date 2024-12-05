package org.espify.server.commands;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.espify.models.Song;
import org.espify.server.handlers.ClientHandler;
import org.espify.utils.YTDownloadAPI;

public class AddSongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: addSong <filePath>");
            return;
        }
        
        // Get the youtube url or keywords
        clientHandler.sendMessage("Arguments: " + Arrays.toString(args));
        String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        
        clientHandler.sendMessage(query);

        // This function treats the query as a youtube url or keywords
        String downloadResult = YTDownloadAPI.DownloadAudio(query);

        // The result is a string with the song name and the file path separated by a pipe
        String[] parts = downloadResult.split("•");
        String songName = parts[0];
        String filePath = parts[1];


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