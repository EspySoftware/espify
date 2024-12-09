package org.espify.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.jline.PromptProvider;

import jakarta.annotation.PreDestroy;

@SpringBootApplication
@CommandScan
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    
    private String clientID = UUID.randomUUID().toString();
    private AudioReceiver audioReceiver;

    static Socket controlSocket;
    static BufferedReader controlInput;
    static PrintWriter controlOutput;

    static Socket audioSocket;
    static DataInputStream audioInputStream;
    static PrintWriter audioOutput;

    static final int CONTROL_PORT = 12345;
    static final int AUDIO_PORT = 12346;

    public static void main(String[] args) throws Exception {
        // Start the Spring Boot application
        SpringApplication application = new SpringApplication(Client.class);
        application.run(args);
    }

    @Bean
    // Custom prompt for the shell
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("espify:> ",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    public void connectToServer(String serverIp) {
        try {
            // Connect to Control Socket
            controlSocket = new Socket(serverIp, CONTROL_PORT);
            controlInput = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            controlOutput = new PrintWriter(controlSocket.getOutputStream(), true);
    
            // Receive client ID from server
            clientID = controlInput.readLine();
    
            if (clientID == null || clientID.isEmpty()) {
                throw new IOException("Failed to receive client ID from control server.");
            }
    
            // Connect to Audio Socket after receiving client ID
            audioSocket = new Socket(serverIp, AUDIO_PORT);
            audioInputStream = new DataInputStream(audioSocket.getInputStream());
            audioOutput = new PrintWriter(audioSocket.getOutputStream(), true);
    
            // Send client ID to audio server
            audioOutput.println(clientID);
            logger.info("Sent client ID to audio server: {}", clientID);
    
            // Initialize AudioReceiver
            audioReceiver = new AudioReceiver(audioInputStream);
            Thread audioThread = new Thread(audioReceiver);
            audioThread.start();

            // Start a thread to listen for server messages
            new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = controlInput.readLine()) != null) {
                        handleServerMessage(serverMessage);
                    }
                } catch (IOException e) {
                    logger.error("Error reading server messages: {}", e.getMessage());
                }
            }).start();

            logger.info("Connected to server at {}", serverIp);

        } catch (IOException e) {
            logger.error("Connection error: {}", e.getMessage());
        }
    }

    public void disconnectFromServer() {
        try {
            controlOutput.println("exit");
            controlSocket.close();
            audioSocket.close();
        } catch (IOException e) {
            logger.error("Error disconnecting from server: {}", e.getMessage());
        }
    }
    
    @PreDestroy
    // Cleanup on shutdown
    public void cleanup() {
        try {
            if (controlSocket != null && !controlSocket.isClosed()) {
                controlOutput.println("exit");
                controlSocket.close();
            }
            if (audioSocket != null && !audioSocket.isClosed()) {
                audioSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing sockets: {}", e.getMessage());
        }
    }

    public void handleServerMessage(String message) {
        if (message.startsWith("play")) {
            long timestamp = parseTimestamp(message);
            schedulePlayback(timestamp);
        } else if (message.startsWith("pause")) {
            if (audioReceiver != null) {
                audioReceiver.pause();
            }
        } else {
            // Display other messages to the user
            System.out.println(message);
        }
    }
    
    private long parseTimestamp(String message) {
        // Extract the timestamp from the message
        try {
            String[] parts = message.split("\\s+");
            if (parts.length > 1) {
                return Long.parseLong(parts[1]);
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid timestamp in message: {}", message);
        }
        return System.currentTimeMillis();
    }
    
    private void schedulePlayback(long timestamp) {
        long delay = timestamp - System.currentTimeMillis();
        if (delay < 0) delay = 0;
    
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (audioReceiver != null) {
                    audioReceiver.play();
                }
            }
        }, delay);
    }

    public static void sendMessage(String message) {
        // logger.info("Sending message: {}", message);
        controlOutput.println(message);
    }

    public String getClientID() {
        return clientID;
    }
}