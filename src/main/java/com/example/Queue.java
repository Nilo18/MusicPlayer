package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Queue {
    private List<Path> queue;
    private Path musicDir = Paths.get(System.getProperty("user.home"), "MpMusic");
    private int currentIndex = 0;

    public Queue() {
        try {
            // Load the playlist
            this.queue = Files.list(musicDir).
                    filter(music -> music.toString().toLowerCase().endsWith(".mp3")).
                    collect(Collectors.toList());

        } catch (IOException e) {
            System.out.println("Couldn't load playlist.");;
        }
    }

    public void add(Path newMusic) {
        // stream() converts the list into a stream which will allow functional operations like filtering and matching
        boolean exists = queue.stream().anyMatch(music -> music.getFileName().toString().equals(newMusic.getFileName().toString()));
        if (!exists) {
            queue.add(newMusic);
        }
    }

    public void remove(String musicName) {
        queue.removeIf(music -> music.getFileName().toString().equals(musicName));
    }

    public Path getNextMusic() {
        if (queue.isEmpty()) {
            return null; // Return nothing if the queue is empty
        }

        currentIndex = (currentIndex + 1) % queue.size(); // Allow wrapping to the first music in the queue

        return queue.get(currentIndex);
    }

    public List<Path> getPlaylist() {
        return queue;
    }
}
