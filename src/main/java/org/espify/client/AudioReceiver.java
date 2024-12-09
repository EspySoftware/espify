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

    public synchronized void pause() {
        if (!isPaused) {
            isPaused = true;
            logger.info("Playback paused.");
        }
    }
    
    public synchronized void play() {
        if (isPaused) {
            isPaused = false;
            notify();
            logger.info("Playback resumed.");
        }
    }
    
    @Override
    public void run() {
        try {
            bis = new BufferedInputStream(audioIn);
            player = new Player(bis);
    
            while (!isStopped) {
                if (isPaused) {
                    synchronized (this) {
                        while (isPaused && !isStopped) {
                            wait();
                        }
                    }
                }
    
                // Play the next frame
                if (!player.play(1)) {
                    break; // End of stream
                }
            }
    
            if (!isStopped && !isPaused && listener != null) {
                listener.onPlaybackCompleted();
            }
        } catch (Exception e) {
            logger.error("Error during playback: {}", e.getMessage());
            if (listener != null) {
                listener.onPlaybackError(e);
            }
        }
    }
}