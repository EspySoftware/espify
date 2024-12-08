package org.espify.client;

import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.DataInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioReceiver implements Runnable {
    private Logger logger = LoggerFactory.getLogger(AudioReceiver.class);

    private DataInputStream audioIn;
    private PlaybackListener listener;
    private Client client;
    private Player player;
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;
    private BufferedInputStream bis;
    
    public void setPlaybackListener(PlaybackListener listener) {
        this.listener = listener;
    }

    public AudioReceiver(DataInputStream audioIn, Client client) {
        this.client = client;
        this.audioIn = audioIn;
    }

    public void play() {
        if (isPaused) {
            logger.info("Resuming playback.");
            isPaused = false;
            synchronized(this) {
                notify(); // Wake up paused thread
            }
        }
    }

    public void pause() {
        if (player != null && !isPaused) {
            logger.info("Pausing playback.");
            isPaused = true;
        }
    }
    
    @Override
    public void run() {
        try {
            while (!isStopped) {
                if (isPaused) {
                    synchronized(this) {
                        while (isPaused && !isStopped) {
                            wait(); // Wait while paused
                        }
                    }
                    continue;
                }

                if (player == null) {
                    bis = new BufferedInputStream(audioIn);
                    player = new Player(bis);
                }

                logger.info("Starting playback.");
                player.play();

                if (!isStopped && !isPaused && listener != null) {
                    listener.onPlaybackCompleted();
                }
            }
        } catch (Exception e) {
            logger.error("Error during playback: {}", e.getMessage());
            if (listener != null) {
                listener.onPlaybackError(e);
            }
        }
    }
}