package org.espify.server.handlers;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javazoom.jl.decoder.JavaLayerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AudioClientHandler.class);
    private ClientHandler clientHandler;
    private Socket socket;
    private DataOutputStream audioOut;
    private volatile boolean streaming = false;
    private volatile boolean paused = false;


    public AudioClientHandler(ClientHandler clientHandler, Socket socket) {
        this.clientHandler = clientHandler;
        this.socket = socket;
        try {
            this.audioOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("Error initializing audio output stream for client ID {}: {}", clientHandler.getClientID(), e.getMessage());
        }
    }

    @Override
    public void run() {
        logger.info("AudioClientHandler for client ID {} started.", clientHandler.getClientID());
        while (!socket.isClosed()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("AudioClientHandler thread interrupted: {}", e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void streamSongAsync(String filePath) {
        if (streaming) {
            logger.warn("Already streaming a song to client ID {}.", clientHandler.getClientID());
            return;
        }
        streaming = true;
        new Thread(() -> {
            try {
                streamSong(filePath);
            } catch (IOException | JavaLayerException e) {
                logger.error("Error streaming song to client ID {}: {}", clientHandler.getClientID(), e.getMessage());
                clientHandler.sendMessage("Error streaming song: " + e.getMessage());
                streaming = false;
            }
        }).start();
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    private void streamSong(String filePath) throws IOException, JavaLayerException {
        try (FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = bis.read(buffer)) != -1 && streaming && !paused) {
                sendAudioData(buffer, bytesRead);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void sendAudioData(byte[] data, int length) throws IOException {
        audioOut.write(data, 0, length);
        audioOut.flush();
        logger.info("Sent {} bytes of audio data to client ID: {}", length, clientHandler.getClientID());
    }

    public void close() {
        try {
            socket.close();
            logger.info("Audio socket closed for client ID: {}", clientHandler.getClientID());
        } catch (IOException e) {
            logger.error("Error closing audio socket for client ID {}: {}", clientHandler.getClientID(), e.getMessage());
        }
    }

    public String getClientID() {
        return clientHandler.getClientID();
    }

    public void pauseStreaming() {
        streaming = false;
        paused = true;
        // Clear output buffer
        try {
            audioOut.flush();
        } catch (IOException e) {
            logger.error("Error flushing audio buffer: {}", e.getMessage());
        }
    }

    public void resumeStreaming() {
        streaming = true;
        paused = false;
        logger.info("Resumed streaming for client ID: {}", clientHandler.getClientID());
    }
}