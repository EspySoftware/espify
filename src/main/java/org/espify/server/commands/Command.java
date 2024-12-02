package org.espify.server.commands;
import org.espify.server.ClientHandler;

public interface Command {
    void execute(String[] args, ClientHandler clientHandler);
}