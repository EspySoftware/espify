package org.espify.server.commands;
import org.espify.server.handlers.ClientHandler;

public interface Command {
    void execute(String[] args, ClientHandler clientHandler);
}