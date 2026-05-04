package com.example;

import com.example.player.PlayerManager;
import com.example.utilities.Color;
import com.example.utilities.Utilities;
import com.github.lalyos.jfiglet.FigletFont;
import com.google.gson.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Scanner;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        try {
            String banner = FigletFont.convertOneLine("Cantante CLI v1.3.1");
            System.out.println(Color.RED + banner + Color.RESET);
        } catch (IOException e) {
            System.out.println("***************************");
            System.out.println("Cantante CLI v1.3.1");
            System.out.println("***************************");
        }
        System.out.println();
        System.out.println("Choose one of the main commands: ");
        System.out.println("""
         1) play -"NAME OF THE SONG/YOUTUBE URL" --- Play the desired music, the music will be downloaded (if it isn't already installed) and played locally. Shortcut: p -"NAME OF THE SONG/YOUTUBE URL"
         2) loop --- Loop the music which is currently playing. Shortcut: l
         3) skip --- Skip the music which is playing currently. Shortcut: s
         4) exit --- Exit the CLI. Shortcut: mp e
         
         Type help for a detailed list of commands.
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
//            Updater.checkForUpdates();
            launch(args);
            return;
        }

    }
}
