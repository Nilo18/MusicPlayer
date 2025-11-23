package com.example;

import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Scanner;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        System.out.println("************************");
        System.out.println("JXPlayer CLI v1.0.1");
        System.out.println("************************");
        System.out.println();
        System.out.println("Choose one of the main commands: ");
        System.out.println("""
         1) mp play -"NAME OF THE SONG/YOUTUBE URL" --- Play the desired music, the music will be downloaded (if it isn't already installed) and played locally. Shortcut: mp p -"NAME OF THE SONG/YOUTUBE URL"
         2) mp loop --- Loop the music which is currently playing. Shortcut: mp l
         3) mp skip --- Skip the music which is playing currently. Shortcut: mp s
         4) mp exit --- Exit the CLI. Shortcut: mp e
         
         Type mp help for a detailed list of commands.
         ************************************************************** \n
         """);

        Platform.setImplicitExit(false); // prevents JVM from exiting

        Thread commandsThread = new Thread(new CliThread());
        commandsThread.setDaemon(true); // ensures JVM can exit if main thread ends
        commandsThread.start();
    }


    public static void main( String[] args ) {
        if (args.length == 0) {
            System.out.println("No argument provided.");
            return;
        }

        if (args[0].equals("init")) {
            launch(args);
            return;
        }
    }
}
