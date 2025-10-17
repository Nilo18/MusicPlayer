package com.example;

import com.google.gson.*;
import java.util.Scanner;

public class App {

    public static void main( String[] args ) {
//        String[] allowedCommands = {""};
        // The allowed commands should be:
        // mp play -"name", mp loop, mp skip, mp exit
        // 1. Analyze the user input for commands
        System.out.println("******************");
        System.out.println("MusicPlayer CLI");
        System.out.println("******************");
        System.out.println();
        System.out.println("Choose a command: ");
        System.out.println("""
         1) mp play -"NAME OF THE SONG/YOUTUBE URL" --- Play the desired music, the music will be downloaded (if it isn't already installed) and played locally
         2) mp loop --- Loop the music which is currently playing.
         3) mp skip --- Skip the music which is playing currently.
         4) mp exit --- Exit the CLI
         ************************************************************** \n
         """);
        Scanner sc = new Scanner(System.in);
        boolean isRunning = true;
        boolean isPromptingDownload = false;

        // 2. Search YouTube by the given keyword if the command is mp play

        while (isRunning) {
            String option = sc.nextLine();

            if (option.contains("mp play -")) {
                String[] token = option.split("\"");
                String musicName = token[1];
                System.out.println("Searching " + musicName + "...");
                Searcher.search(musicName);
            } else if (option.contains("mp exit")) {
                System.out.println("Exiting MusicPlayer...");
                isRunning = false;
            } else {
                System.out.println("Unrecognized command.");
            }
        }

        // 3. Parse the first 5-10 results of the search using Gson
        // 4. Ask the user to choose between one of them by typing numbers 1-10
        // 5. Download the selected option using java-youtube-downloader, it should be stored in MpMusic folder
        // (If the suggested video is already downloaded, play it locally instead)
        // 6. Allow other commands like mp pause, mp loop, etc.
    }
}
