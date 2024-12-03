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
    
    private String clientId;
    static Socket controlSocket;
    static BufferedReader controlInput;
    static PrintWriter controlOutput;
    static Socket dataSocket;
    static InputStream dataInputStream;
    static PrintWriter dataOutputStream;
    static final int CONTROL_PORT = 12345;
    static final int DATA_PORT = 12346;


    public static void main(String[] args) throws Exception {
        // Start the Spring Boot application
        SpringApplication application = new SpringApplication(Client.class);
        application.setBannerMode(Mode.OFF);
        application.run(args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("espify:> ",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @PostConstruct
    public void connectToServer() {
        // When the application starts, connect to the server
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

            // Connect to Data Socket
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
                        logger.error("Error reading control message: {}", e.getMessage());
                    }
                }
            });
            // Set the thread as a daemon so it will automatically stop when the application stops
            controlListenerThread.setDaemon(true);
            controlListenerThread.start();

            // Start data listener thread for music playback
            Thread dataListenerThread = new Thread(() -> {
                try {
                    playMusic(dataInputStream);
                } catch (Exception e) {
                    logger.error("Failed to play music: {}", e.getMessage());
                    System.out.println("Failed to play music.");
                }
            });
            dataListenerThread.setDaemon(true);
            dataListenerThread.start();

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));

        } catch (IOException e) {
            logger.error("Failed to connect to server: {}", e.getMessage());
        }
    }
    
    @PreDestroy
    // Cleanup when the application stops
    public void cleanup() {
        try {
            if (controlSocket != null && !controlSocket.isClosed()) {
                controlSocket.close();
            }
            if (dataSocket != null && !dataSocket.isClosed()) {
                dataSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing sockets: {}", e.getMessage());
        }
    }

    // Handle incoming messages
    void handleMessage(String msg) {
        if (msg.startsWith("CLIENT_ID")) {
            // Already handled in connectToServer
        } else if (msg.startsWith("Now Playing: ")) {
            logger.info("Now Playing message received: {}", msg);
            System.out.println(msg);
        } else {
            logger.info("Message received: {}", msg);
            System.out.println(msg);
        }
    }
    
    public void playMusic(InputStream musicStream) {
        try {
            // Create a buffered stream that can handle chunks of data
            BufferedInputStream bufferedStream = new BufferedInputStream(musicStream);
            Player player = new Player(bufferedStream);
            logger.info("Starting music playback from network stream");
            player.play(); // This will now play as data arrives
        } catch (JavaLayerException e) {
            logger.error("Error during music playback: {}", e.getMessage());
        }
    }

    public static void sendMessage(String message) {
        logger.info("Sending message: {}", message);
        controlOutput.println(message);
    }
}