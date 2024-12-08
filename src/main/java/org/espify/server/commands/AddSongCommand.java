package org.espify.server.commands;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.espify.models.Song;
import org.espify.server.Room;
import org.espify.server.handlers.ClientHandler;
import org.espify.utils.YTDownloadAPI;

public class AddSongCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        if (args.length < 2) {
            clientHandler.sendMessage("Usage: add <YouTube URL or keywords>");
            return;
        }

        String query = args[1].trim();

        // Download the audio and retrieve song details
        String downloadResult = YTDownloadAPI.DownloadAudio(query);
        String[] parts = downloadResult.split("•");

        if (parts.length < 2) {
            clientHandler.sendMessage("Failed to download or parse the song.");
            return;
        }

        String songName = parts[0];
        String filePath = parts[1];

        // Check if the file exists and is an MP3
        if (!Files.exists(Paths.get(filePath)) || !filePath.endsWith(".mp3")) {
            clientHandler.sendMessage("Invalid file: " + filePath);
            return;
        }

        // Add the song to the current room's playlist
        Room currentRoom = clientHandler.getCurrentRoom();
        if (currentRoom != null) {
            Song newSong = new Song(songName, filePath);
            currentRoom.addSong(newSong);
            clientHandler.sendMessage("Added song: " + songName);
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}