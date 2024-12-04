package org.espify.server.handlers;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AudioClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AudioClientHandler.class);
    private ClientHandler clientHandler;
    private Socket socket;
    private DataOutputStream audioOut;

    public AudioClientHandler(ClientHandler clientHandler, Socket socket) {
        this.clientHandler = clientHandler;
        this.socket = socket;
        
        try {
            this.audioOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Error initializing audio output stream for client ID " + clientHandler.getClientID() + ": " + e.getMessage());
        }
    }

    @Override
    public void run() {
        logger.info("AudioClientHandler for client ID {} started.", clientHandler.getClientID());
        // Keep the thread alive
        while (!socket.isClosed()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("AudioClientHandler thread interrupted: " + e.getMessage());
                break;
            }
        }
    }

    public void sendAudioData(byte[] data, int length) {
        try {
            audioOut.write(data, 0, length);
            audioOut.flush();
            logger.info("Sent {} bytes of audio data to client ID: {}", length, clientHandler.getClientID());
        } catch (IOException e) {
            logger.error("Error sending audio data to client ID " + clientHandler.getClientID() + ": " + e.getMessage());
        }
    }

    public void close() {
        try {
            socket.close();
            logger.info("Audio socket closed for client ID: {}", clientHandler.getClientID());
        } catch (IOException e) {
            logger.error("Error closing audio socket for client ID " + clientHandler.getClientID() + ": " + e.getMessage());
        }
    }

    public String getClientID() {
        return clientHandler.getClientID();
    }
}