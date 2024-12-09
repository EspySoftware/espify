package org.espify.client;

import javazoom.jl.player.Player;

import java.io.DataInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioReceiver implements Runnable {
    private Logger logger = LoggerFactory.getLogger(AudioReceiver.class);

    private DataInputStream audioIn;
    private Player player;
    private volatile boolean isPaused = false;
    private volatile boolean isStopped = false;

    public AudioReceiver(DataInputStream audioIn) {
        this.audioIn = audioIn;
    }

    public synchronized void play() {
        if (isPaused) {
            isPaused = false;
            notifyAll();
        }
    }

    public synchronized void pause() {
        if (!isPaused) {
            isPaused = true;
        }
    }
    
    @Override
    public void run() {
        try {
            player = new Player(audioIn);
            while (!isStopped) {
                synchronized (this) {
                    while (isPaused) {
                        wait();
                    }
                }
                if (!player.play(1)) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Playback error: {}", e.getMessage());
        }
    }
}