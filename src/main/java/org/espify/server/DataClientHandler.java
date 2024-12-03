package org.espify.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

public class DataClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(DataClientHandler.class.getName());
    private Socket dataSocket;
    private OutputStream outputStream;

    public DataClientHandler(Socket dataSocket) {
        this.dataSocket = dataSocket;
        try {
            this.outputStream = dataSocket.getOutputStream();
        } catch (IOException e) {
            logger.severe("Failed to get OutputStream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        logger.info("Data client handler started.");
        
        // Keep the thread alive
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.severe("Data client handler interrupted: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendMusicStream(byte[] data) {
        try {
            outputStream.write(data);
            outputStream.flush();
            logger.info("Sent " + data.length + " bytes to client.");
        } catch (IOException e) {
            logger.severe("Error sending music stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            dataSocket.close();
            logger.info("Data socket closed.");
        } catch (IOException e) {
            logger.severe("Error closing data socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}