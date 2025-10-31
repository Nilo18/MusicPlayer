package com.example;

import javafx.application.Platform;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class CliThread implements Runnable {

    @Override
    public void run() {
        // isRunning is Atomic because a lambda has to use it
        AtomicBoolean isRunning = new AtomicBoolean(true);
        Scanner sc = new Scanner(System.in);

        // Join the Object vararg array into a valid string
        // Define a valid syntax with Pattern.compile()
        // to ensure that the user uses the commands with the intended syntax
        CommandHandler.addCommand("mp play", Pattern.compile("^mp play -\"([^\"]+)\"$"),
                (Object... option) ->
                Utilities.playMusic(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)))
        );
        CommandHandler.addCommand("mp loop", Pattern.compile("^mp loop$"),
                (Object... a) -> Platform.runLater(PlayerManager::loop)
        );
        CommandHandler.addCommand("mp skip", Pattern.compile("^mp skip$"),
                (Object... a) -> PlayerManager.skip()
        );
        CommandHandler.addCommand("mp pause", Pattern.compile("^mp pause$"),
            (Object... a) -> PlayerManager.pause()
        );
        CommandHandler.addCommand("mp resume", Pattern.compile("^mp resume$"),
                (Object... a) -> PlayerManager.resume()
        );
        CommandHandler.addCommand("mp exit", Pattern.compile("^mp exit$"),
                (Object... a) -> Utilities.exit(isRunning)
        );

//        for (String key : CommandHandler.getCommandsMap().keySet()) {
//            System.out.println(key);
//        }

        while (isRunning.get()) {
            String option = sc.nextLine().trim();
            String[] tokens = option.split(" ");

            String commandKey;
            Object[] args;

            if (tokens.length >= 2 && tokens[0].equals("mp")) {
                // Ensure that command keys become mp + [stored command] like mp play or mp loop
                commandKey = tokens[0] + " " + tokens[1];
                // coppyOfRange creates a subarray from a given starting index to the given ending index,
                // The first argument is the original array, the second is the starting index and the third
                // is the ending index
                args = Arrays.copyOfRange(tokens,2, tokens.length);
            } else {
                // Fallback
                commandKey = tokens[0];
                args = Arrays.copyOfRange(tokens, 1, 1);
            }

            CommandHandler.executeCommand(option);



//            if (option.contains("mp play -")) {
//                String[] token = option.split("\"");
//                String musicName = token[1];
//                Searcher.search(musicName);
//            } else if (option.equals("mp loop")) {
//                Platform.runLater(() -> PlayerManager.loop());
//            } else if (option.equals("mp skip")) {
//                PlayerManager.skip();
//            } else if(option.equals("mp pause")) {
//                PlayerManager.pause();
//            } else if(option.equals("mp resume")) {
//                PlayerManager.resume();
//            } else if (option.equals("mp exit")) {
//                System.out.println("Exiting MusicPlayer CLI...");
//                isRunning.set(false);
//                Platform.exit(); // Exit javafx thread
//                System.exit(0); // Exit the JVM completely
//            } else {
//                System.out.println("Unrecognized command.");
//            }
        }
    }
}
