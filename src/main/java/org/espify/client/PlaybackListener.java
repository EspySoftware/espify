package org.espify.client;

public interface PlaybackListener {
    void onPlaybackCompleted();
    void onPlaybackError(Exception e);
}