package org.espify.client.commands;

import org.espify.client.Client;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.beans.factory.annotation.Autowired;

@ShellComponent
public class ConnectionCommands {

    @Autowired
    private Client client;

    @ShellMethod("Connect to the Espify server.")
    public String connect(String serverIp) {
        client.connectToServer(serverIp);
        return "Connecting to server at " + serverIp;
    }

    @ShellMethod("Disconnect from the Espify server.")
    public String disconnect() {
        client.disconnectFromServer();
        return "Disconnecting from server";
    }
}