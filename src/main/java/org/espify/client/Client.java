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

    private Player currentPlayer;


    public static void main(String[] args) throws Exception {
        // Start the Spring Boot application
        SpringApplication application = new SpringApplication(Client.class);
        // application.setBannerMode(Mode.OFF);
        application.run(args);
    }

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("espify:> ",
            AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @PostConstruct
    public void connectToServer() {
        try {
            // Connect to Control Socket
            controlSocket = new Socket("localhost", CONTROL_PORT);
            controlInput = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            controlOutput = new PrintWriter(controlSocket.getOutputStream(), true);
    
            // Send client ID to server
            controlOutput.println("CLIENT_ID " + clientId);
    
            // Start Control Listener Thread for Commands
            Thread controlListenerThread = new Thread(() -> {
                String msg;
                try {
                    while ((msg = controlInput.readLine()) != null) {
                        handleMessage(msg);
                    }
                } catch (IOException e) {
                    logger.error("Error reading control message: {}", e.getMessage());
                }
            });
            controlListenerThread.setDaemon(true);
            controlListenerThread.start();
    
            // Connect to Data Socket for Music Streaming
            dataSocket = new Socket("localhost", DATA_PORT);
            dataInputStream = dataSocket.getInputStream();
            dataOutputStream = new PrintWriter(dataSocket.getOutputStream(), true);
    
            // Start Data Listener Thread for Music Streaming
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
    public void cleanup() {
        try {
            if (controlSocket != null && !controlSocket.isClosed()) {
                controlOutput.println("exit");
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
        } else if (msg.equals("You have been disconnected.")) {
            logger.info("Disconnected from the server.");
            stopMusicPlayback();
        } else {
            logger.info("Message received: {}", msg);
            System.out.println(msg);
        }
    }

    public void stopMusicPlayback() {
        if (currentPlayer != null) {
            currentPlayer.close();
            logger.info("Music playback stopped.");
        }
    }
    
    public void playMusic(InputStream musicStream) {
        try {
            BufferedInputStream bufferedStream = new BufferedInputStream(musicStream);
            currentPlayer = new Player(bufferedStream);
            logger.info("Starting music playback from network stream");
            currentPlayer.play();
            logger.info("Music playback completed");
        } catch (JavaLayerException e) {
            logger.error("Error during music playback: {}", e.getMessage());
        }
    }

    public static void sendMessage(String message) {
        logger.info("Sending message: {}", message);
        controlOutput.println(message);
    }
}