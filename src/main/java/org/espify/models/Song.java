package org.espify.models;

public class Song {
    String name;
    String filePath;

    public Song(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }
}