package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javazoom.jl.player.Player;

public class TerminalClient {
    private static Player player;

    public static void main(String[] args) {
        String host = "localhost"; // Change to server IP if connecting remotely
        int port = 12345;

        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server.");

            // Start a thread to listen for server messages
            new Thread(() -> {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        System.out.println("Server: " + response);
                        if (response.startsWith("Now Playing:")) {
                            String songPath = response.substring(12).trim();
                            playSong(songPath);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }).start();

            // Main loop to send commands
            while (true) {
                System.out.print("Enter command (joinRoom <room>, addSong <song>, playSong <path>, or exit): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                out.println(input);

                if (input.startsWith("playSong")) {
                    String[] parts = input.split(" ", 2);
                    if (parts.length == 2) {
                        String songPath = parts[1].trim();
                        player = playSong(songPath);
                    } else {
                        System.out.println("Usage: playSong <path_to_mp3>");
                    }
                }
            }

            // Close the player if open
            if (player != null) {
                player.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to play a song
    private static Player playSong(String filePath) {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            Player player = new Player(bis);

            // Play in a separate thread to prevent blocking
            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    System.out.println("Error playing the song.");
                    e.printStackTrace();
                }
            }).start();

            System.out.println("Playing: " + filePath);
            return player;
        } catch (Exception e) {
            System.out.println("Unable to play the song.");
            e.printStackTrace();
            return null;
        }
    }
}