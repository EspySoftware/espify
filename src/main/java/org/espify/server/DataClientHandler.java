package org.espify.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class DataClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(DataClientHandler.class.getName());
    private Socket dataSocket;
    private OutputStream outputStream;
    private ClientHandler clientHandler;

    public DataClientHandler(Socket dataSocket, ClientHandler clientHandler) {
        this.dataSocket = dataSocket;
        this.clientHandler = clientHandler;
        try {
            this.outputStream = dataSocket.getOutputStream();
        } catch (IOException e) {
            logger.severe("Failed to get OutputStream: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        logger.info("Data client handler started.");
        
        try {
            // Keep the thread alive or handle data transmission
            while (!dataSocket.isClosed()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.severe("Data client handler interrupted: " + e.getMessage());
        } finally {
            // Remove client from the room if data socket closes
            if (clientHandler.getCurrentRoom() != null) {
                clientHandler.getCurrentRoom().removeClient(clientHandler);
            }
            logger.info("Data client handler terminated.");
        }
    }

    public synchronized void sendMusicStream(byte[] data) {
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            logger.severe("Error sending music stream: " + e.getMessage());
        }
    }

    public void close() {
        try {
            dataSocket.close();
            logger.info("Data socket closed.");
        } catch (IOException e) {
            logger.severe("Error closing data socket: " + e.getMessage());
        }
    }

    
}