package client;

import java.io.*;
import java.net.*;

public class Client {
    static String SERVER_ADDRESS = "localhost";
    static int SERVER_PORT = 12345;
    static Socket socket;

    public static void main(String[] args) {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Example: Join a room
            // output.println("joinRoom Room1");

            // Example: Add a song
            // output.println("addSong SongName");

            // Listen for server messages
            String message;
            while ((message = input.readLine()) != null) {
                System.out.println("Server: " + message);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
