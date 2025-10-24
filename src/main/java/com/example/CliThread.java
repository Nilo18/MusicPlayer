package com.example;

import javafx.application.Platform;

import java.util.Scanner;

public class CliThread implements Runnable {

    @Override
    public void run() {
        boolean isRunning = true;
        Scanner sc = new Scanner(System.in);
        System.out.println("I'm running from CliThread.");

        while (isRunning) {
            String option = sc.nextLine();

            if (option.contains("mp play -")) {
                String[] token = option.split("\"");
                String musicName = token[1];
                Searcher.search(musicName);
            } else if (option.equals("mp loop")) {
                System.out.println("Looping...");
            } else if (option.equals("mp exit")) {
                System.out.println("Exiting MusicPlayer CLI...");
                isRunning = false;
                Platform.exit(); // Exit javafx thread
                System.exit(0); // Exit the JVM completely
            } else {
                System.out.println("Unrecognized command.");
            }
        }
    }
}
