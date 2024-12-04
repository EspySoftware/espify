package org.espify.client.commands;

import org.espify.client.Client;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class RoomCommands {
    @ShellMethod("Join a room by name")
    public void join(String roomName) {
        Client.sendMessage("join " + roomName);
    }
    
    @ShellMethod("Add a song to the current room")
    public void add(String filePath) {
        Client.sendMessage("add " + filePath);
    }

    @ShellMethod("Skip the current song")
    public void skip() {
        Client.sendMessage("skip");
    }

    @ShellMethod("List all songs in the current playlist")
    public void ls() {
        Client.sendMessage("ls");
    }

    @ShellMethod("List all rooms")
    public void lr() {
        Client.sendMessage("lr");
    }
}