package org.espify.client.commands;

import org.jline.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class TerminalCommands {

    @Autowired
    private Terminal terminal;

    @ShellMethod(key = "clear", value = "Clear the screen")
    public void clear() {
        terminal.puts(org.jline.utils.InfoCmp.Capability.clear_screen);
        terminal.flush();
    }

    @ShellMethod(key = "help", value = "Display help")
    public void help() {
        terminal.writer().println("Available commands:");
        terminal.writer().println("clear - Clear the screen");
        terminal.writer().println("help - Display help");
        terminal.flush();
    }
}