package com.example;

import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class App extends Application {
    @Override
    public void start(Stage stage) {
//        Player player = new Player();
//        Path p = Paths.get(System.getProperty("user.home"), "MpMusic", "Belisarius Epic Byzantine Music.mp3");
//        player.play(p);

        //        String[] allowedCommands = {""};
        // The allowed commands should be:
        // mp play -"name", mp loop, mp skip, mp exit
        // 1. Analyze the user input for commands
        System.out.println("******************");
        System.out.println("MusicPlayer CLI v1.0");
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

        Platform.setImplicitExit(false); // prevents JVM from exiting

//        stage.setTitle("Hidden");
//        stage.setWidth(0);
//        stage.setHeight(0);
//        stage.show(); // MUST show to initialize toolkit

        Thread commandsThread = new Thread(new CliThread());
        commandsThread.setDaemon(true); // ensures JVM can exit if main thread ends
        commandsThread.start();
    }


    public static void main( String[] args ) {
        launch(args);
        // 3. Parse the first 5-10 results of the search using Gson
        // 4. Ask the user to choose between one of them by typing numbers 1-10
        // 5. Download the selected option using java-youtube-downloader, it should be stored in MpMusic folder
        // (If the suggested video is already downloaded, play it locally instead)
        // 6. Allow other commands like mp pause, mp loop, etc.
    }
}
