package org.espify.server.commands;

import org.espify.server.ClientHandler;
import org.espify.server.Room;
import org.espify.models.Song;

import java.util.ArrayList;

public class ListSongsCommand implements Command {
    @Override
    public void execute(String[] args, ClientHandler clientHandler) {
        Room currentRoom = clientHandler.getCurrentRoom();
        if (currentRoom != null) {
            ArrayList<Song> songs = currentRoom.getPlaylist();
            if (songs.isEmpty()) {
                clientHandler.sendMessage("The playlist is empty.");
            } else {
                StringBuilder songList = new StringBuilder("Playlist:");
                for (int i = 0; i < songs.size(); i++) {
                    Song song = songs.get(i);
                    songList.append("\n").append(i + 1).append(". ").append(song.getName());
                }
                clientHandler.sendMessage(songList.toString());
            }
        } else {
            clientHandler.sendMessage("You are not in a room.");
        }
    }
}
