package org.espify.client.commands;

import org.espify.client.Client;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class RoomCommands {
    @ShellMethod("Join a room by name")
    public void joinRoom(String roomName) {
        Client.sendMessage("joinRoom " + roomName);
    }
    
    @ShellMethod("Add a song to the current room")
    public void addSong(String songName) {
        Client.sendMessage("addSong " + songName);
    }

    @ShellMethod("Skip the current song")
    public void skipSong() {
        Client.sendMessage("skipSong");
    }

    @ShellMethod("List all songs in the current playlist")
    public void listSongs() {
        Client.sendMessage("listSongs");
    }
}