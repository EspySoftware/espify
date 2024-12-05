package org.espify.client;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.DataInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioReceiver implements Runnable {
    private Logger logger = LoggerFactory.getLogger(AudioReceiver.class);

    private DataInputStream audioIn;
    private Player player;

    public AudioReceiver(DataInputStream audioIn) {
        this.audioIn = audioIn;
    }

    @Override
    public void run() {
        try {
            // Initialize the player with the incoming audio stream
            player = new Player(audioIn);
            player.play();

            Client.sendMessage("playbackComplete");
        } catch (JavaLayerException e) {
            logger.error("Error receiving audio data: " + e.getMessage());
        }
    }
}