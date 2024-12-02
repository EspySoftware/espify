package org.espify.client;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.command.annotation.CommandScan;
import org.springframework.shell.jline.PromptProvider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SpringBootApplication
@CommandScan
public class Client {
    static Socket socket;
    static BufferedReader input;
    static PrintWriter output;

    public static void main(String[] args) throws Exception {
        SpringApplication application = new SpringApplication(Client.class);
        application.setBannerMode(Mode.OFF);
        application.run(args);
    }

    @Bean
    // Custom prompt provider
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("espify:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    @PostConstruct
    // When the application starts, connect to the server and start listening for messages
    public void connectToServer() {
        try {
            socket = new Socket("localhost", 12345);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            // Listen for messages from the server
            Thread listenerThread = new Thread(() -> {
                String msg;
                try {
                    while (!socket.isClosed() && (msg = input.readLine()) != null) {
                        System.out.println("Server: " + msg);
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        e.printStackTrace();
                    }
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();
    
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    // When the application stops, close the socket and streams
    public void cleanup() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message) {
        output.println(message);
    }
}