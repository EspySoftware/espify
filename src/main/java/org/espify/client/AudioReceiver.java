package org.espify.client;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.DataInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioReceiver implements Runnable {
    private Logger logger = LoggerFactory.getLogger(AudioReceiver.class);

    private DataInputStream audioIn;
    private Client client;
    private Player player;
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;

    public AudioReceiver(DataInputStream audioIn, Client client) {
        this.client = client;
        this.audioIn = audioIn;
    }

    public void play() {
        if (player != null && isPaused) {
            logger.info("Resuming playback.");
            isPaused = false;
            new Thread(this).start(); // Restart the run method to continue playback
        } else if (player == null) {
            new Thread(this).start(); // Start playback if not already playing
        }
    }

    public void pause() {
        if (player != null && !isPaused) {
            logger.info("Pausing playback.");
            isPaused = true;
            player.close();
        }
    }

    @Override
    public void run() {
        logger.info("AudioClientHandler for client ID {} started.", client.getClientID());
        while (!isStopped) {
            if (!isPaused) {
                // Start playback
                try {
                    BufferedInputStream bis = new BufferedInputStream(audioIn);
                    player = new Player(bis);
                    logger.info("Starting playback.");
                    player.play();
                } catch (Exception e) {
                    logger.error("Error during playback: {}", e.getMessage());
                    break;
                }
            } else {
                // Paused state: Wait without closing the socket
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.error("AudioClientHandler thread interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}