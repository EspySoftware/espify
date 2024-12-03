// src/main/java/org/espify/client/Client.java
package org.espify.client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.jline.PromptProvider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

@SpringBootApplication
@CommandScan
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    static Socket controlSocket;
    static Socket dataSocket;
    
    static InputStream dataInputStream;
    static BufferedReader controlInput;
    static PrintWriter controlOutput;
    static PrintWriter dataOutputStream;
    static final int CONTROL_PORT = 12345;
    static final int DATA_PORT = 12346;

    private String clientId; // Store the received client ID

    public static void main(String[] args) throws Exception {
        SpringApplication application = new SpringApplication(Client.class);
        application.setBannerMode(Mode.OFF);
        application.run(args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("espify:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @PostConstruct
    public void connectToServer() {
        try {
            // Connect to Control Socket
            controlSocket = new Socket("localhost", CONTROL_PORT);
            controlInput = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            controlOutput = new PrintWriter(controlSocket.getOutputStream(), true);

            // Read client ID from control socket
            String idMessage = controlInput.readLine();
            if (idMessage != null && idMessage.startsWith("CLIENT_ID ")) {
                clientId = idMessage.substring("CLIENT_ID ".length());
                logger.info("Received client ID: {}", clientId);
            } else {
                logger.error("Failed to receive client ID from server.");
                return;
            }

            // Connect to Data Socket once
            if (dataSocket == null || dataSocket.isClosed()) {
                dataSocket = new Socket("localhost", DATA_PORT);
                dataOutputStream = new PrintWriter(dataSocket.getOutputStream(), true);
                // Send client ID to server
                dataOutputStream.println("CLIENT_ID " + clientId);
                dataInputStream = dataSocket.getInputStream();
            }

            // Listen for control messages
            Thread controlListenerThread = new Thread(() -> {
                String msg;
                try {
                    while (!controlSocket.isClosed() && (msg = controlInput.readLine()) != null) {
                        handleMessage(msg);
                    }
                } catch (IOException e) {
                    if (!controlSocket.isClosed()) {
                        e.printStackTrace();
                    }
                }
            });
            controlListenerThread.setDaemon(true);
            controlListenerThread.start();

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @PreDestroy
    // When the application stops, close the socket and streams
    public void cleanup() {
        try {
            if (controlSocket != null && !controlSocket.isClosed()) {
                controlSocket.close();
            }
            if (dataSocket != null && !dataSocket.isClosed()) {
                dataSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void handleMessage(String msg) {
        if (msg.startsWith("CLIENT_ID")) {
            // Already handled in connectToServer
        } else if (msg.startsWith("Now Playing: ")) {
            logger.info("Now Playing message received: {}", msg);
            // Start listening to the incoming music stream from the data socket
            try {
                playMusic(dataInputStream);
            } catch (Exception e) {
                logger.error("Failed to play music: {}", e.getMessage());
                System.out.println("Failed to play music.");
            }
        } else {
            logger.info("Message received: {}", msg);
            System.out.println(msg);
        }
    }
    
    public void playMusic(InputStream musicStream) {
        new Thread(() -> {
            try {
                logger.info("Initializing music playback.");
                // Wrap the InputStream with BufferedInputStream for efficient streaming
                BufferedInputStream bufferedStream = new BufferedInputStream(musicStream);
                Player player = new Player(bufferedStream);
                logger.info("Starting music playback.");
                player.play();
                logger.info("Music playback finished.");
            } catch (JavaLayerException e) {
                logger.error("Error during music playback: {}", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    public static void sendMessage(String message) {
        logger.info("Sending message: {}", message);
        controlOutput.println(message);
    }
}